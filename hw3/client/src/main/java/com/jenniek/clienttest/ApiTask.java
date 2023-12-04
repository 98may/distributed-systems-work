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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;

import java.util.concurrent.*;

// optimize logging
public class ApiTask implements Runnable {
    private final String basePath;
    private final int requestsPerThread;
    private final CountDownLatch latch;
    private final String server_type;
    private final String s_numThreadGroups;
    AtomicLong requestCounter;
    AtomicLong failedRequestCounter;

    private ConcurrentLinkedQueue<Long> POST_latencies;
    private ConcurrentLinkedQueue<Long> POST_REVIEW_latencies;

/*
To reduce the new album write load, Just write 100 new albums per thread iteration instead of 1000, and remove the GET request. Then modify you client so that on each thread iteration you:

POST a new album and image

POST two likes and one dislike for the album.
 */

    // make POST_latencies record "POST a new album and image"
    // mkae POST_REVIEW_latencies record "POST two likes and one dislike for the album" 
    public ApiTask(String basePath, int requestsPerThread, CountDownLatch latch, String server_type, String s_numThreadGroups, ConcurrentLinkedQueue<Long> POST_latencies, ConcurrentLinkedQueue<Long> POST_REVIEW_latencies, AtomicLong requestCounter, AtomicLong failedRequestCounter) {
        this.basePath = basePath;
        this.requestsPerThread = requestsPerThread;
        this.latch = latch;
        this.server_type = server_type;
        this.s_numThreadGroups = s_numThreadGroups;
        this.POST_REVIEW_latencies = POST_REVIEW_latencies;
        this.POST_latencies = POST_latencies;
        this.requestCounter = requestCounter;
        this.failedRequestCounter = failedRequestCounter;
    }

    private void postReview(String albumID, String action) {
        HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    
        String url = basePath + "/review/" + action + "/" + albumID;
    
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofMinutes(2))
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();
    
        long startReviewPostTime = System.currentTimeMillis();
        try {
            httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            long endReviewPostTime = System.currentTimeMillis();
            POST_REVIEW_latencies.add(endReviewPostTime - startReviewPostTime);
            Utilities.writeLog(Utilities.getFormattedDate(startReviewPostTime), "POST REVIEW - " + action.toUpperCase(), endReviewPostTime - startReviewPostTime, 200, server_type, s_numThreadGroups);
        } catch (Exception e) {
            long errorTime = System.currentTimeMillis();
            Utilities.writeLog(Utilities.getFormattedDate(errorTime), "POST REVIEW - " + action.toUpperCase(), errorTime - startReviewPostTime, -1, server_type, s_numThreadGroups);
        }
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
                    // 1: POST a new album and image 
                    String imageExample4kb = "/Users/may/Desktop/neu/cs6650_distributed/shortcuts/nmtb.png"; //4kb
                    // String imageExample57kb = "/Users/may/Desktop/neu/cs6650_distributed/shortcuts/smile.png"; //57kb
                    File image = new File(imageExample4kb);
                    AlbumsProfile profile = new AlbumsProfile();
                    profile.setArtist("rich Artist");
                    profile.setTitle("rich Album Title");
                    profile.setYear("1998");

                    long startPostTime = System.currentTimeMillis();
                    apiInstance.newAlbum(image, profile);
                    long endPostTime = System.currentTimeMillis();
                    requestCounter.incrementAndGet();
                    POST_latencies.add(endPostTime - startPostTime);
                    Utilities.writeLog(Utilities.getFormattedDate(startPostTime), "POST ALBUM", endPostTime - startPostTime, 201, server_type, s_numThreadGroups);  // Assume 201 for simplicity

                    // 2: POST two likes and one dislike for the album.
                    String albumID = "1";  // simplify
                    postReview(albumID, "like"); 
                    postReview(albumID, "like"); 
                    postReview(albumID, "dislike"); 

                    success = true;
                } catch (ApiException e) {
                    // Utilities.writeLog(Utilities.getFormattedDate(), "GET", -1, e.getCode(), server_type, s_numThreadGroups);
                    Utilities.writeLog(Utilities.getFormattedDate(), "POST", -1, e.getCode(), server_type, s_numThreadGroups);
                } 
            }
            if(!success){
                failedRequestCounter.incrementAndGet();
                System.err.println("connection failed after 5 try");
            }
        }

        if(latch != null) {
            latch.countDown();
        }
        
    }
    
}



