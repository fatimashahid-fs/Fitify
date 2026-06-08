package com.fitify.ui;

import com.fitify.model.*;
import com.fitify.service.MusicService;
import com.fitify.service.WorkoutService;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

/**
 * SessionPanel - Live workout session with timer, progress, and music auto-sync.
 */
public class SessionPanel {

    private final WorkoutService workoutSvc;
    private final MusicService   musicSvc;
    private final User           user;

    private WorkoutSession activeSession;
    private Timeline       ticker;

    private Label       timerLabel;
    private ProgressBar progressBar;
    private Label       progressLabel;
    private Label       calLabel;
    private Label       statusLabel;
    private Button      startBtn;
    private Button      pauseBtn;
    private Button      stopBtn;

    private BorderPane root;

    public SessionPanel(WorkoutService workoutSvc, MusicService musicSvc, User user) {
        this.workoutSvc = workoutSvc;
        this.musicSvc   = musicSvc;
        this.user       = user;
        buildUI();
    }

    private void buildUI() {
        root = new BorderPane();

        VBox content = new VBox(22);
        content.setPadding(new Insets(30));
        content.setAlignment(Pos.TOP_CENTER);
        content.getStyleClass().add("panel-content");

        Label title = new Label("Start Workout Session");
        title.getStyleClass().add("panel-title");

        // Routine selector
        HBox selRow = new HBox(12);
        selRow.setAlignment(Pos.CENTER_LEFT);
        Label selLabel = new Label("Choose Routine:");
        selLabel.getStyleClass().add("form-label");
        ComboBox<WorkoutRoutine> routineCb = new ComboBox<>();
        routineCb.getItems().addAll(workoutSvc.getMyRoutines());
        routineCb.setPromptText("Select a routine...");
        routineCb.setPrefWidth(300);
        routineCb.getStyleClass().add("form-combo");
        selRow.getChildren().addAll(selLabel, routineCb);

        // Timer display
        VBox timerBox = new VBox(8);
        timerBox.setAlignment(Pos.CENTER);
        timerBox.getStyleClass().add("timer-box");
        timerBox.setPadding(new Insets(28, 60, 28, 60));

        timerLabel    = new Label("00:00:00"); timerLabel.getStyleClass().add("timer-display");
        statusLabel   = new Label("Ready to start"); statusLabel.getStyleClass().add("timer-status");
        progressBar   = new ProgressBar(0); progressBar.setPrefWidth(380); progressBar.getStyleClass().add("session-progress");
        progressLabel = new Label("0% complete"); progressLabel.getStyleClass().add("timer-status");
        calLabel      = new Label("0 kcal"); calLabel.getStyleClass().add("calorie-label");

        timerBox.getChildren().addAll(timerLabel, statusLabel, progressBar, progressLabel, calLabel);

        // Controls
        startBtn = btn("Start",  "btn-start");
        pauseBtn = btn("Pause",  "btn-pause");
        stopBtn  = btn("Finish", "btn-stop");
        pauseBtn.setDisable(true);
        stopBtn.setDisable(true);

        startBtn.setOnAction(e -> {
            WorkoutRoutine r = routineCb.getValue();
            if (r == null) { statusLabel.setText("Please select a routine first."); return; }
            if (activeSession == null) {
                activeSession = workoutSvc.startSession(r);
                autoLinkMusic(r);
            } else {
                workoutSvc.resumeSession();
            }
            startTicker();
            startBtn.setDisable(true); pauseBtn.setDisable(false); stopBtn.setDisable(false);
            statusLabel.setText("Session in progress...");
            routineCb.setDisable(true);
        });

        pauseBtn.setOnAction(e -> {
            workoutSvc.pauseSession(); musicSvc.pause(); stopTicker();
            startBtn.setText("Resume"); startBtn.setDisable(false); pauseBtn.setDisable(true);
            statusLabel.setText("Paused");
        });

        stopBtn.setOnAction(e -> {
            WorkoutSession fin = workoutSvc.finishSession(user.getWeightKg());
            musicSvc.stop(); stopTicker();
            if (fin != null) {
                timerLabel.setText(fin.getElapsedFormatted());
                calLabel.setText(String.format("%.0f kcal", fin.getCaloriesBurned()));
                statusLabel.setText("Session complete! Great work!");
            }
            startBtn.setText("Start"); startBtn.setDisable(false);
            pauseBtn.setDisable(true); stopBtn.setDisable(true);
            routineCb.setDisable(false); activeSession = null;
        });

        HBox controls = new HBox(14, startBtn, pauseBtn, stopBtn);
        controls.setAlignment(Pos.CENTER);

        // Routine preview
        VBox preview = new VBox(6);
        preview.getStyleClass().add("routine-preview");
        routineCb.setOnAction(e -> {
            preview.getChildren().clear();
            WorkoutRoutine r = routineCb.getValue();
            if (r == null) return;
            Label pl = new Label(r.getName() + "   |   " + r.getTotalDurationMinutes()
                + " min   |   ~" + String.format("%.0f", r.getEstimatedCalories(user.getWeightKg())) + " kcal");
            pl.getStyleClass().add("card-title");
            preview.getChildren().add(pl);
            for (Exercise ex : r.getExercises()) {
                Label el = new Label("   " + ex.getTypeIcon() + "  " + ex);
                el.getStyleClass().add("routine-ex-row");
                preview.getChildren().add(el);
            }
        });

        content.getChildren().addAll(title, selRow, timerBox, controls, preview);
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-panel");
        root.setCenter(scroll);
    }

    private void autoLinkMusic(WorkoutRoutine r) {
        if (r.getLinkedPlaylistName() == null || r.getLinkedPlaylistName().isBlank()) return;
        musicSvc.getUserPlaylists().stream()
            .filter(pl -> pl.getName().equalsIgnoreCase(r.getLinkedPlaylistName()))
            .findFirst().ifPresent(musicSvc::playPlaylist);
    }

    private void startTicker() {
        if (ticker != null) ticker.stop();
        ticker = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick()));
        ticker.setCycleCount(Timeline.INDEFINITE);
        ticker.play();
    }

    private void stopTicker() { if (ticker != null) { ticker.stop(); ticker = null; } }

    private void tick() {
        if (activeSession == null) return;
        timerLabel.setText(activeSession.getElapsedFormatted());
        double prog = activeSession.getProgress() / 100.0;
        progressBar.setProgress(prog);
        progressLabel.setText(String.format("%.0f%% complete", activeSession.getProgress()));
        double cals = activeSession.getRoutine().getEstimatedCalories(user.getWeightKg()) * prog;
        calLabel.setText(String.format("%.0f kcal", cals));
    }

    private Button btn(String text, String style) {
        Button b = new Button(text);
        b.getStyleClass().addAll("session-btn", style);
        b.setPrefWidth(120);
        return b;
    }

    public Parent getRoot() { return root; }
}
