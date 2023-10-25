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

// client3: without log version
public class ApiTask implements Runnable {
    private final String basePath;
    private final int requestsPerThread;
    private final CountDownLatch latch;
    private final String server_type;
    private final String s_numThreadGroups;

    public ApiTask(String basePath, int requestsPerThread, CountDownLatch latch, String server_type, String s_numThreadGroups) {
        this.basePath = basePath;
        this.requestsPerThread = requestsPerThread;
        this.latch = latch;
        this.server_type = server_type;
        this.s_numThreadGroups = s_numThreadGroups;
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
                    apiInstance.getAlbumByKey("1");

                    // POST request
                    String imageExample4kb = "/Users/may/Desktop/neu/cs6650_distributed/shortcuts/nmtb.png"; //4kb
                    // String imageExample57kb = "/Users/may/Desktop/neu/cs6650_distributed/shortcuts/smile.png"; //57kb
                    File image = new File(imageExample4kb);
                    AlbumsProfile profile = new AlbumsProfile();
                    profile.setArtist("Artist");
                    profile.setTitle("Album");
                    profile.setYear("2023");

                    apiInstance.newAlbum(image, profile);
                    success = true;
                } catch (ApiException e) {
                    System.err.println("connection failed after 5 try");
                } 
            }
        }

        if(latch != null) {
            latch.countDown();
        }
        
    }
    
}



