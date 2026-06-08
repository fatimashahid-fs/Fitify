package com.fitify.ui;

import com.fitify.model.User;
import com.fitify.service.WorkoutService;
import java.util.Random;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class HomePanel {

    private final User           user;
    private final WorkoutService workoutSvc;
    private       ScrollPane     root;

    // Navigation callbacks set by DashboardScreen
    private Runnable onStartWorkout;
    private Runnable onOpenMusic;
    private Runnable onLogMeal;
    private Runnable onViewProgress;

    private static final String[] QUOTES = {
        "\"Train insane or remain the same.\"",
        "\"The only bad workout is the one that didn't happen.\"",
        "\"Push yourself, because no one else is going to do it.\"",
        "\"Your body can stand almost anything. It's your mind you have to convince.\"",
        "\"Results happen over time, not overnight. Work hard, stay consistent.\""
    };

    public HomePanel(User user, WorkoutService workoutSvc) {
        this.user       = user;
        this.workoutSvc = workoutSvc;
    }

    public HomePanel onStartWorkout(Runnable r) { this.onStartWorkout = r; return this; }
    public HomePanel onOpenMusic(Runnable r)    { this.onOpenMusic    = r; return this; }
    public HomePanel onLogMeal(Runnable r)      { this.onLogMeal      = r; return this; }
    public HomePanel onViewProgress(Runnable r) { this.onViewProgress = r; return this; }

    private static String formatTime(int totalSeconds) {
        if (totalSeconds <= 0) return "0m 0s";
        int h = totalSeconds / 3600;
        int m = (totalSeconds % 3600) / 60;
        int s = totalSeconds % 60;
        if (h > 0) return h + "h " + m + "m " + s + "s";
        return m + "m " + s + "s";
    }

    public Parent getRoot() {
        if (root == null) buildUI();
        return root;
    }

    private void buildUI() {
        VBox content = new VBox(18);
        content.setPadding(new Insets(24));
        content.getStyleClass().add("panel-content");

        // ── 1. Hero Banner ─────────────────────────────────────
        String quote  = QUOTES[new Random().nextInt(QUOTES.length)];
        int streak    = Math.max(workoutSvc.getSessionHistory().size(), 0);

        StackPane banner = new StackPane();
        banner.setStyle(
            "-fx-background-color: linear-gradient(to right, #5B4FE8, #8B5CF6);" +
            "-fx-background-radius: 18px;" +
            "-fx-effect: dropshadow(gaussian, rgba(91,79,232,0.35), 20, 0, 0, 6);"
        );
        banner.setMinHeight(130);

        HBox bannerContent = new HBox();
        bannerContent.setPadding(new Insets(22, 24, 22, 28));
        bannerContent.setAlignment(Pos.CENTER_LEFT);

        VBox bannerLeft = new VBox(6);
        HBox.setHgrow(bannerLeft, Priority.ALWAYS);

        Label greeting = new Label("Hey, " + user.getName() + "! 🤗");
        greeting.setStyle(
            "-fx-font-size: 22px; -fx-font-weight: bold;" +
            "-fx-text-fill: white;" +
            "-fx-font-family: 'Segoe UI Black', 'Arial Black', sans-serif;"
        );

        Label quoteL = new Label(quote);
        quoteL.setStyle("-fx-font-size: 12px; -fx-font-style: italic; -fx-text-fill: rgba(255,255,255,0.75);");
        quoteL.setWrapText(true);

        HBox metaRow = new HBox(12);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        Label goalTag = new Label("🎯 " + user.getFitnessGoal());
        goalTag.setStyle("-fx-text-fill: rgba(255,255,255,0.85); -fx-font-size: 12px;");
        Label dot = new Label("·");
        dot.setStyle("-fx-text-fill: rgba(255,255,255,0.5);");
        Label bmiTag = new Label("📊 BMI: " + user.calculateBMI() + " — " + user.getBMICategory());
        bmiTag.setStyle("-fx-text-fill: rgba(255,255,255,0.85); -fx-font-size: 12px;");
        metaRow.getChildren().addAll(goalTag, dot, bmiTag);
        bannerLeft.getChildren().addAll(greeting, quoteL, metaRow);

        VBox streakBox = new VBox(2);
        streakBox.setAlignment(Pos.CENTER);
        streakBox.setPadding(new Insets(0, 0, 0, 20));
        Label streakNum = new Label(String.valueOf(streak));
        streakNum.setStyle(
            "-fx-font-size: 36px; -fx-font-weight: bold;" +
            "-fx-text-fill: #F59E0B;" +
            "-fx-font-family: 'Segoe UI Black', 'Arial Black', sans-serif;"
        );
        Label streakFire = new Label("🔥 day streak");
        streakFire.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 11px;");
        streakBox.getChildren().addAll(streakNum, streakFire);

        bannerContent.getChildren().addAll(bannerLeft, streakBox);
        banner.getChildren().add(bannerContent);

        // ── 2. Stat Cards ──────────────────────────────────────
        int    totalSeconds = workoutSvc.getTotalWorkoutSeconds();
        int    sessionCount = workoutSvc.getSessionHistory().size();
        double totalCal     = workoutSvc.getTotalCaloriesBurned();
        int    routineCount = workoutSvc.getMyRoutines().size();

        HBox stats = new HBox(14);
        stats.getChildren().addAll(
            colorStatCard("T↕  Workouts", String.valueOf(sessionCount),    "completed",      "#5B7FE8", "#6B8FFF"),
            colorStatCard("⊞  Time",       formatTime(totalSeconds),        "total invested", "#22C55E", "#34D870"),
            colorStatCard("🔥  Calories",  String.format("%.0f", totalCal), "kcal burned",    "#EF4444", "#F97316"),
            colorStatCard("▦  Routines",   String.valueOf(routineCount),     "created",        "#F59E0B", "#FB923C")
        );
        stats.getChildren().forEach(n -> HBox.setHgrow((Region) n, Priority.ALWAYS));

        // ── 3. Body Metrics Card ───────────────────────────────
        VBox metricsCard = new VBox(12);
        metricsCard.getStyleClass().add("info-card");
        metricsCard.setPadding(new Insets(16, 20, 16, 20));

        HBox mTitleRow = new HBox(8);
        mTitleRow.setAlignment(Pos.CENTER_LEFT);
        Label mIcon  = new Label("📊"); mIcon.setStyle("-fx-font-size: 14px;");
        Label mTitle = new Label("Body Metrics");
        mTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #44435A;");
        mTitleRow.getChildren().addAll(mIcon, mTitle);

        HBox metricsRow = new HBox(36);
        metricsRow.getChildren().addAll(
            metricItem("👤 Age",    user.getAge()       + " yrs"),
            metricItem("⚖️ Weight", user.getWeightKg()  + " kg"),
            metricItem("📏 Height", user.getHeightCm()  + " cm"),
            metricItem("🩺 BMI",    String.valueOf(user.calculateBMI())),
            metricItem("✅ Status", user.getBMICategory())
        );
        metricsCard.getChildren().addAll(mTitleRow, metricsRow);

        // ── 4. Achievements preview row ────────────────────────
        VBox achievementsCard = new VBox(14);
        achievementsCard.getStyleClass().add("info-card");
        achievementsCard.setPadding(new Insets(16, 20, 18, 20));

        HBox aTitleRow = new HBox(8);
        aTitleRow.setAlignment(Pos.CENTER_LEFT);
        Label aIcon  = new Label("🏆"); aIcon.setStyle("-fx-font-size: 14px;");
        Label aTitle = new Label("Achievements");
        aTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #44435A;");
        aTitleRow.getChildren().addAll(aIcon, aTitle);

        HBox badgeRow = new HBox(12);
        badgeRow.getChildren().addAll(
            miniBadge("🔥", "Streak Master",   "7+ days active",         sessionCount >= 7),
            miniBadge("💪", "Strength Beast",  "10+ strength\nsessions", sessionCount >= 10),
            miniBadge("🏃", "Cardio Champion", "50km total\ndistance",   false),
            miniBadge("🎵", "Music Warrior",   "100+ songs\nplayed",     false),
            miniBadge("🥗", "Nutrition Pro",   "7-day meal\nlogging",    false),
            miniBadge("🎯", "Goal Crusher",    "Complete a goal",        false)
        );
        achievementsCard.getChildren().addAll(aTitleRow, badgeRow);

        // ── 5. Quick Actions ──────────────────────────────────
        VBox qaSection = new VBox(12);
        Label qaTitle = new Label("⚡ Quick Actions");
        qaTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #44435A;");

        HBox qaRow = new HBox(12);
        Button btnWorkout  = quickActionBtn("T↕  Start Workout", "#5B4FE8");
        Button btnMusic    = quickActionBtn("🎵  Open Music",    "#FF6B2B");
        Button btnMeal     = quickActionBtn("🥗  Log Meal",      "#22C55E");
        Button btnProgress = quickActionBtn("📈  View Progress", "#5B4FE8");

        btnWorkout .setOnAction(e -> { if (onStartWorkout != null) onStartWorkout.run(); });
        btnMusic   .setOnAction(e -> { if (onOpenMusic    != null) onOpenMusic.run(); });
        btnMeal    .setOnAction(e -> { if (onLogMeal      != null) onLogMeal.run(); });
        btnProgress.setOnAction(e -> { if (onViewProgress != null) onViewProgress.run(); });

        qaRow.getChildren().addAll(btnWorkout, btnMusic, btnMeal, btnProgress);
        qaSection.getChildren().addAll(qaTitle, qaRow);

        content.getChildren().addAll(banner, stats, metricsCard, achievementsCard, qaSection);

        root = new ScrollPane(content);
        root.setFitToWidth(true);
        root.getStyleClass().add("scroll-panel");
        root.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        root.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    }

    private VBox colorStatCard(String title, String value, String sub, String from, String to) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(18));
        card.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, " + from + ", " + to + ");" +
            "-fx-background-radius: 16px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 10, 0, 0, 4);"
        );
        Label t = new Label(title);
        t.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 11px;");
        Label v = new Label(value);
        v.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold;" +
                   "-fx-font-family: 'Segoe UI Black', 'Arial Black', sans-serif;");
        Label s = new Label(sub);
        s.setStyle("-fx-text-fill: rgba(255,255,255,0.65); -fx-font-size: 11px;");
        card.getChildren().addAll(t, v, s);
        return card;
    }

    private VBox metricItem(String label, String value) {
        VBox b = new VBox(3);
        Label l = new Label(label); l.getStyleClass().add("metric-label");
        Label v = new Label(value); v.getStyleClass().add("metric-value");
        b.getChildren().addAll(l, v);
        return b;
    }

    private VBox miniBadge(String icon, String name, String desc, boolean unlocked) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(130);
        card.setMinWidth(110);
        card.setPadding(new Insets(14, 10, 14, 10));
        card.setStyle(unlocked
            ? "-fx-background-color:#FFFFFF;-fx-background-radius:14px;" +
              "-fx-border-color:#E2E3EF;-fx-border-radius:14px;-fx-border-width:1.5px;" +
              "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),8,0,0,2);"
            : "-fx-background-color:#F3F4F6;-fx-background-radius:14px;" +
              "-fx-border-color:#E5E7EB;-fx-border-radius:14px;-fx-border-width:1.5px;-fx-opacity:0.7;"
        );
        Label iconL = new Label(icon); iconL.setStyle("-fx-font-size:26px;");
        Label nameL = new Label(name);
        nameL.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-wrap-text:true;" +
                       "-fx-text-alignment:center;-fx-text-fill:" + (unlocked ? "#0F0E17" : "#6B7280") + ";");
        nameL.setWrapText(true);
        nameL.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        Label descL = new Label(desc);
        descL.setStyle("-fx-font-size:10px;-fx-wrap-text:true;-fx-text-alignment:center;" +
                       "-fx-text-fill:" + (unlocked ? "#9292A8" : "#9CA3AF") + ";");
        descL.setWrapText(true);
        descL.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        card.getChildren().addAll(iconL, nameL, descL);
        return card;
    }

    private Button quickActionBtn(String label, String color) {
        Button b = new Button(label);
        b.setStyle(
            "-fx-background-color:" + color + ";-fx-text-fill:white;-fx-font-weight:bold;" +
            "-fx-font-size:13px;-fx-background-radius:11px;-fx-padding:12 20;" +
            "-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.15),8,0,0,3);"
        );
        HBox.setHgrow(b, Priority.ALWAYS);
        b.setMaxWidth(Double.MAX_VALUE);
        return b;
    }
}
