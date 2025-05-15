package entities;

public class CategorieEv {
    private int id;
    private String nom;
    private String description;

    // Constructeur par défaut
    public CategorieEv() {
    }

    // Constructeur sans id (pour l'insertion)
    public CategorieEv(String nom, String description) {
        this.nom = nom;
        this.description = description;
    }

    // Constructeur complet
    public CategorieEv(int id, String nom, String description) {
        this.id = id;
        this.nom = nom;
        this.description = description;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "CategorieEv{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}