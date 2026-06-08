package com.fitify.model;

/**
 * FlexibilityExercise - Inherits from Exercise.
 * OOP Concept: INHERITANCE + POLYMORPHISM
 * Overrides calculateCalories() with low-MET hold-based formula.
 */
public class FlexibilityExercise extends Exercise {

    private int    holdDurationSeconds;
    private int    repetitions;
    private String stretchType;

    public FlexibilityExercise() {
        super();
        setType(ExerciseType.FLEXIBILITY);
    }

    public FlexibilityExercise(String name, String description, int durationMinutes,
                               String muscleGroup, String difficulty,
                               int holdDurationSeconds, int repetitions, String stretchType) {
        super(name, description, durationMinutes, ExerciseType.FLEXIBILITY, muscleGroup, difficulty);
        this.holdDurationSeconds = holdDurationSeconds;
        this.repetitions         = repetitions;
        this.stretchType         = stretchType;
    }

    @Override
    public double calculateCalories(double userWeightKg) {
        return Math.round(baseMETCalories(2.5, userWeightKg) * 10.0) / 10.0;
    }

    @Override
    public String getSummary() {
        return String.format("Flexibility | %s | %d min | Hold: %ds x %d | %s stretch",
            getName(), getDurationMinutes(), holdDurationSeconds, repetitions, stretchType);
    }

    @Override
    public String getTypeIcon() { return "Yoga"; }

    public int    getHoldDurationSeconds()      { return holdDurationSeconds; }
    public void   setHoldDurationSeconds(int h) { this.holdDurationSeconds = h; }
    public int    getRepetitions()              { return repetitions; }
    public void   setRepetitions(int r)         { this.repetitions = r; }
    public String getStretchType()              { return stretchType; }
    public void   setStretchType(String s)      { this.stretchType = s; }
}
