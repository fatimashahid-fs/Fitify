package com.fitify.service;

import com.fitify.dao.UserDAO;
import com.fitify.model.User;
import com.fitify.util.PasswordUtil;

import java.util.Optional;

/**
 * AuthService - Handles user registration, login, and profile updates.
 * OOP Concept: ENCAPSULATION (hides DAO and crypto details from UI)
 */
public class AuthService {

    private final UserDAO userDAO = new UserDAO();
    private User currentUser;

    public Optional<User> register(String name, String email, String password,
                                   String goal, int age, double weight, double height) {
        if (userDAO.findByEmail(email).isPresent()) {
            System.out.println("Email already registered: " + email);
            return Optional.empty();
        }
        User u = new User(name, email, PasswordUtil.createHash(password),
                          goal, age, weight, height);
        return userDAO.save(u) ? Optional.of(u) : Optional.empty();
    }

    public Optional<User> login(String email, String password) {
        Optional<User> opt = userDAO.findByEmail(email);
        if (opt.isEmpty()) return Optional.empty();
        User u = opt.get();
        if (!PasswordUtil.verify(password, u.getPasswordHash())) return Optional.empty();
        currentUser = u;
        return Optional.of(u);
    }

    public void logout() { currentUser = null; }

    public boolean updateProfile(User updated) {
        boolean ok = userDAO.update(updated);
        if (ok) currentUser = updated;
        return ok;
    }

    public User    getCurrentUser() { return currentUser; }
    public boolean isLoggedIn()     { return currentUser != null; }
}
