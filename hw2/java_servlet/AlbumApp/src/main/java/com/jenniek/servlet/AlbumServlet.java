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
                Album album = new Album(rs.getString("album_id"), rs.getString("name"), rs.getString("artist"), rs.getString("release_year"), rs.getString("image"));
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

        try (Connection conn = getDatabaseConnection()) {
            // Handle the image part
            Part imagePart = request.getPart("image");
            String imagePath = Paths.get(imagePart.getSubmittedFileName()).getFileName().toString(); 
            long fileSize = imagePart.getSize();

            /* 
            // Handle the profile part
            Part profilePart = request.getPart("profile"); // Assuming 'profile' is sent as a part
            if (profilePart == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Profile data is missing");
                return;
            }
            AlbumsProfile profile = objectMapper.readValue(profilePart.getInputStream(), AlbumsProfile.class);
*/
            // Handle the profile fields
            
            Part profilePart = request.getPart("profile"); // Assuming 'profile' is sent as a part
            if (profilePart == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Profile data is missing");
                return;
            }

            // InputStream inputStream = profilePart.getInputStream();
            // String json = new BufferedReader(new InputStreamReader(inputStream))
            //             .lines().collect(Collectors.joining("\n"));

            // System.err.println("Profile JSON String = " + json);


            String profileString = request.getParameter("profile");
            System.err.println("@profileString = " + profileString);
            // AlbumsProfile profile = objectMapper.readValue(profileString, AlbumsProfile.class);

            String[] lines = profileString.split("\n");
            String artist = lines[1].split(": ")[1].trim();
            String title = lines[2].split(": ")[1].trim();
            String year = lines[3].split(": ")[1].trim();

            AlbumsProfile profile = new AlbumsProfile(artist, title, year);

            // Insert into database
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO albums (album_id, name, artist, release_year, image) VALUES (?, ?, ?, ?, ?)");

            String albumID = UUID.randomUUID().toString();
            stmt.setString(1, albumID);
            stmt.setString(2, profile.getTitle());
            stmt.setString(3, profile.getArtist());
            stmt.setString(4, profile.getYear());
            stmt.setString(5, imagePath); // Assuming you want to store the image size
            stmt.executeUpdate();
            System.err.println("$may: executeUpdate() is right!");


            // Respond with the new album ID and image size
            Response res = new Response(albumID, Long.toString(fileSize));
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
        private final String image;

        public Response(String albumID, String image) {
            this.albumID = albumID;
            this.image = image;
        }

        public String getAlbumID() {
            return albumID;
        }

        public String getImageSize() {
            return image;
        }
    }
}
