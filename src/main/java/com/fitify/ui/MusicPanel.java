package com.fitify.ui;

import com.fitify.model.*;
import com.fitify.service.JamendoService;
import com.fitify.service.MusicService;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;


/**
 * MusicPanel — Local playlists + Jamendo free music (full tracks, CC licensed).
 */
public class MusicPanel {

    private final MusicService   musicSvc;
    private final JamendoService jamendoSvc;

    private BorderPane       root;
    private Label            nowPlayingLabel;
    private ListView<Playlist> playlistView;
    private ListView<Track>    trackView;
    private ListView<Track>    jamendoResultsView;
    private ListView<Track>    jamendoGenreView;
    private Label              searchStatus;
    private Label              genreStatus;

    private static final String[] GENRES = {
        "electronic", "rock", "hiphop", "pop", "jazz",
        "classical", "metal", "ambient", "funk", "reggae"
    };

    public MusicPanel(MusicService musicSvc) {
        this.musicSvc   = musicSvc;
        this.jamendoSvc = new JamendoService();
        buildUI();
    }

    private void buildUI() {
        root = new BorderPane();

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getStyleClass().add("music-tabs");

        tabs.getTabs().addAll(
            tab("🎵  My Music",      buildMyMusicPane()),
            tab("🔍  Search",        buildSearchPane()),
            tab("🎸  Browse Genres", buildGenrePane())
        );

        root.setCenter(tabs);
        root.setBottom(buildPlayerBar());
    }

    private Tab tab(String label, javafx.scene.Node content) {
        Tab t = new Tab(label, content);
        t.setClosable(false);
        return t;
    }

