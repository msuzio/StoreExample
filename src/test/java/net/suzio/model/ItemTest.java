package net.suzio.model;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
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

    @Test
    public void testMerge() {
        // Test merging identical items and assert we now have one item with a quantity of 2.
        try {
            Item oneBanana = new Item("Bananas", 0.99, 1, "LB");
            Item anotherBanana = new Item("Bananas", 0.99, 1, "LB");
            Item twoBanana = Item.merge(oneBanana, anotherBanana);
            // Should not happen
            assertEquals("Updated Item has wrong name ", oneBanana.getName(), twoBanana.getName());
            assertEquals("Updated Item has wrong units ", oneBanana.getUnits(), twoBanana.getUnits());
            assertEquals("Updated Item has wrong price", oneBanana.getPrice(), twoBanana.getPrice(), 0.0);
            assertEquals("Updated Item has wrong quantity", 2, twoBanana.getQuantity(), 0.0);


            // change price
            Item almonds = new Item("almonds", 4.99, 1, "jar");
            Item increasePrice = new Item("almonds", 10.99, 0, "jar");
            Item repriced = Item.merge(almonds, increasePrice);
            assertEquals("Updated Item has wrong name ", almonds.getName(), repriced.getName());
            assertEquals("Updated Item has wrong units ", almonds.getUnits(), repriced.getUnits());
            assertEquals("Updated Item has wrong price", 10.99, repriced.getPrice(), 0.0);
            assertEquals("Updated Item has wrong quantity", 1, repriced.getQuantity(), 0.0);

            // Change unit measure
            Item bagOfGrapes = new Item("grapes", 5.99, 1, "bag");
            Item lbOfgrapes = new Item("grapes", 2.99, 0, "LB");
            Item newUnits = Item.merge(bagOfGrapes, lbOfgrapes);
            assertEquals("Updated Item has wrong name ", bagOfGrapes.getName(), newUnits.getName());
            assertEquals("Updated Item has wrong units ", newUnits.getUnits(), lbOfgrapes.getUnits());
            assertEquals("Updated Item has wrong price", lbOfgrapes.getPrice(), newUnits.getPrice(), 0.0);
            assertEquals("Updated Item has wrong quantity", 1, newUnits.getQuantity(), 0.0);

        } catch (InvalidItemException e) {
            fail("Error in test case -- invalid Item created: " + e.getMessage());
        }
    }

}
