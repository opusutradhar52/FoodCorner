package com.mr_rabbit.polishedcityfoodcorner.controller;

import com.mr_rabbit.polishedcityfoodcorner.model.Product;
import com.mr_rabbit.polishedcityfoodcorner.util.AlertHelper;
import com.mr_rabbit.polishedcityfoodcorner.util.ImageLoaderUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class FoodCardController {

    @FXML private ImageView foodImage;
    @FXML private Label foodName;
    @FXML private Label foodPrice;
    @FXML private Label countfoodlable;

    private Product product;
    private int count = 0;
    private SaleItemsController parentController;

    public void setProduct(Product product) {
        this.product = product;
        foodName.setText(product.getProductName());
        foodPrice.setText(String.format("%.0f", product.getPrice()));
        foodImage.setImage(ImageLoaderUtil.loadProductImage(product.getImagePath()));
        count = 0;
        countfoodlable.setText("0");
    }

    public void setParentController(SaleItemsController parentController) {
        this.parentController = parentController;
    }

    public Product getProduct() {
        return product;
    }

    @FXML
    private void increamentFoodCount() {
        if (count < product.getStock()) {
            count++;
            countfoodlable.setText(String.valueOf(count));
        }
    }

    @FXML
    private void decrementCount() {
        if (count > 0) {
            count--;
            countfoodlable.setText(String.valueOf(count));
        }
    }

    @FXML
    private void foodaddbtn() {
        if (count <= 0) {
            AlertHelper.warning("Select Quantity", "Use the + button to choose how many " + product.getProductName() + " to add first.");
            return;
        }
        if (parentController != null) {
            parentController.addItemToOrder(product, count);
        }
        count = 0;
        countfoodlable.setText("0");
    }
}

