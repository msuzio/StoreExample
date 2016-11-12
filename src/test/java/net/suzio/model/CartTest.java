package net.suzio.model;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Michael on 11/12/2016.
 */
public class CartTest {

    @Test
    /**
     * Assert that if we add Items to rhe cart, we can get them back (insertion order doesn't matter)
     */
    public void testCartAddandFetchReciprocal() {
        Cart cart = new Cart();

        Set<Item> itemsToAdd = new HashSet<>();
        itemsToAdd.add(new Item("Bananas",0.99,ItemUnit.LB,"FRUIT"));
    }
}
