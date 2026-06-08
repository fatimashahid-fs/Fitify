package com.fitify.util;

import javafx.scene.Scene;

/**
 * ThemeManager - Singleton that switches between light and dark CSS.
 * Swaps stylesheets on the live Scene so every panel updates instantly.
 */
public class ThemeManager {

    private static ThemeManager instance;
    private Scene  scene;
    private boolean dark = false;

    private static final String LIGHT_CSS = "/styles/fitify.css";
    private static final String DARK_CSS  = "/styles/fitify-dark.css";

    private ThemeManager() {}

    public static ThemeManager getInstance() {
        if (instance == null) instance = new ThemeManager();
        return instance;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public boolean isDark() { return dark; }

    public void toggle() {
        dark = !dark;
        apply();
    }

    private void apply() {
        if (scene == null) return;
        scene.getStylesheets().clear();
        String path = dark ? DARK_CSS : LIGHT_CSS;
        String url  = getClass().getResource(path).toExternalForm();
        scene.getStylesheets().add(url);
    }
}
