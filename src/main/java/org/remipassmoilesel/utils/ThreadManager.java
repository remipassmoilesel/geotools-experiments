package org.remipassmoilesel.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by remipassmoilesel on 06/12/16.
 */
public class ThreadManager {

    private static ExecutorService executor = Executors.newFixedThreadPool(5);

    public static void runLater(Runnable r){
        executor.execute(r);
    }

}
