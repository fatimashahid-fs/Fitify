package com.fitify.interfaces;

/**
 * Trackable - Interface for objects that can be started, paused, and stopped.
 * OOP Concept: ABSTRACTION
 * Implemented by: WorkoutSession, MusicService
 */
public interface Trackable {
    void    start();
    void    pause();
    void    stop();
    double  getProgress();   // 0.0 to 100.0
    boolean isActive();
}