    // ── My Music ──────────────────────────────────────────────────
    private SplitPane buildMyMusicPane() {
        // Left: playlists
        VBox left = new VBox(10);
        left.setPadding(new Insets(20, 12, 20, 20));
        left.getStyleClass().add("panel-content");
        left.setPrefWidth(220);

        Label plTitle = new Label("📋  Playlists");
        plTitle.getStyleClass().add("panel-title");

        playlistView = new ListView<>();
        playlistView.getItems().addAll(musicSvc.getUserPlaylists());
        playlistView.getStyleClass().add("playlist-list");
        playlistView.setCellFactory(lv -> new ListCell<>() {
            protected void updateItem(Playlist pl, boolean empty) {
                super.updateItem(pl, empty);
                if (empty || pl == null) { setGraphic(null); return; }
                VBox c = new VBox(2);
                Label n = new Label(pl.getName()); n.getStyleClass().add("routine-cell-name");
                Label i = new Label(pl.getTracks().size() + " tracks"); i.getStyleClass().add("routine-cell-info");
                c.getChildren().addAll(n, i);
                setGraphic(c);
            }
        });
        VBox.setVgrow(playlistView, Priority.ALWAYS);
        playlistView.getSelectionModel().selectedItemProperty().addListener((obs, ov, pl) -> {
            if (pl != null) trackView.getItems().setAll(pl.getTracks());
        });

        Button newPl = new Button("＋  New Playlist");
        newPl.getStyleClass().add("btn-secondary");
        newPl.setMaxWidth(Double.MAX_VALUE);
        newPl.setOnAction(e -> showNewPlaylistDialog());
        left.getChildren().addAll(plTitle, playlistView, newPl);

        // Right: tracks
        VBox center = new VBox(10);
        center.setPadding(new Insets(20));
        center.getStyleClass().add("panel-content");

        Label trkTitle = new Label("🎶  Tracks");
        trkTitle.getStyleClass().add("panel-title");

        trackView = new ListView<>();
        trackView.getItems().addAll(musicSvc.getAllTracks());
        trackView.getStyleClass().add("track-list");
        trackView.setCellFactory(lv -> trackCell());
        VBox.setVgrow(trackView, Priority.ALWAYS);
        trackView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Track t = trackView.getSelectionModel().getSelectedItem();
                if (t != null) { musicSvc.play(t); updateNowPlaying(t); }
            }
        });

        Button addTrack = new Button("＋  Add from File");
        addTrack.getStyleClass().add("btn-secondary");
        addTrack.setOnAction(e -> addTrackFromFile());
        center.getChildren().addAll(trkTitle, trackView, addTrack);

        SplitPane split = new SplitPane(left, center);
        split.setDividerPositions(0.3);
        return split;
    }

    // ── Jamendo Search ────────────────────────────────────────────
    private VBox buildSearchPane() {
        VBox pane = new VBox(14);
        pane.setPadding(new Insets(20));
        pane.getStyleClass().add("panel-content");

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label icon = new Label("🎵");
        icon.setStyle("-fx-font-size:30px;");
        VBox titleBox = new VBox(2);
        Label title = new Label("Jamendo Music");
        title.setStyle(
            "-fx-font-size:22px;-fx-font-weight:900;" +
            "-fx-font-family:'Segoe UI Black',Arial,sans-serif;-fx-text-fill:#5B4FE8;"
        );
        Label sub = new Label("Free, full-length tracks · Creative Commons · No login needed");
        sub.getStyleClass().add("panel-subtitle");
        titleBox.getChildren().addAll(title, sub);
        header.getChildren().addAll(icon, titleBox);

        // Search bar
        TextField searchField = new TextField();
        searchField.setPromptText("Search songs, artists, albums...");
        searchField.getStyleClass().add("search-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Button searchBtn = new Button("Search");
        searchBtn.getStyleClass().add("btn-primary");

        HBox searchBar = new HBox(10, searchField, searchBtn);
        searchBar.setAlignment(Pos.CENTER_LEFT);

        searchStatus = new Label("Type a name and hit Search — results stream full tracks ✅");
        searchStatus.getStyleClass().add("panel-subtitle");

        jamendoResultsView = new ListView<>();
        jamendoResultsView.getStyleClass().add("track-list");
        jamendoResultsView.setCellFactory(lv -> jamendoTrackCell());
        VBox.setVgrow(jamendoResultsView, Priority.ALWAYS);

        Label hint = new Label("💡 Double-click to play · Right-click to save to My Music");
        hint.getStyleClass().add("panel-subtitle");

        Runnable doSearch = () -> {
            String q = searchField.getText().trim();
            if (q.isEmpty()) return;
            searchStatus.setText("🔍  Searching Jamendo...");
            jamendoResultsView.getItems().clear();
            Thread t = new Thread(() -> {
                List<Track> res = jamendoSvc.search(q);
                Platform.runLater(() -> {
                    if (res.isEmpty()) searchStatus.setText("No results found. Try a different query.");
                    else {
                        searchStatus.setText("✅  " + res.size() + " full tracks found");
                        jamendoResultsView.getItems().setAll(res);
                    }
                });
            });
            t.setDaemon(true);
            t.start();
        };

        searchBtn.setOnAction(e -> doSearch.run());
        searchField.setOnAction(e -> doSearch.run());

        jamendoResultsView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Track t = jamendoResultsView.getSelectionModel().getSelectedItem();
                if (t != null) { musicSvc.play(t); updateNowPlaying(t); }
            }
        });

        attachContextMenu(jamendoResultsView, searchStatus);
        pane.getChildren().addAll(header, searchBar, searchStatus, jamendoResultsView, hint);
        return pane;
    }

    // ── Genre Browser ─────────────────────────────────────────────
    private VBox buildGenrePane() {
        VBox pane = new VBox(14);
        pane.setPadding(new Insets(20));
        pane.getStyleClass().add("panel-content");

        Label title = new Label("🎸  Browse by Genre");
        title.getStyleClass().add("panel-title");

        Label sub = new Label("Pick a genre — top tracks load automatically");
        sub.getStyleClass().add("panel-subtitle");

        // Genre chip row
        FlowPane chips = new FlowPane(8, 8);
        for (String genre : GENRES) {
            Button chip = genreChip(genre);
            chip.setOnAction(e -> loadGenre(genre));
            chips.getChildren().add(chip);
        }

        genreStatus = new Label("Select a genre above 👆");
        genreStatus.getStyleClass().add("panel-subtitle");

        jamendoGenreView = new ListView<>();
        jamendoGenreView.getStyleClass().add("track-list");
        jamendoGenreView.setCellFactory(lv -> jamendoTrackCell());
        VBox.setVgrow(jamendoGenreView, Priority.ALWAYS);

        jamendoGenreView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Track t = jamendoGenreView.getSelectionModel().getSelectedItem();
                if (t != null) { musicSvc.play(t); updateNowPlaying(t); }
            }
        });

        attachContextMenu(jamendoGenreView, genreStatus);
        pane.getChildren().addAll(title, sub, chips, genreStatus, jamendoGenreView);
        return pane;
    }

    private void loadGenre(String genre) {
        genreStatus.setText("🔍  Loading " + genre + "...");
        jamendoGenreView.getItems().clear();
        Thread t = new Thread(() -> {
            List<Track> res = jamendoSvc.browseByGenre(genre);
            Platform.runLater(() -> {
                if (res.isEmpty()) genreStatus.setText("No tracks found for \"" + genre + "\".");
                else {
                    genreStatus.setText("✅  " + res.size() + " tracks · " + genre);
                    jamendoGenreView.getItems().setAll(res);
                }
            });
        });
        t.setDaemon(true);
        t.start();
    }

    private Button genreChip(String genre) {
        Button b = new Button(genreEmoji(genre) + "  " + capitalize(genre));
        b.setStyle(
            "-fx-background-color:#F0F1FA;-fx-text-fill:#5B4FE8;" +
            "-fx-background-radius:20px;-fx-border-color:#DDD6FF;" +
            "-fx-border-radius:20px;-fx-border-width:1.5px;" +
            "-fx-padding:6 14;-fx-cursor:hand;-fx-font-size:12px;" +
            "-fx-font-family:'Segoe UI Semibold',sans-serif;"
        );
        b.setOnMouseEntered(e -> b.setStyle(
            "-fx-background-color:#5B4FE8;-fx-text-fill:#FFFFFF;" +
            "-fx-background-radius:20px;-fx-border-color:transparent;" +
            "-fx-border-radius:20px;-fx-border-width:1.5px;" +
            "-fx-padding:6 14;-fx-cursor:hand;-fx-font-size:12px;" +
            "-fx-font-family:'Segoe UI Semibold',sans-serif;"
        ));
        b.setOnMouseExited(e -> b.setStyle(
            "-fx-background-color:#F0F1FA;-fx-text-fill:#5B4FE8;" +
            "-fx-background-radius:20px;-fx-border-color:#DDD6FF;" +
            "-fx-border-radius:20px;-fx-border-width:1.5px;" +
            "-fx-padding:6 14;-fx-cursor:hand;-fx-font-size:12px;" +
            "-fx-font-family:'Segoe UI Semibold',sans-serif;"
        ));
        return b;
    }

    private String genreEmoji(String g) {
        return switch (g) {
            case "electronic" -> "⚡";
            case "rock"       -> "🎸";
            case "hiphop"     -> "🎤";
            case "pop"        -> "🌟";
            case "jazz"       -> "🎷";
            case "classical"  -> "🎻";
            case "metal"      -> "🤘";
            case "ambient"    -> "🌊";
            case "funk"       -> "🕺";
            case "reggae"     -> "🌴";
            default           -> "🎵";
        };
    }

    private String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    // ── Player Bar ────────────────────────────────────────────────
    private VBox buildPlayerBar() {
        VBox bar = new VBox(10);
        bar.getStyleClass().add("player-bar");
        bar.setPadding(new Insets(14, 22, 14, 22));

        nowPlayingLabel = new Label("🎵  Select a track to play");
        nowPlayingLabel.getStyleClass().add("now-playing-label");

        ProgressBar prog = new ProgressBar(0);
        prog.setMaxWidth(Double.MAX_VALUE);
        prog.getStyleClass().add("track-progress");

        Button prevBtn  = ctrlBtn("⏮");
        Button playBtn  = ctrlBtn("▶");
        Button pauseBtn = ctrlBtn("⏸");
        Button skipBtn  = ctrlBtn("⏭");
        Button muteBtn  = ctrlBtn("🔊");

        prevBtn.setOnAction(e  -> { musicSvc.previous(); updateNowPlayingFromService(); });
        playBtn.setOnAction(e  -> { musicSvc.start();    updateNowPlayingFromService(); });
        pauseBtn.setOnAction(e -> musicSvc.pause());
        skipBtn.setOnAction(e  -> { musicSvc.skip();     updateNowPlayingFromService(); });
        muteBtn.setOnAction(e  -> {
            musicSvc.toggleMute();
            muteBtn.setText(musicSvc.isMuted() ? "🔇" : "🔊");
        });

        Slider vol = new Slider(0, 1, musicSvc.getVolume());
        vol.setPrefWidth(110);
        vol.getStyleClass().add("volume-slider");
        vol.valueProperty().addListener((obs, ov, nv) -> musicSvc.setVolume(nv.doubleValue()));

        Label volLbl = new Label("🔉");
        volLbl.getStyleClass().add("panel-subtitle");

        HBox controls = new HBox(10, prevBtn, playBtn, pauseBtn, skipBtn, muteBtn, volLbl, vol);
        controls.setAlignment(Pos.CENTER);
        bar.getChildren().addAll(nowPlayingLabel, prog, controls);
        return bar;
    }

    // ── Cells ─────────────────────────────────────────────────────
    private ListCell<Track> trackCell() {
        return new ListCell<>() {
            protected void updateItem(Track t, boolean empty) {
                super.updateItem(t, empty);
                if (empty || t == null) { setGraphic(null); return; }
                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER_LEFT);
                Label ttl = new Label(t.getTitle());  ttl.getStyleClass().add("track-title");  HBox.setHgrow(ttl, Priority.ALWAYS);
                Label art = new Label(t.getArtist()); art.getStyleClass().add("track-artist");
                Label dur = new Label(t.getFormattedDuration()); dur.getStyleClass().add("track-dur");

                Button delBtn = new Button("🗑");
                delBtn.getStyleClass().add("btn-secondary");
                delBtn.setStyle("-fx-padding:2 8;-fx-font-size:13px;");
                delBtn.setOnAction(e -> {
                    musicSvc.deleteTrack(t);
                    getListView().getItems().remove(t);
                });

                row.getChildren().addAll(ttl, art, dur, delBtn);
                setGraphic(row);
            }
        };
    }

    private ListCell<Track> jamendoTrackCell() {
        return new ListCell<>() {
            protected void updateItem(Track t, boolean empty) {
                super.updateItem(t, empty);
                if (empty || t == null) { setGraphic(null); return; }
                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER_LEFT);

                Label musicIcon = new Label("🎵");
                musicIcon.setStyle("-fx-font-size:15px;");

                VBox info = new VBox(2);
                HBox.setHgrow(info, Priority.ALWAYS);
                Label ttl = new Label(t.getTitle());
                ttl.getStyleClass().add("track-title");
                String albumStr = (t.getAlbum() != null && !t.getAlbum().isEmpty())
                    ? "  ·  " + t.getAlbum() : "";
                Label art = new Label(t.getArtist() + albumStr);
                art.getStyleClass().add("track-artist");
                info.getChildren().addAll(ttl, art);

                VBox meta = new VBox(2);
                meta.setAlignment(Pos.CENTER_RIGHT);
                Label dur = new Label(t.getFormattedDuration());
                dur.getStyleClass().add("track-dur");
                Label badge = new Label("FULL");
                badge.setStyle(
                    "-fx-background-color:#22C55E;-fx-text-fill:white;" +
                    "-fx-padding:2 6;-fx-background-radius:4;-fx-font-size:9px;" +
                    "-fx-font-weight:bold;"
                );
                meta.getChildren().addAll(dur, badge);
                row.getChildren().addAll(musicIcon, info, meta);
                setGraphic(row);
            }
        };
    }

    // ── Helpers ───────────────────────────────────────────────────
    private void attachContextMenu(ListView<Track> list, Label statusLabel) {
        ContextMenu ctx = new ContextMenu();
        MenuItem play = new MenuItem("▶  Play");
        MenuItem save = new MenuItem("➕  Save to My Music");
        ctx.getItems().addAll(play, save);
        play.setOnAction(e -> {
            Track t = list.getSelectionModel().getSelectedItem();
            if (t != null) { musicSvc.play(t); updateNowPlaying(t); }
        });
        save.setOnAction(e -> {
            Track t = list.getSelectionModel().getSelectedItem();
            if (t != null) {
                musicSvc.addTrack(t);
                trackView.getItems().add(t);
                statusLabel.setText("✅  Saved \"" + t.getTitle() + "\" to My Music");
            }
        });
        list.setContextMenu(ctx);
    }

    private void updateNowPlaying(Track t) {
        if (t != null)
            nowPlayingLabel.setText("🎵  " + t.getTitle() + "  —  " + t.getArtist());
    }

    private void updateNowPlayingFromService() {
        Track t = musicSvc.getCurrentTrack();
        if (t != null) updateNowPlaying(t);
    }

    private void addTrackFromFile() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Audio File");
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Audio Files", "*.mp3","*.wav","*.m4a","*.aac"));
        File f = fc.showOpenDialog(null);
        if (f == null) return;
        String name = f.getName().replaceFirst("\\.[^.]+$", "");
        Track t = new Track(name, "Unknown Artist", f.getAbsolutePath(), 0, 120, "Unknown");
        musicSvc.addTrack(t);
        trackView.getItems().add(t);
    }

    private void showNewPlaylistDialog() {
        Dialog<Playlist> dialog = new Dialog<>();
        dialog.setTitle("New Playlist");
        ButtonType saveBtn = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);
        VBox form = new VBox(10);
        form.setPadding(new Insets(16));
        TextField nameFld = new TextField(); nameFld.setPromptText("Playlist name");
        ComboBox<String> moodCb = new ComboBox<>();
        moodCb.getItems().addAll("High Energy","Chill","Motivational","Warm-Up","Cool Down");
        moodCb.setPromptText("Mood tag");
        ComboBox<String> typeCb = new ComboBox<>();
        typeCb.getItems().addAll("Cardio","Strength","Flexibility","General");
        typeCb.setPromptText("Workout type");
        form.getChildren().addAll(new Label("Name:"), nameFld, new Label("Mood:"), moodCb, new Label("Type:"), typeCb);
        dialog.getDialogPane().setContent(form);
        dialog.setResultConverter(btn -> btn != saveBtn ? null :
            new Playlist(0, nameFld.getText().trim(), moodCb.getValue(), typeCb.getValue()));
        dialog.showAndWait().ifPresent(pl -> {
            if (pl != null && !pl.getName().isEmpty()) {
                musicSvc.savePlaylist(pl);
                playlistView.getItems().add(pl);
            }
        });
    }

    private Button ctrlBtn(String text) {
        Button b = new Button(text);
        b.getStyleClass().add("player-ctrl-btn");
        return b;
    }

    public Parent getRoot() { return root; }
}
