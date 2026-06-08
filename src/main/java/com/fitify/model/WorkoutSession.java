package com.fitify.model;

import com.fitify.interfaces.Trackable;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * WorkoutSession - Represents a live or completed workout session.
 * OOP Concept: ABSTRACTION (implements Trackable interface)
 */
public class WorkoutSession implements Trackable {

    public enum SessionStatus { NOT_STARTED, IN_PROGRESS, PAUSED, COMPLETED }

    private int            sessionId;
    private int            userId;
    private WorkoutRoutine routine;
    private LocalDateTime  startTime;
    private LocalDateTime  endTime;
    private int            durationSeconds;
    private double         caloriesBurned;
    private SessionStatus  status;
    private String         notes;

    private LocalDateTime  pauseTime;
    private int            totalPausedSeconds;

    public WorkoutSession() {
        this.status             = SessionStatus.NOT_STARTED;
        this.totalPausedSeconds = 0;
    }

    public WorkoutSession(int userId, WorkoutRoutine routine) {
        this();
        this.userId  = userId;
        this.routine = routine;
    }

    // --- Trackable interface implementation ---

    @Override
    public void start() {
        if (status == SessionStatus.NOT_STARTED) {
            startTime = LocalDateTime.now();
            status    = SessionStatus.IN_PROGRESS;
        } else if (status == SessionStatus.PAUSED) {
            if (pauseTime != null)
                totalPausedSeconds += (int) Duration.between(pauseTime, LocalDateTime.now()).getSeconds();
            status = SessionStatus.IN_PROGRESS;
        }
    }

    @Override
    public void pause() {
        if (status == SessionStatus.IN_PROGRESS) {
            pauseTime = LocalDateTime.now();
            status    = SessionStatus.PAUSED;
        }
    }

    @Override
    public void stop() {
        if (status == SessionStatus.IN_PROGRESS || status == SessionStatus.PAUSED) {
            endTime         = LocalDateTime.now();
            durationSeconds = getElapsedSeconds();
            status          = SessionStatus.COMPLETED;
        }
    }

    @Override
    public double getProgress() {
        if (routine == null) return 0;
        int totalSec = routine.getTotalDurationMinutes() * 60;
        if (totalSec == 0) return 0;
        return Math.min(100.0, (getElapsedSeconds() / (double) totalSec) * 100.0);
    }

    @Override
    public boolean isActive() { return status == SessionStatus.IN_PROGRESS; }

    public int getElapsedSeconds() {
        if (startTime == null) return 0;
        LocalDateTime end = (endTime != null) ? endTime : LocalDateTime.now();
        return (int) Duration.between(startTime, end).getSeconds() - totalPausedSeconds;
    }

    public String getElapsedFormatted() {
        int s = getElapsedSeconds();
        return String.format("%02d:%02d:%02d", s / 3600, (s % 3600) / 60, s % 60);
    }

    public int            getSessionId()               { return sessionId; }
    public void           setSessionId(int id)         { this.sessionId = id; }
    public int            getUserId()                  { return userId; }
    public void           setUserId(int uid)           { this.userId = uid; }
    public WorkoutRoutine getRoutine()                 { return routine; }
    public void           setRoutine(WorkoutRoutine r) { this.routine = r; }
    public LocalDateTime  getStartTime()               { return startTime; }
    public void           setStartTime(LocalDateTime t){ this.startTime = t; }
    public LocalDateTime  getEndTime()                 { return endTime; }
    public void           setEndTime(LocalDateTime t)  { this.endTime = t; }
    public int            getDurationSeconds()         { return durationSeconds; }
    public void           setDurationSeconds(int d)    { this.durationSeconds = d; }
    public double         getCaloriesBurned()          { return caloriesBurned; }
    public void           setCaloriesBurned(double c)  { this.caloriesBurned = c; }
    public SessionStatus  getStatus()                  { return status; }
    public void           setStatus(SessionStatus s)   { this.status = s; }
    public String         getNotes()                   { return notes; }
    public void           setNotes(String n)           { this.notes = n; }
}
