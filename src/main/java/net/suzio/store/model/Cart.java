package net.suzio.store.model;

import java.util.ArrayList;
import java.util.List;

/**
 * cart for use by a Shopper. Exists to separate concerns
 * Created by Michael on 11/12/2016.
 */
public class Cart {
    // No pressing need to size this explicitly just yet; default constructor is OK
    private final List<Item> items = new ArrayList<>();

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

    public void clear() {
        items.clear();
    }

    public void removeItem(Item item) {
        // not efficient, but not called often enough to make us revise our data structure
        items.remove(item);
    }

    @Override
    public String toString() {
        //noinspection StringBufferReplaceableByString
        @SuppressWarnings("StringBufferReplaceableByString") StringBuilder sb = new StringBuilder("Cart{");
        sb.append("items=").append(items);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Cart cart = (Cart) o;

        return items.equals(cart.items);
    }

    @Override
    public int hashCode() {
        return items.hashCode();
    }
}
