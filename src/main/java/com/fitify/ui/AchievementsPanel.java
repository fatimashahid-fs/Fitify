package com.fitify.ui;

import com.fitify.service.WorkoutService;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * AchievementsPanel — matches the screenshot exactly.
 * Progress Summary card (unlocked / locked / completion % + progress bar)
 * Badges grouped by BEGINNER, INTERMEDIATE, ADVANCED tiers.
 */
public class AchievementsPanel {

    private final WorkoutService workoutSvc;
    private ScrollPane root;

    record Badge(String icon, String name, String desc, String tier, boolean unlocked) {}

    public AchievementsPanel(WorkoutService workoutSvc) {
        this.workoutSvc = workoutSvc;
        buildUI();
    }

    private void buildUI() {
        int sessions   = workoutSvc.getSessionHistory().size();
        int totalSec   = workoutSvc.getTotalWorkoutSeconds();
        double totalCal = workoutSvc.getTotalCaloriesBurned();
        int streakDays = Math.min(sessions, 365);

        List<Badge> badges = List.of(
            // BEGINNER
            new Badge("🏅", "First Workout",    "Complete your first\nsession",       "BEGINNER",     sessions >= 1),
            new Badge("🎯", "Goal Setter",       "Create your first\nfitness goal",   "BEGINNER",     true),
            new Badge("💧", "Hydration Start",   "Log your first\nwater intake",      "BEGINNER",     false),
            new Badge("🥗", "Nutrition Track",   "Log your first meal",               "BEGINNER",     false),
            // INTERMEDIATE
            new Badge("🔥", "7-Day Warrior",     "Maintain a 7-day\nstreak",          "INTERMEDIATE", streakDays >= 7),
            new Badge("💪", "10 Workouts",        "Complete 10\nworkout sessions",     "INTERMEDIATE", sessions >= 10),
            new Badge("⚡", "30-Day Streak",      "30 consecutive\nworkout days",      "INTERMEDIATE", streakDays >= 30),
            new Badge("🏃", "50 Workouts",        "Complete 50\nworkout sessions",     "INTERMEDIATE", sessions >= 50),
            // ADVANCED
            new Badge("🦁", "100 Workouts",       "Complete 100\nworkout sessions",    "ADVANCED",     sessions >= 100),
            new Badge("🔥", "100K Calories",      "Burn 100,000\ntotal calories",      "ADVANCED",     totalCal >= 100000),
            new Badge("⏱️", "10 Hours Trained",   "Accumulate 10 hours\nof training", "ADVANCED",     totalSec >= 36000),
            new Badge("🏆", "Goal Master",        "Complete\n5 goals",                 "ADVANCED",     false),
            // ELITE (hidden tier, shown as part of advanced)
            new Badge("🌟", "365-Day Streak",     "Train every day\nfor a year",       "ADVANCED",     streakDays >= 365),
            new Badge("👑", "Fitness Legend",     "500 workout\nsessions",             "ADVANCED",     sessions >= 500),
            new Badge("🚀", "Calorie Inferno",    "Burn 500,000\ntotal calories",      "ADVANCED",     totalCal >= 500000),
            new Badge("🎖️", "Iron Will",          "250 hours of\ntraining time",      "ADVANCED",     totalSec >= 900000)
        );

        long unlocked = badges.stream().filter(Badge::unlocked).count();
        long locked   = badges.size() - unlocked;
        double pct    = (double) unlocked / badges.size();

        VBox content = new VBox(20);
        content.setPadding(new Insets(28, 28, 28, 28));
        content.getStyleClass().add("panel-content");

        // ── Title ──────────────────────────────────────────────
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label trophyIcon = new Label("🏆");
        trophyIcon.setStyle("-fx-font-size:22px;");
        Label titleL = new Label("Achievements");
        titleL.setStyle(
            "-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:#1A1628;" +
            "-fx-font-family:'Segoe UI Black','Arial Black',sans-serif;"
        );
        titleRow.getChildren().addAll(trophyIcon, titleL);

        // ── Progress Summary Card ──────────────────────────────
        VBox summaryCard = new VBox(14);
        summaryCard.getStyleClass().add("info-card");
        summaryCard.setPadding(new Insets(20, 24, 20, 24));

        Label summaryTitle = new Label("Progress Summary");
        summaryTitle.setStyle(
            "-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#1A1628;" +
            "-fx-font-family:'Segoe UI Semibold',sans-serif;"
        );

        HBox statsRow = new HBox(48);
        statsRow.setAlignment(Pos.CENTER_LEFT);
        statsRow.getChildren().addAll(
            summaryStatCol("🏅 Unlocked",  unlocked + " / " + badges.size(), "#5B4FE8"),
            summaryStatCol("🔒 Locked",    locked + " remaining",             "#44435A"),
            summaryStatCol("📊 Completion", String.format("%.0f%%", pct * 100), "#22C55E")
        );

        ProgressBar bar = new ProgressBar(pct);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setPrefHeight(10);
        bar.setStyle("-fx-accent:#5B4FE8;-fx-background-radius:6px;-fx-background-color:#E2E3EF;");

        summaryCard.getChildren().addAll(summaryTitle, statsRow, bar);

        content.getChildren().addAll(titleRow, summaryCard);

        // ── Badge sections by tier ─────────────────────────────
        for (String tier : new String[]{"BEGINNER", "INTERMEDIATE", "ADVANCED"}) {
            VBox section = new VBox(14);
            section.getStyleClass().add("info-card");
            section.setPadding(new Insets(18, 20, 20, 20));

            HBox tierHeader = new HBox(8);
            tierHeader.setAlignment(Pos.CENTER_LEFT);
            Label tierIcon = new Label(tierIcon(tier));
            tierIcon.setStyle("-fx-font-size:14px;");
            Label tierLabel = new Label(tier);
            tierLabel.setStyle(
                "-fx-font-size:12px;-fx-font-weight:bold;" +
                "-fx-text-fill:" + tierColor(tier) + ";" +
                "-fx-font-family:'Segoe UI Semibold',sans-serif;" +
                "-fx-letter-spacing:1.5;"
            );
            tierHeader.getChildren().addAll(tierIcon, tierLabel);

            // Badge grid row (wrapping)
            FlowPane flow = new FlowPane(14, 14);
            flow.setPrefWrapLength(Double.MAX_VALUE);
            for (Badge b : badges) {
                if (b.tier().equals(tier)) flow.getChildren().add(badgeCard(b));
            }

            section.getChildren().addAll(tierHeader, flow);
            content.getChildren().add(section);
        }

        root = new ScrollPane(content);
        root.setFitToWidth(true);
        root.getStyleClass().add("scroll-panel");
        root.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        root.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    }

