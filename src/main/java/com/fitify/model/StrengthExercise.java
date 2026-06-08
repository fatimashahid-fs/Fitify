package com.fitify.model;

/**
 * StrengthExercise - Inherits from Exercise.
 * OOP Concept: INHERITANCE + POLYMORPHISM
 * Overrides calculateCalories() with compound/isolation load formula.
 */
public class StrengthExercise extends Exercise {

    private int     sets;
    private int     reps;
    private double  weightKg;
    private String  equipment;
    private boolean isCompound;

    public StrengthExercise() {
        super();
        setType(ExerciseType.STRENGTH);
    }

    public StrengthExercise(String name, String description, int durationMinutes,
                            String muscleGroup, String difficulty,
                            int sets, int reps, double weightKg,
                            String equipment, boolean isCompound) {
        super(name, description, durationMinutes, ExerciseType.STRENGTH, muscleGroup, difficulty);
        this.sets       = sets;
        this.reps       = reps;
        this.weightKg   = weightKg;
        this.equipment  = equipment;
        this.isCompound = isCompound;
    }

    @Override
    public double calculateCalories(double userWeightKg) {
        double met       = isCompound ? 5.0 : 3.5;
        double base      = baseMETCalories(met, userWeightKg);
        double loadBonus = (weightKg / Math.max(userWeightKg, 1)) * 0.1 * base;
        return Math.round((base + loadBonus) * 10.0) / 10.0;
    }

    @Override
    public String getSummary() {
        return String.format("Strength | %s | %d sets x %d reps @ %.1f kg | %s | %s",
            getName(), sets, reps, weightKg, equipment, isCompound ? "Compound" : "Isolation");
    }

    @Override
    public String getTypeIcon() { return "Strength"; }

    public double getTotalVolume() { return sets * reps * weightKg; }

    public int     getSets()            { return sets; }
    public void    setSets(int s)       { this.sets = s; }
    public int     getReps()            { return reps; }
    public void    setReps(int r)       { this.reps = r; }
    public double  getWeightKg()        { return weightKg; }
    public void    setWeightKg(double w){ this.weightKg = w; }
    public String  getEquipment()       { return equipment; }
    public void    setEquipment(String e){ this.equipment = e; }
    public boolean isCompound()         { return isCompound; }
    public void    setCompound(boolean c){ this.isCompound = c; }
}
