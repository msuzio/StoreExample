package net.suzio.store.model;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Should not be testing overall system behavior; that is too encompassing for test cases just to validate our contract
 * Created by Michael on 11/12/2016.
 */
public class StoreTest {

    private static final String MERGED_ITEM_WAS_NULL = "Merged Item was null";
    private static final String BANANAS = "Bananas";
    private static final String MILK = "MILK";
    private static final String GALLON = "Gallon";

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
        boolean shouldNotBeAllowed = store.addWaitingShopper(new Shopper(store, new ArrayList<>()));
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
    public void testAddItemSingle() {
        Store store = new Store();

        // Setup our Items
        Item oneBanana = new Item(BANANAS, 0.99, 1, "LB");

        // add a single Item to the Store and we expect the exact same (immutable) Item back
        Item stockedItem = store.addItem(oneBanana);
        assertSame(oneBanana, stockedItem);
    }

    // The rest of the logic consists of correct behavior of Item.merge.  We'll test a known case where that works,
    // and one where it fails and not explore more cases here
    @Test
    public void testAddItemTwice() {
        // Setup our Items
        Item oneBanana = new Item(BANANAS, 0.99, 1, "LB");

        // start fresh
        // working case -- add same data twice and expect two of the same

        // start fresh
        Store store = new Store();
        store.addItem(oneBanana);
        Item twoBanana = store.addItem(oneBanana);

        assertNotNull(MERGED_ITEM_WAS_NULL, twoBanana);

        // This one *really* should not happen.
        assertEquals("Updated Item has wrong name ", oneBanana.getName(), twoBanana.getName());
        assertEquals("Updated Item has wrong units ", oneBanana.getUnits(), twoBanana.getUnits());
        assertEquals("Updated Item has wrong price", oneBanana.getPrice(), twoBanana.getPrice(), 0.0);
        assertEquals("Updated Item has wrong quantity", 2, twoBanana.getQuantity(), 0.0);
    }

    @Test
    public void testAddNegative() {
        // Setup our Items

        Item oneBanana = new Item(BANANAS, 0.99, 1, "LB");
        Item negativeQuantity = new Item(BANANAS, 0.99, -1, "LB");

        Store store = new Store();

        store.addItem(oneBanana);
        final Item noBananas = store.addItem(negativeQuantity);

        assertNotNull(MERGED_ITEM_WAS_NULL, noBananas);

        // This one *really* should not happen.
        assertEquals("Updated Item has wrong name ", oneBanana.getName(), noBananas.getName());
        assertEquals("Updated Item has wrong units ", oneBanana.getUnits(), noBananas.getUnits());
        assertEquals("Updated Item has wrong price", oneBanana.getPrice(), noBananas.getPrice(), 0.0);
        assertEquals("Updated Item has wrong quantity", 0, noBananas.getQuantity());
    }

    @Test
    public void testTakeItem() {
        Item threeGallons = new Item(MILK, 2.99, 3, GALLON);

        // put an Item in the Store
        Store store = new Store();
        Item stockedItem = store.addItem(threeGallons);

        // Better be the same object returned
        assertSame(stockedItem, threeGallons);

        // Now try to take some milk
        final Item tookTwoGallons = store.takeItem(MILK, 2);

        assertEquals("Item taken from stock has wrong name ", threeGallons.getName(), tookTwoGallons.getName());
        assertEquals("Item taken from stock has wrong units ", threeGallons.getUnits(), tookTwoGallons.getUnits());
        assertEquals("Item taken from stock has wrong price", threeGallons.getPrice(), tookTwoGallons.getPrice(), 0.0);
        assertEquals("Item taken from stock has wrong quantity", 2, tookTwoGallons.getQuantity());

        // Now we expect stock to be one gallon
        Item newStock = store.queryItem(MILK);
        assertEquals("Item taken from stock has wrong name ", threeGallons.getName(), newStock.getName());
        assertEquals("Item taken from stock has wrong units ", threeGallons.getUnits(), newStock.getUnits());
        assertEquals("Item taken from stock has wrong price", threeGallons.getPrice(), newStock.getPrice(), 0.0);
        assertEquals("Item taken from stock has wrong quantity", 1, newStock.getQuantity());
    }

    @Test
    public void testTakeAll() {
        Item threeGallons = new Item(MILK, 2.99, 3, GALLON);

        // put Item in the Store
        Store store = new Store();
        store.addItem(threeGallons);

        //Now try to take more than we have -- weshould only get up to all current available stock
        Item tryTakeFourGallons = store.takeItem(MILK, 4);
        assertEquals("Item taken from stock has wrong name ", threeGallons.getName(), tryTakeFourGallons.getName());
        assertEquals("Item taken from stock has wrong units ", threeGallons.getUnits(), tryTakeFourGallons.getUnits());
        assertEquals("Item taken from stock has wrong price", threeGallons.getPrice(), tryTakeFourGallons.getPrice(), 0.0);
        assertEquals("Item taken from stock has wrong quantity", 3, tryTakeFourGallons.getQuantity());

        // Now we expect stock to be zero gallons
        Item newStock = store.queryItem(MILK);
        assertEquals("Item taken from stock has wrong name ", threeGallons.getName(), newStock.getName());
        assertEquals("Item taken from stock has wrong units ", threeGallons.getUnits(), newStock.getUnits());
        assertEquals("Item taken from stock has wrong price", threeGallons.getPrice(), newStock.getPrice(), 0.0);
        assertEquals("Item taken from stock has wrong quantity", 0, newStock.getQuantity());
    }

    @Test
    public void testTakeFromEmptyStock() {
        Store store = new Store();
        Item nothing = store.takeItem("anything", 1);
        assertNull("should not have received valid Item from an empty store", nothing);
    }

    @Test
    public void testTakeUnknown() {
        Item threeGallons = new Item(MILK, 2.99, 3, GALLON);
        // put Item in the Store
        Store store = new Store();
        store.addItem(threeGallons);

        Item nothing = store.takeItem("unknown", 1);
        assertNull("should not have received valid Item from an unknown item name", nothing);
    }
}

