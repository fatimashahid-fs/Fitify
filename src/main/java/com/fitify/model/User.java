package com.fitify.model;

/**
 * User - Core entity representing an application user.
 * OOP Concept: ENCAPSULATION
 * All fields are private; access is through getters and setters only.
 */
public class User {

    private int    userId;
    private String name;
    private String email;
    private String passwordHash;
    private String fitnessGoal;
    private int    age;
    private double weightKg;
    private double heightCm;

    public User() {}

    public User(String name, String email, String passwordHash,
                String fitnessGoal, int age, double weightKg, double heightCm) {
        this.name         = name;
        this.email        = email;
        this.passwordHash = passwordHash;
        this.fitnessGoal  = fitnessGoal;
        this.age          = age;
        this.weightKg     = weightKg;
        this.heightCm     = heightCm;
    }

    /** Calculate BMI from stored weight and height */
    public double calculateBMI() {
        if (heightCm <= 0) return 0;
        double h = heightCm / 100.0;
        return Math.round((weightKg / (h * h)) * 10.0) / 10.0;
    }

    /** Return a human-readable BMI category */
    public String getBMICategory() {
        double bmi = calculateBMI();
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25.0) return "Normal weight";
        if (bmi < 30.0) return "Overweight";
        return "Obese";
    }

    /** Masked display of email for UI privacy */
    public String getMaskedEmail() {
        if (email == null || !email.contains("@")) return "***";
        int at = email.indexOf('@');
        return email.charAt(0) + "***" + email.substring(at);
    }

    // Getters and Setters
    public int    getUserId()                    { return userId; }
    public void   setUserId(int id)              { this.userId = id; }
    public String getName()                      { return name; }
    public void   setName(String name)           { this.name = name; }
    public String getEmail()                     { return email; }
    public void   setEmail(String email)         { this.email = email; }
    public String getPasswordHash()              { return passwordHash; }
    public void   setPasswordHash(String h)      { this.passwordHash = h; }
    public String getFitnessGoal()               { return fitnessGoal; }
    public void   setFitnessGoal(String g)       { this.fitnessGoal = g; }
    public int    getAge()                       { return age; }
    public void   setAge(int age)                { this.age = age; }
    public double getWeightKg()                  { return weightKg; }
    public void   setWeightKg(double w)          { this.weightKg = w; }
    public double getHeightCm()                  { return heightCm; }
    public void   setHeightCm(double h)          { this.heightCm = h; }

    @Override
    public String toString() {
        return "User{id=" + userId + ", name='" + name + "', goal='" + fitnessGoal + "'}";
    }
}
