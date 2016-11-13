package net.suzio.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by Michael on 11/12/2016.
 */
public class Cart {
    // No pressing need to size this explicitly just yet; defslt constructor is OK
    private List<Item> items = new ArrayList<>();

    public void addItem(Item item) {
        items.add(item);
    }

    public List<Item> getItems() {
        return items;
    }
}
