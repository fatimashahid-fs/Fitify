package com.fitify.model;

/**
 * Exercise - Abstract base class for all exercise types.
 * OOP Concepts: INHERITANCE + ABSTRACTION
 * Subclasses: CardioExercise, StrengthExercise, FlexibilityExercise
 */
public abstract class Exercise {

    public enum ExerciseType { CARDIO, STRENGTH, FLEXIBILITY }

    private int          exerciseId;
    private String       name;
    private String       description;
    private int          durationMinutes;
    private ExerciseType type;
    private String       muscleGroup;
    private String       difficulty;

    public Exercise() {}

    public Exercise(String name, String description, int durationMinutes,
                    ExerciseType type, String muscleGroup, String difficulty) {
        this.name            = name;
        this.description     = description;
        this.durationMinutes = durationMinutes;
        this.type            = type;
        this.muscleGroup     = muscleGroup;
        this.difficulty      = difficulty;
    }

    // Abstract methods - each subclass must provide its own implementation
    public abstract double calculateCalories(double userWeightKg);
    public abstract String getSummary();
    public abstract String getTypeIcon();

    /** MET-based calorie baseline shared by all subclasses */
    protected double baseMETCalories(double met, double userWeightKg) {
        return met * userWeightKg * (durationMinutes / 60.0);
    }

    public String getTypeLabel() {
        return switch (type) {
            case CARDIO      -> "Cardio";
            case STRENGTH    -> "Strength";
            case FLEXIBILITY -> "Flexibility";
        };
    }

    @Override
    public String toString() {
        return name + " [" + getTypeLabel() + "] - " + durationMinutes + " min";
    }

    // Getters and Setters
    public int          getExerciseId()             { return exerciseId; }
    public void         setExerciseId(int id)        { this.exerciseId = id; }
    public String       getName()                    { return name; }
    public void         setName(String n)            { this.name = n; }
    public String       getDescription()             { return description; }
    public void         setDescription(String d)     { this.description = d; }
    public int          getDurationMinutes()         { return durationMinutes; }
    public void         setDurationMinutes(int d)    { this.durationMinutes = d; }
    public ExerciseType getType()                    { return type; }
    public void         setType(ExerciseType t)      { this.type = t; }
    public String       getMuscleGroup()             { return muscleGroup; }
    public void         setMuscleGroup(String m)     { this.muscleGroup = m; }
    public String       getDifficulty()              { return difficulty; }
    public void         setDifficulty(String d)      { this.difficulty = d; }
}
