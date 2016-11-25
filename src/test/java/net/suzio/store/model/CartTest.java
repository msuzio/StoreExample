package net.suzio.store.model;

import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CartTest {


    /**
     * Assert that if we add Items to the cart, we can get them back (insertion order doesn't matter)
     */
    @Test
    public void testCartAddAndFetchReciprocal() {
        Cart cart = new Cart();

        Set<Item> itemsToAdd = new HashSet<>();
        itemsToAdd.add(new Item("Bananas", 0.99, 1, "LB"));
        itemsToAdd.add(new Item("Almonds", 5.99, 4, "jar"));
        itemsToAdd.add(new Item("Orange Juice", 2.99, 1, "Gallon"));


        itemsToAdd.forEach(cart::addItem);

        List<Item> cartItems = cart.getItems();
        assertEquals("Cart list retrieved from cart does not have correct number of elements", itemsToAdd.size(), cartItems.size());

        // elegance of lambda is a bit lost here.
        cartItems.forEach(i ->
                assertTrue("Item" + i + " + was never added to Cart, but was present in list returned from it", itemsToAdd.contains(i)));
    }
}