    private VBox summaryStatCol(String label, String value, String valueColor) {
        VBox col = new VBox(3);
        Label l = new Label(label);
        l.setStyle("-fx-font-size:11px;-fx-text-fill:#9292A8;");
        Label v = new Label(value);
        v.setStyle("-fx-font-size:17px;-fx-font-weight:bold;-fx-text-fill:" + valueColor + ";" +
                   "-fx-font-family:'Segoe UI Semibold',sans-serif;");
        col.getChildren().addAll(l, v);
        return col;
    }

    private VBox badgeCard(Badge b) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(148);
        card.setMinWidth(120);
        card.setPadding(new Insets(16, 12, 16, 12));

        if (b.unlocked()) {
            card.setStyle(
                "-fx-background-color:#FFFFFF;" +
                "-fx-background-radius:14px;" +
                "-fx-border-color:#E2E3EF;-fx-border-radius:14px;-fx-border-width:1.5px;" +
                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),8,0,0,2);"
            );
        } else {
            card.setStyle(
                "-fx-background-color:#F8F8FB;" +
                "-fx-background-radius:14px;" +
                "-fx-border-color:#EBEBF0;-fx-border-radius:14px;-fx-border-width:1.5px;"
            );
        }

        Label iconL = new Label(b.unlocked() ? b.icon() : b.icon());
        iconL.setStyle("-fx-font-size:30px;" + (b.unlocked() ? "" : "-fx-opacity:0.35;"));

        Label nameL = new Label(b.name());
        nameL.setStyle(
            "-fx-font-size:11px;-fx-font-weight:bold;-fx-wrap-text:true;" +
            "-fx-text-alignment:center;-fx-text-fill:" + (b.unlocked() ? "#1A1628" : "#6B7280") + ";" +
            "-fx-font-family:'Segoe UI Semibold',sans-serif;"
        );
        nameL.setWrapText(true);
        nameL.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Label descL = new Label(b.desc());
        descL.setStyle(
            "-fx-font-size:10px;-fx-wrap-text:true;-fx-text-alignment:center;" +
            "-fx-text-fill:" + (b.unlocked() ? "#9292A8" : "#ABABBB") + ";"
        );
        descL.setWrapText(true);
        descL.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        card.getChildren().addAll(iconL, nameL, descL);

        if (b.unlocked()) {
            Label unlockL = new Label("✓ Unlocked");
            unlockL.setStyle(
                "-fx-font-size:9px;-fx-font-weight:bold;" +
                "-fx-text-fill:#22C55E;"
            );
            card.getChildren().add(unlockL);
        } else {
            Label lockL = new Label("🔒 Locked");
            lockL.setStyle("-fx-font-size:9px;-fx-text-fill:#ABABBB;");
            card.getChildren().add(lockL);
        }
        return card;
    }

    private String tierIcon(String tier) {
        return switch (tier) {
            case "BEGINNER"     -> "🌱";
            case "INTERMEDIATE" -> "≡";
            case "ADVANCED"     -> "⚡";
            default             -> "🏅";
        };
    }

    private String tierColor(String tier) {
        return switch (tier) {
            case "BEGINNER"     -> "#10B981";
            case "INTERMEDIATE" -> "#3B82F6";
            case "ADVANCED"     -> "#F97316";
            default             -> "#8B83A8";
        };
    }

    public Parent getRoot() { return root; }
}
