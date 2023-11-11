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

@WebServlet("/albums/*")
@MultipartConfig
public class AlbumServlet extends HttpServlet {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Connection getDatabaseConnection() throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver"); // Ensure the JDBC driver is loaded.
        } catch (ClassNotFoundException e) {
            e.printStackTrace(); // Handle exception: log it or throw an error.
        }
        return DriverManager.getConnection(Config.jdbcUrl, Config.username, Config.password);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // String albumID = request.getParameter("albumID");
        String pathInfo = request.getPathInfo();  // /{albumID}
        String[] pathParts = pathInfo.split("/");
        String albumID = null;
        if (pathParts.length > 1) {
            albumID = pathParts[1];
        }
    
        if (albumID == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Album ID is missing");
            return;
        }

        try (Connection conn = getDatabaseConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM albums WHERE album_id = ?");
            stmt.setString(1, albumID);
            ResultSet rs = stmt.executeQuery();
    
            if (!rs.next()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Album not found + albumID=" + albumID);
            } else {
                Album album = new Album(rs.getString("album_id"), rs.getString("name"), rs.getString("artist"), rs.getString("release_year"), rs.getLong("image_size"));
                response.setContentType("application/json; charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(response.getOutputStream(), album);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Log the exception
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Get the image part
        Part imagePart = request.getPart("image");
        long fileSize = imagePart.getSize();
        // System.err.println("@may: image fileSize = "+fileSize);

        // System.err.println("@may: Get the profile part");
        // Get the profile part
        String profileJson = request.getParameter("profile");
        if (profileJson == null || profileJson.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Profile data is missing");
            return;
        }
        System.err.println("@may: profileJson = "+profileJson);
        
        try (Connection conn = getDatabaseConnection()) {
            // Assuming you have a table `albums` and columns `album_id`, `name`, `artist`, `release_year`, `image_size`
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO albums (album_id, name, artist, release_year, image_size) VALUES (?, ?, ?, ?, ?)");
    
            // Extract album data from `profileJson` using ObjectMapper
            Album album = objectMapper.readValue(profileJson, Album.class);
    
            stmt.setString(1, album.getAlbumID());
            stmt.setString(2, album.getTitle());
            stmt.setString(3, album.getArtist());
            stmt.setString(4, album.getYear());
            stmt.setLong(5, fileSize);
            stmt.executeUpdate();
    
            // Respond with the new album ID and image size
            Response res = new Response(album.getAlbumID(), String.valueOf(fileSize));
            response.setContentType("application/json; charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(response.getOutputStream(), res);
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private static class Response {
        private final String albumID;
        private final String imageSize;

        public Response(String albumID, String imageSize) {
            this.albumID = albumID;
            this.imageSize = imageSize;
        }

        public String getAlbumID() {
            return albumID;
        }

        public String getImageSize() {
            return imageSize;
        }
    }
}
