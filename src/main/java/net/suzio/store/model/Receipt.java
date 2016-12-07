package net.suzio.store.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Receipt associated with a Shoppers shopping run
 */
public class Receipt {
    private final List<Item> orderItems = new ArrayList<>();

    public void addItems(List<Item> items) {
        orderItems.addAll(items);
    }

    public String getFormattedTotal() {
        double total = 0;
        for (Item item : orderItems) {
            total += item.getPrice();
        }

        return String.format("$%(,.2f", total);
    }

    public List<String> getItemizedLines() {
        // sort sand collect in a stream (functionally without side-effects)
        return orderItems.stream().sorted((item1, item2) -> {
            double priceComp = item2.getPrice() - item1.getPrice();
            return priceComp == 0 ? item1.getName().compareTo(item2.getName()) : priceComp < 0 ? -1 : 1;
        }).map(this::formatItem).collect(Collectors.toList());
    }

    private String formatItem(Item item) {
        // Fixed format currently
        String format = "%s %d @$%(,.2f/%s == $%(,.2f";
        double sum = item.getQuantity() * item.getPrice();
        return String.format(format, item.getName(), item.getQuantity(), item.getPrice(), item.getUnits(), sum);
    }

    @Override
    // I consider this spurious at best, and the concatenation version will flag other inspectors
    @SuppressWarnings("StringBufferReplaceableByString")
    public String toString() {
        StringBuilder sb = new StringBuilder("Receipt{");
        sb.append(", formatted Total =").append(getFormattedTotal());
        sb.append(", Itemized lines=").append(String.join(",", getItemizedLines()));
        sb.append('}');
        return sb.toString();
    }
}
