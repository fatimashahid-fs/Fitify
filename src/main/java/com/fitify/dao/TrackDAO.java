package com.fitify.dao;

import com.fitify.interfaces.Storable;
import com.fitify.model.*;
import com.fitify.util.DatabaseManager;

import java.sql.*;
import java.util.*;

/**
 * TrackDAO - Implements Storable<Track> for MySQL music persistence.
 * Also handles Playlist CRUD.
 * OOP Concept: ABSTRACTION (implements Storable interface)
 */
public class TrackDAO implements Storable<Track> {

    private Connection conn() { return DatabaseManager.getInstance().getConnection(); }

    // ── Track CRUD ────────────────────────────────────────────────

    @Override
    public boolean save(Track t) {
        String sql = "INSERT INTO tracks(user_id,title,artist,album,file_path,duration_sec,bpm,genre) VALUES(?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt   (1, t.getUserId());
            ps.setString(2, t.getTitle());
            ps.setString(3, t.getArtist());
            ps.setString(4, t.getAlbum());
            ps.setString(5, t.getFilePath());
            ps.setInt   (6, t.getDurationSec());
            ps.setInt   (7, t.getBpm());
            ps.setString(8, t.getGenre());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) t.setTrackId(keys.getInt(1));
            return true;
        } catch (SQLException e) { System.err.println("TrackDAO.save: " + e.getMessage()); return false; }
    }

    public List<Track> findByUser(int userId) {
        List<Track> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM tracks WHERE user_id=? ORDER BY title")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapTrack(rs));
        } catch (SQLException e) { System.err.println("TrackDAO.findByUser: " + e.getMessage()); }
        return list;
    }

    @Override
    public Optional<Track> findById(int id) {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM tracks WHERE track_id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapTrack(rs));
        } catch (SQLException e) { System.err.println("TrackDAO.findById: " + e.getMessage()); }
        return Optional.empty();
    }

    @Override
    public List<Track> findAll() {
        List<Track> list = new ArrayList<>();
        try (ResultSet rs = conn().createStatement().executeQuery("SELECT * FROM tracks ORDER BY title")) {
            while (rs.next()) list.add(mapTrack(rs));
        } catch (SQLException e) { System.err.println("TrackDAO.findAll: " + e.getMessage()); }
        return list;
    }

    @Override
    public boolean update(Track t) {
        String sql = "UPDATE tracks SET title=?,artist=?,album=?,file_path=?,duration_sec=?,bpm=?,genre=? WHERE track_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, t.getTitle());
            ps.setString(2, t.getArtist());
            ps.setString(3, t.getAlbum());
            ps.setString(4, t.getFilePath());
            ps.setInt   (5, t.getDurationSec());
            ps.setInt   (6, t.getBpm());
            ps.setString(7, t.getGenre());
            ps.setInt   (8, t.getTrackId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("TrackDAO.update: " + e.getMessage()); return false; }
    }

    @Override
    public boolean delete(int id) {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM tracks WHERE track_id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("TrackDAO.delete: " + e.getMessage()); return false; }
    }

    // ── Playlist persistence ──────────────────────────────────────

    public boolean savePlaylist(Playlist pl) {
        String sql = "INSERT INTO playlists(user_id,name,mood_tag,workout_type) VALUES(?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt   (1, pl.getUserId());
            ps.setString(2, pl.getName());
            ps.setString(3, pl.getMoodTag());
            ps.setString(4, pl.getWorkoutType());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                pl.setPlaylistId(keys.getInt(1));
                savePlaylistTracks(pl);
            }
            return true;
        } catch (SQLException e) { System.err.println("TrackDAO.savePlaylist: " + e.getMessage()); return false; }
    }

    private void savePlaylistTracks(Playlist pl) throws SQLException {
        String sql = "INSERT IGNORE INTO playlist_tracks(playlist_id,track_id,sort_order) VALUES(?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            for (int i = 0; i < pl.getTracks().size(); i++) {
                ps.setInt(1, pl.getPlaylistId());
                ps.setInt(2, pl.getTracks().get(i).getTrackId());
                ps.setInt(3, i);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public List<Playlist> findPlaylistsByUser(int userId) {
        List<Playlist> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM playlists WHERE user_id=?")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Playlist pl = mapPlaylist(rs);
                pl.setTracks(loadTracksForPlaylist(pl.getPlaylistId()));
                list.add(pl);
            }
        } catch (SQLException e) { System.err.println("TrackDAO.findPlaylistsByUser: " + e.getMessage()); }
        return list;
    }

    private List<Track> loadTracksForPlaylist(int playlistId) throws SQLException {
        List<Track> list = new ArrayList<>();
        String sql = "SELECT t.* FROM tracks t " +
                     "JOIN playlist_tracks pt ON t.track_id=pt.track_id " +
                     "WHERE pt.playlist_id=? ORDER BY pt.sort_order";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, playlistId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapTrack(rs));
        }
        return list;
    }

    private Track mapTrack(ResultSet rs) throws SQLException {
        Track t = new Track();
        t.setTrackId    (rs.getInt   ("track_id"));
        t.setUserId     (rs.getInt   ("user_id"));
        t.setTitle      (rs.getString("title"));
        t.setArtist     (rs.getString("artist"));
        t.setAlbum      (rs.getString("album"));
        t.setFilePath   (rs.getString("file_path"));
        t.setDurationSec(rs.getInt   ("duration_sec"));
        t.setBpm        (rs.getInt   ("bpm"));
        t.setGenre      (rs.getString("genre"));
        return t;
    }

    private Playlist mapPlaylist(ResultSet rs) throws SQLException {
        Playlist pl = new Playlist();
        pl.setPlaylistId (rs.getInt   ("playlist_id"));
        pl.setUserId     (rs.getInt   ("user_id"));
        pl.setName       (rs.getString("name"));
        pl.setMoodTag    (rs.getString("mood_tag"));
        pl.setWorkoutType(rs.getString("workout_type"));
        return pl;
    }
}
