package net.suzio.store.service;

import net.suzio.store.model.Register;
import net.suzio.store.model.Shopper;
import org.junit.Test;

import static org.junit.Assert.assertSame;

/**
 * Test of RegisterService
 * Created by Michael on 11/18/2016.
 */
public class RegisterServiceTest {

    @Test
    public void testAddRegister() {
        RegisterService service = new RegisterService();
        Register register = new Register();
        // expect regiser returned
        final Register returned = service.addRegister(register);
        assertSame(register, returned);
    }

    @Test
    public void testRemoveRegister() {
        RegisterService service = new RegisterService();
        Register register = new Register();
        service.addRegister(register);
        Register removed = service.removeRegister(register);
        assertSame(register, removed);
    }

    @Test
    public void testAddShopperToRegister() {
        RegisterService service = new RegisterService();
        final Register register = new Register();
        service.addRegister(register);
        final Register shopperRegister = service.addShopperToRegister(new Shopper());

        // only one Register -- Shopper has to go to that one
        assertSame(register, shopperRegister);

    }
}
