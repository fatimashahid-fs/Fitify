package com.fitify.ui;

import com.fitify.model.User;
import com.fitify.util.ThemeManager;
import com.fitify.service.AuthService;
import com.fitify.service.MusicService;
import com.fitify.service.WorkoutService;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * DashboardScreen - Premium sidebar shell with full nav system.
 * Sidebar matches screenshot: sections FITNESS, MUSIC, INSIGHTS, ACCOUNT
 * with proper scrollable sidebar and scrollable content area.
 */
public class DashboardScreen {

    private final Stage          stage;
    private final AuthService    auth;
    private final User           user;
    private final WorkoutService workoutSvc;
    private final MusicService   musicSvc;

    private BorderPane root;
    private StackPane  contentArea;

    public DashboardScreen(Stage stage, AuthService auth) {
        this.stage      = stage;
        this.auth       = auth;
        this.user       = auth.getCurrentUser();
        this.workoutSvc = new WorkoutService(user.getUserId());
        this.musicSvc   = new MusicService(user.getUserId());
        buildUI();
    }

    private void buildUI() {
        root        = new BorderPane();
        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");
        contentArea.setAlignment(Pos.TOP_LEFT);
        root.setLeft(buildSidebar());
        root.setCenter(contentArea);
        root.setBottom(buildMiniPlayer());
        showHome();
    }

    private void setContent(Parent node) {
        if (node instanceof ScrollPane sp) {
            sp.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        }
        contentArea.getChildren().setAll(node);
    }

    private ScrollPane buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(228);
        sidebar.setSpacing(0);

        // Brand
        Label brandRow = new Label("FITIFY ⚡");
        brandRow.setStyle(
            "-fx-font-size: 22px;" +
            "-fx-font-family: 'Segoe UI Black', 'Segoe UI', Arial, sans-serif;" +
            "-fx-font-weight: 900;" +
            "-fx-text-fill: #5B4FE8;" +
            "-fx-padding: 18 16 6 18;" +
            "-fx-effect: dropshadow(gaussian, #5B4FE860, 10, 0.2, 0, 1);"
        );

        // User chip
        HBox chip = new HBox(12);
        chip.getStyleClass().add("sidebar-user-chip");
        chip.setPadding(new Insets(10, 14, 10, 14));
        chip.setAlignment(Pos.CENTER_LEFT);
        HBox chipWrap = new HBox(chip);
        chipWrap.setPadding(new Insets(0, 10, 6, 10));

        // Avatar circle
        Label avatar = new Label(String.valueOf(user.getName().charAt(0)).toUpperCase());
        avatar.setStyle(
            "-fx-background-color: #5B4FE8;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 16px; -fx-font-weight: bold;" +
            "-fx-background-radius: 20px;" +
            "-fx-min-width: 36px; -fx-min-height: 36px;" +
            "-fx-max-width: 36px; -fx-max-height: 36px;" +
            "-fx-alignment: center;"
        );

        VBox chipText = new VBox(2);
        Label nameL = new Label(user.getName());
        nameL.getStyleClass().add("sidebar-username");
        Label goalL = new Label("✅ " + user.getFitnessGoal());
        goalL.setStyle("-fx-text-fill: #22C55E; -fx-font-size: 11px;");
        Label streakL = new Label("🔥 " + workoutSvc.getSessionHistory().size() + " day streak");
        streakL.setStyle("-fx-text-fill: #FF6B2B; -fx-font-size: 11px;");
        chipText.getChildren().addAll(nameL, goalL, streakL);
        chip.getChildren().addAll(avatar, chipText);

        Separator sep = new Separator();
        sep.getStyleClass().add("sidebar-sep");

        // ── Navigation ────────────────────────────────────────
        VBox nav = new VBox(2);
        nav.setPadding(new Insets(6, 8, 6, 8));

        // Section: FITNESS
        nav.getChildren().add(sectionLabel("FITNESS"));
        nav.getChildren().addAll(
            navBtn("🏠  Dashboard",          () -> showHome()),
            navBtn("🏋️  Exercise Library",   () -> showExercises()),
            navBtn("📋  My Routines",         () -> showRoutines()),
            navBtn("▶️  Start Workout",       () -> showSession())
        );

        // Section: MUSIC
        nav.getChildren().add(sectionLabel("MUSIC"));
        nav.getChildren().addAll(
            navBtn("🎵  Music Player",        () -> showMusic())
        );

        // Section: INSIGHTS
        nav.getChildren().add(sectionLabel("INSIGHTS"));
        nav.getChildren().addAll(
            navBtn("🥗  Nutrition",           () -> showNutrition()),
            navBtn("🎯  Goals",               () -> showGoals()),
            navBtn("📈  Progress",            () -> showProgress()),
            navBtn("🏆  Achievements",        () -> showAchievements()),
            navBtn("💧  Water Tracker",       () -> showWater())
        );

        // Section: ACCOUNT
        nav.getChildren().add(sectionLabel("ACCOUNT"));
        nav.getChildren().addAll(
            navBtn("👤  Profile",             () -> showProfile())
        );

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        spacer.setMinHeight(16);

