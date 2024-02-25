package com.driver;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class EventMonitoringServer {
    private static final int THREAD_POOL_SIZE = 5;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    private static final CountDownLatch shutdownLatch = new CountDownLatch(1);
    private static final AtomicBoolean highMagnitudeEventDetected = new AtomicBoolean(false);

    public static void main(String[] args) {
        try {
            startServer();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            stopServer();
        }
    }


    private static void startServer() throws InterruptedException {
        System.out.println("Event monitoring server started. Enter 'shutdown' to stop the server manually.");

        // Start the event processing tasks
        for (int i = 1; i <= 10; i++) {
            final int j = i;
            executorService.submit(() -> processEvent(j));
        }

        // Wait for the high magnitude event or manual shutdown signal
        waitForShutdownSignal();

        // Inform about the shutdown
        System.out.println("Shutting down the server gracefully...");
    }

    private static void processEvent(int eventId) {
        // Simulate event processing task
        System.out.println("Event " + eventId + " processed.");

        // Simulate detecting a high magnitude event (for testing purposes)
        if (eventId == 5) {
            System.out.println("High magnitude event detected!");
            highMagnitudeEventDetected.set(true);
            shutdownLatch.countDown(); // Release the latch to allow server shutdown
        }
    }

    private static void waitForShutdownSignal() throws InterruptedException {
        while (true) {
            if (highMagnitudeEventDetected.get() || "shutdown".equalsIgnoreCase(getUserInput())) {
                // Trigger the shutdown on detecting a high magnitude event or manual user input
                shutdownLatch.countDown();
                break;
            }

            // Sleep for a short interval before checking again
            TimeUnit.MILLISECONDS.sleep(500);
        }
    }

    private static String getUserInput() {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

    private static void stopServer() {
        try {
            executorService.shutdown();
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                // Some tasks are still running after 10 seconds, force shutdown
                System.out.println("Forcing shutdown due to pending tasks...");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}