package com.fitify.ui;

import com.fitify.model.*;
import com.fitify.service.WorkoutService;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * RoutinePanel - Create, view, and manage personal workout routines.
 */
public class RoutinePanel {

    private final WorkoutService         workoutSvc;
    private final User                   user;
    private       SplitPane              root;
    private       ListView<WorkoutRoutine> listView;

    public RoutinePanel(WorkoutService workoutSvc, User user) {
        this.workoutSvc = workoutSvc;
        this.user       = user;
        buildUI();
    }

    private void buildUI() {
        root = new SplitPane();
        root.getStyleClass().add("split-panel");

        // Left pane - routine list
        VBox left = new VBox(12);
        left.setPadding(new Insets(24, 16, 24, 24));
        left.getStyleClass().add("panel-content");

        Label title = new Label("My Routines");
        title.getStyleClass().add("panel-title");

        listView = new ListView<>();
        listView.getItems().addAll(workoutSvc.getMyRoutines());
        listView.getStyleClass().add("routine-list");
        listView.setCellFactory(lv -> new ListCell<>() {
            protected void updateItem(WorkoutRoutine r, boolean empty) {
                super.updateItem(r, empty);
                if (empty || r == null) { setGraphic(null); return; }
                VBox cell = new VBox(3);
                Label n = new Label(r.getName()); n.getStyleClass().add("routine-cell-name");
                Label i = new Label(r.getExercises().size() + " exercises - "
                    + r.getTotalDurationMinutes() + " min");
                i.getStyleClass().add("routine-cell-info");
                cell.getChildren().addAll(n, i);
                setGraphic(cell);
            }
        });
        VBox.setVgrow(listView, Priority.ALWAYS);

        Button createBtn = new Button("+ Create New Routine");
        createBtn.getStyleClass().add("btn-primary");
        createBtn.setMaxWidth(Double.MAX_VALUE);
        createBtn.setOnAction(e -> showCreateDialog());

        left.getChildren().addAll(title, listView, createBtn);

        // Right pane - detail view
        VBox right = new VBox(14);
        right.setPadding(new Insets(24));
        right.getStyleClass().add("panel-content");
        Label placeholder = new Label("Select a routine to view details");
        placeholder.getStyleClass().add("panel-subtitle");
        right.getChildren().add(placeholder);

        listView.getSelectionModel().selectedItemProperty().addListener((obs, ov, r) -> {
            if (r != null) showDetail(right, r);
        });

        root.getItems().addAll(left, right);
        root.setDividerPositions(0.38);
    }

    private void showDetail(VBox pane, WorkoutRoutine r) {
        pane.getChildren().clear();

        Label name = new Label(r.getName());
        name.getStyleClass().add("panel-title");

        Label info = new Label("Created: " + r.getDateCreated()
            + "   |   " + r.getExercises().size() + " exercises"
            + "   |   " + r.getTotalDurationMinutes() + " min"
            + "   |   ~" + String.format("%.0f", r.getEstimatedCalories(user.getWeightKg())) + " kcal");
        info.getStyleClass().add("panel-subtitle");
        info.setWrapText(true);

        Label exLabel = new Label("Exercises:");
        exLabel.getStyleClass().add("card-title");

        VBox exList = new VBox(8);
        for (int i = 0; i < r.getExercises().size(); i++) {
            Exercise ex = r.getExercises().get(i);
            HBox row = new HBox(10);
            row.getStyleClass().add("exercise-row");
            row.setPadding(new Insets(10, 12, 10, 12));
            Label num  = new Label((i + 1) + "."); num.getStyleClass().add("ex-row-num");
            Label icon = new Label(ex.getTypeIcon());
            VBox  det  = new VBox(2);
            Label n    = new Label(ex.getName()); n.getStyleClass().add("ex-row-name");
            Label s    = new Label(ex.getSummary()); s.getStyleClass().add("ex-row-summary");
            s.setWrapText(true);
            det.getChildren().addAll(n, s);
            HBox.setHgrow(det, Priority.ALWAYS);
            row.getChildren().addAll(num, icon, det);
            exList.getChildren().add(row);
        }
        ScrollPane scroll = new ScrollPane(exList);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("inner-scroll");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Button deleteBtn = new Button("Delete Routine");
        deleteBtn.getStyleClass().add("btn-danger");
        deleteBtn.setOnAction(e -> {
            workoutSvc.deleteRoutine(r.getRoutineId());
            listView.getItems().remove(r);
            pane.getChildren().setAll(new Label("Routine deleted."));
        });

        pane.getChildren().addAll(name, info, exLabel, scroll, deleteBtn);
    }

    private void showCreateDialog() {
        Dialog<WorkoutRoutine> dialog = new Dialog<>();
        dialog.setTitle("Create New Routine");
        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        VBox form = new VBox(10);
        form.setPadding(new Insets(20));
        TextField nameFld = new TextField(); nameFld.setPromptText("Routine name");
        TextField descFld = new TextField(); descFld.setPromptText("Description (optional)");
        TextField plFld   = new TextField(); plFld.setPromptText("Linked playlist name (optional)");
        Label exLabel     = new Label("Select exercises (hold Ctrl for multi-select):");

        ListView<Exercise> picker = new ListView<>();
        picker.getItems().addAll(workoutSvc.getExerciseLibrary());
        picker.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        picker.setPrefHeight(160);
        picker.setCellFactory(lv -> new ListCell<>() {
            protected void updateItem(Exercise e, boolean empty) {
                super.updateItem(e, empty);
                setText(empty || e == null ? null : e.getTypeIcon() + "  " + e);
            }
        });

        form.getChildren().addAll(
            new Label("Name:"), nameFld,
            new Label("Description:"), descFld,
            new Label("Playlist:"), plFld,
            exLabel, picker
        );
        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(btn -> {
            if (btn != saveBtn) return null;
            WorkoutRoutine r = new WorkoutRoutine(user.getUserId(),
                nameFld.getText().trim(), descFld.getText().trim());
            r.setLinkedPlaylistName(plFld.getText().trim());
            picker.getSelectionModel().getSelectedItems().forEach(r::addExercise);
            return r;
        });

        dialog.showAndWait().ifPresent(r -> {
            if (!r.getName().isEmpty()) {
                workoutSvc.createRoutine(r);
                listView.getItems().add(0, r);
            }
        });
    }

    public Parent getRoot() { return root; }
}
