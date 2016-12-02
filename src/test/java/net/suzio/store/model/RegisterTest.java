package net.suzio.store.model;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test of Register class
 * Created by Michael on 11/17/2016.
 */
public class RegisterTest {

    @Test
    public void testIdAssignment() {
        Register r1 = new Register();
        assertTrue("Id was not assigned to Register", r1.getId() >= 0);

        // cannot have two Registers with the same id
        Register r2 = new Register();
        assertNotEquals("Register objects have same auto-generated id", r1.getId(), r2.getId());
        // and their logical equality is based on that
        assertFalse("Two created Register objects should never be logically equal", r1.equals(r2));
    }

    @Test
    public void addWaitingShopperUnbounded() {
        Register register = new Register();
        int maxShoppers = 200; // not too big, but test what we think is unbounded
        for (int i = 0; i < maxShoppers; i++) {
            Shopper shopper = new Shopper();
            assertTrue("Unbounded Register line refused entry by Shopper #:" + i, register.addShopper(shopper));
        }
        assertTrue("Remaining capacity of wait line is zero or less", register.getRemainingWaitLimit() > 0);
    }

    @Test
    public void addWaitingShopperLimited() {
        int maxShoppers = 10;
        Register register = new Register(maxShoppers);
        for (int i = 0; i < maxShoppers; i++) {
            Shopper shopper = new Shopper();
            assertTrue("Register line refused entry by Shopper #: " + i + "while under expected line limit", register.addShopper(shopper));
        }
        @SuppressWarnings("unused") Shopper overLimitShopper = new Shopper();
        assertFalse("Full Register allowed in new Shopper", register.addShopper(overLimitShopper));
        assertEquals("Full register line reports unexpected capacity ", maxShoppers, register.getWaitingCount());
        assertEquals("Full wait line still reports remaining capacity", register.getRemainingWaitLimit(), 0);
    }

    @Test
    public void testCheckout() {
        Register register = new Register();
        Shopper shopper = mock(Shopper.class);
        Cart oneItemCart = mock(Cart.class);

        // Mock out entire successful shopping run resulting in a full Cart
        when(shopper.getCart()).thenReturn(oneItemCart);

        // Just let trivial get/set logic pass through
        doCallRealMethod().when(shopper).setReceipt(any(Receipt.class));
        //noinspection ResultOfMethodCallIgnored
        doCallRealMethod().when(shopper).getReceipt();
        // verify cart clear merhod got called

        when(oneItemCart.getItems()).thenReturn(Collections.singletonList(new Item("Test", 1.0, 4, "Packages")));

        register.addShopper(shopper);
        register.checkoutNext();

        // Shopper should now have a Receipt with one Item
        Receipt r = shopper.getReceipt();
        assertNotNull("Shopper checkout did not result in a valid receipt", r);
        assertEquals("Shopper receipt did not have correct number of items", 1, r.getItemizedLines().size());

        // The shopper cart should be cleared
        verify(oneItemCart).clear();
    }
}
