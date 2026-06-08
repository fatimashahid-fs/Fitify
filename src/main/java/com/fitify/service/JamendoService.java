package com.fitify.service;

import com.fitify.model.Track;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * JamendoService — searches and streams full tracks via the Jamendo public API.
 *
 * Why Jamendo over Deezer:
 *  - Full tracks (not 30-second previews)
 *  - Completely free, no API key needed for read-only search
 *  - All music is Creative Commons licensed
 *  - Direct MP3 stream URLs playable by JavaFX MediaPlayer
 *
 * API docs: https://developer.jamendo.com/v3.0/tracks
 */
public class JamendoService {

    // Jamendo public client ID — free tier, read-only, no sign-up needed
    private static final String CLIENT_ID = "5415afc1";
    private static final String BASE_URL  =
        "https://api.jamendo.com/v3.0/tracks/?client_id=" + CLIENT_ID +
        "&format=json&limit=30&include=musicinfo&audioformat=mp32";

    private final HttpClient http   = HttpClient.newHttpClient();
    private final JSONParser parser = new JSONParser();

    /**
     * Search Jamendo tracks by name/artist/tag.
     * Returns full-length streamable MP3 tracks.
     */
    public List<Track> search(String query) {
        List<Track> results = new ArrayList<>();
        if (query == null || query.isBlank()) return results;
        try {
            String encoded = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
            String url = BASE_URL + "&namesearch=" + encoded;

            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET().build();

            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            JSONObject root    = (JSONObject) parser.parse(res.body());
            JSONArray  results_ = (JSONArray) root.get("results");
            if (results_ == null) return results;

            for (Object obj : results_) {
                JSONObject song = (JSONObject) obj;

                String title      = str(song, "name");
                String artist     = str(song, "artist_name");
                String album      = str(song, "album_name");
                String audioUrl   = str(song, "audio");       // full MP3 stream URL
                String shareUrl   = str(song, "shareurl");
                int    duration   = num(song, "duration");
                int    bpm        = 128; // Jamendo doesn't expose BPM in free tier

                // genre tag
                JSONObject musicInfo = (JSONObject) song.get("musicinfo");
                String genre = "";
                if (musicInfo != null) {
                    JSONObject tags = (JSONObject) musicInfo.get("tags");
                    if (tags != null) {
                        JSONArray genres = (JSONArray) tags.get("genres");
                        if (genres != null && !genres.isEmpty())
                            genre = genres.get(0).toString();
                    }
                }

                if (audioUrl == null || audioUrl.isEmpty()) continue;

                Track t = new Track(title, artist, audioUrl, duration, bpm, genre);
                t.setAlbum(album != null ? album : "");
                results.add(t);
            }
        } catch (Exception e) {
            System.err.println("JamendoService.search error: " + e.getMessage());
        }
        return results;
    }

    /**
     * Browse tracks by genre tag  (e.g. "electronic", "rock", "hiphop").
     */
    public List<Track> browseByGenre(String genre) {
        List<Track> results = new ArrayList<>();
        try {
            String encoded = URLEncoder.encode(genre.trim(), StandardCharsets.UTF_8);
            String url = BASE_URL + "&tags=" + encoded + "&orderby=popularity_total";

            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET().build();

            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            JSONObject root = (JSONObject) parser.parse(res.body());
            JSONArray  arr  = (JSONArray) root.get("results");
            if (arr == null) return results;

            for (Object obj : arr) {
                JSONObject song = (JSONObject) obj;
                String audioUrl = str(song, "audio");
                if (audioUrl == null || audioUrl.isEmpty()) continue;
                Track t = new Track(
                    str(song, "name"),
                    str(song, "artist_name"),
                    audioUrl,
                    num(song, "duration"),
                    128,
                    genre
                );
                t.setAlbum(str(song, "album_name"));
                results.add(t);
            }
        } catch (Exception e) {
            System.err.println("JamendoService.browseByGenre error: " + e.getMessage());
        }
        return results;
    }

    /** Fetch trending workout tracks (popular + energetic tags). */
    public List<Track> getTrendingWorkout() {
        return browseByGenre("electronic");
    }

    private String str(JSONObject o, String key) {
        if (o == null) return "";
        Object v = o.get(key);
        return v != null ? v.toString() : "";
    }

    private int num(JSONObject o, String key) {
        if (o == null) return 0;
        Object v = o.get(key);
        if (v instanceof Long)   return ((Long) v).intValue();
        if (v instanceof Number) return ((Number) v).intValue();
        return 0;
    }
}
