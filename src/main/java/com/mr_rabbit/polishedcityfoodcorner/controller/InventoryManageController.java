package com.mr_rabbit.polishedcityfoodcorner.controller;

import com.mr_rabbit.polishedcityfoodcorner.dao.ProductDAO;
import com.mr_rabbit.polishedcityfoodcorner.dao.ShiftDAO;
import com.mr_rabbit.polishedcityfoodcorner.model.Product;
import com.mr_rabbit.polishedcityfoodcorner.util.AlertHelper;
import com.mr_rabbit.polishedcityfoodcorner.util.ImageLoaderUtil;
import com.mr_rabbit.polishedcityfoodcorner.util.SceneSwitcher;
import com.mr_rabbit.polishedcityfoodcorner.util.SearchFilterUtil;
import com.mr_rabbit.polishedcityfoodcorner.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;

public class InventoryManageController {

    @FXML private TableView<Product> inventorymangnetable;
    @FXML private TableColumn<Product, String> inventorymangnetableproductid;
    @FXML private TableColumn<Product, String> inventorymangnetableproductname;
    @FXML private TableColumn<Product, String> inventorymangnetableproducttype;
    @FXML private TableColumn<Product, Number> inventorymangnetableproductstock;
    @FXML private TableColumn<Product, Number> inventorymangnetableproductprice;
    @FXML private TableColumn<Product, String> inventorymangnetableproductstatus;
    @FXML private TableColumn<Product, String> inventorymangnetableproductdate;

    @FXML private TextField inventorymangesearchfield;
    @FXML private Button inventorymangesearchbtn;

    @FXML private TextField productidfield;
    @FXML private TextField productnamefield;
    @FXML private ComboBox<String> producttypecombox;
    @FXML private TextField productstockfield;
    @FXML private TextField productpricefield;
    @FXML private ComboBox<String> productstatuscombox;

    @FXML private Button inventory_importBtn;
    @FXML private ImageView importedimage;

    private final ProductDAO productDAO = new ProductDAO();
    private final ShiftDAO shiftDAO = new ShiftDAO();

    private final ObservableList<Product> masterList = FXCollections.observableArrayList();
    private FilteredList<Product> filteredProducts;

        private String importedImagePath = null;

