package com.fitify.ui;

import com.fitify.model.User;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * GoalsPanel - Create and track fitness goals with progress cards.
 */
public class GoalsPanel {

    private final User user;
    private ScrollPane root;

    private VBox goalsContainer;
    private final List<double[]> goals = new ArrayList<>(); // [current, target, deadline-days]
    private final List<String[]> goalsMeta = new ArrayList<>(); // [type, name]

    public GoalsPanel(User user) {
        this.user = user;
        buildUI();
    }

    private void buildUI() {
        VBox content = new VBox(22);
        content.setPadding(new Insets(30));
        content.getStyleClass().add("panel-content");

        Label title = new Label("🎯  Fitness Goals");
        title.getStyleClass().add("panel-title");
        Label sub = new Label("Set targets, track progress, crush your goals.");
        sub.getStyleClass().add("panel-subtitle");

        // ── Create goal form ────────────────────────────────
        VBox formCard = new VBox(12);
        formCard.getStyleClass().add("info-card");
        formCard.setPadding(new Insets(20));
        Label formTitle = new Label("Create New Goal");
        formTitle.getStyleClass().add("card-title");

        ComboBox<String> typeCb = new ComboBox<>();
        typeCb.getItems().addAll(
            "⚖️ Lose Weight (kg)", "💪 Gain Weight (kg)", "🏋️ Build Muscle (sessions)",
            "🏃 Improve Cardio (km)", "👣 Daily Steps", "💧 Water Intake (L/day)",
            "🔥 Calories Burned", "📅 Workout Frequency (per week)"
        );
        typeCb.setValue("⚖️ Lose Weight (kg)");
        typeCb.getStyleClass().add("form-combo");
        typeCb.setMaxWidth(Double.MAX_VALUE);

        HBox row2 = new HBox(10);
        TextField currentFld = field("Current value");
        TextField targetFld  = field("Target value");
        TextField daysFld    = field("Days to achieve");
        row2.getChildren().addAll(currentFld, targetFld, daysFld);
        row2.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));

        Label feedback = new Label();
        feedback.getStyleClass().add("form-error");

        Button addBtn = new Button("➕  Add Goal");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> {
            try {
                double current = Double.parseDouble(currentFld.getText().trim());
                double target  = Double.parseDouble(targetFld.getText().trim());
                int    days    = Integer.parseInt(daysFld.getText().trim());
                if (days < 1) throw new NumberFormatException();

                String type = typeCb.getValue();
                goals.add(new double[]{current, target, days});
                goalsMeta.add(new String[]{type, "Goal " + (goals.size())});

                refreshGoals();
                currentFld.clear(); targetFld.clear(); daysFld.clear();
                feedback.setStyle("-fx-text-fill:#10B981;");
                feedback.setText("✓ Goal created!");
            } catch (NumberFormatException ex) {
                feedback.setText("Enter valid numbers for current, target and days.");
            }
        });

        formCard.getChildren().addAll(formTitle, lbl("Goal Type"), typeCb,
            lbl("Current  /  Target  /  Days"), row2, feedback, addBtn);

        // ── Goals list ──────────────────────────────────────
        Label goalsTitle = new Label("Active Goals");
        goalsTitle.getStyleClass().add("card-title");

        goalsContainer = new VBox(12);
        Label emp = new Label("No goals yet. Create your first goal above! 🚀");
        emp.getStyleClass().add("panel-subtitle");
        goalsContainer.getChildren().add(emp);

        content.getChildren().addAll(title, sub, formCard, goalsTitle, goalsContainer);
        root = new ScrollPane(content);
        root.setFitToWidth(true);
        root.getStyleClass().add("scroll-panel");
    }

    private void refreshGoals() {
        goalsContainer.getChildren().clear();
        if (goals.isEmpty()) {
            Label emp = new Label("No goals yet. Create your first goal above! 🚀");
            emp.getStyleClass().add("panel-subtitle");
            goalsContainer.getChildren().add(emp);
            return;
        }
        for (int i = 0; i < goals.size(); i++) {
            double[] g = goals.get(i);
            String[] m = goalsMeta.get(i);
            goalsContainer.getChildren().add(goalCard(m[0], g[0], g[1], (int)g[2]));
        }
    }

    private VBox goalCard(String type, double current, double target, int days) {
        VBox card = new VBox(10);
        card.getStyleClass().add("info-card");
        card.setPadding(new Insets(16));

        double progress = target != 0 ? Math.min(1.0, Math.abs(current / target)) : 0;
        double pct = progress * 100;

        // For "lose weight" goals, progress is how close current is to 0 (or target)
        // Simplified: progress = |current - start| / |target - start|
        // We treat it as fraction of target reached
        double fraction = (target != 0) ? Math.min(1.0, current / target) : 0;
        double progressPct = fraction * 100;

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label typeL = new Label(type); typeL.getStyleClass().add("card-title");
        HBox.setHgrow(typeL, Priority.ALWAYS);
        Label pctL = new Label(String.format("%.0f%%", progressPct));
        pctL.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:" + pctColor(progressPct) + ";");
        header.getChildren().addAll(typeL, pctL);

        HBox stats = new HBox(28);
        stats.getChildren().addAll(
            statItem("Current",  String.format("%.1f", current)),
            statItem("Target",   String.format("%.1f", target)),
            statItem("Deadline", days + " days")
        );

        ProgressBar bar = new ProgressBar(Math.min(1.0, fraction));
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setStyle("-fx-accent:" + pctColor(progressPct) + ";");

        Label daysLeft = new Label(days + " days remaining  •  " +
            (progressPct >= 100 ? "🎉 GOAL ACHIEVED!" : "Keep going!"));
        daysLeft.getStyleClass().add("panel-subtitle");

        card.getChildren().addAll(header, stats, bar, daysLeft);
        return card;
    }

    private String pctColor(double pct) {
        if (pct >= 100) return "#10B981";
        if (pct >= 50)  return "#F97316";
        return "#3B82F6";
    }

    private VBox statItem(String label, String value) {
        VBox b = new VBox(2);
        Label l = new Label(label); l.getStyleClass().add("stat-card-title");
        Label v = new Label(value); v.getStyleClass().add("metric-value");
        b.getChildren().addAll(l, v);
        return b;
    }

    private TextField field(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.getStyleClass().add("form-field");
        return f;
    }

    private Label lbl(String t) { Label l = new Label(t); l.getStyleClass().add("form-label"); return l; }

    public Parent getRoot() { return root; }
}
