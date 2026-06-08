package com.fitify.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/**
 * DatabaseManager - Singleton managing the MySQL JDBC connection.
 * OOP Concept: ENCAPSULATION (Singleton pattern)
 *
 * Edit src/main/resources/db.properties to configure your MySQL credentials.
 */
public class DatabaseManager {

    private static DatabaseManager instance;
    private Connection connection;
    private String host, port, dbName, user, password;

    private DatabaseManager() { loadConfig(); }

    public static DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    private void loadConfig() {
        Properties props = new Properties();
        try (InputStream is = getClass().getResourceAsStream("/db.properties")) {
            if (is != null) props.load(is);
            else System.err.println("Warning: db.properties not found, using defaults.");
        } catch (IOException e) {
            System.err.println("Warning: " + e.getMessage());
        }
        host     = props.getProperty("db.host",     "localhost");
        port     = props.getProperty("db.port",     "3306");
        dbName   = props.getProperty("db.name",     "fitify");
        user     = props.getProperty("db.user",     "root");
        password = props.getProperty("db.password", "");
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName
                    + "?useSSL=false&allowPublicKeyRetrieval=true"
                    + "&serverTimezone=UTC&characterEncoding=UTF-8";
                connection = DriverManager.getConnection(url, user, password);
            }
        } catch (SQLException e) {
            System.err.println("DB connection error: " + e.getMessage());
        }
        return connection;
    }

    public void initializeDatabase() {
        try (Statement st = getConnection().createStatement()) {

            st.execute("CREATE TABLE IF NOT EXISTS users (" +
                "user_id INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(120) NOT NULL," +
                "email VARCHAR(255) NOT NULL UNIQUE," +
                "password_hash VARCHAR(255) NOT NULL," +
                "fitness_goal VARCHAR(80)," +
                "age INT," +
                "weight_kg DOUBLE," +
                "height_cm DOUBLE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            st.execute("CREATE TABLE IF NOT EXISTS exercises (" +
                "exercise_id INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(120) NOT NULL," +
                "description TEXT," +
                "duration_minutes INT," +
                "type VARCHAR(20) NOT NULL," +
                "muscle_group VARCHAR(80)," +
                "difficulty VARCHAR(30)," +
                "extra_data TEXT" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            st.execute("CREATE TABLE IF NOT EXISTS routines (" +
                "routine_id INT AUTO_INCREMENT PRIMARY KEY," +
                "user_id INT NOT NULL," +
                "name VARCHAR(120) NOT NULL," +
                "description TEXT," +
                "date_created DATE," +
                "playlist_name VARCHAR(120)," +
                "CONSTRAINT fk_routine_user FOREIGN KEY (user_id)" +
                "  REFERENCES users(user_id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            st.execute("CREATE TABLE IF NOT EXISTS routine_exercises (" +
                "routine_id INT NOT NULL," +
                "exercise_id INT NOT NULL," +
                "sort_order INT DEFAULT 0," +
                "PRIMARY KEY (routine_id, exercise_id)," +
                "CONSTRAINT fk_re_routine  FOREIGN KEY (routine_id)" +
                "  REFERENCES routines(routine_id)   ON DELETE CASCADE," +
                "CONSTRAINT fk_re_exercise FOREIGN KEY (exercise_id)" +
                "  REFERENCES exercises(exercise_id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            st.execute("CREATE TABLE IF NOT EXISTS sessions (" +
                "session_id INT AUTO_INCREMENT PRIMARY KEY," +
                "user_id INT," +
                "routine_id INT," +
                "start_time DATETIME," +
                "end_time DATETIME," +
                "duration_seconds INT," +
                "calories_burned DOUBLE," +
                "notes TEXT," +
                "CONSTRAINT fk_sess_user    FOREIGN KEY (user_id)" +
                "  REFERENCES users(user_id)       ON DELETE SET NULL," +
                "CONSTRAINT fk_sess_routine FOREIGN KEY (routine_id)" +
                "  REFERENCES routines(routine_id) ON DELETE SET NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            st.execute("CREATE TABLE IF NOT EXISTS tracks (" +
                "track_id INT AUTO_INCREMENT PRIMARY KEY," +
                "user_id INT," +
                "title VARCHAR(200) NOT NULL," +
                "artist VARCHAR(120)," +
                "album VARCHAR(120)," +
                "file_path TEXT," +
                "duration_sec INT," +
                "bpm INT," +
                "genre VARCHAR(60)," +
                "CONSTRAINT fk_track_user FOREIGN KEY (user_id)" +
                "  REFERENCES users(user_id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            st.execute("CREATE TABLE IF NOT EXISTS playlists (" +
                "playlist_id INT AUTO_INCREMENT PRIMARY KEY," +
                "user_id INT," +
                "name VARCHAR(120) NOT NULL," +
                "mood_tag VARCHAR(60)," +
                "workout_type VARCHAR(60)," +
                "CONSTRAINT fk_pl_user FOREIGN KEY (user_id)" +
                "  REFERENCES users(user_id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            st.execute("CREATE TABLE IF NOT EXISTS playlist_tracks (" +
                "playlist_id INT NOT NULL," +
                "track_id INT NOT NULL," +
                "sort_order INT DEFAULT 0," +
                "PRIMARY KEY (playlist_id, track_id)," +
                "CONSTRAINT fk_pt_playlist FOREIGN KEY (playlist_id)" +
                "  REFERENCES playlists(playlist_id) ON DELETE CASCADE," +
                "CONSTRAINT fk_pt_track FOREIGN KEY (track_id)" +
                "  REFERENCES tracks(track_id)       ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            // Migration: add user_id to tracks if it doesn't exist yet (for existing databases)
            try {
                st.execute(
                    "ALTER TABLE tracks ADD COLUMN user_id INT NULL, " +
                    "ADD CONSTRAINT fk_track_user FOREIGN KEY (user_id) " +
                    "  REFERENCES users(user_id) ON DELETE CASCADE"
                );
                System.out.println("Migration: added user_id column to tracks.");
            } catch (SQLException ignored) {
                // Column already exists — safe to ignore
            }

            System.out.println("Fitify MySQL schema initialised.");
            seedDefaultExercises(st);

        } catch (SQLException e) {
            System.err.println("Schema init error: " + e.getMessage());
        }
    }

    private void seedDefaultExercises(Statement st) throws SQLException {
        ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM exercises");
        rs.next();
        if (rs.getInt(1) > 0) return;

        String[][] seeds = {
            {"Running",            "Outdoor or treadmill run",    "30","CARDIO",     "Full Body",   "Intermediate","speed=8.0,dist=4.0,hr=150"},
            {"Cycling",            "Stationary or outdoor bike",  "45","CARDIO",     "Legs",        "Beginner",    "speed=20.0,dist=15.0,hr=130"},
            {"Jump Rope",          "High-intensity skipping",     "20","CARDIO",     "Full Body",   "Intermediate","speed=0.0,dist=0.0,hr=160"},
            {"Walking",            "Brisk walking pace",          "40","CARDIO",     "Legs",        "Beginner",    "speed=5.5,dist=3.5,hr=100"},
            {"Bench Press",        "Barbell flat bench press",    "20","STRENGTH",   "Chest",       "Intermediate","sets=4,reps=8,weight=60,equip=Barbell,compound=true"},
            {"Squat",              "Barbell back squat",          "25","STRENGTH",   "Quads/Glutes","Advanced",    "sets=5,reps=5,weight=80,equip=Barbell,compound=true"},
            {"Deadlift",           "Conventional deadlift",       "25","STRENGTH",   "Full Body",   "Advanced",    "sets=4,reps=5,weight=100,equip=Barbell,compound=true"},
            {"Dumbbell Curl",      "Alternating bicep curl",      "15","STRENGTH",   "Biceps",      "Beginner",    "sets=3,reps=12,weight=15,equip=Dumbbells,compound=false"},
            {"Pull-up",            "Bodyweight pull-up",          "15","STRENGTH",   "Back",        "Intermediate","sets=3,reps=8,weight=0,equip=Bodyweight,compound=true"},
            {"Push-up",            "Standard push-up",            "15","STRENGTH",   "Chest",       "Beginner",    "sets=3,reps=15,weight=0,equip=Bodyweight,compound=true"},
            {"Yoga Sun Salutation","Full-body flow sequence",     "20","FLEXIBILITY", "Full Body",  "Beginner",    "hold=5,reps=5,stretch=Dynamic"},
            {"Hamstring Stretch",  "Seated hamstring hold",       "10","FLEXIBILITY", "Hamstrings", "Beginner",    "hold=30,reps=3,stretch=Static"},
            {"Hip Flexor Stretch", "Lunge hip flexor hold",       "10","FLEXIBILITY", "Hip Flexors","Beginner",    "hold=30,reps=3,stretch=Static"},
        };

        String sql = "INSERT INTO exercises(name,description,duration_minutes,type," +
                     "muscle_group,difficulty,extra_data) VALUES(?,?,?,?,?,?,?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            for (String[] row : seeds) {
                ps.setString(1, row[0]); ps.setString(2, row[1]);
                ps.setInt   (3, Integer.parseInt(row[2]));
                ps.setString(4, row[3]); ps.setString(5, row[4]);
                ps.setString(6, row[5]); ps.setString(7, row[6]);
                ps.addBatch();
            }
            ps.executeBatch();
            System.out.println("Default exercise library seeded.");
        }
    }

    public void closeConnection() {
        try { if (connection != null && !connection.isClosed()) connection.close(); }
        catch (SQLException e) { System.err.println("Close error: " + e.getMessage()); }
    }
}
