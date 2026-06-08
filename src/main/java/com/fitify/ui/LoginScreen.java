package com.fitify.ui;

import com.fitify.model.User;
import com.fitify.service.AuthService;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * LoginScreen - Stylised entry point with big logo + tabs.
 */
public class LoginScreen {

    private final Stage       stage;
    private final AuthService authService = new AuthService();
    private       VBox        root;

    public LoginScreen(Stage stage) {
        this.stage = stage;
        buildUI();
    }

    private void buildUI() {
        root = new VBox();
        root.getStyleClass().add("login-root");
        root.setAlignment(Pos.CENTER);
        root.setSpacing(0);

        // ── Hero section ──────────────────────────────────
        VBox hero = new VBox(6);
        hero.setAlignment(Pos.CENTER);
        hero.setPadding(new Insets(50, 20, 28, 20));

        // App name logo - bold, readable, guaranteed font
        Label logoBox = new Label("FITIFY ⚡");
        logoBox.setStyle(
            "-fx-font-size: 54px;" +
            "-fx-font-family: 'Segoe UI Black', 'Segoe UI', Arial, sans-serif;" +
            "-fx-font-weight: 900;" +
            "-fx-text-fill: #5B4FE8;" +
            "-fx-effect: dropshadow(gaussian, #5B4FE870, 20, 0.3, 0, 3);"
        );

        Label tagline = new Label("🔥  Train Hard. Track Smart. Dominate.");
        tagline.getStyleClass().add("login-tagline");

        // Three mini feature badges
        HBox badges = new HBox(10);
        badges.setAlignment(Pos.CENTER);
        badges.setPadding(new Insets(10, 0, 0, 0));
        badges.getChildren().addAll(
            badge("💪 Workouts"),
            badge("🎵 Music"),
            badge("📊 Progress")
        );

        hero.getChildren().addAll(logoBox, tagline, badges);

        // ── Login card ────────────────────────────────────
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getStyleClass().add("login-tabs");
        tabs.setMaxWidth(420);
        tabs.getTabs().addAll(
            new Tab("🔑  Sign In",  buildLoginForm()),
            new Tab("✨  Register", buildRegisterForm())
        );

        VBox card = new VBox(tabs);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(440);
        card.getStyleClass().add("login-card");

        root.getChildren().addAll(hero, card);
    }

    private Label styledLogo(String text, String colour) {
        Label l = new Label(text);
        l.setStyle(
            "-fx-font-size: 56px;" +
            "-fx-font-family: 'Arial Black';" +
            "-fx-font-weight: 900;" +
            "-fx-text-fill: " + colour + ";" +
            "-fx-effect: dropshadow(gaussian," + colour + ",18,0.35,0,2);"
        );
        return l;
    }

    private Label badge(String text) {
        Label l = new Label(text);
        l.setStyle(
            "-fx-background-color:#F0F1FA;" +
            "-fx-text-fill:#44435A;" +
            "-fx-background-radius:20px;" +
            "-fx-border-color:#E2E3EF;" +
            "-fx-border-radius:20px;" +
            "-fx-border-width:1px;" +
            "-fx-padding:5 12;" +
            "-fx-font-size:11px;" +
            "-fx-font-family:'Segoe UI Semibold',sans-serif;"
        );
        return l;
    }

    private VBox buildLoginForm() {
        VBox form = new VBox(12);
        form.setPadding(new Insets(24, 30, 30, 30));

        TextField     emailFld = field("📧  Email address");
        PasswordField passFld  = pass("🔒  Password");
        Label         errLbl   = errLabel();
        Button        btn      = primaryBtn("Sign In  →");

        btn.setOnAction(e -> {
            String email = emailFld.getText().trim();
            String pass  = passFld.getText();
            if (email.isEmpty() || pass.isEmpty()) {
                errLbl.setText("⚠️  Please fill in all fields.");
                return;
            }
            Optional<User> user = authService.login(email, pass);
            if (user.isPresent()) openDashboard();
            else errLbl.setText("❌  Invalid email or password.");
        });

        form.getChildren().addAll(
            lbl("Email"), emailFld,
            lbl("Password"), passFld,
            errLbl, btn
        );
        return form;
    }

    private VBox buildRegisterForm() {
        VBox form = new VBox(10);
        form.setPadding(new Insets(24, 30, 30, 30));

        TextField     nameFld   = field("👤  Full name");
        TextField     emailFld  = field("📧  Email address");
        PasswordField passFld   = pass("🔒  Password (min 6 chars)");

        ComboBox<String> goalCb = new ComboBox<>();
        goalCb.getItems().addAll("Lose Weight","Build Muscle","Improve Endurance","Stay Active","Flexibility");
        goalCb.setPromptText("🎯  Select fitness goal");
        goalCb.setMaxWidth(Double.MAX_VALUE);
        goalCb.getStyleClass().add("form-combo");

        TextField ageFld    = field("Age");
        TextField weightFld = field("Weight kg");
        TextField heightFld = field("Height cm");
        HBox stats = new HBox(8, ageFld, weightFld, heightFld);
        HBox.setHgrow(ageFld,    Priority.ALWAYS);
        HBox.setHgrow(weightFld, Priority.ALWAYS);
        HBox.setHgrow(heightFld, Priority.ALWAYS);

        Label  errLbl = errLabel();
        Button btn    = primaryBtn("Create Account  →");

        btn.setOnAction(e -> {
            try {
                String name  = nameFld.getText().trim();
                String email = emailFld.getText().trim();
                String pass  = passFld.getText();
                String goal  = goalCb.getValue();
                int    age   = Integer.parseInt(ageFld.getText().trim());
                double wt    = Double.parseDouble(weightFld.getText().trim());
                double ht    = Double.parseDouble(heightFld.getText().trim());
                if (name.isEmpty() || email.isEmpty() || pass.length() < 6 || goal == null) {
                    errLbl.setText("⚠️  Complete all fields. Password must be 6+ chars.");
                    return;
                }
                Optional<User> user = authService.register(name, email, pass, goal, age, wt, ht);
                if (user.isPresent()) { authService.login(email, pass); openDashboard(); }
                else errLbl.setText("❌  Email already registered or error occurred.");
            } catch (NumberFormatException ex) {
                errLbl.setText("⚠️  Age, weight and height must be numbers.");
            }
        });

        form.getChildren().addAll(
            lbl("Full Name"), nameFld,
            lbl("Email"), emailFld,
            lbl("Password"), passFld,
            lbl("Fitness Goal"), goalCb,
            lbl("Age / Weight (kg) / Height (cm)"), stats,
            errLbl, btn
        );
        return form;
    }

    private void openDashboard() {
        DashboardScreen dash = new DashboardScreen(stage, authService);
        stage.getScene().setRoot(dash.getRoot());
        stage.setTitle("Fitify ⚡ — " + authService.getCurrentUser().getName());
    }

    private TextField     field(String p)  { TextField f = new TextField(); f.setPromptText(p); f.getStyleClass().add("form-field"); return f; }
    private PasswordField pass(String p)   { PasswordField f = new PasswordField(); f.setPromptText(p); f.getStyleClass().add("form-field"); return f; }
    private Label         lbl(String t)    { Label l = new Label(t); l.getStyleClass().add("form-label"); return l; }
    private Label         errLabel()       { Label l = new Label(); l.getStyleClass().add("form-error"); return l; }
    private Button        primaryBtn(String t) { Button b = new Button(t); b.setMaxWidth(Double.MAX_VALUE); b.getStyleClass().add("btn-primary"); return b; }

    public Parent getRoot() { return root; }
}
