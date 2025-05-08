// src/main/java/entities/DossierMedical.java
package entities;

import java.time.LocalDate;

public class DossierMedical {
    private int id;
    private int utilisateurId;
    private LocalDate date;
    private String fichier;
    private String unite;
    private double mesure;

    public DossierMedical() {}

    public DossierMedical(int id, int utilisateurId, LocalDate date, String fichier, String unite, double mesure) {
        this.id = id;
        this.utilisateurId = utilisateurId;
        this.date = date;
        this.fichier = fichier;
        this.unite = unite;
        this.mesure = mesure;
    }

    public DossierMedical(int utilisateurId, LocalDate date, String fichier, String unite, double mesure) {
        this.utilisateurId = utilisateurId;
        this.date = date;
        this.fichier = fichier;
        this.unite = unite;
        this.mesure = mesure;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(int utilisateurId) { this.utilisateurId = utilisateurId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getFichier() { return fichier; }
    public void setFichier(String fichier) { this.fichier = fichier; }
    public String getUnite() { return unite; }
    public void setUnite(String unite) { this.unite = unite; }
    public double getMesure() { return mesure; }
    public void setMesure(double mesure) { this.mesure = mesure; }

    @Override
    public String toString() {
        return "DossierMedical{" +
                "id=" + id +
                ", utilisateurId=" + utilisateurId +
                ", date=" + date +
                ", fichier='" + fichier + '\'' +
                ", unite='" + unite + '\'' +
                ", mesure=" + mesure +
                '}';
    }
}