package org.example.Entities;

/**
 * Entity class representing a User
 */
public class Utilisateur {
    private int id;
    private String name;
    private String email;

    /**
     * Default constructor
     */
    public Utilisateur() {
    }

    /**
     * Parameterized constructor
     * @param id User ID
     * @param name User name
     * @param email User email
     */
    public Utilisateur(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    /**
     * Constructor without ID for new users
     * @param name User name
     * @param email User email
     */
    public Utilisateur(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}