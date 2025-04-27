package utils;

import models.Utilisateur;

public class SessionManager {
    private static SessionManager instance;
    private Utilisateur currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(Utilisateur user) {
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public Utilisateur getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
