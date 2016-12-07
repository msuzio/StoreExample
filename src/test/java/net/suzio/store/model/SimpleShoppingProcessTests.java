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
        // Store must let us shop and checkout
        when(store.startShopper(any(Shopper.class))).thenReturn(true);
        when(store.startShopperCheckout(any(Shopper.class))).thenReturn(true);

        // Store must return back anything we ask for
        when(store.takeItem(anyString(), anyInt())).then(invoke -> {
            String name = invoke.getArgument(0);
            return itemsMap.get(name);
        });

        Shopper shopper = new Shopper(store, items);
        shopper.run();

        // Expect a shopping list full of empty items
        List<Item> doneShoppingList = shopper.getShoppingList();
        assertNotNull(doneShoppingList);
        assertEquals("Shopping list on successful shopping run is of wrong size", items.size(), doneShoppingList.size());
        doneShoppingList.forEach(i -> assertEquals("quantity of item on successful shopping run is wrong",
                                                   0, i.getQuantity()));

        Cart cart = shopper.getCart();
        assertNotNull("Shopper's cart was null after shopping when all items should have been found", cart);
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
    public void testFailShop() {
        Store store = mock(Store.class);

        // Store won't let us shop at all
        when(store.startShopper(any(Shopper.class))).thenReturn(false);

        List<Item> items = Arrays.asList(
                new Item("Peanut Butter", 5.99, 1, "jar"),
                new Item("Jelly", 5.99, 1, "jar")
        );

        Shopper shopper = new Shopper(store, items);
        shopper.run();
        List<Item> doneShoppingList = shopper.getShoppingList();
        assertNotNull(doneShoppingList);
        assertEquals("Shopper's finished Item list in expected non-shopping case is not of expected size", items.size(), doneShoppingList.size());
        items.forEach(i -> assertTrue("Shopper's finished list does not contain expected  Item " + i, doneShoppingList.contains(i)));
    }

    @Test
    public void testFailCheckout() {
        // test items - we want them both as list and map for ease of test
        Item pb = new Item("Peanut Butter", 5.99, 1, "jar");
        Item jelly = new Item("Jelly", 5.99, 1, "jar");

        Map<String, Item> itemsMap = new HashMap<>();
        itemsMap.put(pb.getName(), pb);
        itemsMap.put(jelly.getName(), jelly);
        ArrayList<Item> items = new ArrayList<>(itemsMap.values());
        Store store = mock(Store.class);

        // mocking behavior:
        // Store must let us shop
        when(store.startShopper(any(Shopper.class))).thenReturn(true);

        // Store must return back anything we ask for
        when(store.takeItem(anyString(), anyInt())).then(invoke -> {
            String name = invoke.getArgument(0);
            return itemsMap.get(name);
        });

        //.. but fail at checkout
        when(store.startShopperCheckout(any(Shopper.class))).thenReturn(false);

        Shopper shopper = new Shopper(store, items);
        shopper.run();

        // Shopper should have empty Cart, but none of the items on its list have been fulfilled
        Cart cart = shopper.getCart();
        // Just in case
        assertNotNull("Shopper's cart was null after shopping run when checkout failed", cart);
        assertTrue("Shopper cart should be empty after run when checkout failed", cart.getItems().isEmpty());
        List<Item> doneShoppingList = shopper.getShoppingList();
        assertNotNull(doneShoppingList);
        assertEquals("Shopper's finished Item list in expected non-shopping case is not of expected size", items.size(), doneShoppingList.size());
        items.forEach(i -> assertTrue("Shopper's finished list does not contain expected  Item " + i, doneShoppingList.contains(i)));

        // should not have Receipt
        assertNull("shopper should not have a Receipt when checkout failed", shopper.getReceipt());
    }
}
