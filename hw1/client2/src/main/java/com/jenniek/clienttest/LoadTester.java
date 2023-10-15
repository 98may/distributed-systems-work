package com.jenniek.clienttest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.AlbumsProfile;

import java.util.concurrent.*;
import java.util.stream.*;

public class LoadTester {
    private static final int INITIAL_THREAD_COUNT = 10;
    private static final int INIT_REQUESTS_PER_THREAD = 100;
    private static final int LOAD_TEST_REQUESTS_PER_THREAD = 1000;
    private static final int MAX_RETRIES = 5;

    private static final List<Long> GET_latencies = Collections.synchronizedList(new ArrayList<Long>());
    private static final List<Long> POST_latencies = Collections.synchronizedList(new ArrayList<Long>());

    private static final AtomicLong requestCounter = new AtomicLong(0);
    private static final List<Long> throughputs = Collections.synchronizedList(new ArrayList<Long>());
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String CLIENT_LOG_PATH = "/Users/may/Desktop/neu/cs6650_distributed/distributed-systems-work/hw1/test_results";

    private static String server_type = "";    
    private static String s_numThreadGroups = "";


    public static void main(String[] args) {       
        System.out.println("==================start=======================");

        if (args.length < 3) {
            System.err.println("Usage: LoadTester <threadGroupSize> <numThreadGroups> <delay> [java|go]");
            return;
        }
        if (args.length > 0) {
            System.err.print("Executing: LoadTester ");
            for (String arg : args) {
                System.err.print(arg + " ");
            }
            System.err.println(); // Move to the next line after printing all args
        }
        int threadGroupSize = Integer.parseInt(args[0]);
        int numThreadGroups = Integer.parseInt(args[1]);
        s_numThreadGroups = args[1];
        int delay = Integer.parseInt(args[2]) * 1000;  // Convert to milliseconds

        String javaServletAddress = "http://3.80.33.155:8080/AlbumApp";
        String goServerAddress = "http://3.80.33.155:8081/IGORTON/AlbumStore/1.0.0";
        String IPAddr = javaServletAddress;  // Default to Java servlet address

        if (args.length > 3) {
            server_type = args[3];
            if ("go".equals(args[3])) {
                IPAddr = goServerAddress;  // Switch to Go servlet address
            } else if (!"java".equals(args[3])) {
                System.err.println("@ayan: illegal server choice, should be either 'java' or 'go'");
                return;
            }
        }

        // Initialization phase
        String formattedDate = sdf.format(new Date(System.currentTimeMillis()));
        // System.out.println("======new new Start Initialization phase=====");
        // System.out.println(formattedDate);
        // long t1 = System.currentTimeMillis();

        // ExecutorService mainExecutor = Executors.newFixedThreadPool(INITIAL_THREAD_COUNT);
        // for (int i = 0; i < INITIAL_THREAD_COUNT; i++) {
        //     mainExecutor.execute(new ApiTask(IPAddr, INIT_REQUESTS_PER_THREAD, null));
        // }
        // mainExecutor.shutdown();
        // try {
        //     mainExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        // } catch (InterruptedException e) {
        //     e.printStackTrace();
        // }

        // long t2 = System.currentTimeMillis();
        // formattedDate = sdf.format(new Date(System.currentTimeMillis()));
        // System.out.println("Initialization phase=====");
        // System.out.println(formattedDate);
        
        // long wt = (t2-t1);
        // System.out.println(String.format("Init Phase - walltime = %d mill seconds", wt));
        // System.out.println(String.format("Init Phase - throughput =  %d", (INITIAL_THREAD_COUNT * INIT_REQUESTS_PER_THREAD * 1000)/wt));


        // Main load test phase
        System.out.println("================Start Main load test phase================");
        long startTime = System.currentTimeMillis();

        formattedDate = sdf.format(new Date(System.currentTimeMillis()));
        System.out.println(formattedDate);

        // Scheduled task to log throughput every second.
        ScheduledExecutorService throughputs_observer = Executors.newScheduledThreadPool(1);
        throughputs_observer.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                throughputs.add(requestCounter.getAndSet(0)); // Log and reset counter.
            }
        }, 0, 1, TimeUnit.SECONDS);


        CountDownLatch latch = new CountDownLatch(numThreadGroups * threadGroupSize);
        // CountDownLatch latch = new CountDownLatch(numThreadGroups * threadGroupSize * LOAD_TEST_REQUESTS_PER_THREAD);
        System.out.println("Init - latch.getCount() = " + latch.getCount());
        for (int i = 0; i < numThreadGroups; i++) {
            ExecutorService executor = Executors.newFixedThreadPool(threadGroupSize);
            for (int j = 0; j < threadGroupSize; j++) {
                executor.execute(new ApiTask(IPAddr, LOAD_TEST_REQUESTS_PER_THREAD, latch));
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

        throughputs_observer.shutdown();

        long endTime = System.currentTimeMillis();
        
        // Calculate and output results
        long wallTime = (endTime - startTime) / 1000;
        long totalRequests = (long) threadGroupSize * numThreadGroups * LOAD_TEST_REQUESTS_PER_THREAD * 2;  // 2 for both POST and GET
        long throughput = totalRequests / wallTime;
        if (args.length > 0) {
            System.err.print("Executing: LoadTester ");
            for (String arg : args) {
                System.err.print(arg + " ");
            }
            System.err.println(); 
        }
        System.out.println("totalRequests: " + totalRequests );
        System.out.println(sdf.format(new Date(startTime)) + " - " + sdf.format(new Date(endTime)));  
        System.out.println("Wall Time: " + wallTime + " seconds");        
        System.out.println("Throughput: " + throughput + " requests/second");
        calculateStats(GET_latencies, "GET");
        calculateStats(POST_latencies, "POST");
        writeThroughputToFile(throughputs, args[3], args[1]);
        System.out.println("==================end=======================");
        return;
    }

    static class ApiTask implements Runnable {
        private final String basePath;
        private final int requestsPerThread;
        private final CountDownLatch latch;

        public ApiTask(String basePath, int requestsPerThread, CountDownLatch latch) {
            this.basePath = basePath;
            this.requestsPerThread = requestsPerThread;
            this.latch = latch;
        }

        @Override
        public void run() {
            ApiClient apiClient = new ApiClient();
            apiClient.setBasePath(basePath);
            DefaultApi apiInstance = new DefaultApi(apiClient);

            for (int i = 0; i < requestsPerThread; i++) {
                boolean success = false;
                int try_time = 0;
                while(try_time < MAX_RETRIES && !success){
                    try_time++;
                    try {
                        // GET request
                        long startGetTime = System.currentTimeMillis();
                        apiInstance.getAlbumByKey("1"); // 0.1s
                        long endGetTime = System.currentTimeMillis();   
                        requestCounter.incrementAndGet();                 
                        GET_latencies.add(endGetTime - startGetTime);

                        writeLog(sdf.format(new Date(startGetTime)), "GET", endGetTime - startGetTime, 200);

                        // POST request
                        String imageExample4kb = "/Users/may/Desktop/neu/cs6650_distributed/shortcuts/nmtb.png"; //4kb
                        // String imageExample57kb = "/Users/may/Desktop/neu/cs6650_distributed/shortcuts/smile.png"; //57kb
                        File image = new File(imageExample4kb);
                        AlbumsProfile profile = new AlbumsProfile();
                        profile.setArtist("Artist");
                        profile.setTitle("Album");
                        profile.setYear("2023");

                        long startPostTime = System.currentTimeMillis();
                        apiInstance.newAlbum(image, profile);
                        long endPostTime = System.currentTimeMillis();
                        requestCounter.incrementAndGet();
                        POST_latencies.add(endPostTime - startPostTime);
                        writeLog(sdf.format(new Date(startPostTime)), "POST", endPostTime - startPostTime, 201);  // Assume 201 for simplicity
                        success = true;
                    } catch (ApiException e) {
                        System.err.println("connection failed after 5 try");
                        writeLog(sdf.format(new Date(System.currentTimeMillis())), "GET", -1, e.getCode());
                        writeLog(sdf.format(new Date(System.currentTimeMillis())), "POST", -1, e.getCode());
                    } 
                }
            }

            if(latch != null) {
                latch.countDown();
                // System.out.println(sdf.format(new Date(System.currentTimeMillis())) + " : latch.getCount() == left Running Threads == " + latch.getCount());
            }
            
        }

        private void writeLog(String startTime, String requestType, long latency, int responseCode) {
            String record = String.format("%s, %s, %d, %d\n", startTime, requestType, latency, responseCode);
            try {
                // Writing to a file named "client2_logs.csv" in append mode
                FileWriter fw = new FileWriter(CLIENT_LOG_PATH+"/"+server_type+"_"+s_numThreadGroups+".csv", true);
                fw.write(record);
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        
    }
    
    private static void calculateStats(List<Long> latencies, String requestType) {
        Collections.sort(latencies);
        long sum = 0;
        for (long latency : latencies) {
            sum += latency;
        }
        double mean = (double) sum / latencies.size();
        double median = latencies.get(latencies.size() / 2);
        double p99 = latencies.get((int) Math.ceil(0.99 * latencies.size()));
        long max = Collections.max(latencies);
        long min = Collections.min(latencies);

        System.out.println("----- " + requestType + " request statistics -----");
        System.out.println("Mean: " + mean + " ms");
        System.out.println("Median: " + median + " ms");
        System.out.println("P99: " + p99 + " ms");
        System.out.println("Min: " + min + " ms");
        System.out.println("Max: " + max + " ms");
    }

    private static void writeThroughputToFile(List<Long> throughputs, String server, String n) {
        try (PrintWriter writer = new PrintWriter(new File(CLIENT_LOG_PATH+"/" +server + "_" + n +"_throughputs.csv"))) {
            StringBuilder sb = new StringBuilder();
            sb.append("Time(s),Throughput(req/s)\n");
            for (int i = 0; i < throughputs.size(); i++) {
                sb.append(i).append(',').append(throughputs.get(i)).append('\n');
            }
            writer.write(sb.toString());
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

}