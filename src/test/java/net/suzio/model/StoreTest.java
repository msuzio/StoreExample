package net.suzio.model;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Should not be testing overall system behavior; that is too encompassing for test cases just to validate our contract
 * Created by Michael on 11/12/2016.
 */
public class StoreTest {

    @Test
    public void storeStartsClosed() {
        Store store = new Store();
        assertFalse("Store.isOpen returns true on initialization.", store.isOpen());
        assertTrue("Store.isClosed returns false on initialization.", store.isClosed());
    }

    @Test
    public void storeLineLimit() {
        int lineLimit = 5;
        Store store = new Store(lineLimit);
        int shopperNumber;
        for (shopperNumber = 0; shopperNumber < lineLimit; shopperNumber++) {
            boolean allowed = store.addWaitingShopper(new Shopper());
            assertTrue("Shopper" + shopperNumber + " not allowed in waiting line with set limit of " + lineLimit, allowed);
        }

        // reached our limit, should refuse Shopper
        boolean shouldNotBeAllowed = store.addWaitingShopper(new Shopper());
        assertFalse("Shopper " + shopperNumber + "allowed in past set Store wait limit of" + lineLimit, shouldNotBeAllowed);

    }

    @Test
    public void testAcceptShopper() {
        Store store = new Store();
        boolean allowed;
        Shopper shopper = new Shopper();
        allowed = store.enter(shopper);
        assertFalse("Shopper allowed into closed Store", allowed);
        store.open();
        allowed = store.enter(shopper);
        assertTrue("Shopper refused entry into open Store", allowed);
    }

    @Test
    public void testAddItem() {
        Store store;
        try {
            store = new Store();
            // add a single Item to the Store and we expect the exact same (immutable) Item back
            Item item = new Item("Bananas", 0.99, 1, "LB");
            Item stockedItem = store.addItem(item);
            assertSame(item, stockedItem);

            // The rest of the logic consists of correct behavior of Item.merge.  We'll test a known case where that works,
            // and one where it fails and not explore more cases

            // start fresh
            // working case -- add same data twice and expect two of the same
            store = new Store();
            Item oneBanana = new Item("Bananas", 0.99, 1, "LB");
            store.addItem(oneBanana);
            Item twoBanana = store.addItem(new Item("Bananas", 0.99, 1, "LB"));
            // This one *really* should not happen.
            assertEquals("Updated Item has wrong name ", oneBanana.getName(), twoBanana.getName());
            assertEquals("Updated Item has wrong units ", oneBanana.getUnits(), twoBanana.getUnits());
            assertEquals("Updated Item has wrong price", oneBanana.getPrice(), twoBanana.getPrice(), 0.0);
            assertEquals("Updated Item has wrong quantity", 2, twoBanana.getQuantity(), 0.0);

            // removing three Bananas now results in -1 Bananas!
            Item negativeQuantity = new Item("Bananas", 0.99, -3, "LB");
            Item shouldNotChange = store.addItem(negativeQuantity);
            assertSame(twoBanana, shouldNotChange);

        } catch (InvalidItemException e) {
            fail("Error in test case -- invalid Item created: " + e.getMessage());
        }

    }
}
