package org.example.Entities;

/**
 * Class representing an item in a shopping cart
 */
public class CartItem {
    private Produit produit;
    private int quantity;

    /**
     * Parameterized constructor
     * @param produit Product in cart
     * @param quantity Quantity of the product
     */
    public CartItem(Produit produit, int quantity) {
        this.produit = produit;
        this.quantity = quantity;
    }

    // Getters and setters
    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public int getQuantity() {
        return quantity;
    }

    public CartItem setQuantity(int quantity) {
        this.quantity = quantity;
        return this;
    }

    /**
     * Calculate the total price for this item (price × quantity)
     * @return The total price
     */
    public float getTotal() {
        return produit.getPrix() * quantity;
    }
}