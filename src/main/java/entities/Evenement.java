package entities;

import java.time.LocalDate;

/**
 * Represents an event with details such as title, description, dates, location, and coordinates.
 */
public class Evenement {
    private Integer id;
    private String titre;
    private String description;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String lieu;
    private String statut;
    private String image;
    private int categorieId;
    private double latitude;  // Stored in DB
    private double longitude; // Stored in DB

    public Evenement() {
    }

    public Evenement(Evenement e) {
        this.id = e.id;
        this.titre = e.titre;
        this.description = e.description;
        this.dateDebut = e.dateDebut;
        this.dateFin = e.dateFin;
        this.lieu = e.lieu;
        this.statut = e.statut;
        this.image = e.image;
        this.categorieId = e.categorieId;
        this.latitude = e.latitude;
        this.longitude = e.longitude;
    }

    public Evenement(String titre, String description, LocalDate dateDebut, LocalDate dateFin, String lieu, String statut, String image) {
        this.titre = titre;
        this.description = description;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.lieu = lieu;
        this.statut = statut;
        this.image = image;
    }

    public Evenement(int id, String titre, String description, LocalDate dateDebut, LocalDate dateFin, String lieu, String statut, String image) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.lieu = lieu;
        this.statut = statut;
        this.image = image;
    }

    // Getters & Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getCategorieId() {
        return categorieId;
    }

    public void setCategorieId(int categorieId) {
        this.categorieId = categorieId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "Evenement{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", lieu='" + lieu + '\'' +
                ", statut='" + statut + '\'' +
                ", image='" + image + '\'' +
                ", categorieId=" + categorieId +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

    public String getTitle() {
        return titre;
    }

    public void setTitle(String titre) {
        this.titre = titre;
    }

    public String getLocation() {
        return lieu;
    }

    public void setLocation(String lieu) {
        this.lieu = lieu;
    }
}