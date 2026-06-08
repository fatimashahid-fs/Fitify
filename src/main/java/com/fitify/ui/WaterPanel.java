package com.fitify.ui;

import com.fitify.model.User;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 * WaterPanel - Daily water intake tracker with animated progress ring.
 */
public class WaterPanel {

    private final User user;
    private ScrollPane root;

    private double currentMl = 0;
    private final double goalMl;

    private Label currentLbl, percentLbl, remainingLbl;
    private Arc progressArc;
    private ProgressBar linearBar;
    private VBox logList;

    public WaterPanel(User user) {
        this.user   = user;
        this.goalMl = 2500; // recommended ~2.5L/day
        buildUI();
    }

    private void buildUI() {
        VBox content = new VBox(24);
        content.setPadding(new Insets(30));
        content.getStyleClass().add("panel-content");

        Label title = new Label("💧  Water Tracker");
        title.getStyleClass().add("panel-title");
        Label sub = new Label("Daily goal: " + (int)(goalMl/1000) + "." + (int)((goalMl%1000)/100) + "L  •  Staying hydrated boosts performance by up to 20%");
        sub.getStyleClass().add("panel-subtitle");

        // ── Circular progress ring ──────────────────────────
        StackPane ring = buildRing();

        // ── Linear bar summary ──────────────────────────────
        VBox summaryCard = new VBox(10);
        summaryCard.getStyleClass().add("info-card");
        summaryCard.setPadding(new Insets(18));

        HBox summRow = new HBox(30);
        summRow.setAlignment(Pos.CENTER_LEFT);
        VBox consBlock = metricBlock("Consumed",  "0 ml",   "#3B82F6");
        VBox goalBlock = metricBlock("Goal",      "2500 ml","#8B83A8");
        VBox remBlock  = metricBlock("Remaining", "2500 ml","#10B981");
        currentLbl   = (Label)((VBox) consBlock).getChildren().get(1);
        remainingLbl = (Label)((VBox) remBlock).getChildren().get(1);
        summRow.getChildren().addAll(consBlock, goalBlock, remBlock);

        linearBar = new ProgressBar(0);
        linearBar.setMaxWidth(Double.MAX_VALUE);
        linearBar.getStyleClass().add("water-bar");

        summaryCard.getChildren().addAll(summRow, linearBar);

        // ── Quick-add buttons ───────────────────────────────
        VBox addCard = new VBox(12);
        addCard.getStyleClass().add("info-card");
        addCard.setPadding(new Insets(18));
        Label addTitle = new Label("Quick Add");
        addTitle.getStyleClass().add("card-title");

        HBox quickBtns = new HBox(10);
        quickBtns.setAlignment(Pos.CENTER_LEFT);
        for (int ml : new int[]{150, 250, 350, 500, 750, 1000}) {
            Button b = new Button((ml >= 1000 ? (ml/1000) + "L" : ml + "ml"));
            b.getStyleClass().add("btn-secondary");
            final int amount = ml;
            b.setOnAction(e -> addWater(amount));
            quickBtns.getChildren().add(b);
        }

        HBox customRow = new HBox(10);
        customRow.setAlignment(Pos.CENTER_LEFT);
        TextField customFld = new TextField();
        customFld.setPromptText("Custom amount (ml)");
        customFld.getStyleClass().add("form-field");
        customFld.setPrefWidth(180);
        Button customBtn = new Button("Add");
        customBtn.getStyleClass().add("btn-primary");
        customBtn.setOnAction(e -> {
            try {
                int ml = Integer.parseInt(customFld.getText().trim());
                if (ml > 0) { addWater(ml); customFld.clear(); }
            } catch (NumberFormatException ex) { /* ignore */ }
        });
        customRow.getChildren().addAll(customFld, customBtn);

        addCard.getChildren().addAll(addTitle, quickBtns, lbl("Custom Amount"), customRow);

        // ── Log list ────────────────────────────────────────
        Label logTitle = new Label("Today's Log");
        logTitle.getStyleClass().add("card-title");
        logList = new VBox(6);
        Label emp = new Label("No water logged yet. Stay hydrated! 💧");
        emp.getStyleClass().add("panel-subtitle");
        logList.getChildren().add(emp);

        content.getChildren().addAll(title, sub, ring, summaryCard, addCard, logTitle, logList);
        root = new ScrollPane(content);
        root.setFitToWidth(true);
        root.getStyleClass().add("scroll-panel");
    }

