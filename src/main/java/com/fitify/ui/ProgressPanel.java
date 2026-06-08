package com.fitify.ui;

import com.fitify.model.WorkoutSession;
import com.fitify.service.WorkoutService;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * ProgressPanel - Displays session history and aggregate stats.
 * Fixed: time now shown as h m s, not just minutes.
 */
public class ProgressPanel {

    private final WorkoutService workoutSvc;
    private       ScrollPane     root;

    public ProgressPanel(WorkoutService workoutSvc) {
        this.workoutSvc = workoutSvc;
        buildUI();
    }

    // ── helpers ───────────────────────────────────────────────────

    /** Format total seconds as "1h 23m 45s", "23m 45s", or "45s" */
    private static String formatTime(int totalSeconds) {
        if (totalSeconds <= 0) return "0s";
        int h = totalSeconds / 3600;
        int m = (totalSeconds % 3600) / 60;
        int s = totalSeconds % 60;
        if (h > 0) return String.format("%dh %02dm %02ds", h, m, s);
        if (m > 0) return String.format("%dm %02ds", m, s);
        return s + "s";
    }

    /** Format session duration stored as seconds into "Xm Ys" */
    private static String formatDuration(int seconds) {
        if (seconds <= 0) return "0s";
        int m = seconds / 60;
        int s = seconds % 60;
        if (m > 0) return String.format("%dm %02ds", m, s);
        return s + "s";
    }

    // ── UI ────────────────────────────────────────────────────────

    private void buildUI() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.getStyleClass().add("panel-content");

        Label title = new Label("Progress & History");
        title.getStyleClass().add("panel-title");

        VBox sumCard = new VBox(8);
        sumCard.getStyleClass().add("info-card");
        sumCard.setPadding(new Insets(18));
        Label sumTitle = new Label("Your Stats");
        sumTitle.getStyleClass().add("card-title");
        Label sumText = new Label(workoutSvc.generateSummary());
        sumText.getStyleClass().add("panel-subtitle");
        sumText.setWrapText(true);
        sumCard.getChildren().addAll(sumTitle, sumText);

        // Stats row — time now shows h/m/s
        List<WorkoutSession> history = workoutSvc.getSessionHistory();
        int totalSec = workoutSvc.getTotalWorkoutSeconds();
        HBox statsRow = new HBox(14);
        statsRow.getChildren().addAll(
            pStat("Sessions",    String.valueOf(history.size())),
            pStat("Total Time",  formatTime(totalSec)),
            pStat("Total kcal",  String.format("%.0f", workoutSvc.getTotalCaloriesBurned())),
            pStat("Avg Session",
                history.isEmpty() ? "-"
                    : formatDuration(totalSec / history.size()))
        );
        statsRow.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));

        // Session history list
        Label histTitle = new Label("Session History");
        histTitle.getStyleClass().add("card-title");

        VBox histList = new VBox(8);
        if (history.isEmpty()) {
            Label empty = new Label("No sessions yet. Start your first workout!");
            empty.getStyleClass().add("panel-subtitle");
            histList.getChildren().add(empty);
        } else {
            for (WorkoutSession s : history) {
                HBox row = new HBox(16);
                row.getStyleClass().add("history-row");
                row.setPadding(new Insets(10, 14, 10, 14));
                row.setAlignment(Pos.CENTER_LEFT);

                Label dateL = new Label(s.getStartTime() != null
                    ? s.getStartTime().toLocalDate().toString() : "-");
                dateL.getStyleClass().add("history-date");
                dateL.setPrefWidth(110);

                // FIX: show full h/m/s breakdown, not just minutes
                Label durL = new Label(formatDuration(s.getDurationSeconds()));
                durL.getStyleClass().add("history-dur");
                durL.setPrefWidth(100);

                Label calL = new Label(String.format("%.0f kcal", s.getCaloriesBurned()));
                calL.getStyleClass().add("history-cal");

                Region sp = new Region();
                HBox.setHgrow(sp, Priority.ALWAYS);
                row.getChildren().addAll(dateL, durL, calL, sp);
                histList.getChildren().add(row);
            }
        }

        Button exportBtn = new Button("Export Log (CSV)");
        exportBtn.getStyleClass().add("btn-secondary");
        exportBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export Log");
            alert.setHeaderText("Session CSV Data");
            TextArea ta = new TextArea(workoutSvc.exportLog());
            ta.setPrefHeight(200);
            alert.getDialogPane().setContent(ta);
            alert.show();
        });

        content.getChildren().addAll(title, sumCard, statsRow, histTitle, histList, exportBtn);
        root = new ScrollPane(content);
        root.setFitToWidth(true);
        root.getStyleClass().add("scroll-panel");
    }

    private VBox pStat(String label, String value) {
        VBox card = new VBox(4);
        card.getStyleClass().add("stat-card");
        card.setPadding(new Insets(14, 18, 14, 18));
        Label l = new Label(label); l.getStyleClass().add("stat-card-title");
        Label v = new Label(value); v.getStyleClass().add("stat-card-value");
        card.getChildren().addAll(l, v);
        return card;
    }

    public Parent getRoot() { return root; }
}
