package com.fitify.dao;

import com.fitify.interfaces.Storable;
import com.fitify.model.*;
import com.fitify.util.DatabaseManager;

import java.time.LocalDate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Date;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * WorkoutDAO - Implements Storable<WorkoutRoutine> for MySQL.
 * Also handles session persistence and exercise library queries.
 * OOP Concept: ABSTRACTION (implements Storable interface)
 */
public class WorkoutDAO implements Storable<WorkoutRoutine> {

    private Connection conn() { return DatabaseManager.getInstance().getConnection(); }

    // ── WorkoutRoutine CRUD ───────────────────────────────────────

    @Override
    public boolean save(WorkoutRoutine r) {
        String sql = "INSERT INTO routines(user_id,name,description,date_created,playlist_name) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt   (1, r.getUserId());
            ps.setString(2, r.getName());
            ps.setString(3, r.getDescription());
            ps.setDate(4, java.sql.Date.valueOf(r.getDateCreated()));
            ps.setString(5, r.getLinkedPlaylistName());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                r.setRoutineId(keys.getInt(1));
                saveExerciseLinks(r);
            }
            return true;
        } catch (SQLException e) { System.err.println("WorkoutDAO.save: " + e.getMessage()); return false; }
    }

    private void saveExerciseLinks(WorkoutRoutine r) throws SQLException {
        String sql = "INSERT IGNORE INTO routine_exercises(routine_id,exercise_id,sort_order) VALUES(?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            for (int i = 0; i < r.getExercises().size(); i++) {
                ps.setInt(1, r.getRoutineId());
                ps.setInt(2, r.getExercises().get(i).getExerciseId());
                ps.setInt(3, i);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    @Override
    public Optional<WorkoutRoutine> findById(int id) {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM routines WHERE routine_id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                WorkoutRoutine r = mapRoutine(rs);
                r.setExercises(loadExercisesForRoutine(r.getRoutineId()));
                return Optional.of(r);
            }
        } catch (SQLException e) { System.err.println("WorkoutDAO.findById: " + e.getMessage()); }
        return Optional.empty();
    }

    public List<WorkoutRoutine> findByUser(int userId) {
        List<WorkoutRoutine> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM routines WHERE user_id=? ORDER BY routine_id DESC")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                WorkoutRoutine r = mapRoutine(rs);
                r.setExercises(loadExercisesForRoutine(r.getRoutineId()));
                list.add(r);
            }
        } catch (SQLException e) { System.err.println("WorkoutDAO.findByUser: " + e.getMessage()); }
        return list;
    }

    @Override
    public List<WorkoutRoutine> findAll() {
        List<WorkoutRoutine> list = new ArrayList<>();
        try (ResultSet rs = conn().createStatement().executeQuery("SELECT * FROM routines")) {
            while (rs.next()) list.add(mapRoutine(rs));
        } catch (SQLException e) { System.err.println("WorkoutDAO.findAll: " + e.getMessage()); }
        return list;
    }

    @Override
    public boolean update(WorkoutRoutine r) {
        String sql = "UPDATE routines SET name=?,description=?,playlist_name=? WHERE routine_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, r.getName());
            ps.setString(2, r.getDescription());
            ps.setString(3, r.getLinkedPlaylistName());
            ps.setInt   (4, r.getRoutineId());
            conn().createStatement().execute(
                "DELETE FROM routine_exercises WHERE routine_id=" + r.getRoutineId());
            saveExerciseLinks(r);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("WorkoutDAO.update: " + e.getMessage()); return false; }
    }

    @Override
    public boolean delete(int id) {
        try {
            conn().createStatement().execute("DELETE FROM routine_exercises WHERE routine_id=" + id);
            try (PreparedStatement ps = conn().prepareStatement("DELETE FROM routines WHERE routine_id=?")) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) { System.err.println("WorkoutDAO.delete: " + e.getMessage()); return false; }
    }

    // ── Session persistence ───────────────────────────────────────

    public boolean saveSession(WorkoutSession s) {
        String sql = "INSERT INTO sessions(user_id,routine_id,start_time,end_time," +
                     "duration_seconds,calories_burned,notes) VALUES(?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt      (1, s.getUserId());
            ps.setInt      (2, s.getRoutine() != null ? s.getRoutine().getRoutineId() : 0);
            ps.setTimestamp(3, s.getStartTime() != null ? Timestamp.valueOf(s.getStartTime()) : null);
            ps.setTimestamp(4, s.getEndTime()   != null ? Timestamp.valueOf(s.getEndTime())   : null);
            ps.setInt      (5, s.getDurationSeconds());
            ps.setDouble   (6, s.getCaloriesBurned());
            ps.setString   (7, s.getNotes());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) s.setSessionId(keys.getInt(1));
            return true;
        } catch (SQLException e) { System.err.println("WorkoutDAO.saveSession: " + e.getMessage()); return false; }
    }

    public List<WorkoutSession> findSessionsByUser(int userId) {
        List<WorkoutSession> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM sessions WHERE user_id=? ORDER BY session_id DESC")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapSession(rs));
        } catch (SQLException e) { System.err.println("WorkoutDAO.findSessionsByUser: " + e.getMessage()); }
        return list;
    }

    // ── Exercise library ──────────────────────────────────────────

    public List<Exercise> findAllExercises() {
        List<Exercise> list = new ArrayList<>();
        try (ResultSet rs = conn().createStatement().executeQuery("SELECT * FROM exercises")) {
            while (rs.next()) list.add(mapExercise(rs));
        } catch (SQLException e) { System.err.println("WorkoutDAO.findAllExercises: " + e.getMessage()); }
        return list;
    }

    // ── Private mapping helpers ───────────────────────────────────

    private List<Exercise> loadExercisesForRoutine(int routineId) throws SQLException {
        List<Exercise> list = new ArrayList<>();
        String sql = "SELECT e.* FROM exercises e " +
                     "JOIN routine_exercises re ON e.exercise_id=re.exercise_id " +
                     "WHERE re.routine_id=? ORDER BY re.sort_order";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, routineId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapExercise(rs));
        }
        return list;
    }

    private Exercise mapExercise(ResultSet rs) throws SQLException {
        String type  = rs.getString("type");
        String extra = rs.getString("extra_data");
        Map<String, String> ex = parseExtra(extra);

        Exercise e;
        switch (type) {
            case "CARDIO" -> {
                CardioExercise c = new CardioExercise();
                c.setSpeedKmh       (parseDouble(ex, "speed",  8.0));
                c.setDistanceKm     (parseDouble(ex, "dist",   0.0));
                c.setTargetHeartRate(parseInt   (ex, "hr",     140));
                e = c;
            }
            case "STRENGTH" -> {
                StrengthExercise s = new StrengthExercise();
                s.setSets     (parseInt   (ex, "sets",   3));
                s.setReps     (parseInt   (ex, "reps",   10));
                s.setWeightKg (parseDouble(ex, "weight", 0.0));
                s.setEquipment(ex.getOrDefault("equip", "Bodyweight"));
                s.setCompound ("true".equals(ex.get("compound")));
                e = s;
            }
            case "FLEXIBILITY" -> {
                FlexibilityExercise f = new FlexibilityExercise();
                f.setHoldDurationSeconds(parseInt(ex, "hold", 30));
                f.setRepetitions        (parseInt(ex, "reps", 3));
                f.setStretchType        (ex.getOrDefault("stretch", "Static"));
                e = f;
            }
            default -> e = new CardioExercise();
        }
        e.setExerciseId     (rs.getInt   ("exercise_id"));
        e.setName           (rs.getString("name"));
        e.setDescription    (rs.getString("description"));
        e.setDurationMinutes(rs.getInt   ("duration_minutes"));
        e.setMuscleGroup    (rs.getString("muscle_group"));
        e.setDifficulty     (rs.getString("difficulty"));
        return e;
    }

    private WorkoutRoutine mapRoutine(ResultSet rs) throws SQLException {
        WorkoutRoutine r = new WorkoutRoutine();
        r.setRoutineId         (rs.getInt   ("routine_id"));
        r.setUserId            (rs.getInt   ("user_id"));
        r.setName              (rs.getString("name"));
        r.setDescription       (rs.getString("description"));
        r.setLinkedPlaylistName(rs.getString("playlist_name"));
        java.sql.Date d = rs.getDate("date_created");
        r.setDateCreated(d != null ? d.toLocalDate() : LocalDate.now());
        return r;
    }

    private WorkoutSession mapSession(ResultSet rs) throws SQLException {
        WorkoutSession s = new WorkoutSession();
        s.setSessionId      (rs.getInt   ("session_id"));
        s.setUserId         (rs.getInt   ("user_id"));
        s.setDurationSeconds(rs.getInt   ("duration_seconds"));
        s.setCaloriesBurned (rs.getDouble("calories_burned"));
        s.setNotes          (rs.getString("notes"));
        Timestamp st = rs.getTimestamp("start_time");
        if (st != null) s.setStartTime(st.toLocalDateTime());
        Timestamp et = rs.getTimestamp("end_time");
        if (et != null) s.setEndTime(et.toLocalDateTime());
        return s;
    }

    private Map<String, String> parseExtra(String extra) {
        Map<String, String> map = new HashMap<>();
        if (extra == null) return map;
        for (String pair : extra.split(",")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) map.put(kv[0].trim(), kv[1].trim());
        }
        return map;
    }

    private double parseDouble(Map<String, String> m, String k, double def) {
        try { return Double.parseDouble(m.getOrDefault(k, String.valueOf(def))); }
        catch (NumberFormatException e) { return def; }
    }

    private int parseInt(Map<String, String> m, String k, int def) {
        try { return Integer.parseInt(m.getOrDefault(k, String.valueOf(def))); }
        catch (NumberFormatException e) { return def; }
    }
}
