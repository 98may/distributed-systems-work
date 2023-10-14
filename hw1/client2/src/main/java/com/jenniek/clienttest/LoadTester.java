package com.jenniek.clienttest;

import io.swagger.client.*;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.*;
import java.io.File;
import java.util.concurrent.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LoadTester {
    private static final int INITIAL_THREAD_COUNT = 100;
    private static final int INIT_REQUESTS_PER_THREAD = 100;
    private static final int LOAD_TEST_REQUESTS_PER_THREAD = 500;
    // private static final int MAX_RETRIES = 5;

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: LoadTester <threadGroupSize> <numThreadGroups> <delay> [java|go]");
            return;
        }

        int threadGroupSize = Integer.parseInt(args[0]);
        int numThreadGroups = Integer.parseInt(args[1]);
        int delay = Integer.parseInt(args[2]) * 1000;  // Convert to milliseconds

        String javaServletAddress = "http://3.80.33.155:8080/AlbumApp";
        String goServerAddress = "http://3.80.33.155:8081/IGORTON/AlbumStore/1.0.0";
        String IPAddr = javaServletAddress;  // Default to Java servlet address

        if (args.length > 3) {
            if ("go".equals(args[3])) {
                IPAddr = goServerAddress;  // Switch to Go servlet address
            } else if (!"java".equals(args[3])) {
                System.err.println("@ayan: illegal server choice, should be either 'java' or 'go'");
                return;
            }
        }

        // Initialization phase
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = sdf.format(new Date(System.currentTimeMillis()));
        long t1 = System.currentTimeMillis();
        System.out.println("======new new Start Initialization phase=====");
        System.out.println(formattedDate);



        ExecutorService mainExecutor = Executors.newFixedThreadPool(INITIAL_THREAD_COUNT);
        for (int i = 0; i < INITIAL_THREAD_COUNT; i++) {
            mainExecutor.execute(new ApiTask(IPAddr, INIT_REQUESTS_PER_THREAD));
        }
        mainExecutor.shutdown();
        try {
            mainExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long t2 = System.currentTimeMillis();
        formattedDate = sdf.format(new Date(System.currentTimeMillis()));
        System.out.println("====new End Initialization phase=====");
        System.out.println(formattedDate);
        
        long wt = (t2-t1);
        System.out.println(String.format("walltime = %d mill seconds", wt));
        System.out.println(String.format("throughput = walltime/requests() = %d", (INITIAL_THREAD_COUNT * INIT_REQUESTS_PER_THREAD * 1000)/wt));

        // Start measuring time
        long startTime = System.currentTimeMillis();

        // Main load test phase
        System.out.println("================Start Main load test phase================");

        // System.out.println(numThreadGroups);
        // System.out.println(threadGroupSize);
        // System.out.println(LOAD_TEST_REQUESTS_PER_THREAD);
        // System.out.println(IPAddr);
        
        formattedDate = sdf.format(new Date(System.currentTimeMillis()));
        System.out.println(formattedDate);

        ExecutorService executor = Executors.newFixedThreadPool(threadGroupSize);
        for (int i = 0; i < numThreadGroups; i++) {
            // System.out.println("------numThreadGroup # ------");
            System.out.println(i);

            formattedDate = sdf.format(new Date(System.currentTimeMillis()));
            System.out.println(formattedDate);
            for (int j = 0; j < threadGroupSize; j++) {
                executor.execute(new ApiTask(IPAddr, LOAD_TEST_REQUESTS_PER_THREAD));
            }
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            formattedDate = sdf.format(new Date(System.currentTimeMillis()));
            System.out.println(formattedDate);
            System.out.println(i);
            // System.out.println("-------next numThreadGroup # ------");
        }

        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // End measuring time
        long endTime = System.currentTimeMillis();
        
        // Calculate and output results
        long wallTime = (endTime - startTime) / 1000;
        long totalRequests = (long) threadGroupSize * numThreadGroups * LOAD_TEST_REQUESTS_PER_THREAD * 2;  // 2 for both POST and GET
        long throughput = totalRequests / wallTime;
        System.out.println("Wall Time: " + wallTime + " seconds");
        System.out.println("Throughput: " + throughput + " requests/second");


        
    }

    static class ApiTask implements Runnable {
        private final String basePath;
        private final int requestsPerThread;

        public ApiTask(String basePath, int requestsPerThread) {
            this.basePath = basePath;
            this.requestsPerThread = requestsPerThread;
        }

        @Override
        public void run() {
            ApiClient apiClient = new ApiClient();
            apiClient.setBasePath(basePath);
            DefaultApi apiInstance = new DefaultApi(apiClient);
            for (int i = 0; i < requestsPerThread; i++) {
                try {
                    // GET request
                    apiInstance.getAlbumByKey("1"); // 0.1s


                    // POST request
                    String imageExample4kb = "/Users/may/Desktop/neu/cs6650_distributed/shortcuts/nmtb.png"; //4kb
                    // String imageExample57kb = "/Users/may/Desktop/neu/cs6650_distributed/shortcuts/smile.png"; //57kb
                    
                    File image = new File(imageExample4kb);
                    // File image = new File(imageExample57kb);
                    AlbumsProfile profile = new AlbumsProfile();
                    profile.setArtist("Artist");
                    profile.setTitle("Album");
                    profile.setYear("2023");
                    apiInstance.newAlbum(image, profile);
                } catch (ApiException e) {
                    System.err.println("@ayan     connection failed");
                }
            }
        }

        
    }
}
