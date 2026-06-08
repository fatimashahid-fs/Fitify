package com.fitify.service;

import com.fitify.dao.WorkoutDAO;
import com.fitify.interfaces.Reportable;
import com.fitify.model.*;

import java.util.List;

/**
 * WorkoutService - Business logic for routines and sessions.
 * OOP Concept: ENCAPSULATION + ABSTRACTION (implements Reportable)
 */
public class WorkoutService implements Reportable {

    private final WorkoutDAO  dao = new WorkoutDAO();
    private WorkoutSession    activeSession;
    private final int         userId;

    public WorkoutService(int userId) { this.userId = userId; }

    // ── Routine management ────────────────────────────────────────

    public List<WorkoutRoutine> getMyRoutines()       { return dao.findByUser(userId); }
    public boolean createRoutine(WorkoutRoutine r)    { r.setUserId(userId); return dao.save(r); }
    public boolean updateRoutine(WorkoutRoutine r)    { return dao.update(r); }
    public boolean deleteRoutine(int routineId)       { return dao.delete(routineId); }
    public List<Exercise> getExerciseLibrary()        { return dao.findAllExercises(); }

    // ── Session lifecycle ─────────────────────────────────────────

    public WorkoutSession startSession(WorkoutRoutine routine) {
        activeSession = new WorkoutSession(userId, routine);
        activeSession.start();
        return activeSession;
    }

    public void pauseSession()  { if (activeSession != null) activeSession.pause(); }
    public void resumeSession() { if (activeSession != null) activeSession.start(); }

    public WorkoutSession finishSession(double userWeightKg) {
        if (activeSession == null) return null;
        activeSession.stop();

        // Scale calories proportionally to actual time worked vs full routine duration
        double routineTotalSeconds = activeSession.getRoutine().getTotalDurationMinutes() * 60.0;
        double actualSeconds       = activeSession.getDurationSeconds();
        double fullCalories        = activeSession.getRoutine().getEstimatedCalories(userWeightKg);
        double scaledCalories;
        if (routineTotalSeconds > 0) {
            double ratio = Math.min(1.0, actualSeconds / routineTotalSeconds);
            scaledCalories = fullCalories * ratio;
        } else {
            scaledCalories = fullCalories;
        }
        activeSession.setCaloriesBurned(scaledCalories);

        dao.saveSession(activeSession);
        WorkoutSession finished = activeSession;
        activeSession = null;
        return finished;
    }

    public WorkoutSession        getActiveSession()   { return activeSession; }
    public List<WorkoutSession>  getSessionHistory()  { return dao.findSessionsByUser(userId); }

    // ── Stats ─────────────────────────────────────────────────────

    public double getTotalCaloriesBurned() {
        return getSessionHistory().stream().mapToDouble(WorkoutSession::getCaloriesBurned).sum();
    }

    public int getTotalWorkoutSeconds() {
        return getSessionHistory().stream()
            .mapToInt(WorkoutSession::getDurationSeconds)
            .sum();
    }

    public int getTotalWorkoutMinutes() {
        return getTotalWorkoutSeconds() / 60;
    }

    // ── Reportable interface ──────────────────────────────────────

    @Override
    public String generateSummary() {
        List<WorkoutSession> h = getSessionHistory();
        int totalSec = getTotalWorkoutSeconds();
        int hrs  = totalSec / 3600;
        int mins = (totalSec % 3600) / 60;
        int secs = totalSec % 60;
        String timeStr = (hrs > 0)
            ? String.format("%dh %02dm %02ds", hrs, mins, secs)
            : (mins > 0)
                ? String.format("%dm %02ds", mins, secs)
                : secs + "s";
        return String.format(
            "Workout Summary | Sessions: %d | Total time: %s | Calories: %.0f kcal",
            h.size(), timeStr, getTotalCaloriesBurned());
    }

    @Override
    public String exportLog() {
        StringBuilder sb = new StringBuilder("session_id,start_time,duration_sec,calories\n");
        for (WorkoutSession s : getSessionHistory()) {
            sb.append(s.getSessionId()).append(",")
              .append(s.getStartTime()).append(",")
              .append(s.getDurationSeconds()).append(",")
              .append(s.getCaloriesBurned()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public void display() { System.out.println(generateSummary()); }
}
