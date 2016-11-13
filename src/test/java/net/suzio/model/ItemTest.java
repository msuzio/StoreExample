package net.suzio.model;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.Assert.fail;

public class ItemTest {
    /**
     * Check our item validations
     */
    @Test
    public void testItemValidation() {
        Item i;
        try {
            i = new Item(null, 9.0, "units", "category");
            fail("Constructor allowed creation of invalid item; name should not be null");
        } catch (InvalidItemException e) {
            // expected
        }

        String[] invalidStrings = new String[]{null, "_", "&", "-"};
        for (String invalid : invalidStrings) {
            try {
                i = new Item(invalid, 9.0, "units", "category");
                fail("Constructor allowed creation of invalid item with +" +
                        "name'" +
                        "' -- should validate it is not null and contsins an alphanumerican alphanumeric");
            } catch (InvalidItemException e) {
                // expected
            }
        }
    }

        /**
         * As Items will be passed around elements of the system frequently, they should be barred from
         * direct manipulation once created (the creation and inventory population assumed to be
         * implemented in a process outside this scope)
         */
        @Test
        public void testItemImmutable () {
            Class clazz = Item.class;

	/* 
     * naive assumption to start -- look at all fields, ensure there is no Bean type method corresponding to them.
	 * This keeps a robust test that will serve to warn us if we might stray from our contract and make us validate intent.
	 */

            Field[] fields = clazz.getFields();

            for (Field f : fields) {
                String name = f.getName();
                Method[] methods = clazz.getMethods();

                String probeName = "get" + name.substring(0,1).toUpperCase() + name.substring(1, name.length() - 1);
                for (Method m : methods) {
                    if (m.getName().equals(probeName)) {
                        fail("Field '" + name + "' appears to have a setter '" + m.getName());
                    }
                }
            }
        }
    }
