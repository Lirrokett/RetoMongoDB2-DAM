/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

/**
 *
 * @author 2dam
 */
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import exception.OurException;

public class ConnectionRetention {
    private static final int MAX_CONNECTIONS = 5;
    private static int activeConnections = 0;

    private static final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    private ConnectionRetention() {

    }

    public static synchronized void retain() throws OurException {
        if (activeConnections >= MAX_CONNECTIONS) {
            throw new OurException(
                "Connection pool exhausted (max " + MAX_CONNECTIONS + ")"
            );
        }

        activeConnections++;
        System.out.println("Connection retained. Active: " + activeConnections);

        scheduler.schedule(
            new ReleaseTask(),
            30,
            TimeUnit.SECONDS
        );
    }

    private static class ReleaseTask implements Runnable {
        @Override
        public void run() {
            synchronized (ConnectionRetention.class) {
                activeConnections--;
                System.out.println(
                   "Connection released after retention. Active: " + activeConnections
                );
            }
        }
    }
}
