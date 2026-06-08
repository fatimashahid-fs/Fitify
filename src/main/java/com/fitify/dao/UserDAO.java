package com.fitify.dao;

import com.fitify.interfaces.Storable;
import com.fitify.model.User;
import com.fitify.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * UserDAO - Implements Storable<User> for MySQL persistence.
 * OOP Concept: ABSTRACTION (implements Storable interface)
 */
public class UserDAO implements Storable<User> {

    private Connection conn() { return DatabaseManager.getInstance().getConnection(); }

    @Override
    public boolean save(User u) {
        String sql = "INSERT INTO users(name,email,password_hash,fitness_goal,age,weight_kg,height_cm) VALUES(?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getName());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getPasswordHash());
            ps.setString(4, u.getFitnessGoal());
            ps.setInt   (5, u.getAge());
            ps.setDouble(6, u.getWeightKg());
            ps.setDouble(7, u.getHeightCm());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) u.setUserId(keys.getInt(1));
            return true;
        } catch (SQLException e) {
            System.err.println("UserDAO.save: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Optional<User> findById(int id) {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM users WHERE user_id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
        } catch (SQLException e) { System.err.println("UserDAO.findById: " + e.getMessage()); }
        return Optional.empty();
    }

    public Optional<User> findByEmail(String email) {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM users WHERE email=?")) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
        } catch (SQLException e) { System.err.println("UserDAO.findByEmail: " + e.getMessage()); }
        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        try (ResultSet rs = conn().createStatement().executeQuery("SELECT * FROM users")) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { System.err.println("UserDAO.findAll: " + e.getMessage()); }
        return list;
    }

    @Override
    public boolean update(User u) {
        String sql = "UPDATE users SET name=?,email=?,fitness_goal=?,age=?,weight_kg=?,height_cm=? WHERE user_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, u.getName());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getFitnessGoal());
            ps.setInt   (4, u.getAge());
            ps.setDouble(5, u.getWeightKg());
            ps.setDouble(6, u.getHeightCm());
            ps.setInt   (7, u.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("UserDAO.update: " + e.getMessage()); return false; }
    }

    @Override
    public boolean delete(int id) {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM users WHERE user_id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("UserDAO.delete: " + e.getMessage()); return false; }
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId      (rs.getInt   ("user_id"));
        u.setName        (rs.getString("name"));
        u.setEmail       (rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFitnessGoal (rs.getString("fitness_goal"));
        u.setAge         (rs.getInt   ("age"));
        u.setWeightKg    (rs.getDouble("weight_kg"));
        u.setHeightCm    (rs.getDouble("height_cm"));
        return u;
    }
}
