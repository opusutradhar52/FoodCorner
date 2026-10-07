package com.mr_rabbit.polishedcityfoodcorner.controller;

import com.mr_rabbit.polishedcityfoodcorner.dao.ProductDAO;
import com.mr_rabbit.polishedcityfoodcorner.dao.SaleDAO;
import com.mr_rabbit.polishedcityfoodcorner.dao.ShiftDAO;
import com.mr_rabbit.polishedcityfoodcorner.model.OrderItem;
import com.mr_rabbit.polishedcityfoodcorner.model.Product;
import com.mr_rabbit.polishedcityfoodcorner.util.AlertHelper;
import com.mr_rabbit.polishedcityfoodcorner.util.SceneSwitcher;
import com.mr_rabbit.polishedcityfoodcorner.util.SessionManager;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SaleItemsController {

    @FXML private TableView<OrderItem> ordertablevVew;
    @FXML private TableColumn<OrderItem, String> orderdedproductname;
    @FXML private TableColumn<OrderItem, Number> orderdproductquantity;
    @FXML private TableColumn<OrderItem, Number> orderdprofuctprice;

    @FXML private Label totaltaka;
    @FXML private TextField ammounttaken;
    @FXML private Label changableammount;

    @FXML private TextField customernamefiled;
    @FXML private TextField customermobilenumber;

    @FXML private GridPane menuGrid;
    @FXML private TextField menusearchfield;
    @FXML private Button menusearchbtn;

    private final ProductDAO productDAO = new ProductDAO();
    private final SaleDAO saleDAO = new SaleDAO();
    private final ShiftDAO shiftDAO = new ShiftDAO();

    private final ObservableList<OrderItem> orderItems = FXCollections.observableArrayList();
    private static final int GRID_COLUMNS = 3;

    // Snapshot of the most recently completed sale, shown by the Receipt button.
    private ReceiptData lastReceipt = null;

    @FXML
    private void initialize() {
        orderdedproductname.setCellValueFactory(data -> data.getValue().productNameProperty());
        orderdproductquantity.setCellValueFactory(data -> data.getValue().quantityProperty());
        orderdprofuctprice.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getLineTotal()));

        ordertablevVew.setItems(orderItems);

        menusearchfield.textProperty().addListener((obs, oldVal, newVal) -> {
            String query = newVal == null ? "" : newVal.trim();
            menusearchbtn.setText(query.isEmpty() ? "Search" : "Refresh");
            loadMenuGrid(query);
        });

        ammounttaken.textProperty().addListener((obs, oldVal, newVal) -> recalcChange());

        loadMenuGrid("");
    }

    private void loadMenuGrid(String query) {
        menuGrid.getChildren().clear();
        List<Product> products = productDAO.getSellableProducts();
        String lowerQuery = query.toLowerCase();

        int row = 0;
        int col = 0;
        for (Product product : products) {
            if (!lowerQuery.isEmpty() && !product.getProductName().toLowerCase().contains(lowerQuery)) {
                continue;
            }
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/com/mr_rabbit/polishedcityfoodcorner/FoodCard.fxml"));
                javafx.scene.Parent card = loader.load();
                FoodCardController controller = loader.getController();
                controller.setProduct(product);
                controller.setParentController(this);

                menuGrid.add(card, col, row);
                col++;
                if (col == GRID_COLUMNS) {
                    col = 0;
                    row++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

        public void addItemToOrder(Product product, int quantity) {
        Optional<OrderItem> existing = orderItems.stream()
                .filter(item -> item.getProductId().equals(product.getProductId()))
                .findFirst();

        if (existing.isPresent()) {
            OrderItem item = existing.get();
            int combined = item.getQuantity() + quantity;
            if (combined > product.getStock()) {
                combined = product.getStock();
                AlertHelper.warning("Stock Limit", "Only " + product.getStock() + " " + product.getProductName() + " available - quantity capped.");
            }
            item.setQuantity(combined);
            ordertablevVew.refresh();
        } else {
            int qty = Math.min(quantity, product.getStock());
            orderItems.add(new OrderItem(product.getProductId(), product.getProductName(), qty, product.getPrice()));
        }
        recalcTotal();
    }

    private void recalcTotal() {
        double total = orderItems.stream().mapToDouble(OrderItem::getLineTotal).sum();
        totaltaka.setText(String.format("%.2f", total));
        recalcChange();
    }

    private void recalcChange() {
        double total = parseDoubleOrZero(totaltaka.getText());
        Double paid = parseDoubleOrNull(ammounttaken.getText());
        double change = (paid == null) ? 0 : paid - total;
        changableammount.setText(String.format("%.2f", change));
    }

    @FXML
    private void searchIt(ActionEvent event) {
        menusearchfield.clear();
    }

    @FXML
    private void removeitembtn() {
        OrderItem selected = ordertablevVew.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.warning("No Item Selected", "Please select an item in the current order to remove.");
            return;
        }
        orderItems.remove(selected);
        recalcTotal();
    }

    @FXML
    private void paybtn() {
        String customerName = textOf(customernamefiled);
        String customerMobile = textOf(customermobilenumber);

        if (customerName.isEmpty() || customerMobile.isEmpty()) {
            AlertHelper.warning("Missing Information", "Must take customer name and mobile number.");
            return;
        }
        if (orderItems.isEmpty()) {
            AlertHelper.warning("Empty Order", "Please add at least one item before taking payment.");
            return;
        }

        double total = parseDoubleOrZero(totaltaka.getText());
        Double amountPaid = parseDoubleOrNull(ammounttaken.getText());
        if (amountPaid == null || amountPaid < total) {
            AlertHelper.warning("Insufficient Amount", "The amount taken must be a number that covers the total.");
            return;
        }
        double change = amountPaid - total;

        String sellerId = SessionManager.getEmployeeCode();
        String sellerName = SessionManager.getEmployeeName();

        int saleId = saleDAO.recordSale(customerName, customerMobile, total, amountPaid, change,
                sellerId, sellerName, new ArrayList<>(orderItems));

        if (saleId == -1) {
            AlertHelper.error("Payment Failed", "Could not save this sale. Please check the stock and try again.");
            return;
        }

        lastReceipt = new ReceiptData(saleId, customerName, customerMobile, new ArrayList<>(orderItems),
                total, amountPaid, change, sellerId, sellerName, LocalDateTime.now());

        AlertHelper.info("Payment Successful", "Sale #" + saleId + " recorded. Change due: " + String.format("%.2f", change));

        orderItems.clear();
        customernamefiled.clear();
        customermobilenumber.clear();
        ammounttaken.clear();
        recalcTotal();
        loadMenuGrid(menusearchfield.getText() == null ? "" : menusearchfield.getText().trim());
    }

    @FXML
    private void receiptbtn() {
        if (lastReceipt == null) {
            AlertHelper.warning("No Receipt", "There is no completed sale to show a receipt for yet.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("City Food Corner\n");
        sb.append("Sale #").append(lastReceipt.saleId).append("  ")
                .append(lastReceipt.timestamp.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n\n");
        sb.append("Customer: ").append(lastReceipt.customerName).append("\n");
        sb.append("Mobile: ").append(lastReceipt.customerMobile).append("\n\n");
        for (OrderItem item : lastReceipt.items) {
            sb.append(String.format("%-20s x%-3d %10.2f%n", item.getProductName(), item.getQuantity(), item.getLineTotal()));
        }
        sb.append("\n");
        sb.append(String.format("Total:  %.2f%n", lastReceipt.total));
        sb.append(String.format("Paid:   %.2f%n", lastReceipt.amountPaid));
        sb.append(String.format("Change: %.2f%n%n", lastReceipt.change));
        sb.append("Served by: ").append(lastReceipt.sellerName).append(" (").append(lastReceipt.sellerId).append(")");

        AlertHelper.info("Receipt", sb.toString());
    }

    @FXML
    private void gotologinpagebtn(ActionEvent event) throws IOException {
        shiftDAO.checkOut(SessionManager.getEmployeeCode());
        SessionManager.clear();
        SceneSwitcher sceneSwitcher = new SceneSwitcher();
        sceneSwitcher.switchscene(event, "UserLogin.fxml");
    }

    private String textOf(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private double parseDoubleOrZero(String text) {
        Double value = parseDoubleOrNull(text);
        return value == null ? 0 : value;
    }

    private Double parseDoubleOrNull(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

        private static class ReceiptData {
        final int saleId;
        final String customerName;
        final String customerMobile;
        final List<OrderItem> items;
        final double total;
        final double amountPaid;
        final double change;
        final String sellerId;
        final String sellerName;
        final LocalDateTime timestamp;

        ReceiptData(int saleId, String customerName, String customerMobile, List<OrderItem> items,
                    double total, double amountPaid, double change, String sellerId, String sellerName,
                    LocalDateTime timestamp) {
            this.saleId = saleId;
            this.customerName = customerName;
            this.customerMobile = customerMobile;
            this.items = items;
            this.total = total;
            this.amountPaid = amountPaid;
            this.change = change;
            this.sellerId = sellerId;
            this.sellerName = sellerName;
            this.timestamp = timestamp;
        }
    }
}

