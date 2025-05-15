package org.example.Services;

import org.example.Entities.CartItem;
import org.example.Entities.Produit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for managing CartItem objects
 * Note: Since CartItem is not a database entity, this service operates in memory
 */
public class CartItemService {
    private Map<Integer, CartItem> cartItems; // productId -> CartItem

    public CartItemService() {
        this.cartItems = new HashMap<>();
    }

    // 1️⃣ Add item to cart
    public void addCartItem(Produit produit, int quantity) {
        int produitId = produit.getId();

        if (cartItems.containsKey(produitId)) {
            // If product already in cart, update quantity
            CartItem item = cartItems.get(produitId);
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            // Otherwise, add new cart item
            cartItems.put(produitId, new CartItem(produit, quantity));
        }
    }

    // 2️⃣ Update item quantity
    public void updateCartItemQuantity(int produitId, int quantity) {
        if (cartItems.containsKey(produitId)) {
            if (quantity > 0) {
                cartItems.get(produitId).setQuantity(quantity);
            } else {
                removeCartItem(produitId);
            }
        }
    }

    // 3️⃣ Remove item from cart
    public void removeCartItem(int produitId) {
        cartItems.remove(produitId);
    }

    // 4️⃣ Get all items in cart
    public List<CartItem> getAllCartItems() {
        return new ArrayList<>(cartItems.values());
    }

    // 5️⃣ Get cart item by product ID
    public CartItem getCartItemByProduitId(int produitId) {
        return cartItems.get(produitId);
    }

    // 6️⃣ Clear the cart
    public void clearCart() {
        cartItems.clear();
    }

    // 7️⃣ Calculate total price of items in cart
    public float calculateCartTotal() {
        float total = 0;

        for (CartItem item : cartItems.values()) {
            total += item.getTotal();
        }

        return total;
    }

    // 8️⃣ Count items in cart
    public int countCartItems() {
        return cartItems.size();
    }

    // 9️⃣ Count total quantity of all items in cart
    public int countTotalQuantity() {
        int totalQuantity = 0;

        for (CartItem item : cartItems.values()) {
            totalQuantity += item.getQuantity();
        }

        return totalQuantity;
    }

    // 🔟 Check if cart contains a specific product
    public boolean containsProduit(int produitId) {
        return cartItems.containsKey(produitId);
    }
}