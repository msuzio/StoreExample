package net.suzio.store.model.util;

import net.suzio.store.model.Item;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for common Item operations not core to Item class
 * <p>
 * Created by Michael on 11/16/2016.
 */
@SuppressWarnings("UtilityClass")
public final class ItemUtil {

    @SuppressWarnings("ImplicitCallToSuper")
    private ItemUtil() {
    }

    public static Map<String, Item> itemstoMap(Iterable<Item> items) {
        Map<String, Item> map = new HashMap<>();

        items.forEach(i -> {
            String name = i.getName();
            Item existing = map.get(name);
            Item listItem = i;
            if (existing != null) {
                listItem = Item.merge(existing, i);
            }
            map.put(name, listItem);
        });

        return map;
    }
}
