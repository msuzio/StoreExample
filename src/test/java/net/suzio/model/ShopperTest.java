package net.suzio.model;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Placeholder for tests on Shopper object
 * Created by Michael on 11/13/2016.
 */
public class ShopperTest {

    @Test
    public void testMakeShoppingList() {
        Store store = new Store();
        List<Item> items = null;
        try {
            items = Arrays.asList(
                    new Item("Peanut Butter", 5.99, 1, "jar"),
                    new Item("Jelly", 5.99, 1, "jar")
            );
        } catch (InvalidItemException e) {
            fail(TestUtil.ERROR_IN_TEST_CASE_INVALID_ITEM_CREATED + e.getMessage());
        }
        Shopper shopper = new Shopper(store, items);

        // get items back
        List<Item> shoppingList = shopper.getShoppingList();
        items.forEach(shoppingList::contains);
    }

    @Test
    public void testMakeShoppingListWithRepeats() {
        Store store = new Store();
        List<Item> items = null;

        Item jelly = null;
        Item pb = null;
        Item twoPb = null;
        try {
            jelly = new Item("Jelly", 5.99, 1, "jar");
            pb = new Item("Peanut Butter", 5.99, 1, "jar");
            items = Arrays.asList(
                    jelly, pb, pb
            );
            twoPb = new Item(pb.getName(), pb.getPrice(), 2, pb.getUnits());
        } catch (InvalidItemException e) {
            fail(TestUtil.ERROR_IN_TEST_CASE_INVALID_ITEM_CREATED + e.getMessage());
        }
        Shopper shopper = new Shopper(store, items);

        // get items back
        // clumsy handling below may indicate we're better making in and out values be Maps
        List<Item> shoppingList = shopper.getShoppingList();
        assertNotNull("returned shopping list was null", shoppingList);

        assertEquals("Condensed shopping List did not have correct item count", 2, shoppingList.size());
        // Now that we know the size is correct, contains tells us if the values are right
        assertTrue("returned shopping list does not contain expected Item " + twoPb, shoppingList.contains(twoPb));
        assertTrue("returned shopping list does not contain expected Item " + jelly, shoppingList.contains(jelly));

        // redundant, but let's complete our assertions
        assertFalse("returned shopping list contains unexpected Item " + pb, shoppingList.contains(pb));
    }
}
