package net.suzio.model;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.fail;


public class ItemTest {
    /**
     * Check our item validations
     */
    @Test
    @SuppressWarnings("UnusedAssignment")
    public void testItemValidation() {
        Item i;

        String[] invalidStrings = new String[]{null, "", "_", "&", "-"};

        for (String invalid : invalidStrings) {

            // Invalid names
            try {
                i = new Item(invalid, 9.0, 1, "category");
                fail("Constructor allowed creation of invalid item with " +
                        "name'" + invalid +
                        "' -- should validate it is not null and contains an alphanumeric");
            } catch (InvalidItemException e) {
                // expected
            }

            // invalid categories
            try {
                i = new Item("valid", 9.0, 1, invalid);
                fail("Constructor allowed creation of invalid item with " +
                        "category'" + invalid +
                        "' -- should validate it is not null and contsins an alphanumeric");
            } catch (InvalidItemException e) {
                // expected
            }

            // negative price
            try {
                i = new Item("valid", -9.0, 1, "OK");
                fail("Constructor allowed creation of invalid item with negative price");
            } catch (InvalidItemException e) {
                // expected
            }

            // zero price
            try {
                i = new Item("OK", 0, 1, "OK");
            } catch (InvalidItemException e) {
                fail("Item constructor failed to create valid uitem with price of 0");
            }

            // zero quantity, negative quantity
            try {
                i = new Item("OK", 0, 1, "OK");
            } catch (InvalidItemException e) {
                fail("Item constructor failed to create valid item with price of 0");
            }

            try {
                i = new Item("OK", 9.99, 0, "OK");
            } catch (InvalidItemException e) {
                fail("Item constructor failed to create valid item with quantity of 0");
            }

            try {
                i = new Item("OK", 9.99, -1, "OK");
            } catch (InvalidItemException e) {
                fail("Item constructor failed to create valid item with quantity of -1");
            }
        }
    }

    /**
     * As Items will be passed around elements of the system frequently, they should be barred from
     * direct manipulation once created
     */
    @Test
    public void testItemImmutable() {
        Class clazz = Item.class;

	/* 
     * naive assumption to start -- look at all fields, ensure there is no Bean type method corresponding to them.
	 * This keeps a robust test that will serve to warn us if we might stray from our contract and make us validate intent.
	 */
        Field[] fields = clazz.getDeclaredFields();
        Method[] methods = clazz.getDeclaredMethods();
        Set<String> methodNames = Stream.of(methods)
                .map(Method::getName)
                .filter(n -> n.startsWith("set"))
                .collect(Collectors.toSet());

        for (Field f : fields) {
            String name = f.getName();

            String probeName = "set" + name.substring(0, 1).toUpperCase() + name.substring(1, name.length());

            if (methodNames.contains(probeName)) {
                fail("Field '" + name + "' appears to have a setter '" + probeName + "'");
            }
        }
    }
}
