package com.mr_rabbit.polishedcityfoodcorner.model;

import javafx.beans.property.SimpleStringProperty;


public class HistoryRecord {

    private final SimpleStringProperty saleId;
    private final SimpleStringProperty customerName;
    private final SimpleStringProperty customerMobile;
    private final SimpleStringProperty buyingItems;   // comma separated summary of items bought
    private final SimpleStringProperty taka;           // total amount, formatted
    private final SimpleStringProperty date;
    private final SimpleStringProperty sellerId;

    public HistoryRecord(String saleId, String customerName, String customerMobile, String buyingItems,
                          String taka, String date, String sellerId) {
        this.saleId = new SimpleStringProperty(saleId);
        this.customerName = new SimpleStringProperty(customerName);
        this.customerMobile = new SimpleStringProperty(customerMobile);
        this.buyingItems = new SimpleStringProperty(buyingItems);
        this.taka = new SimpleStringProperty(taka);
        this.date = new SimpleStringProperty(date);
        this.sellerId = new SimpleStringProperty(sellerId);
    }

    public String getSaleId() { return saleId.get(); }
    public SimpleStringProperty saleIdProperty() { return saleId; }

    public String getCustomerName() { return customerName.get(); }
    public SimpleStringProperty customerNameProperty() { return customerName; }

    public String getCustomerMobile() { return customerMobile.get(); }
    public SimpleStringProperty customerMobileProperty() { return customerMobile; }

    public String getBuyingItems() { return buyingItems.get(); }
    public SimpleStringProperty buyingItemsProperty() { return buyingItems; }

    public String getTaka() { return taka.get(); }
    public SimpleStringProperty takaProperty() { return taka; }

    public String getDate() { return date.get(); }
    public SimpleStringProperty dateProperty() { return date; }

    public String getSellerId() { return sellerId.get(); }
    public SimpleStringProperty sellerIdProperty() { return sellerId; }
}
