package com.jenniek.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.jenniek.model.*;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.annotation.MultipartConfig;
import java.util.regex.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.*;
import java.nio.file.Paths;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariConfig;

@WebServlet("/review/*")
public class AlbumReviewServlet extends HttpServlet {
    // implementation for handling like/dislike

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static HikariDataSource dataSource;
    static {
        try{
            HikariConfig config = new HikariConfig();
            config.setDriverClassName("com.mysql.cj.jdbc.Driver"); // key!!!

            config.setJdbcUrl(Config.jdbcUrl); // Replace with your JDBC URL
            config.setUsername(Config.username); // Replace with your username
            config.setPassword(Config.password); // Replace with your password

            // Additional HikariCP settings - adjust as necessary
            config.setMaximumPoolSize(30); // Set the maximum pool size
            config.setMinimumIdle(5); // Set the minimum number of idle connections HikariCP maintains in the pool
            config.setIdleTimeout(600000); // Set the maximum time (in milliseconds) that a connection is allowed to sit idle in the pool
            dataSource = new HikariDataSource(config);
        }catch(Exception e){
            e.printStackTrace(); 
            System.err.println("@Error initializing HikariCP: " + e.getMessage());
        }
    }

    private Connection getDatabaseConnection() throws SQLException {
        if (dataSource == null) {
            System.err.println("@@ayan DataSource is not initialized.");
            throw new SQLException("DataSource is not initialized.");
        }
        return dataSource.getConnection();
    }
    
/* 
    private Connection getDatabaseConnection() throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver"); // Ensure the JDBC driver is loaded.
        } catch (ClassNotFoundException e) {
            e.printStackTrace(); // Handle exception: log it or throw an error.
        }
        return DriverManager.getConnection(Config.jdbcUrl, Config.username, Config.password);
    }*/


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo(); // e.g., /like/123 or /dislike/123
        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing like or dislike action and album ID");
            return;
        }

        String[] pathParts = pathInfo.split("/");
        if (pathParts.length != 3) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid URL pattern");
            return;
        }

        String action = pathParts[1];
        String albumID = pathParts[2];

        if (!"like".equals(action) && !"dislike".equals(action)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Action must be 'like' or 'dislike'");
            return;
        }

        try (Connection conn = getDatabaseConnection()) {
            // Use a SQL transaction for atomicity
            conn.setAutoCommit(false);
            
            // Check if the album exists before updating likes/dislikes
            PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM albums WHERE album_id = ?");
            checkStmt.setString(1, albumID);
            var rs = checkStmt.executeQuery();
            
            // Make sure album exists
            int albumCount = 0;
            if (rs.next()) {
                albumCount = rs.getInt(1);
            }
            
            if (albumCount == 0) {
                conn.rollback(); // Roll back transaction
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Album not found with ID: " + albumID);
                return;
            }
            
            // // Prepare the SQL statement for updating likes
            // String sql = "UPDATE albums SET likes = likes + ? WHERE album_id = ?";
            // PreparedStatement stmt = conn.prepareStatement(sql);
            
            // // Increment or decrement the likes based on the action
            // stmt.setInt(1, "like".equals(action) ? 1 : -1);
            // stmt.setString(2, albumID);
            String sql;
            if ("like".equals(action)) {
                // Increment the likes count
                sql = "UPDATE albums SET likes = likes + 1 WHERE album_id = ?";
            } else {
                // Increment the dislikes count
                sql = "UPDATE albums SET dislikes = dislikes + 1 WHERE album_id = ?";
            }

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, albumID);

            // Execute the update
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                conn.rollback(); // Roll back transaction if no rows affected
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "No album found with ID: " + albumID);
            } else {
                conn.commit(); // Commit the transaction
                response.setStatus(HttpServletResponse.SC_OK);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Log the exception
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

}
