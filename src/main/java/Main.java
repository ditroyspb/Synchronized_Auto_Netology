import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        int timeOfManufacturing = 1000; //время изготовления автомобиля
        int carLimit = 10; //максимальное количество изготовленных автомобилей данной модели
        int timeOfCustomerCome = 500; //время появления нового покупателя
        List<Car> carsStock = new ArrayList<>(); //список автомобилей в наличии
        List<Customer> waitingList = new ArrayList<>(); //лист ожидания покупателей

        new Thread(() -> { //поток производителя автомобилей
            for (int i = 0; i < carLimit; i++) {
                Car car = new Car("Автомобиль" + (i + 1));
                synchronized (carsStock) {
                    carsStock.add(car);
                    carsStock.notify();
                    System.out.println("Производитель Mazda Inc доставил на дилерский склад " + car);
                }
                try {
                    Thread.sleep(timeOfManufacturing);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }).start();

        new Thread(() -> { //поток покупателей
            int count = 0; //счетчик купленных автомобилей
            int customerCount = 0;
            int timeOfWaiting = 300; //время ожидания покупателем автомобиля
            while (count < carLimit) { //цикл до продажи 10 автомобилей

                Customer customer = new Customer("Покупатель" + (customerCount + 1));
                customerCount++;
                synchronized (carsStock) { //если в очереди ожидания уже есть покупатели, один из них покупает поступивший автомобиль
                    if (!waitingList.isEmpty() && !carsStock.isEmpty()) {
                        System.out.println(waitingList.remove(0) + " забрал автомобиль " + carsStock.remove(0));
                        count ++;
                    }
                }

                System.out.println(customer + " зашел в автосалон.");

                synchronized (carsStock) { //если в очереди ожидания нет покупателей, то первый пришедший покупатель купит автомобиль в наличии
                    if (!carsStock.isEmpty()) {
                        count ++;
                        System.out.println(customer + " забрал автомобиль " + carsStock.remove(0));
                    } else {
                        try {
                            System.out.println("К сожалению, нет доступных для покупки автомобилей.");
                            carsStock.wait(timeOfWaiting);
                            //если ввести время ожидания (timeOfWaiting), то методы wait(), notify() теряют смысл,
                            //так как формируется постепенная очередь на автомобили и появление нового автомобиля не
                            //влечет за собой его моментальную покупку.
                            //если же не вводить время (timeOfWaiting), то этот покупатель гарантированно будет покупать
                            //первый поступивший автомобиль, что не соответствует примеру, указанному в задании, где
                            //также формируется очередь из покупателей.
                            waitingList.add(customer);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        Thread.sleep(timeOfCustomerCome);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        }).start();
    }
}