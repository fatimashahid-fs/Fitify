package com.fitify.interfaces;

/**
 * Reportable - Contract for objects that generate summaries and exports.
 * OOP Concept: ABSTRACTION
 * Implemented by: WorkoutService
 */
public interface Reportable {
    String generateSummary();
    String exportLog();
    void   display();
}
