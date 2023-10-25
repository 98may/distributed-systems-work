package com.jenniek.clienttest;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.DefaultApi;

public class SendGetRequests implements Runnable {
    private final String basePath;
    private final int requestsPerThread;

    SendGetRequests(String basePath, int requestsPerThread){
        this.basePath = basePath;
        this.requestsPerThread = requestsPerThread;
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

                    success = true;
                } catch (ApiException e) {
                    System.err.println("connection failed after 5 try");
                } 
            }
        }
    }
}
