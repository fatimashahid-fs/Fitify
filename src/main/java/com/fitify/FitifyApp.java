package com.fitify;

import com.fitify.ui.LoginScreen;
import com.fitify.util.DatabaseManager;
import com.fitify.util.ThemeManager;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FitifyApp extends Application {

    public static final String APP_TITLE  = "Fitify - Dynamic Fitness & Music";
    public static final int    WIN_WIDTH  = 1100;
    public static final int    WIN_HEIGHT = 720;

    @Override
    public void start(Stage primaryStage) {
        DatabaseManager.getInstance().initializeDatabase();

        LoginScreen loginScreen = new LoginScreen(primaryStage);
        Scene scene = new Scene(loginScreen.getRoot(), WIN_WIDTH, WIN_HEIGHT);
        scene.getStylesheets().add(
            getClass().getResource("/styles/fitify.css").toExternalForm()
        );
        ThemeManager.getInstance().setScene(scene);

        primaryStage.setTitle(APP_TITLE);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    @Override
    public void stop() {
        DatabaseManager.getInstance().closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}