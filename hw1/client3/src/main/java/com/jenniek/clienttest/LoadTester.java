package com.jenniek.clienttest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.*;

public class LoadTester {
    private String server_type = "java"; // default server is Java servlet    
    private String IPAddr = Config.javaServletAddress;
    private String s_numThreadGroups = "";

    private int threadGroupSize;
    private int numThreadGroups;
    private int delay;


    public static void main(String[] args) {
        LoadTester loadTester = new LoadTester();
        loadTester.parse(args);
        loadTester.startUp();
        loadTester.mainLoad();
    }

    private void parse(String[] args) {
        // 1. handle illegal args
        if (args.length < 3) {
            System.err.println("Usage: LoadTester <threadGroupSize> <numThreadGroups> <delay> [java|go]");
            return;
        }
        // 2. handle legal args
        if (args.length > 0) {
            System.err.print("Executing: LoadTester ");
            for (String arg : args) {
                System.err.print(arg + " ");
            }
            System.err.println(); 
        }
        // 3. parse 3 compulsory args
        threadGroupSize = Integer.parseInt(args[0]);
        numThreadGroups = Integer.parseInt(args[1]);
        s_numThreadGroups = args[1];
        delay = Integer.parseInt(args[2]) * 1000;  // Convert to milliseconds
        // 4. parse 1 optional arg
        if (args.length > 3) {
            server_type = args[3];
            if ("go".equals(args[3])) {
                IPAddr = Config.goServerAddress;  // Switch to Go servlet address
            } else if (!"java".equals(args[3])) {
                System.err.println("@ayan: illegal server choice, should be either 'java' or 'go'");
                return;
            }
        }
    }

    private void startUp(){
        System.out.println("====== Start startup Phase =====");
        System.out.println(Utilities.getFormattedDate());
        long t1 = System.currentTimeMillis();

        ExecutorService mainExecutor = Executors.newFixedThreadPool(Config.INITIAL_THREAD_COUNT);
        for (int i = 0; i < Config.INITIAL_THREAD_COUNT; i++) {
            mainExecutor.execute(new SendGetRequests(IPAddr, Config.INIT_REQUESTS_PER_THREAD));
        }
        mainExecutor.shutdown();
        try {
            mainExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long t2 = System.currentTimeMillis();
        System.out.println(String.format("Init Phase - walltime = %d mill seconds", t2-t1));
        System.out.println(String.format("Init Phase - throughput =  %d", (Config.INITIAL_THREAD_COUNT * Config.INIT_REQUESTS_PER_THREAD * 1000)/(t2-t1)));
        System.out.println(Utilities.getFormattedDate());
        System.out.println("====== End startup Phase =====");
    }

    private void mainLoad(){
        System.out.println("\n====== Start Main Load Phase ======");
        System.out.println(Utilities.getFormattedDate());
        long startTime = System.currentTimeMillis();

        // 2. main load
        CountDownLatch latch = new CountDownLatch(numThreadGroups * threadGroupSize);
        for (int i = 0; i < numThreadGroups; i++) {
            ExecutorService executor = Executors.newFixedThreadPool(threadGroupSize);
            for (int j = 0; j < threadGroupSize; j++) {
                executor.execute(new ApiTask(IPAddr, Config.LOAD_TEST_REQUESTS_PER_THREAD, latch, server_type, s_numThreadGroups));
            }
            executor.shutdown();
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            latch.await(); // Wait until all tasks are finished
            System.out.println("All tasks completed.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 4. calculate and output results
        long endTime = System.currentTimeMillis();
        long wallTime = (endTime - startTime) / 1000;
        long totalRequests = (long) threadGroupSize * numThreadGroups * Config.LOAD_TEST_REQUESTS_PER_THREAD * 2;  // 2 for both POST and GET
        long throughput = totalRequests / wallTime;
        System.out.println("totalRequests: " + totalRequests );
        System.out.println(Utilities.getFormattedDate(startTime) + " - " + Utilities.getFormattedDate(endTime));  
        System.out.println("Wall Time: " + wallTime + " seconds");        
        System.out.println("Throughput: " + throughput + " requests/second");
        System.out.println(Utilities.getFormattedDate());
        System.out.println("====== End Main Load Phase =====");
        return;
    }


}


