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

    private String formatItem(Item i) {
        // Fixed format currently
        String format = "%s %d @$%(,.2f/%s == $%(,.2f";
        double sum = i.getQuantity() * i.getPrice();
        return String.format(format, i.getName(), i.getQuantity(), i.getPrice(), i.getUnits(), sum);
    }
}
