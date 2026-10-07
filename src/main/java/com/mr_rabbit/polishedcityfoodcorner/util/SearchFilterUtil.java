package com.mr_rabbit.polishedcityfoodcorner.util;

import javafx.collections.transformation.FilteredList;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.util.function.BiPredicate;

public class SearchFilterUtil {

    private SearchFilterUtil() {
    }

    public static <T> void attachLiveFilter(TextField searchField, Button searchButton,
                                             FilteredList<T> filteredList, BiPredicate<T, String> matcher) {
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            String query = newValue == null ? "" : newValue.trim().toLowerCase();
            if (query.isEmpty()) {
                filteredList.setPredicate(item -> true);
                searchButton.setText("Search");
            } else {
                filteredList.setPredicate(item -> matcher.test(item, query));
                searchButton.setText("Refresh");
            }
        });
    }
}
