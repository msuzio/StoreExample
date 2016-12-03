package net.suzio.store.model;

import net.suzio.store.model.util.ItemUtil;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests of pieces of the shopping process
 * The current set of tests run single-threaded by design, and work even if Shopper does eventually implement Runnable
 * Created by Michael on 11/16/2016.
 */
public class SimpleShoppingProcessTests {

    @Test
    public void testFailShop() {
        Store store = mock(Store.class);

        // Store won't let us shop at all
        when(store.startShopper(any(Shopper.class))).thenReturn(false);

        List<Item> items = Arrays.asList(
                new Item("Peanut Butter", 5.99, 1, "jar"),
                new Item("Jelly", 5.99, 1, "jar")
        );

        Shopper shopper = new Shopper(store, items);
        shopper.shop();
        List<Item> doneShoppingList = shopper.getShoppingList();
        assertNotNull(doneShoppingList);
        assertEquals("Shopper's finished Item list in expected non-shopping case is not of expected size", items.size(), doneShoppingList.size());
        items.forEach(i -> assertTrue("Shopper's finished list does not contain expected  Item " + i, doneShoppingList.contains(i)));
    }

    @Test
    public void testShopGetsAllItemsExactly() {
        Store store = mock(Store.class);

        // test items - we want them both as list and map for ease of test
        Item pb = new Item("Peanut Butter", 5.99, 1, "jar");
        Item jelly = new Item("Jelly", 5.99, 1, "jar");
        Map<String, Item> itemsMap = new HashMap<>();
        itemsMap.put(pb.getName(), pb);
        itemsMap.put(jelly.getName(), jelly);
        ArrayList<Item> items = new ArrayList<>(itemsMap.values());

        // Mocking behavior:
        // Store must let us shop
        when(store.startShopper(any(Shopper.class))).thenReturn(true);

        // Store must return back anything we ask for
        when(store.takeItem(anyString(), anyInt())).then(invoke -> {
            String name = invoke.getArgument(0);
            return itemsMap.get(name);
        });

        Shopper shopper = new Shopper(store, items);
        shopper.shop();

        // Expect a shopping list full of empty items
        List<Item> doneShoppingList = shopper.getShoppingList();
        assertNotNull(doneShoppingList);
        assertEquals("Shopping list on successful shopping run is of wrong size", items.size(), doneShoppingList.size());
        doneShoppingList.forEach(i -> assertEquals("quantity of item on successful shopping run is wrong",
                                                   0, i.getQuantity()));

        Cart cart = shopper.getCart();
        List<Item> cartItems = cart.getItems();
        assertNotNull(cartItems);
        assertEquals("Shopping cart contains incorrect number of Items", items.size(), cartItems.size());

        Map<String, Item> cartItemsMap = ItemUtil.itemsToMap(cartItems);
        itemsMap.keySet().forEach(k -> {
                                      assertTrue("Cart does not contain expected Item name " + k, cartItemsMap.containsKey(k));
                                      assertTrue("Cart does not have expected Item ", itemsMap.get(k).equals(cartItemsMap.get(k)));
                                  }
        );
    }

    @Test
    public void testShopSkipsUnknownItem() {
        Store store = mock(Store.class);

        List<Item> items;
        Item pb = new Item("Peanut Butter", 5.99, 1, "jar");
        Item unknown = new Item("Unknown", 5.99, 1, "jar");
        items = Arrays.asList(
                pb,
                unknown
        );

        // Mocking behavior:
        // Store must let us shop
        when(store.startShopper(any(Shopper.class))).thenReturn(true);
        // store returns valid item in known case, null otherwise (default behavior of mock)
        when(store.takeItem(anyString(), anyInt())).then(invoke -> {
            String name = invoke.getArgument(0);
            return name.equals(pb.getName()) ? pb : null;
        });

        Shopper shopper = new Shopper(store, items);
        shopper.shop();

        // Expect a shopping list with on unfulfilled Item, and a cart wth the one known Item
        List<Item> doneShoppingList = shopper.getShoppingList();
        assertNotNull(doneShoppingList);
        assertEquals("Shopping list from shopping run with unknown item is of wrong size", 2, doneShoppingList.size());

        Map<String, Item> doneItemsMap = ItemUtil.itemsToMap(doneShoppingList);
        Item unknownFinal = doneItemsMap.get(unknown.getName());
        assertNotNull(unknownFinal);
        assertTrue("Unknown item on finished shopping list is not equal to expected object", unknown.equals(unknownFinal));

        Item pbFinal = doneItemsMap.get(pb.getName());
        assertNotNull(pbFinal);
        assertEquals("Known item on finished shopping list does not have expected quantity", 0, pbFinal.getQuantity());


        Cart cart = shopper.getCart();
        List<Item> cartItems = cart.getItems();
        assertNotNull(cartItems);
        assertEquals("Shopping cart contains incorrect number of Items", 1, cartItems.size());
        Item foundItem = cartItems.get(0);
        assertEquals("Item found on shopping run does not equal expected", pb, foundItem);
    }
}
