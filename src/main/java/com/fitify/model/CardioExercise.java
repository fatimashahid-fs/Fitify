package com.fitify.model;

/**
 * CardioExercise - Inherits from Exercise.
 * OOP Concept: INHERITANCE + POLYMORPHISM
 * Overrides calculateCalories() with MET-based formula using speed.
 */
public class CardioExercise extends Exercise {

    private double speedKmh;
    private double distanceKm;
    private int    targetHeartRate;

    public CardioExercise() {
        super();
        setType(ExerciseType.CARDIO);
    }

    public CardioExercise(String name, String description, int durationMinutes,
                          String muscleGroup, String difficulty,
                          double speedKmh, double distanceKm, int targetHeartRate) {
        super(name, description, durationMinutes, ExerciseType.CARDIO, muscleGroup, difficulty);
        this.speedKmh        = speedKmh;
        this.distanceKm      = distanceKm;
        this.targetHeartRate = targetHeartRate;
    }

    @Override
    public double calculateCalories(double userWeightKg) {
        double met;
        if      (speedKmh < 6)  met = 3.5;
        else if (speedKmh < 10) met = 7.0;
        else if (speedKmh < 15) met = 11.0;
        else                    met = 8.0;
        return Math.round(baseMETCalories(met, userWeightKg) * 10.0) / 10.0;
    }

    @Override
    public String getSummary() {
        return String.format("Cardio | %s | %d min | %.1f km/h | %.1f km | HR: %d bpm",
            getName(), getDurationMinutes(), speedKmh, distanceKm, targetHeartRate);
    }

    @Override
    public String getTypeIcon() { return "Running"; }

    public double getSpeedKmh()              { return speedKmh; }
    public void   setSpeedKmh(double s)      { this.speedKmh = s; }
    public double getDistanceKm()            { return distanceKm; }
    public void   setDistanceKm(double d)    { this.distanceKm = d; }
    public int    getTargetHeartRate()       { return targetHeartRate; }
    public void   setTargetHeartRate(int hr) { this.targetHeartRate = hr; }
}