    @FXML
    private void initialize() {
        inventorymangnetableproductid.setCellValueFactory(data -> data.getValue().productIdProperty());
        inventorymangnetableproductname.setCellValueFactory(data -> data.getValue().productNameProperty());
        inventorymangnetableproducttype.setCellValueFactory(data -> data.getValue().typeProperty());
        inventorymangnetableproductstock.setCellValueFactory(data -> data.getValue().stockProperty());
        inventorymangnetableproductprice.setCellValueFactory(data -> data.getValue().priceProperty());
        inventorymangnetableproductstatus.setCellValueFactory(data -> data.getValue().statusProperty());
        inventorymangnetableproductdate.setCellValueFactory(data -> data.getValue().dateProperty());

        producttypecombox.setItems(FXCollections.observableArrayList("Main Course", "Beverage", "Snacks", "Dessert"));
        productstatuscombox.setItems(FXCollections.observableArrayList("Available", "Low Stock", "Out of Stock"));

        productidfield.setEditable(false);

        filteredProducts = new FilteredList<>(masterList, p -> true);
        inventorymangnetable.setItems(filteredProducts);

        SearchFilterUtil.attachLiveFilter(inventorymangesearchfield, inventorymangesearchbtn, filteredProducts,
                (product, query) -> product.getProductId().toLowerCase().contains(query)
                        || product.getProductName().toLowerCase().contains(query));

        inventorymangnetable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) populateForm(newVal);
        });

        refreshTable();
        resetFormToNewProduct();
    }

    private void refreshTable() {
        masterList.setAll(productDAO.getAllProducts());
    }

    private void populateForm(Product p) {
        productidfield.setText(p.getProductId());
        productnamefield.setText(p.getProductName());
        producttypecombox.setValue(p.getType());
        productstockfield.setText(String.valueOf(p.getStock()));
        productpricefield.setText(String.valueOf(p.getPrice()));
        productstatuscombox.setValue(p.getStatus());
        importedImagePath = p.getImagePath();
        importedimage.setImage(ImageLoaderUtil.loadProductImage(p.getImagePath()));
    }

    private void resetFormToNewProduct() {
        productidfield.setText(productDAO.generateNextProductId());
        productnamefield.clear();
        producttypecombox.setValue(null);
        productstockfield.clear();
        productpricefield.clear();
        productstatuscombox.setValue(null);
        importedImagePath = null;
        importedimage.setImage(ImageLoaderUtil.loadProductImage(null));
        inventorymangnetable.getSelectionModel().clearSelection();
    }

        private Product readFormAsProduct() {
        String id = productidfield.getText();
        String name = productnamefield.getText() == null ? "" : productnamefield.getText().trim();
        String type = producttypecombox.getValue();
        String status = productstatuscombox.getValue();

        if (name.isEmpty() || type == null || status == null) {
            AlertHelper.warning("Missing Information", "Please fill in product name, type and status.");
            return null;
        }

        Integer stock = parseNonNegativeInt(productstockfield.getText());
        if (stock == null) {
            AlertHelper.warning("Invalid Stock", "Stock must be a whole number, 0 or more.");
            return null;
        }

        Double price = parsePositiveDouble(productpricefield.getText());
        if (price == null) {
            AlertHelper.warning("Invalid Price", "Price must be a positive number.");
            return null;
        }

        return new Product(id, name, type, stock, price, status, "", importedImagePath);
    }

    @FXML
    private void inventorymangesearchbtn(ActionEvent event) {
        // Real-time filtering already happens as the user types (see initialize());
        // clicking the button while it reads "Refresh" simply clears the search.
        inventorymangesearchfield.clear();
    }

    @FXML
    private void addbtn() {
        Product product = readFormAsProduct();
        if (product == null) return;

        boolean success = productDAO.addProduct(product);
        if (success) {
            refreshTable();
            resetFormToNewProduct();
        } else {
            AlertHelper.error("Failed", "Could not add this product. Please try again.");
        }
    }

    @FXML
    private void updatebtn() {
        Product selected = inventorymangnetable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.warning("No Product Selected", "Please select a product from the table to update.");
            return;
        }
        Product product = readFormAsProduct();
        if (product == null) return;

        boolean success = productDAO.updateProduct(product);
        if (success) {
            refreshTable();
            resetFormToNewProduct();
        } else {
            AlertHelper.error("Failed", "Could not update this product. Please try again.");
        }
    }

    @FXML
    private void deletebtn() {
        Product selected = inventorymangnetable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.warning("No Product Selected", "Please select a product from the table to delete.");
            return;
        }
        if (!AlertHelper.confirm("Confirm Delete", "Delete " + selected.getProductName() + " from inventory?")) {
            return;
        }
        productDAO.deleteProduct(selected.getProductId());
        refreshTable();
        resetFormToNewProduct();
    }

    @FXML
    private void clearbtn() {
        resetFormToNewProduct();
    }

    @FXML
    private void importbtn() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Product Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(inventory_importBtn.getScene().getWindow());
        if (file != null) {
            importedImagePath = file.getAbsolutePath();
            importedimage.setImage(ImageLoaderUtil.loadProductImage(importedImagePath));
        }
    }

    @FXML
    private void gotologinpagebtn(ActionEvent event) throws IOException {
        shiftDAO.checkOut(SessionManager.getEmployeeCode());
        SessionManager.clear();
        SceneSwitcher sceneSwitcher = new SceneSwitcher();
        sceneSwitcher.switchscene(event, "UserLogin.fxml");
    }

    private Integer parseNonNegativeInt(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            int value = Integer.parseInt(text.trim());
            return value < 0 ? null : value;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double parsePositiveDouble(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            double value = Double.parseDouble(text.trim());
            return value <= 0 ? null : value;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

