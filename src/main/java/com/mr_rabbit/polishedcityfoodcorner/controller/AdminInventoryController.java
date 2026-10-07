package com.mr_rabbit.polishedcityfoodcorner.controller;

import com.mr_rabbit.polishedcityfoodcorner.dao.ProductDAO;
import com.mr_rabbit.polishedcityfoodcorner.model.Product;
import com.mr_rabbit.polishedcityfoodcorner.util.SearchFilterUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class AdminInventoryController {

    @FXML private TableView<Product> admininventorytable;
    @FXML private TableColumn<Product, String> admininventorytableproductid;
    @FXML private TableColumn<Product, String> admininventorytableproductname;
    @FXML private TableColumn<Product, String> admininventorytableproducttype;
    @FXML private TableColumn<Product, Number> admininventorytableproductstock;
    @FXML private TableColumn<Product, Number> admininventorytableproductprice;
    @FXML private TableColumn<Product, String> admininventorytableproductstatus;
    @FXML private TableColumn<Product, String> admininventorytableproductdate;

    @FXML private TextField admininventorysearchfield;
    @FXML private Button admininventorysearchbtn;

    private final ProductDAO productDAO = new ProductDAO();

    @FXML
    private void initialize() {
        admininventorytableproductid.setCellValueFactory(data -> data.getValue().productIdProperty());
        admininventorytableproductname.setCellValueFactory(data -> data.getValue().productNameProperty());
        admininventorytableproducttype.setCellValueFactory(data -> data.getValue().typeProperty());
        admininventorytableproductstock.setCellValueFactory(data -> data.getValue().stockProperty());
        admininventorytableproductprice.setCellValueFactory(data -> data.getValue().priceProperty());
        admininventorytableproductstatus.setCellValueFactory(data -> data.getValue().statusProperty());
        admininventorytableproductdate.setCellValueFactory(data -> data.getValue().dateProperty());

        refreshTable();

        admininventorysearchbtn.setOnAction(event -> admininventorysearchfield.clear());
    }

    private void refreshTable() {
        ObservableList<Product> products = FXCollections.observableArrayList(productDAO.getAllProducts());
        FilteredList<Product> filtered = new FilteredList<>(products, p -> true);

        SearchFilterUtil.attachLiveFilter(admininventorysearchfield, admininventorysearchbtn, filtered,
                (product, query) -> product.getProductId().toLowerCase().contains(query)
                        || product.getProductName().toLowerCase().contains(query));

        admininventorytable.setItems(filtered);
    }
}

