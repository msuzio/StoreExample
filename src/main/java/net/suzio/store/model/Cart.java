package net.suzio.store.model;

import java.util.ArrayList;
import java.util.List;

/**
 * cart for use by a Shopper. Exists to separate concerns
 * Created by Michael on 11/12/2016.
 */
public class Cart {
    // No pressing need to size this explicitly just yet; default constructor is OK
    private List<Item> items = new ArrayList<>();

    public void addItem(Item item) {
        items.add(item);
    }

    /**
     * Get the items added to this Cart
     *
     * @return A copy of the Cart item list.
     */
    public List<Item> getItems() {
        return new ArrayList<>(items);
    }
}
