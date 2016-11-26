package net.suzio.store.model;

import net.suzio.store.service.RegisterService;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test of Register class
 * Created by Michael on 11/17/2016.
 */
public class RegisterTest {

    @Test
    public void addWaitingShopperUnbounded() {
        Register register = new Register();
        int maxShoppers = 200; // not too big, but test what we think is unbounded
        for (int i = 0; i < maxShoppers; i++) {
            Shopper shopper = new Shopper();
            assertTrue("Unbounded Register line refused entry by Shopper #:" + i, register.addWaitingShopper(shopper));
        }
        assertTrue("Remaining capacity of wait line is zero or less", register.getRemainingWaitLimit() > 0);
    }

    @Test
    public void addWaitingShopperLimited() {
        int maxShoppers = 10;
        Register register = new Register(maxShoppers);
        for (int i = 0; i < maxShoppers; i++) {
            Shopper shopper = new Shopper();
            assertTrue("Register line refused entry by Shopper #: " + i + "while under expected line limit", register.addWaitingShopper(shopper));
        }
        @SuppressWarnings("unused") Shopper overLimitShopper = new Shopper();
        assertEquals("Full register line reports unexpected capacity ", maxShoppers, register.getWaitingCount());
        assertEquals("Full wait line still reports remaining capacity", register.getRemainingWaitLimit(), 0);
    }

    @Test
    public void testCheckout() {
        RegisterService service = new RegisterService();
        service.addRegister(new Register());
        Shopper shopper = new Shopper(new Store(), Collections.emptyList());
        service.addShopperToRegister(shopper);
    }
}
