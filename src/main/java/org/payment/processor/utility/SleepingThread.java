package org.payment.processor.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SleepingThread {
    private static final Logger log = LoggerFactory.getLogger(SleepingThread.class);

    public static void sleepForMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.error("Thread sleep interrupted: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