        Button themeBtn = new Button("🌙  Dark Mode");
        themeBtn.getStyleClass().add("btn-secondary");
        themeBtn.setMaxWidth(Double.MAX_VALUE);
        themeBtn.setOnAction(e -> {
            ThemeManager.getInstance().toggle();
            themeBtn.setText(ThemeManager.getInstance().isDark() ? "☀️  Light Mode" : "🌙  Dark Mode");
        });

        Button logoutBtn = new Button("🚪  Logout");
        logoutBtn.getStyleClass().add("btn-logout");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> logout());

        VBox bottomBox = new VBox(8, themeBtn, logoutBtn);
        bottomBox.setPadding(new Insets(0, 8, 16, 8));

        sidebar.getChildren().addAll(brandRow, chipWrap, sep, nav, spacer, bottomBox);

        // Wrap in ScrollPane so sidebar is fully scrollable
        ScrollPane sideScroll = new ScrollPane(sidebar);
        sideScroll.setFitToWidth(true);
        sideScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sideScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sideScroll.getStyleClass().add("sidebar-scroll");
        sideScroll.setPrefWidth(228);
        sideScroll.setMinWidth(228);
        sideScroll.setMaxWidth(228);
        // Match sidebar background
        sideScroll.setStyle(
            "-fx-background-color: #FFFFFF;" +
            "-fx-border-color: transparent #E2E3EF transparent transparent;" +
            "-fx-border-width: 0 1.5 0 0;"
        );
        return sideScroll;
    }

    private Label sectionLabel(String text) {
        Label l = new Label(text);
        l.setStyle(
            "-fx-font-size:10px;-fx-font-weight:bold;" +
            "-fx-text-fill:#9292A8;" +
            "-fx-padding:12 16 4 16;" +
            "-fx-font-family:'Segoe UI Semibold',sans-serif;"
        );
        return l;
    }

    private HBox buildMiniPlayer() {
        HBox bar = new HBox(12);
        bar.getStyleClass().add("mini-player");
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(8, 16, 8, 16));

        Label nowPlaying = new Label("🎶  No track playing");
        nowPlaying.getStyleClass().add("mini-player-label");

        Button playBtn = new Button("▶");
        playBtn.getStyleClass().add("mini-player-btn");
        playBtn.setOnAction(e -> {
            if (musicSvc.isActive()) { musicSvc.pause(); playBtn.setText("▶"); }
            else                     { musicSvc.start(); playBtn.setText("⏸"); }
        });

        Button skipBtn = new Button("⏭");
        skipBtn.getStyleClass().add("mini-player-btn");
        skipBtn.setOnAction(e -> musicSvc.skip());

        Slider volSlider = new Slider(0, 1, musicSvc.getVolume());
        volSlider.setPrefWidth(90);
        volSlider.getStyleClass().add("volume-slider");
        volSlider.valueProperty().addListener((obs, ov, nv) -> musicSvc.setVolume(nv.doubleValue()));

        Label volIcon = new Label("🔊");
        volIcon.getStyleClass().add("mini-player-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        bar.getChildren().addAll(nowPlaying, spacer, playBtn, skipBtn, volIcon, volSlider);
        return bar;
    }

    private Button navBtn(String text, Runnable action) {
        Button b = new Button(text);
        b.getStyleClass().add("nav-btn");
        b.setMaxWidth(Double.MAX_VALUE);
        b.setAlignment(Pos.CENTER_LEFT);
        b.setOnAction(e -> action.run());
        return b;
    }

    // ── Screen switches ───────────────────────────────────────────

    private void showHome() {
        setContent(new HomePanel(user, workoutSvc)
            .onStartWorkout(() -> showSession())
            .onOpenMusic(   () -> showMusic())
            .onLogMeal(     () -> showNutrition())
            .onViewProgress(() -> showProgress())
            .getRoot());
    }
    private void showExercises()    { setContent(new ExercisePanel(workoutSvc).getRoot()); }
    private void showRoutines()     { setContent(new RoutinePanel(workoutSvc, user).getRoot()); }
    private void showSession()      { setContent(new SessionPanel(workoutSvc, musicSvc, user).getRoot()); }
    private void showMusic()        { setContent(new MusicPanel(musicSvc).getRoot()); }
    private void showProgress()     { setContent(new ProgressPanel(workoutSvc).getRoot()); }
    private void showProfile()      { setContent(new ProfilePanel(auth).getRoot()); }
    private void showNutrition()    { setContent(new NutritionPanel(user).getRoot()); }
    private void showGoals()        { setContent(new GoalsPanel(user).getRoot()); }
    private void showAchievements() { setContent(new AchievementsPanel(workoutSvc).getRoot()); }
    private void showWater()        { setContent(new WaterPanel(user).getRoot()); }

    private void logout() {
        auth.logout();
        LoginScreen login = new LoginScreen(stage);
        stage.getScene().setRoot(login.getRoot());
        stage.setTitle("Fitify ⚡");
    }

    public Parent getRoot() { return root; }
}
