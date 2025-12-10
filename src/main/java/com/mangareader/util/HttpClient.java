package com.mangareader.util;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Component
public class HttpClient {

    private static final int TIMEOUT = 10000; // 10 seconds

    /**
     * Perform a GET request and return the response body as a string
     */
    public String get(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        try {
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("User-Agent", "MangaUniversalBackend/1.0");

            int responseCode = conn.getResponseCode();

            if (responseCode >= 200 && responseCode < 300) {
                return readResponse(conn);
            } else {
                throw new Exception("HTTP error code: " + responseCode);
            }
        } finally {
            conn.disconnect();
        }
    }

    private String readResponse(HttpURLConnection conn) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }
}