    private void addWater(int ml) {
        currentMl += ml;
        double progress = Math.min(1.0, currentMl / goalMl);

        // Animate ring
        double targetAngle = -(progress * 360);
        Timeline anim = new Timeline(
            new KeyFrame(Duration.millis(400),
                new KeyValue(progressArc.lengthProperty(), targetAngle, Interpolator.EASE_OUT))
        );
        anim.play();

        linearBar.setProgress(progress);

        currentLbl.setText((int)currentMl + " ml");
        double remaining = Math.max(0, goalMl - currentMl);
        remainingLbl.setText((int)remaining + " ml");
        remainingLbl.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:" + (remaining == 0 ? "#10B981" : "#3B82F6") + ";");
        percentLbl.setText(String.format("%.0f%%", progress * 100));

        // Add to log
        HBox row = new HBox(12);
        row.getStyleClass().add("history-row");
        row.setPadding(new Insets(8, 14, 8, 14));
        row.setAlignment(Pos.CENTER_LEFT);
        Label timeL = new Label(java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
        timeL.getStyleClass().add("history-date"); timeL.setPrefWidth(60);
        Label amtL = new Label("+" + ml + " ml");
        amtL.setStyle("-fx-text-fill:#3B82F6;-fx-font-size:14px;-fx-font-weight:bold;");
        HBox.setHgrow(amtL, Priority.ALWAYS);
        Label totL = new Label("Total: " + (int)currentMl + " ml");
        totL.getStyleClass().add("history-cal");
        row.getChildren().addAll(timeL, amtL, totL);

        // Remove empty state label on first entry
        if (logList.getChildren().size() == 1 && logList.getChildren().get(0) instanceof Label) {
            logList.getChildren().clear();
        }
        logList.getChildren().add(0, row);
    }

    private StackPane buildRing() {
        StackPane pane = new StackPane();
        pane.setAlignment(Pos.CENTER);
        pane.setPrefHeight(200);

        Circle bg = new Circle(80);
        bg.setFill(javafx.scene.paint.Color.TRANSPARENT);
        bg.setStroke(javafx.scene.paint.Color.web("#1E2A3A"));
        bg.setStrokeWidth(14);

        progressArc = new Arc(0, 0, 80, 80, 90, 0);
        progressArc.setType(ArcType.OPEN);
        progressArc.setFill(javafx.scene.paint.Color.TRANSPARENT);
        progressArc.setStroke(javafx.scene.paint.Color.web("#3B82F6"));
        progressArc.setStrokeWidth(14);
        progressArc.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);

        VBox center = new VBox(2);
        center.setAlignment(Pos.CENTER);
        Label dropLbl = new Label("💧");
        dropLbl.setStyle("-fx-font-size:28px;");
        percentLbl = new Label("0%");
        percentLbl.setStyle("-fx-font-size:28px;-fx-font-weight:bold;-fx-text-fill:#3B82F6;-fx-font-family:'Segoe UI Black',sans-serif;");
        Label goalL = new Label("of " + (int)(goalMl) + "ml");
        goalL.getStyleClass().add("panel-subtitle");
        center.getChildren().addAll(dropLbl, percentLbl, goalL);

        pane.getChildren().addAll(bg, progressArc, center);
        return pane;
    }

    private VBox metricBlock(String label, String value, String color) {
        VBox b = new VBox(2);
        Label l = new Label(label); l.getStyleClass().add("stat-card-title");
        Label v = new Label(value); v.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
        b.getChildren().addAll(l, v);
        HBox.setHgrow(b, Priority.ALWAYS);
        return b;
    }

    private Label lbl(String t) { Label l = new Label(t); l.getStyleClass().add("form-label"); return l; }

    public Parent getRoot() { return root; }
}
