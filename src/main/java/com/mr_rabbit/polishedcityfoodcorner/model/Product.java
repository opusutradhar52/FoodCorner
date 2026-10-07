package com.mr_rabbit.polishedcityfoodcorner.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;


public class Product {

    private final SimpleStringProperty productId;
    private final SimpleStringProperty productName;
    private final SimpleStringProperty type;
    private final SimpleIntegerProperty stock;
    private final SimpleDoubleProperty price;
    private final SimpleStringProperty status;
    private final SimpleStringProperty date;
    private final SimpleStringProperty imagePath;

    public Product(String productId, String productName, String type, int stock, double price,
                    String status, String date, String imagePath) {
        this.productId = new SimpleStringProperty(productId);
        this.productName = new SimpleStringProperty(productName);
        this.type = new SimpleStringProperty(type);
        this.stock = new SimpleIntegerProperty(stock);
        this.price = new SimpleDoubleProperty(price);
        this.status = new SimpleStringProperty(status);
        this.date = new SimpleStringProperty(date);
        this.imagePath = new SimpleStringProperty(imagePath);
    }

    public String getProductId() { return productId.get(); }
    public SimpleStringProperty productIdProperty() { return productId; }

    public String getProductName() { return productName.get(); }
    public SimpleStringProperty productNameProperty() { return productName; }

    public String getType() { return type.get(); }
    public SimpleStringProperty typeProperty() { return type; }

    public int getStock() { return stock.get(); }
    public SimpleIntegerProperty stockProperty() { return stock; }

    public double getPrice() { return price.get(); }
    public SimpleDoubleProperty priceProperty() { return price; }

    public String getStatus() { return status.get(); }
    public SimpleStringProperty statusProperty() { return status; }

    public String getDate() { return date.get(); }
    public SimpleStringProperty dateProperty() { return date; }

    public String getImagePath() { return imagePath.get(); }
    public SimpleStringProperty imagePathProperty() { return imagePath; }
}
