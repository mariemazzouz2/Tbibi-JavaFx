package org.example.Entities;

/**
 * Entity class representing a Product
 */
public class Produit {
    private int id;
    private float prix;
    private String type;
    private String image;
    private String nom;
    private String description;
    private Commande commande;

    /**
     * Default constructor
     */
    public Produit() {
    }

    /**
     * Parameterized constructor with all fields
     */
    public Produit(int id, float prix, String type, String image, String nom, String description, Commande commande) {
        this.id = id;
        this.prix = prix;
        this.type = type;
        this.image = image;
        this.nom = nom;
        this.description = description;
        this.commande = commande;
    }

    /**
     * Constructor without ID for new products
     */
    public Produit(float prix, String type, String image, String nom, String description, Commande commande) {
        this.prix = prix;
        this.type = type;
        this.image = image;
        this.nom = nom;
        this.description = description;
        this.commande = commande;
    }

    /**
     * Constructor without Commande for products not yet added to an order
     */
    public Produit(float prix, String type, String image, String nom, String description) {
        this.prix = prix;
        this.type = type;
        this.image = image;
        this.nom = nom;
        this.description = description;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getPrix() {
        return prix;
    }

    public void setPrix(float prix) {
        this.prix = prix;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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

    public Commande getCommande() {
        return commande;
    }

    public void setCommande(Commande commande) {
        this.commande = commande;
    }

    @Override
    public String toString() {
        return nom != null ? nom : "Produit #" + id;
    }
}