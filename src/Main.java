import java.util.concurrent.*;

public class Main {

    private static final int NUM_SPECIALISTS = 4; // Количество специалистов
    private static final int CALLS_COUNT = 60; // Количество звонков
    private static final int CALL_GENERATION_DELAY = 1000; // Задержка между звонками (в миллисекундах)
    private static final int PROCESSING_DELAY = 3000; // Задержка обработки звонка (в миллисекундах)

    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<Integer> callQueue = new ArrayBlockingQueue<>(CALLS_COUNT); // Очередь звонков

        CountDownLatch latch = new CountDownLatch(CALLS_COUNT);

        // Создаем ExecutorService для потоков специалистов и потока-ATC
        ExecutorService executor = Executors.newFixedThreadPool(NUM_SPECIALISTS + 1);

        // Запускаем поток-АТС
        executor.execute(() -> {
            for (int i = 1; i <= CALLS_COUNT; i++) {
                try {
                    System.out.println("Звонок #" + i + " добавлен в очередь.");
                    callQueue.put(i); // Добавляем звонок в очередь
                    Thread.sleep(CALL_GENERATION_DELAY); // Задержка между звонками
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        // Запускаем потоки специалистов
        for (int i = 0; i < NUM_SPECIALISTS; i++) {
            final int finalI = i + 1;
            executor.execute(() -> {
                while (!Thread.interrupted()) {
                    try {
                        int callId = callQueue.take(); // Извлекаем звонок из очереди
                        System.out.println("Специалист #" + finalI + " взял звонок #" + callId + " в работу.");
                        Thread.sleep(PROCESSING_DELAY); // Задержка обработки
                        System.out.println("Специалист #" + finalI + " завершил обработку звонка #" + callId);
                        latch.countDown(); // Уменьшение счетчика
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }

        // Ждем окончания работы всех потоков
        latch.await();
        executor.shutdownNow();

        System.out.println("Все звонки обработаны.");
    }
}