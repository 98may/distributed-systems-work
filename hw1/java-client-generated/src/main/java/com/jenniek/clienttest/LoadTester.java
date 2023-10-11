package com.jenniek.clienttest;

import io.swagger.client.*;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.*;
import java.io.File;
import java.util.concurrent.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LoadTester {
    private static final int INITIAL_THREAD_COUNT = 10;
    private static final int INIT_REQUESTS_PER_THREAD = 100;
    private static final int LOAD_TEST_REQUESTS_PER_THREAD = 1000;
    private static final int MAX_RETRIES = 5;

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
        System.out.println("======Start Initialization phase=====");
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
        formattedDate = sdf.format(new Date(System.currentTimeMillis()));
        System.out.println("====End Initialization phase=====");
        System.out.println(formattedDate);


        // Start measuring time
        long startTime = System.currentTimeMillis();

        // Main load test phase
        System.out.println("================Start Main load test phase================");
        formattedDate = sdf.format(new Date(System.currentTimeMillis()));
        System.out.println(formattedDate);

        ExecutorService executor = Executors.newFixedThreadPool(threadGroupSize);
        for (int i = 0; i < numThreadGroups; i++) {
            for (int j = 0; j < threadGroupSize; j++) {
                executor.execute(new ApiTask(IPAddr, LOAD_TEST_REQUESTS_PER_THREAD));
            }
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

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
                sendRequest(apiInstance);
            }
        }

        private void sendRequest(DefaultApi apiInstance) {
            boolean success = false;
            int retries = 0;
            while (!success && retries < MAX_RETRIES) {
                try {
                    // POST request
                    String imageExample = "/Users/may/Desktop/neu/cs6650_distributed/shortcuts/smile.png";
                    File image = new File(imageExample);
                    AlbumsProfile profile = new AlbumsProfile();
                    profile.setArtist("Artist");
                    profile.setTitle("Album");
                    profile.setYear("2023");
                    apiInstance.newAlbum(image, profile);

                    // Only GET request during initialization phase
                    apiInstance.getAlbumByKey("1");

                    success = true;  // Set success to true if no exception is thrown
                } catch (ApiException e) {
                    if (e.getCode() >= 400 && e.getCode() < 600) {
                        retries++;
                        if (retries == MAX_RETRIES) {
                            System.err.println("Failed request after " + MAX_RETRIES + " retries: " + e.getMessage());
                        }
                    } else {
                        System.err.println("Failed request due to other error: " + e.getMessage());
                        break;  // Break out of loop if error is not 4XX or 5XX
                    }
                }
            }
        }
    }
}
