package com.fitify.ui;

import com.fitify.model.User;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * NutritionPanel - Food logging and macro tracking.
 * Tracks calories, protein, carbs, fats per meal category.
 */
public class NutritionPanel {

    private final User user;
    private ScrollPane root;

    // In-memory meal log (each entry: [meal, food, calories, protein, carbs, fat])
    private final List<String[]> mealLog = new ArrayList<>();

    // Running daily totals
    private double totalCalories = 0, totalProtein = 0, totalCarbs = 0, totalFat = 0;
    private final double calGoal;

    // Live UI references
    private Label calConsumedLbl, calRemainingLbl;
    private Label proteinLbl, carbsLbl, fatLbl;
    private ProgressBar calBar, proteinBar, carbsBar, fatBar;
    private VBox logList;

    public NutritionPanel(User user) {
        this.user = user;
        // Rough TDEE estimate: bodyweight × 30 kcal/kg
        this.calGoal = Math.max(1500, user.getWeightKg() * 30);
        buildUI();
    }

    private void buildUI() {
        VBox content = new VBox(22);
        content.setPadding(new Insets(30));
        content.getStyleClass().add("panel-content");

        Label title = new Label("🥗  Nutrition Tracker");
        title.getStyleClass().add("panel-title");

        Label dateL = new Label("Today — " + LocalDate.now());
        dateL.getStyleClass().add("panel-subtitle");

        // ── Daily calories summary card ─────────────────────
        VBox calCard = new VBox(10);
        calCard.getStyleClass().add("info-card");
        calCard.setPadding(new Insets(20));
        Label calTitle = new Label("Daily Calories");
        calTitle.getStyleClass().add("card-title");

        HBox calRow = new HBox(30);
        calRow.setAlignment(Pos.CENTER_LEFT);

        VBox calConsumed = metricBlock("Consumed", "0 kcal", "#3B82F6");
        VBox calGoalBox  = metricBlock("Goal", String.format("%.0f kcal", calGoal), "#8B83A8");
        VBox calRemBox   = metricBlock("Remaining", String.format("%.0f kcal", calGoal), "#10B981");

        calConsumedLbl  = (Label) ((VBox) calConsumed).getChildren().get(1);
        calRemainingLbl = (Label) ((VBox) calRemBox).getChildren().get(1);

        calBar = new ProgressBar(0);
        calBar.setPrefWidth(Double.MAX_VALUE);
        calBar.getStyleClass().add("nutrition-bar-blue");
        calBar.setMaxWidth(Double.MAX_VALUE);

        calRow.getChildren().addAll(calConsumed, calGoalBox, calRemBox);
        calCard.getChildren().addAll(calTitle, calRow, calBar);

        // ── Macros row ──────────────────────────────────────
        HBox macroRow = new HBox(14);
        VBox protCard = macroCard("Protein", "0g",   "#10B981", "protein");
        VBox carbCard = macroCard("Carbs",   "0g",   "#F97316", "carbs");
        VBox fatCard  = macroCard("Fat",     "0g",   "#3B82F6", "fat");

        proteinLbl  = (Label) ((VBox)((VBox) protCard).getChildren().get(0)).getChildren().get(1);
        carbsLbl    = (Label) ((VBox)((VBox) carbCard).getChildren().get(0)).getChildren().get(1);
        fatLbl      = (Label) ((VBox)((VBox) fatCard).getChildren().get(0)).getChildren().get(1);
        proteinBar  = (ProgressBar)((VBox) protCard).getChildren().get(1);
        carbsBar    = (ProgressBar)((VBox) carbCard).getChildren().get(1);
        fatBar      = (ProgressBar)((VBox) fatCard).getChildren().get(1);

        macroRow.getChildren().addAll(protCard, carbCard, fatCard);
        macroRow.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));

        // ── Add meal form ───────────────────────────────────
        VBox addCard = new VBox(12);
        addCard.getStyleClass().add("info-card");
        addCard.setPadding(new Insets(20));
        Label addTitle = new Label("Log Meal");
        addTitle.getStyleClass().add("card-title");

        ComboBox<String> mealCb = new ComboBox<>();
        mealCb.getItems().addAll("🌅 Breakfast", "☀️ Lunch", "🌙 Dinner", "🍎 Snack");
        mealCb.setValue("🌅 Breakfast");
        mealCb.getStyleClass().add("form-combo");

        TextField foodFld = new TextField(); foodFld.setPromptText("Food name (e.g. Chicken Breast)");
        foodFld.getStyleClass().add("form-field");

        HBox macroFields = new HBox(10);
        TextField calFld  = numField("Calories (kcal)");
        TextField proFld  = numField("Protein (g)");
        TextField crbFld  = numField("Carbs (g)");
        TextField fatFldF = numField("Fat (g)");
        macroFields.getChildren().addAll(calFld, proFld, crbFld, fatFldF);
        macroFields.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));

        Label feedback = new Label();
        feedback.getStyleClass().add("form-error");

        Button addBtn = new Button("➕  Add to Log");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> {
            String food = foodFld.getText().trim();
            if (food.isEmpty()) { feedback.setText("Enter a food name."); return; }
            try {
                double cal = parseOrZero(calFld.getText());
                double pro = parseOrZero(proFld.getText());
                double crb = parseOrZero(crbFld.getText());
                double fat = parseOrZero(fatFldF.getText());

                String meal = mealCb.getValue();
                mealLog.add(new String[]{meal, food,
                    String.valueOf(cal), String.valueOf(pro),
                    String.valueOf(crb), String.valueOf(fat)});

                totalCalories += cal;
                totalProtein  += pro;
                totalCarbs    += crb;
                totalFat      += fat;

                updateSummary();
                refreshLog();
                foodFld.clear(); calFld.clear(); proFld.clear(); crbFld.clear(); fatFldF.clear();
                feedback.setStyle("-fx-text-fill:#10B981;");
                feedback.setText("✓ " + food + " logged!");
            } catch (NumberFormatException ex) {
                feedback.setText("Enter valid numbers for macros.");
            }
        });

        addCard.getChildren().addAll(addTitle, mealCb, lbl("Food Name"), foodFld,
            lbl("Macros"), macroFields, feedback, addBtn);

        // ── Meal log list ───────────────────────────────────
        Label logTitle = new Label("Today's Log");
        logTitle.getStyleClass().add("card-title");
        logList = new VBox(6);

        content.getChildren().addAll(title, dateL, calCard, macroRow, addCard, logTitle, logList);
        root = new ScrollPane(content);
        root.setFitToWidth(true);
        root.getStyleClass().add("scroll-panel");
    }

    private void updateSummary() {
        calConsumedLbl.setText(String.format("%.0f kcal", totalCalories));
        double remaining = calGoal - totalCalories;
        calRemainingLbl.setText(String.format("%.0f kcal", Math.max(0, remaining)));
        calRemainingLbl.setStyle("-fx-text-fill:" + (remaining < 0 ? "#EF4444" : "#10B981") + ";-fx-font-size:18px;-fx-font-weight:bold;");
        calBar.setProgress(Math.min(1.0, totalCalories / calGoal));

        proteinLbl.setText(String.format("%.1fg", totalProtein));
        carbsLbl.setText(String.format("%.1fg", totalCarbs));
        fatLbl.setText(String.format("%.1fg", totalFat));

        double totalMacroG = totalProtein + totalCarbs + totalFat;
        if (totalMacroG > 0) {
            proteinBar.setProgress(totalProtein / totalMacroG);
            carbsBar.setProgress(totalCarbs / totalMacroG);
            fatBar.setProgress(totalFat / totalMacroG);
        }
    }

    private void refreshLog() {
        logList.getChildren().clear();
        for (String[] entry : mealLog) {
            HBox row = new HBox(12);
            row.getStyleClass().add("history-row");
            row.setPadding(new Insets(10, 14, 10, 14));
            row.setAlignment(Pos.CENTER_LEFT);
            Label mealL = new Label(entry[0]); mealL.getStyleClass().add("history-date"); mealL.setPrefWidth(120);
            Label foodL = new Label(entry[1]); foodL.getStyleClass().add("history-dur"); HBox.setHgrow(foodL, Priority.ALWAYS);
            Label calL  = new Label(entry[2] + " kcal"); calL.getStyleClass().add("history-cal");
            Label proL  = new Label("P:" + entry[3] + "g"); proL.setStyle("-fx-text-fill:#10B981;-fx-font-size:11px;");
            Label crbL  = new Label("C:" + entry[4] + "g"); crbL.setStyle("-fx-text-fill:#F97316;-fx-font-size:11px;");
            Label fatL  = new Label("F:" + entry[5] + "g"); fatL.setStyle("-fx-text-fill:#3B82F6;-fx-font-size:11px;");
            row.getChildren().addAll(mealL, foodL, calL, proL, crbL, fatL);
            logList.getChildren().add(row);
        }
        if (mealLog.isEmpty()) {
            Label emp = new Label("No meals logged yet. Add your first meal above!");
            emp.getStyleClass().add("panel-subtitle");
            logList.getChildren().add(emp);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────

    private VBox metricBlock(String labelText, String valueText, String color) {
        VBox b = new VBox(2);
        Label l = new Label(labelText); l.getStyleClass().add("stat-card-title");
        Label v = new Label(valueText); v.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
        b.getChildren().addAll(l, v);
        HBox.setHgrow(b, Priority.ALWAYS);
        return b;
    }

    private VBox macroCard(String name, String value, String color, String tag) {
        VBox outer = new VBox(6);
        outer.getStyleClass().add("info-card");
        outer.setPadding(new Insets(14));

        VBox top = new VBox(2);
        Label l = new Label(name); l.getStyleClass().add("stat-card-title");
        Label v = new Label(value); v.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
        top.getChildren().addAll(l, v);

        ProgressBar bar = new ProgressBar(0);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.getStyleClass().add("nutrition-bar");
        bar.setStyle("-fx-accent:" + color + ";");

        outer.getChildren().addAll(top, bar);
        return outer;
    }

    private TextField numField(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.getStyleClass().add("form-field");
        return f;
    }

    private Label lbl(String t) { Label l = new Label(t); l.getStyleClass().add("form-label"); return l; }

    private double parseOrZero(String s) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return 0; }
    }

    public Parent getRoot() { return root; }
}
