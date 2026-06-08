package com.fitify.ui;

import com.fitify.model.User;
import com.fitify.service.AuthService;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * ProfilePanel - View and edit user profile.
 */
public class ProfilePanel {

    private final AuthService auth;
    private final User        user;
    private       ScrollPane  root;

    public ProfilePanel(AuthService auth) {
        this.auth = auth;
        this.user = auth.getCurrentUser();
        buildUI();
    }

    private void buildUI() {
        VBox content = new VBox(22);
        content.setPadding(new Insets(30));
        content.getStyleClass().add("panel-content");

        Label title = new Label("My Profile");
        title.getStyleClass().add("panel-title");

        // Health metrics card
        VBox metricsCard = new VBox(10);
        metricsCard.getStyleClass().add("stat-card");
        metricsCard.setPadding(new Insets(18));
        Label mTitle = new Label("Health Metrics");
        mTitle.getStyleClass().add("card-title");
        HBox mRow = new HBox(28);
        mRow.getChildren().addAll(
            mItem("BMI",      String.valueOf(user.calculateBMI())),
            mItem("Category", user.getBMICategory()),
            mItem("Weight",   user.getWeightKg() + " kg"),
            mItem("Height",   user.getHeightCm() + " cm"),
            mItem("Age",      user.getAge() + " yrs")
        );
        metricsCard.getChildren().addAll(mTitle, mRow);

        // Edit form
        VBox formCard = new VBox(12);
        formCard.getStyleClass().add("info-card");
        formCard.setPadding(new Insets(20));
        Label fTitle = new Label("Update Profile");
        fTitle.getStyleClass().add("card-title");

        TextField nameFld    = field(user.getName());
        TextField emailFld   = field(user.getEmail());
        emailFld.setEditable(false); emailFld.setOpacity(0.6);

        ComboBox<String> goalCb = new ComboBox<>();
        goalCb.getItems().addAll("Lose Weight","Build Muscle","Improve Endurance","Stay Active","Flexibility");
        goalCb.setValue(user.getFitnessGoal());
        goalCb.setMaxWidth(Double.MAX_VALUE);
        goalCb.getStyleClass().add("form-combo");

        TextField ageFld    = field(String.valueOf(user.getAge()));
        TextField weightFld = field(String.valueOf(user.getWeightKg()));
        TextField heightFld = field(String.valueOf(user.getHeightCm()));
        HBox statsRow = new HBox(10, ageFld, weightFld, heightFld);
        HBox.setHgrow(ageFld, Priority.ALWAYS);
        HBox.setHgrow(weightFld, Priority.ALWAYS);
        HBox.setHgrow(heightFld, Priority.ALWAYS);

        Label feedback = new Label();
        feedback.getStyleClass().add("form-error");

        Button saveBtn = new Button("Save Changes");
        saveBtn.getStyleClass().add("btn-primary");
        saveBtn.setOnAction(e -> {
            try {
                user.setName(nameFld.getText().trim());
                user.setFitnessGoal(goalCb.getValue());
                user.setAge(Integer.parseInt(ageFld.getText().trim()));
                user.setWeightKg(Double.parseDouble(weightFld.getText().trim()));
                user.setHeightCm(Double.parseDouble(heightFld.getText().trim()));
                if (auth.updateProfile(user)) {
                    feedback.setStyle("-fx-text-fill: #10b981;");
                    feedback.setText("Profile updated successfully!");
                } else {
                    feedback.setText("Update failed. Please try again.");
                }
            } catch (NumberFormatException ex) {
                feedback.setText("Age, weight and height must be valid numbers.");
            }
        });

        formCard.getChildren().addAll(
            fTitle,
            lbl("Full Name"), nameFld,
            lbl("Email (cannot change)"), emailFld,
            lbl("Fitness Goal"), goalCb,
            lbl("Age  /  Weight (kg)  /  Height (cm)"), statsRow,
            feedback, saveBtn
        );

        content.getChildren().addAll(title, metricsCard, formCard);
        root = new ScrollPane(content);
        root.setFitToWidth(true);
        root.getStyleClass().add("scroll-panel");
    }

    private VBox mItem(String label, String value) {
        VBox b = new VBox(2);
        Label l = new Label(label); l.getStyleClass().add("metric-label");
        Label v = new Label(value); v.getStyleClass().add("metric-value");
        b.getChildren().addAll(l, v);
        return b;
    }

    private Label     lbl(String t)   { Label l = new Label(t); l.getStyleClass().add("form-label"); return l; }
    private TextField field(String v) { TextField f = new TextField(v); f.getStyleClass().add("form-field"); return f; }

    public Parent getRoot() { return root; }
}
