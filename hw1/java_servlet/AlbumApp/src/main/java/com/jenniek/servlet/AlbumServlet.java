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

@WebServlet("/albums/*")
@MultipartConfig
public class AlbumServlet extends HttpServlet {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Album> dummyAlbums = new HashMap<String, Album>() {{
        put("1", new Album("1", "Album 1", "Artist 1", "2001"));
        put("2", new Album("2", "Album 2", "Artist 2", "2002"));
    }};

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
        Album album = dummyAlbums.get(albumID);

        if (album == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Album not found + albumID="+albumID);
            return;
        }

        response.setContentType("application/json; charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getOutputStream(), album);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
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
            // System.err.println("@may: profileJson = "+profileJson);
            String albumID = "888";
            Response res = new Response(albumID, String.valueOf(fileSize));

            response.setContentType("application/json; charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(response.getOutputStream(), res);
        } catch (Exception e) {
            e.printStackTrace();  // Log the exception
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
