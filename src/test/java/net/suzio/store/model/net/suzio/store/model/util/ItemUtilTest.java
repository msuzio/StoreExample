package net.suzio.store.model.net.suzio.store.model.util;

import net.suzio.store.model.Item;
import net.suzio.store.model.util.ItemUtil;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests of ItemUtil class
 * Created by Michael on 11/17/2016.
 */
public class ItemUtilTest {
    @Test
    public void testListToMap() {
        // Must be three different Items
        Item threeGallons = new Item("milk", 2.99, 3, "Gallon");
        Item almonds = new Item("almonds", 2.99, 1, "jar");
        Item bananas = new Item("Bananas", 2.99, 1, "LB");
        List<Item> items = Arrays.asList(threeGallons, almonds, bananas);
        final Map<String, Item> itemMap = ItemUtil.itemstoMap(items);
        assertEquals("Map generated from list has incorrect number of mappings", items.size(), itemMap.size());
        items.forEach(i -> {
            Item found = itemMap.get(i.getName());
            assertNotNull("Item " + i.getName() + "not found in generated Map", found);
            assertTrue("Item found in generated Map under key " + i.getName() + " does not match expected value", i.equals(found));
        });
    }

    @Test
    public void testListToMapMerges() {
        // three different Items, and the expected result
        // Test merging identical items and assert we now have one item with a quantity of 1
        final String bananas = "Bananas";

        // set up bananas
        Item oneBanana = new Item(bananas, 0.99, 1, "LB");
        Item threeBanana = new Item(oneBanana.getName(), oneBanana.getPrice(), 3, oneBanana.getUnits());
        Item takeThreeBanana = new Item(oneBanana.getName(), oneBanana.getPrice(), -3, oneBanana.getUnits());

        List<Item> items = Arrays.asList(oneBanana, threeBanana, takeThreeBanana);
        final Map<String, Item> itemMap = ItemUtil.itemstoMap(items);

        assertEquals("Map generated from list has incorrect number of mappings", 1, itemMap.size());
        Item found = itemMap.get(bananas);
        assertNotNull("Did not findItem under expected Mapping " + bananas, found);
        assertTrue("Item found in generated map did not match expected value", oneBanana.equals(found));

    }
}
