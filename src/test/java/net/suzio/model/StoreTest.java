package net.suzio.model;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Should not be testing overall system behavior; that is too encompassing for test cases just to validate our contract
 * Created by Michael on 11/12/2016.
 */
public class StoreTest {

    @Test
    public void storeStartsClosed() {
        Store store = new Store();
        assertFalse("Store.isOpen returns true on initialization.",store.isOpen());
        assertTrue("Store.isClosed returns false on initialization.",store.isClosed());
    }

    @Test
    public void storelineLimit() {
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
}
