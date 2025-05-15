package org.example.Entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity class representing an Order
 */
public class Commande {
    private int id;
    private float montantTotal;
    private LocalDate dateCommande;
    private String statut;
    private List<Produit> produits;
    private Utilisateur user;

    /**
     * Default constructor
     */
    public Commande() {
        this.produits = new ArrayList<>();
        this.montantTotal = 0;
    }

    /**
     * Parameterized constructor with all fields
     */
    public Commande(int id, float montantTotal, LocalDate dateCommande, String statut, Utilisateur user) {
        this.id = id;
        this.montantTotal = montantTotal;
        this.dateCommande = dateCommande;
        this.statut = statut;
        this.user = user;
        this.produits = new ArrayList<>();
    }

    /**
     * Constructor without ID for new orders
     */
    public Commande(float montantTotal, LocalDate dateCommande, String statut, Utilisateur user) {
        this.montantTotal = montantTotal;
        this.dateCommande = dateCommande;
        this.statut = statut;
        this.user = user;
        this.produits = new ArrayList<>();
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(float montantTotal) {
        this.montantTotal = montantTotal;
    }

    public LocalDate getDateCommande() {
        return dateCommande;
    }

    public void setDateCommande(LocalDate dateCommande) {
        this.dateCommande = dateCommande;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public List<Produit> getProduits() {
        return produits;
    }

    public void setProduits(List<Produit> produits) {
        this.produits = produits;
    }

    public Utilisateur getUser() {
        return user;
    }

    public void setUser(Utilisateur user) {
        this.user = user;
    }

    /**
     * Add a product to the order
     * @param produit Product to add
     */
    public void addProduit(Produit produit) {
        if (!this.produits.contains(produit)) {
            this.produits.add(produit);
            produit.setCommande(this);
        }
    }

    /**
     * Remove a product from the order
     * @param produit Product to remove
     */
    public void removeProduit(Produit produit) {
        if (this.produits.remove(produit)) {
            if (produit.getCommande() == this) {
                produit.setCommande(null);
            }
        }
    }

    /**
     * Calculate the total amount of the order
     */
    public void calculerMontantTotal() {
        float total = 0;
        for (Produit produit : this.produits) {
            total += produit.getPrix();
        }
        this.montantTotal = total;
    }
}