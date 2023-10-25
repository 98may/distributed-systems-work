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

public class ApiTask implements Runnable {
    private final String basePath;
    private final int requestsPerThread;
    private final CountDownLatch latch;
    private final String server_type;
    private final String s_numThreadGroups;
    List<Long> GET_latencies;
    List<Long> POST_latencies;
    AtomicLong requestCounter;


    public ApiTask(String basePath, int requestsPerThread, CountDownLatch latch, String server_type, String s_numThreadGroups, List<Long> GET_latencies, List<Long> POST_latencies, AtomicLong requestCounter) {
        this.basePath = basePath;
        this.requestsPerThread = requestsPerThread;
        this.latch = latch;
        this.server_type = server_type;
        this.s_numThreadGroups = s_numThreadGroups;
        this.GET_latencies = GET_latencies;
        this.POST_latencies = POST_latencies;
        this.requestCounter = requestCounter;
    }

    @Override
    public void run() {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(basePath);
        DefaultApi apiInstance = new DefaultApi(apiClient);

        for (int i = 0; i < requestsPerThread; i++) {
            boolean success = false;
            int try_time = 0;
            while(try_time < Config.MAX_RETRIES && !success){
                try_time++;
                try {
                    // GET request
                    long startGetTime = System.currentTimeMillis();
                    apiInstance.getAlbumByKey("1");
                    long endGetTime = System.currentTimeMillis();   
                    requestCounter.incrementAndGet();                 
                    GET_latencies.add(endGetTime - startGetTime);

                    Utilities.writeLog(Utilities.getFormattedDate(startGetTime), "GET", endGetTime - startGetTime, 200, server_type, s_numThreadGroups);

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
                    Utilities.writeLog(Utilities.getFormattedDate(startPostTime), "POST", endPostTime - startPostTime, 201, server_type, s_numThreadGroups);  // Assume 201 for simplicity
                    success = true;
                } catch (ApiException e) {
                    System.err.println("connection failed after 5 try");
                    Utilities.writeLog(Utilities.getFormattedDate(), "GET", -1, e.getCode(), server_type, s_numThreadGroups);
                    Utilities.writeLog(Utilities.getFormattedDate(), "POST", -1, e.getCode(), server_type, s_numThreadGroups);
                } 
            }
        }

        if(latch != null) {
            latch.countDown();
        }
        
    }
    
}



