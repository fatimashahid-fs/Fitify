package com.fitify.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * WorkoutRoutine - A named collection of exercises belonging to a user.
 * OOP Concept: ENCAPSULATION + COMPOSITION (has-a List of Exercise)
 */
public class WorkoutRoutine {

    private int            routineId;
    private int            userId;
    private String         name;
    private String         description;
    private LocalDate      dateCreated;
    private List<Exercise> exercises;
    private String         linkedPlaylistName;

    public WorkoutRoutine() {
        this.exercises   = new ArrayList<>();
        this.dateCreated = LocalDate.now();
    }

    public WorkoutRoutine(int userId, String name, String description) {
        this();
        this.userId      = userId;
        this.name        = name;
        this.description = description;
    }

    public void addExercise(Exercise e)        { exercises.add(e); }
    public boolean removeExercise(int exerciseId) {
        return exercises.removeIf(e -> e.getExerciseId() == exerciseId);
    }

    /** Total duration across all exercises */
    public int getTotalDurationMinutes() {
        return exercises.stream().mapToInt(Exercise::getDurationMinutes).sum();
    }

    /** Estimated total calories for a given user weight - polymorphic dispatch */
    public double getEstimatedCalories(double userWeightKg) {
        return exercises.stream().mapToDouble(e -> e.calculateCalories(userWeightKg)).sum();
    }

    /** Count exercises of a given type */
    public long countByType(Exercise.ExerciseType type) {
        return exercises.stream().filter(e -> e.getType() == type).count();
    }

    public int            getRoutineId()                 { return routineId; }
    public void           setRoutineId(int id)           { this.routineId = id; }
    public int            getUserId()                    { return userId; }
    public void           setUserId(int uid)             { this.userId = uid; }
    public String         getName()                      { return name; }
    public void           setName(String n)              { this.name = n; }
    public String         getDescription()               { return description; }
    public void           setDescription(String d)       { this.description = d; }
    public LocalDate      getDateCreated()               { return dateCreated; }
    public void           setDateCreated(LocalDate d)    { this.dateCreated = d; }
    public List<Exercise> getExercises()                 { return exercises; }
    public void           setExercises(List<Exercise> e) { this.exercises = e; }
    public String         getLinkedPlaylistName()        { return linkedPlaylistName; }
    public void           setLinkedPlaylistName(String p){ this.linkedPlaylistName = p; }

    @Override
    public String toString() {
        return name + " (" + exercises.size() + " exercises, " + getTotalDurationMinutes() + " min)";
    }
}
