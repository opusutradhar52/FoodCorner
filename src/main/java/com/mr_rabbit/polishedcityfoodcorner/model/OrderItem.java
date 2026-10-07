package com.mr_rabbit.polishedcityfoodcorner.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;


public class OrderItem {

    private final SimpleStringProperty productId;
    private final SimpleStringProperty productName;
    private final SimpleIntegerProperty quantity;
    private final SimpleDoubleProperty unitPrice;

    public OrderItem(String productId, String productName, int quantity, double unitPrice) {
        this.productId = new SimpleStringProperty(productId);
        this.productName = new SimpleStringProperty(productName);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.unitPrice = new SimpleDoubleProperty(unitPrice);
    }

    public String getProductId() { return productId.get(); }
    public SimpleStringProperty productIdProperty() { return productId; }

    public String getProductName() { return productName.get(); }
    public SimpleStringProperty productNameProperty() { return productName; }

    public int getQuantity() { return quantity.get(); }
    public void setQuantity(int value) { quantity.set(value); }
    public SimpleIntegerProperty quantityProperty() { return quantity; }

    public double getUnitPrice() { return unitPrice.get(); }
    public SimpleDoubleProperty unitPriceProperty() { return unitPrice; }

    /** Line total shown in the Price column (unitPrice * quantity). */
    public double getLineTotal() { return unitPrice.get() * quantity.get(); }
}
