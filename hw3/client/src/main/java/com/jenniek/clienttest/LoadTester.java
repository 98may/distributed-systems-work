package com.jenniek.clienttest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import java.util.concurrent.*;

public class LoadTester {
    private ConcurrentLinkedQueue<Long> POST_REVIEW_latencies = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<Long> POST_latencies = new ConcurrentLinkedQueue<>();
    private AtomicLong requestCounter = new AtomicLong(0);
    private AtomicLong failedRequestCounter = new AtomicLong(0);
    private ConcurrentLinkedQueue<Long> throughputs = new ConcurrentLinkedQueue<>();
    
    private String server_type = "java"; // default server is Java servlet    
    private String IPAddr = Config.javaServletAddress;
    private String s_numThreadGroups = "";

    private int threadGroupSize;
    private int numThreadGroups;
    private int delay;


    public static void main(String[] args) {
        LoadTester loadTester = new LoadTester();
        loadTester.parse(args);
        // loadTester.startUp();
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

    private void mainLoad(){
        System.out.println("\n====== Start hw3 Main Load Phase ======");
        System.out.println(Utilities.getFormattedDate());
        long startTime = System.currentTimeMillis();

        // 1. schedule task to log throughput every second
        ScheduledExecutorService throughputs_observer = Executors.newScheduledThreadPool(1);
        throughputs_observer.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                throughputs.add(requestCounter.getAndSet(0)); // Log and reset counter
            }
        }, 0, 1, TimeUnit.SECONDS);

        // 2. main load
        CountDownLatch latch = new CountDownLatch(numThreadGroups * threadGroupSize);
        for (int i = 0; i < numThreadGroups; i++) {
            for (int j = 0; j < threadGroupSize; j++) {
                // Create a new thread with an ApiTask instance and start the thread
                // make POST_latencies record "POST a new album and image"
                // mkae POST_REVIEW_latencies record "POST two likes and one dislike for the album" 
                // GET_latencies deprecated
                Thread t = new Thread(new ApiTask(IPAddr, Config.LOAD_TEST_REQUESTS_PER_THREAD, latch, server_type, s_numThreadGroups, POST_latencies, POST_REVIEW_latencies, requestCounter, failedRequestCounter));
                t.start();
            }
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            latch.await();  // Wait until all tasks are finished
            System.out.println("All tasks completed.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 3. stop logging throughput
        throughputs_observer.shutdown();

        // 4. calculate and output results
        long endTime = System.currentTimeMillis();

        Long[] postReviewLatenciesBoxed = POST_REVIEW_latencies.toArray(new Long[0]);
        long[] postReviewLatencies = new long[postReviewLatenciesBoxed.length];
        for (int i = 0; i < postReviewLatenciesBoxed.length; i++) {
            postReviewLatencies[i] = postReviewLatenciesBoxed[i];
        }

        Long[] postLatenciesBoxed = POST_latencies.toArray(new Long[0]);
        long[] postLatencies = new long[postLatenciesBoxed.length];
        for (int i = 0; i < postLatenciesBoxed.length; i++) {
            postLatencies[i] = postLatenciesBoxed[i];
        }
        
        long wallTime = (endTime - startTime) / 1000;
        // 4 requests per thread: POST a new album and image + POST two likes and one dislike for the album.
        long totalRequests = (long) threadGroupSize * numThreadGroups * Config.LOAD_TEST_REQUESTS_PER_THREAD * 4;  // 4 POST per thread
        long failedRequests = failedRequestCounter.get();
        long throughput = totalRequests / wallTime;
        System.out.println("totalRequests: " + totalRequests +", Failure Rate: " + (failedRequests*1.0 / totalRequests));
        System.out.println("failedRequets: " + failedRequestCounter );
        System.out.println("successfulRequets: " + (totalRequests - failedRequests) );
        System.out.println(Utilities.getFormattedDate(startTime) + " - " + Utilities.getFormattedDate(endTime));  
        System.out.println("Wall Time: " + wallTime + " seconds");        
        System.out.println("Throughput: " + throughput + " requests/second");
        Utilities.calculateStats(postReviewLatencies, "POST ALBUM REVIEW");
        Utilities.calculateStats(postLatencies, "POST ALBUM");
        List<Long> throughputList = new ArrayList<>(throughputs);
        Utilities.writeThroughputToFile(throughputList, server_type, s_numThreadGroups);
        System.out.println(Utilities.getFormattedDate());
        System.out.println("====== End hw3 Main Load Phase =====");
        return;
    }


}


