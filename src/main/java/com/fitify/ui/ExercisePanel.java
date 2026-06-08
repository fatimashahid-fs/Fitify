package com.fitify.ui;

import com.fitify.model.Exercise;
import com.fitify.service.WorkoutService;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * ExercisePanel - Browse and filter the exercise library.
 */
public class ExercisePanel {

    private final WorkoutService         workoutSvc;
    private       ObservableList<Exercise> all;
    private       ScrollPane             root;

    public ExercisePanel(WorkoutService workoutSvc) {
        this.workoutSvc = workoutSvc;
        this.all        = FXCollections.observableArrayList(workoutSvc.getExerciseLibrary());
        buildUI();
    }

    private void buildUI() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(30));
        content.getStyleClass().add("panel-content");

        Label title = new Label("Exercise Library");
        title.getStyleClass().add("panel-title");
        Label sub = new Label("Browse and filter all exercises");
        sub.getStyleClass().add("panel-subtitle");

        // Filter bar
        TextField searchFld = new TextField();
        searchFld.setPromptText("Search by name...");
        searchFld.getStyleClass().add("form-field");
        searchFld.setPrefWidth(220);

        ComboBox<String> typeCb = new ComboBox<>();
        typeCb.getItems().addAll("All Types","CARDIO","STRENGTH","FLEXIBILITY");
        typeCb.setValue("All Types");
        typeCb.getStyleClass().add("form-combo");

        ComboBox<String> diffCb = new ComboBox<>();
        diffCb.getItems().addAll("All Levels","Beginner","Intermediate","Advanced");
        diffCb.setValue("All Levels");
        diffCb.getStyleClass().add("form-combo");

        HBox filterBar = new HBox(10, searchFld, typeCb, diffCb);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        FlowPane grid = new FlowPane();
        grid.setHgap(14); grid.setVgap(14);
        grid.setPrefWrapLength(820);

        Runnable filter = () -> {
            String search = searchFld.getText().toLowerCase();
            String type   = typeCb.getValue();
            String diff   = diffCb.getValue();
            grid.getChildren().clear();
            all.stream()
               .filter(e -> e.getName().toLowerCase().contains(search))
               .filter(e -> type.equals("All Types")  || e.getType().name().equals(type))
               .filter(e -> diff.equals("All Levels") || e.getDifficulty().equals(diff))
               .map(this::card)
               .forEach(grid.getChildren()::add);
        };

        searchFld.textProperty().addListener((o, ov, nv) -> filter.run());
        typeCb.valueProperty().addListener((o, ov, nv)   -> filter.run());
        diffCb.valueProperty().addListener((o, ov, nv)   -> filter.run());
        filter.run();

        content.getChildren().addAll(title, sub, filterBar, grid);
        root = new ScrollPane(content);
        root.setFitToWidth(true);
        root.getStyleClass().add("scroll-panel");
    }

    private VBox card(Exercise e) {
        VBox c = new VBox(6);
        c.getStyleClass().add("exercise-card");
        c.setPrefWidth(230);
        c.setPadding(new Insets(14));

        Label name  = new Label(e.getTypeIcon() + "  " + e.getName());
        name.getStyleClass().add("exercise-card-title");
        name.setWrapText(true);

        Label type  = new Label(e.getTypeLabel() + " - " + e.getDifficulty());
        type.getStyleClass().add("exercise-card-type");

        Label muscle = new Label(e.getMuscleGroup());
        muscle.getStyleClass().add("exercise-card-muscle");

        Label dur   = new Label(e.getDurationMinutes() + " min");
        dur.getStyleClass().add("exercise-card-duration");

        Label sum   = new Label(e.getSummary());
        sum.getStyleClass().add("exercise-card-summary");
        sum.setWrapText(true);

        c.getChildren().addAll(name, type, muscle, dur, sum);
        return c;
    }

    public Parent getRoot() { return root; }
}
