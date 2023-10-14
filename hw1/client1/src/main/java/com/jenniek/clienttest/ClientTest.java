package com.jenniek.clienttest;

import io.swagger.client.*;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.*;
import java.lang.*;

import java.io.File;

public class ClientTest {

    public static void main(String[] args) {
        // Create an instance of ApiClient
        ApiClient apiClient = new ApiClient();
        
        String javaServletAdress = "http://3.80.33.155:8080/AlbumApp";
        String goServerAdress = "http://3.80.33.155:8081/IGORTON/AlbumStore/1.0.0";
        // Set the base path to your server URI
        // apiClient.setBasePath(javaServletAdress);
        apiClient.setBasePath(goServerAdress);
        
        System.err.println("new jennyk");
        
        
        // Create an instance of DefaultApi using the ApiClient
        DefaultApi apiInstance = new DefaultApi(apiClient);

        // Test GET request
        String albumID = "1";  // replace with a valid album ID， both "1" and "2" works
        try {
            AlbumInfo result = apiInstance.getAlbumByKey(albumID);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("new jennyk Exception when calling DefaultApi#getAlbumByKey");
            e.printStackTrace();
        }

        // Test POST request
        String imageExample = "/Users/may/Desktop/neu/cs6650_distributed/shortcuts/smile.png";
        File image = new File(imageExample);  
        // File image = new File(imageExample);  
        AlbumsProfile profile = new AlbumsProfile();  
        profile.setArtist("jenniek Artist");
        profile.setTitle("jenniek Album");
        profile.setYear("2023");
        try {
            ImageMetaData result = apiInstance.newAlbum(image, profile);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DefaultApi#newAlbum");
            e.printStackTrace();
        }
    }
}
