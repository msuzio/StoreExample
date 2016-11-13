package net.suzio.model;

import org.junit.Test;

import java.lang.reflect.Constructor;

import static org.junit.Assert.*;

/**
 * Should not be testing overall system behavior; that is too encompassing for test cases just to validate our contract
 * Created by Michael on 11/12/2016.
 */
public class StoreTest {

    @Test
    public void testIsSingleton() {
        // no public constructor
        Constructor<?>[] constructors = Store.class.getConstructors();
        assertEquals("Error in Store singleton implemention. Mumber of constructors",0,constructors.length);

        // instances from getInstance are same object
        Store s1 = Store.getInstance();
        Store s2 = Store.getInstance();

        assertSame(s1,s2);
    }

    @Test
    public void storeStartsClosed() {
        Store store = Store.getInstance();
        assertFalse("Store.isOpen returns true on initialization.",store.isOpen());
        assertTrue("Store.isClosed returns false on initialization.",store.isClosed());
    }
}
