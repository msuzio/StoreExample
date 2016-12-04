package net.suzio.store.model;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * A more full-featured, complex, set of shopping tests run end-to-end.
 * Separated out because other test cases were already too big and complex.
 * <p>
 * Created by Michael on 12/2/2016.
 */
public class FullShoppingProcessTests {

    @Test
    public void testSingleThreadShopping() {
        // Create a Store
        Store store = new Store();
        // Add Items
        List<Item> items = Arrays.asList(
                new Item("Apples", 2.99, 4, "Lb"),
                new Item("Grapes", 3.99, 2, "Lb"),
                new Item("Orange Juice", 2.99, 4, "Gallon"),
                new Item("Steak", 3.99, 5, "Lb")
        );
        // add items
        items.forEach(store::addItem);
        // Store must open
        store.open();

        // Add Registers
        for (int i = 0; i < items.size(); i++) {
            store.addRegister(new Register());
        }
        // create a collection of Shoppers, designed to each take one of the Store items
        // and completely deplete the supply
        List<Shopper> shoppers = new ArrayList<>();
        items.forEach(i ->
                      {
                          List<Item> list = new ArrayList<>();
                          list.add(i);
                          Shopper shopper = new Shopper(store, list);
                          shoppers.add(shopper);
                          // We should shop and get to checkout step
                          shopper.shop();
                      }
        );

        // run main loop -- should checkout all Shoppers
        store.run();
        store.shutdownStore();

        for (int shopNum = 0; shopNum < shoppers.size(); shopNum++) {
            // now all Shoppers should:
            Shopper s = shoppers.get(shopNum);

            // * have an empty cart
            Cart c = s.getCart();
            assertTrue("Shopper cart was not empty after full shopping and checkout", c.getItems().isEmpty());

            // * Have a shopping list with the item they shopped for and zero quantity
            Item expected = items.get(shopNum);
            for (Item i : s.getShoppingList()) {
                assertEquals(i.getName(), expected.getName());
                assertEquals("Shopper " + shopNum + " had unexpected quantity in shopping list for item '" +
                                     i.getName(), 0, i.getQuantity());
            }

            // * Have a non-null Receipt with an itemized list corresponding to the one Item they shopped for
            Receipt r = s.getReceipt();
            assertNotNull("Receipt on shopper " + shopNum + "was null", r);
            List<String> lines = r.getItemizedLines();
            assertEquals("Receipt for shopper " + shopNum + " had unexpected itemized line count ", 1, lines.size());
            String itemLine = lines.get(0);
            assertTrue("Itemized line for shopper" + shopNum + " did not contain expected item name " +
                               expected.getName(), itemLine.contains(expected.getName()));

        }
    }
}
