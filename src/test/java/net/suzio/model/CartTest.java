package net.suzio.model;

import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.fail;

/**
 *
 * Created by Michael on 11/12/2016.
 */
public class CartTest {


    /**
     * Assert that if we add Items to rhe cart, we can get them back (insertion order doesn't matter)
     */
    @Test
    public void testCartAddandFetchReciprocal() {
        Cart cart = new Cart();

        Set<Item> itemsToAdd = new HashSet<>();
        try {
            itemsToAdd.add(new Item("Bananas", 0.99, 1, "LB"));
            itemsToAdd.add(new Item("Almonds", 5.99, 4, "jar"));
            itemsToAdd.add(new Item("Orange Juice", 2.99, 1, "Gallon"));
        } catch (InvalidItemException e) {
            fail("Error in test case -- created an invalid Item: " +e.getMessage());
        }

        for (Item item : itemsToAdd) {
            cart.addItem(item);
        }

        List<Item> cartItems = cart.getItems();
    }
}
