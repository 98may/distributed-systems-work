package com.jenniek.servlet;

public class Config {
    public static final String hostname = "dsdatabase.ccfcharlh91s.us-east-1.rds.amazonaws.com"; // aws rds endpoint
    public static final String port = "3306";
    public static final String databaseName = "ds_hw2_db";
    public static final String username = "admin";
    public static final String password = "01234567";
    public static final String jdbcUrl = "jdbc:mysql://" + hostname + ":" + port + "/" + databaseName;

}