package com.mr_rabbit.polishedcityfoodcorner.controller;

import com.mr_rabbit.polishedcityfoodcorner.dao.ProductDAO;
import com.mr_rabbit.polishedcityfoodcorner.dao.SaleDAO;
import com.mr_rabbit.polishedcityfoodcorner.model.HistoryRecord;
import com.mr_rabbit.polishedcityfoodcorner.model.Product;
import com.mr_rabbit.polishedcityfoodcorner.util.AlertHelper;
import com.mr_rabbit.polishedcityfoodcorner.util.ImageLoaderUtil;
import com.mr_rabbit.polishedcityfoodcorner.util.SearchFilterUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

public class HistoryController {

    private static final DateTimeFormatter INPUT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yy");

    @FXML private TableView<HistoryRecord> customerhistable;
    @FXML private TableColumn<HistoryRecord, String> customerhistablecustomername;
    @FXML private TableColumn<HistoryRecord, String> customerhistablecutomermobilenumber;
    @FXML private TableColumn<HistoryRecord, String> customerhistablebuyingitem;
    @FXML private TableColumn<HistoryRecord, String> customerhistabletaka;
    @FXML private TableColumn<HistoryRecord, String> customerhistabledate;
    @FXML private TableColumn<HistoryRecord, String> customerhistablesalesmanid;

    @FXML private TextField customerhistorysearchfield;
    @FXML private Button customerhistorysearchbtn;

    @FXML private BarChart<String, Number> customeroverviewbarchart;

    @FXML private ImageView topsaleonepic;
    @FXML private ImageView topsaletwopic;
    @FXML private ImageView topsalethreepic;
    @FXML private Label topsaleonelable;
    @FXML private Label topsaletwolable;
    @FXML private Label topsalethreelable;

    @FXML private TextField histsortstart;
    @FXML private TextField histsortend;

    @FXML private Label bestsaleridlable;
    @FXML private Label bestsalernamelable;

    private final SaleDAO saleDAO = new SaleDAO();
    private final ProductDAO productDAO = new ProductDAO();

    private final ObservableList<HistoryRecord> masterList = FXCollections.observableArrayList();
    private FilteredList<HistoryRecord> filteredRecords;

    @FXML
    private void initialize() {
        customerhistablecustomername.setCellValueFactory(data -> data.getValue().customerNameProperty());
        customerhistablecutomermobilenumber.setCellValueFactory(data -> data.getValue().customerMobileProperty());
        customerhistablebuyingitem.setCellValueFactory(data -> data.getValue().buyingItemsProperty());
        customerhistabletaka.setCellValueFactory(data -> data.getValue().takaProperty());
        customerhistabledate.setCellValueFactory(data -> data.getValue().dateProperty());
        customerhistablesalesmanid.setCellValueFactory(data -> data.getValue().sellerIdProperty());

        filteredRecords = new FilteredList<>(masterList, r -> true);
        customerhistable.setItems(filteredRecords);

        SearchFilterUtil.attachLiveFilter(customerhistorysearchfield, customerhistorysearchbtn, filteredRecords,
                (record, query) -> record.getCustomerName().toLowerCase().contains(query)
                        || record.getCustomerMobile().contains(query));
        customerhistorysearchbtn.setOnAction(event -> customerhistorysearchfield.clear());

        if (customeroverviewbarchart != null && customeroverviewbarchart.getXAxis() instanceof javafx.scene.chart.CategoryAxis) {
            javafx.scene.chart.CategoryAxis xAxis = (javafx.scene.chart.CategoryAxis) customeroverviewbarchart.getXAxis();
            xAxis.setTickLabelRotation(0);
            xAxis.setTickLabelGap(5);
        }

        loadHistory(null, null);
        loadTopSellers();
        loadCustomerOverviewChart();
        loadBestSeller();
    }

    private void loadHistory(LocalDate startDate, LocalDate endDate) {
        masterList.setAll(saleDAO.getHistory(null, startDate, endDate));
    }

    private void loadTopSellers() {
        List<String> topItems = saleDAO.getTop3SellingItems();
        ImageView[] pics = {topsaleonepic, topsaletwopic, topsalethreepic};
        Label[] labels = {topsaleonelable, topsaletwolable, topsalethreelable};

        for (int i = 0; i < 3; i++) {
            if (i < topItems.size()) {
                String name = topItems.get(i);
                labels[i].setText(name);
                Product product = productDAO.findByName(name);
                if (product != null) {
                    pics[i].setImage(ImageLoaderUtil.loadProductImage(product.getImagePath()));
                }
            } else {
                labels[i].setText("-");
            }
        }
    }

    private void loadCustomerOverviewChart() {
        customeroverviewbarchart.setAnimated(false);
        customeroverviewbarchart.getXAxis().setAnimated(false);
        customeroverviewbarchart.getYAxis().setAnimated(false);
        Map<String, Integer> monthlyCounts = saleDAO.getMonthlyOrderCounts(6);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Orders");
        monthlyCounts.forEach((month, count) -> series.getData().add(new XYChart.Data<>(month, count)));
        customeroverviewbarchart.getData().setAll(series);
    }

    private void loadBestSeller() {
        String[] best = saleDAO.getBestSellerOfMonth();
        bestsaleridlable.setText(best[0]);
        bestsalernamelable.setText(best[1]);
    }

    @FXML
    private void histapplybtn() {
        String start = textOf(histsortstart);
        String end = textOf(histsortend);

        if (start.isEmpty() && end.isEmpty()) {
            loadHistory(null, null);
            return;
        }
        if (start.isEmpty() || end.isEmpty()) {
            AlertHelper.warning("Missing Date", "Please fill in both the start and end date.");
            return;
        }
        try {
            LocalDate startDate = LocalDate.parse(start, INPUT_DATE_FORMAT);
            LocalDate endDate = LocalDate.parse(end, INPUT_DATE_FORMAT);
            loadHistory(startDate, endDate);
        } catch (DateTimeParseException e) {
            AlertHelper.warning("Invalid Date", "Please enter dates as DD/MM/YY.");
        }
    }

    private String textOf(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }
}

