package net.suzio.service;

import net.suzio.model.Item;
import net.suzio.model.Store;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple implementation of Store stocking, suitable for testing in cases where we want to show full lifecycle
 * Created by Michael on 11/16/2016.
 */
public class MockStockService implements StockService {
    List<Item> items = new ArrayList<>();

    void addItem(Item item) {
        items.add(item);
    }

    @Override
    public void stockStore(Store store) {
        items.forEach(store::addItem);
    }
}
