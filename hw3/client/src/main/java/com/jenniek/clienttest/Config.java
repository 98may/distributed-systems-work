package com.jenniek.clienttest;

public class Config {
    public static final int INITIAL_THREAD_COUNT = 10;
    public static final int INIT_REQUESTS_PER_THREAD = 100;
    public static final int LOAD_TEST_REQUESTS_PER_THREAD = 100; // To reduce the new album write load, Just write 100 new albums per thread iteration instead of 1000,
    public static final int MAX_RETRIES = 5;

    public static final String CLIENT_LOG_PATH = "/Users/may/Desktop/neu/cs6650_distributed/distributed-systems-work/hw3/test_results/";

    public static final String javaServletAddress = "http://3.80.33.155:8080/AlbumApp";
    public static final String javaServletAddress2 = "http://54.221.189.103:8080/AlbumApp";
    public static final String goServerAddress = "http://3.80.33.155:8081/IGORTON/AlbumStore/1.0.0";
}
