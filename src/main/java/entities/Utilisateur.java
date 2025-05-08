// src/main/java/entities/Utilisateur.java
package entities;

import java.time.LocalDate;

public class Utilisateur {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String roles;       // Obligatoire
    private String password;    // Obligatoire
    private int telephone;      // Obligatoire
    private String adresse;     // Obligatoire
    private LocalDate dateNaissance; // Obligatoire
    private String sexe;        // Obligatoire

    // Champs optionnels (facultatifs)
    private Double taille;
    private Integer poids;
    private String image;
    private Integer status;
    private String diplome;
    private String specialite;
    private String faceEncoding;

    public Utilisateur() {}

    public Utilisateur(String nom, String prenom, String email, String roles, String password, int telephone,
                       String adresse, LocalDate dateNaissance, String sexe) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.roles = roles;
        this.password = password;
        this.telephone = telephone;
        this.adresse = adresse;
        this.dateNaissance = dateNaissance;
        this.sexe = sexe;
    }

    // Getters et setters pour les champs obligatoires
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRoles() { return roles; }
    public void setRoles(String roles) { this.roles = roles; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public int getTelephone() { return telephone; }
    public void setTelephone(int telephone) { this.telephone = telephone; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }
    public String getSexe() { return sexe; }
    public void setSexe(String sexe) { this.sexe = sexe; }

    // Getters et setters pour les champs optionnels (exemples)
    public Double getTaille() { return taille; }
    public void setTaille(Double taille) { this.taille = taille; }
    public Integer getPoids() { return poids; }
    public void setPoids(Integer poids) { this.poids = poids; }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", roles='" + roles + '\'' +
                ", telephone=" + telephone +
                ", adresse='" + adresse + '\'' +
                ", dateNaissance=" + dateNaissance +
                ", sexe='" + sexe + '\'' +
                '}';
    }
}