package org.example.Controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.Entities.CartItem;
import org.example.Entities.Produit;
import org.example.Services.CartItemService;
import org.example.Services.ProduitService;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Controller for the front office store view
 */
public class FrontOfficeController implements Initializable {

    @FXML
    private FlowPane productCardsContainer;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private ComboBox<String> priceRangeComboBox;

    @FXML
    private Label cartItemCountLabel;

    @FXML
    private Label dateTimeLabel;

    @FXML
    private Label userLabel;

    private ProduitService produitService;
    private CartItemService cartItemService;

    private List<Produit> allProducts;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        produitService = new ProduitService();
        cartItemService = new CartItemService(); // This will manage cart items in memory

        // Initialize datetime display
        initClock();

        // Set current user
        userLabel.setText("Malak");

        // Set up search field listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchProducts(newValue);
        });

        // Set up category filter
        categoryComboBox.getItems().add("All");
        // We'll populate this with real categories from the product database

        // Set up price range filter
        priceRangeComboBox.getItems().addAll(
                "All",
                "Under €50",
                "€50 - €100",
                "€100 - €200",
                "Over €200"
        );

        // Set default values
        categoryComboBox.setValue("All");
        priceRangeComboBox.setValue("All");

        // Load products with animation
        loadProducts();

        // Update cart count
        updateCartCount();

        // Add animation to cart count label
        setupCartCountAnimation();
    }

    /**
     * Initialize the clock display
     */
    private void initClock() {
        // Initialize with current time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        dateTimeLabel.setText(LocalDateTime.now().format(formatter));

        // Update every minute
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(60), e -> {
            dateTimeLabel.setText(LocalDateTime.now().format(formatter));
        }));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    /**
     * Setup animation for cart count label
     */
    private void setupCartCountAnimation() {
        // Make the cart count fade in/out when updated
        cartItemCountLabel.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), cartItemCountLabel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        // Set up a periodic pulse effect for the cart icon when there are items
        Timeline pulsate = new Timeline(
                new KeyFrame(Duration.seconds(0),
                        new KeyValue(cartItemCountLabel.scaleXProperty(), 1),
                        new KeyValue(cartItemCountLabel.scaleYProperty(), 1)
                ),
                new KeyFrame(Duration.seconds(1.5),
                        new KeyValue(cartItemCountLabel.scaleXProperty(), 1.2),
                        new KeyValue(cartItemCountLabel.scaleYProperty(), 1.2)
                ),
                new KeyFrame(Duration.seconds(3),
                        new KeyValue(cartItemCountLabel.scaleXProperty(), 1),
                        new KeyValue(cartItemCountLabel.scaleYProperty(), 1)
                )
        );
        pulsate.setCycleCount(Timeline.INDEFINITE);

        // Only start pulsing if cart has items
        if (cartItemService.countTotalQuantity() > 0) {
            pulsate.play();
        }
    }

    /**
     * Load all products from database
     */
    private void loadProducts() {
        allProducts = produitService.getAllProduits();

        // Extract unique categories for the filter
        Set<String> categories = allProducts.stream()
                .map(Produit::getType)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        categoryComboBox.getItems().addAll(categories);

        // Display products with animation
        displayProducts(allProducts);
    }

    /**
     * Display products in card view
     * @param products List of products to display
     */
    private void displayProducts(List<Produit> products) {
        productCardsContainer.getChildren().clear();

        // Create a fade transition for the product container
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), productCardsContainer);
        fadeIn.setFromValue(0.3);
        fadeIn.setToValue(1.0);

        // Add products with staggered animation
        for (int i = 0; i < products.size(); i++) {
            final int index = i;
            // Add with slight delay for each card for staggered effect
            Platform.runLater(() -> {
                if (index < products.size()) {
                    VBox productCard = createProductCard(products.get(index));
                    productCard.setOpacity(0);
                    productCardsContainer.getChildren().add(productCard);

                    FadeTransition cardFade = new FadeTransition(Duration.millis(200), productCard);
                    cardFade.setDelay(Duration.millis(50 * index));
                    cardFade.setFromValue(0);
                    cardFade.setToValue(1);
                    cardFade.play();
                }
            });
        }

        fadeIn.play();
    }

    /**
     * Create a product card view
     * @param product The product to display
     * @return VBox containing the product card
     */
    private VBox createProductCard(Produit product) {
        // Main card container
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card");
        card.setPrefSize(220, 320);

        // Product image container
        StackPane imageContainer = new StackPane();
        imageContainer.getStyleClass().add("product-image-container");
        imageContainer.setMinHeight(150);

        // Product image
        ImageView productImage = new ImageView();
        productImage.getStyleClass().add("product-image");

        // Try to load image if available
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            try {
                File imageFile = new File(product.getImage());
                if (imageFile.exists()) {
                    String imageUrl = imageFile.toURI().toURL().toString();
                    Image image = new Image(imageUrl, 180, 130, true, true, true);
                    productImage.setImage(image);
                } else {
                    // Use placeholder image
                    productImage.setImage(new Image(getClass().getResourceAsStream("/images/placeholder.png")));
                }
            } catch (Exception e) {
                // Use placeholder image
                productImage.setImage(new Image(getClass().getResourceAsStream("/images/placeholder.png")));
            }
        } else {
            // Use placeholder image
            productImage.setImage(new Image(getClass().getResourceAsStream("/images/placeholder.png")));
        }

        imageContainer.getChildren().add(productImage);

        // Product title
        Label titleLabel = new Label(product.getNom());
        titleLabel.getStyleClass().add("product-title");
        titleLabel.setWrapText(true);

        // Product type/category
        Label typeLabel = new Label(product.getType());
        typeLabel.getStyleClass().add("product-type");

        // Product price
        Label priceLabel = new Label(String.format("€%.2f", product.getPrix()));
        priceLabel.getStyleClass().add("product-price");

        // Product description (truncated)
        Text descriptionText = new Text(product.getDescription());
        descriptionText.getStyleClass().add("product-description");
        descriptionText.setWrappingWidth(190);

        // Create HBox for price and add-to-cart button
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        Button addToCartBtn = new Button("Add to Cart");
        addToCartBtn.getStyleClass().add("add-to-cart-btn");

        // Add to cart button action
        addToCartBtn.setOnAction(e -> {
            cartItemService.addCartItem(product, 1);
            updateCartCount();

            // Animate button to confirm addition
            ScaleTransition pulse = new ScaleTransition(Duration.millis(200), addToCartBtn);
            pulse.setFromX(1.0);
            pulse.setFromY(1.0);
            pulse.setToX(0.8);
            pulse.setToY(0.8);
            pulse.setCycleCount(2);
            pulse.setAutoReverse(true);
            pulse.play();

            // Show tooltip notification
            Tooltip tooltip = new Tooltip("Added to cart!");
            tooltip.setStyle("-fx-font-size: 14px; -fx-background-color: #2ecc71; -fx-text-fill: white;");
            tooltip.setShowDelay(Duration.ZERO);
            tooltip.show(addToCartBtn,
                    addToCartBtn.localToScreen(addToCartBtn.getBoundsInLocal()).getMinX(),
                    addToCartBtn.localToScreen(addToCartBtn.getBoundsInLocal()).getMinY() - 30);

            // Hide tooltip after a delay
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1.5), evt -> tooltip.hide()));
            timeline.play();

            // Make cart count badge pop
            playCartAnimation();
        });

        // Create HBox with price on left and button on right
        HBox priceCartBox = new HBox();
        priceCartBox.setAlignment(Pos.CENTER);
        priceCartBox.setSpacing(10);
        priceCartBox.getChildren().addAll(priceLabel, addToCartBtn);
        HBox.setHgrow(priceLabel, Priority.ALWAYS);

        // Add all elements to card
        card.getChildren().addAll(
                imageContainer,
                titleLabel,
                typeLabel,
                descriptionText,
                priceCartBox
        );

        // Add some padding inside the card
        card.setPadding(new Insets(10));

        return card;
    }

    /**
     * Play animation on the cart count badge when products are added
     */
    private void playCartAnimation() {
        // Scale animation for cart count
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), cartItemCountLabel);
        scaleUp.setFromX(1.0);
        scaleUp.setFromY(1.0);
        scaleUp.setToX(1.5);
        scaleUp.setToY(1.5);
        scaleUp.setCycleCount(1);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), cartItemCountLabel);
        scaleDown.setFromX(1.5);
        scaleDown.setFromY(1.5);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);
        scaleDown.setCycleCount(1);

        // Create sequence of animations
        SequentialTransition sequence = new SequentialTransition(scaleUp, scaleDown);
        sequence.play();
    }

    /**
     * Search products by name or description
     * @param query Search query
     */
    private void searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            // If query is empty, display all products (respecting current filters)
            applyFilters();
            return;
        }

        // Filter products by name or description containing the query
        String lowerCaseQuery = query.toLowerCase().trim();
        List<Produit> filteredProducts = allProducts.stream()
                .filter(p ->
                        (p.getNom() != null && p.getNom().toLowerCase().contains(lowerCaseQuery)) ||
                                (p.getDescription() != null && p.getDescription().toLowerCase().contains(lowerCaseQuery)))
                .collect(Collectors.toList());

        // Apply additional filters
        filteredProducts = applyPriceFilter(filteredProducts, priceRangeComboBox.getValue());
        filteredProducts = applyCategoryFilter(filteredProducts, categoryComboBox.getValue());

        // Display filtered products
        displayProducts(filteredProducts);
    }

    /**
     * Apply selected filters
     */
    @FXML
    private void handleApplyFilters() {
        applyFilters();
    }

    /**
     * Reset all filters
     */
    @FXML
    private void handleResetFilters() {
        searchField.clear();
        categoryComboBox.setValue("All");
        priceRangeComboBox.setValue("All");
        displayProducts(allProducts);
    }

    /**
     * Apply current filters to product list
     */
    private void applyFilters() {
        String query = searchField.getText();
        String category = categoryComboBox.getValue();
        String priceRange = priceRangeComboBox.getValue();

        List<Produit> filteredProducts = new ArrayList<>(allProducts);

        // Apply search query filter
        if (query != null && !query.trim().isEmpty()) {
            String lowerCaseQuery = query.toLowerCase().trim();
            filteredProducts = filteredProducts.stream()
                    .filter(p ->
                            (p.getNom() != null && p.getNom().toLowerCase().contains(lowerCaseQuery)) ||
                                    (p.getDescription() != null && p.getDescription().toLowerCase().contains(lowerCaseQuery)))
                    .collect(Collectors.toList());
        }

        // Apply category filter
        filteredProducts = applyCategoryFilter(filteredProducts, category);

        // Apply price range filter
        filteredProducts = applyPriceFilter(filteredProducts, priceRange);

        // Display filtered products
        displayProducts(filteredProducts);
    }

    /**
     * Apply category filter
     * @param products List of products to filter
     * @param category Selected category
     * @return Filtered list of products
     */
    private List<Produit> applyCategoryFilter(List<Produit> products, String category) {
        if (category == null || category.equals("All")) {
            return products;
        }

        return products.stream()
                .filter(p -> p.getType() != null && p.getType().equals(category))
                .collect(Collectors.toList());
    }

    /**
     * Apply price range filter
     * @param products List of products to filter
     * @param priceRange Selected price range
     * @return Filtered list of products
     */
    private List<Produit> applyPriceFilter(List<Produit> products, String priceRange) {
        if (priceRange == null || priceRange.equals("All")) {
            return products;
        }

        switch (priceRange) {
            case "Under €50":
                return products.stream()
                        .filter(p -> p.getPrix() < 50)
                        .collect(Collectors.toList());
            case "€50 - €100":
                return products.stream()
                        .filter(p -> p.getPrix() >= 50 && p.getPrix() <= 100)
                        .collect(Collectors.toList());
            case "€100 - €200":
                return products.stream()
                        .filter(p -> p.getPrix() > 100 && p.getPrix() <= 200)
                        .collect(Collectors.toList());
            case "Over €200":
                return products.stream()
                        .filter(p -> p.getPrix() > 200)
                        .collect(Collectors.toList());
            default:
                return products;
        }
    }

    /**
     * Update the cart item count label
     */
    private void updateCartCount() {
        int count = cartItemService.countTotalQuantity();
        cartItemCountLabel.setText(String.valueOf(count));

        // Change visibility of the count badge based on quantity
        if (count > 0) {
            cartItemCountLabel.setVisible(true);
            // Add a pop animation
            ScaleTransition popTransition = new ScaleTransition(Duration.millis(200), cartItemCountLabel);
            popTransition.setFromX(0.5);
            popTransition.setFromY(0.5);
            popTransition.setToX(1.0);
            popTransition.setToY(1.0);
            popTransition.play();
        } else {
            cartItemCountLabel.setVisible(false);
        }
    }

    /**
     * Handle view cart button click
     */
    @FXML
    private void handleViewCart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOffice/CartView.fxml"));
            Parent root = loader.load();

            CartViewController controller = loader.getController();
            controller.setCartItemService(cartItemService);
            controller.setCartUpdatedCallback(this::updateCartCount);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Your Cart");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Update cart count after cart view is closed
            updateCartCount();
        } catch (Exception e) {
            Logger.getLogger(FrontOfficeController.class.getName()).log(Level.SEVERE, null, e);
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open cart view: " + e.getMessage());
        }
    }

    /**
     * Show an alert dialog
     * @param type Alert type
     * @param title Dialog title
     * @param message Dialog message
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}