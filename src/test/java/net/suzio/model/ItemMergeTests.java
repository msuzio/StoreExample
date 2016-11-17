package net.suzio.model;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for Iten.Merge() method; got too big to keep in ItemTest
 *
 * @see Item#merge(Item, Item)
 * Created by Michael on 11/16/2016.
 */
public class ItemMergeTests {
    @Test
    public void testMergeName() {
        // same name is OK
        final String bananas = "Bananas";
        final String grapes = "Grapes";

        // different names fails
        Item oneBanana = null;
        Item grapeItem = null;

        try {
            oneBanana = new Item(bananas, 0.99, 1, "LB");
            grapeItem = new Item(grapes, 2.99, 1, "LB");
        } catch (InvalidItemException e) {
            fail(TestUtil.ERROR_IN_TEST_CASE_INVALID_ITEM_CREATED + e.getMessage());
        }

        // Merging with different names returns original Item untouched
        final Item result = Item.merge(oneBanana, grapeItem);
        assertSame(oneBanana, result);
    }

    @Test
    public void testMergePrice() {
        // change price
        Item almonds = null;
        Item increasePrice = null;
        try {
            almonds = new Item("almonds", 4.99, 1, "jar");
            increasePrice = new Item("almonds", 10.99, 0, "jar");
        } catch (InvalidItemException e) {
            fail(TestUtil.ERROR_IN_TEST_CASE_INVALID_ITEM_CREATED + e.getMessage());
        }

        final Item repriced = Item.merge(almonds, increasePrice);
        assertNotNull(repriced);
        assertEquals("Updated Item has wrong name ", almonds.getName(), repriced.getName());
        assertEquals("Updated Item has wrong units ", almonds.getUnits(), repriced.getUnits());
        assertEquals("Updated Item has wrong price", 10.99, repriced.getPrice(), 0.0);
        assertEquals("Updated Item has wrong quantity", 1, repriced.getQuantity());

    }


    @Test
    public void testMergeQuantity() {
        // Test merging identical items and assert we now have one item with a quantity of 2
        final String bananas = "Bananas";
        Item oneBanana = null;
        Item noBanana = null;
        Item anotherBanana = null;
        Item sameBanana;

        // set up bananas
        try {
            oneBanana = new Item(bananas, 0.99, 1, "LB");
            noBanana = new Item(oneBanana.getName(), oneBanana.getPrice(), 0, oneBanana.getUnits());
            anotherBanana = new Item(oneBanana.getName(), oneBanana.getPrice(), 1, oneBanana.getUnits());
        } catch (InvalidItemException e) {
            fail(TestUtil.ERROR_IN_TEST_CASE_INVALID_ITEM_CREATED + e.getMessage());
        }

        // nothing added
        final Item noChange = Item.merge(oneBanana, noBanana);
        // Should not happen -- names are constant
        assertEquals("Updated Item has wrong name ", oneBanana.getName(), noChange.getName());
        assertEquals("Updated Item has wrong units ", oneBanana.getUnits(), noChange.getUnits());
        assertEquals("Updated Item has wrong price", oneBanana.getPrice(), noBanana.getPrice(), 0.0);
        assertEquals("Updated Item has wrong quantity", oneBanana.getQuantity(), noChange.getQuantity());

        // add together items
        final Item twoBanana = Item.merge(oneBanana, anotherBanana);
        assertNotNull(twoBanana);

        // Should not happen -- names are constant
        assertEquals("Updated Item has wrong name ", oneBanana.getName(), twoBanana.getName());
        assertEquals("Updated Item has wrong units ", oneBanana.getUnits(), twoBanana.getUnits());
        assertEquals("Updated Item has wrong price", oneBanana.getPrice(), twoBanana.getPrice(), 0.0);
        assertEquals("Updated Item has wrong quantity", 2, twoBanana.getQuantity());

        // merge with itself -- expect identical results as above (but different object references)
        sameBanana = oneBanana;
        final Item doubleBanana = Item.merge(oneBanana, sameBanana);

        assertEquals("Updated Item has wrong name ", oneBanana.getName(), doubleBanana.getName());
        assertEquals("Updated Item has wrong units ", oneBanana.getUnits(), doubleBanana.getUnits());
        assertEquals("Updated Item has wrong price", oneBanana.getPrice(), doubleBanana.getPrice(), 0.0);
        assertEquals("Updated Item has wrong quantity", 2, doubleBanana.getQuantity());

        // different, but logically equal
        assertTrue("Merge should have produced different object references", twoBanana != doubleBanana);
        final boolean equality = twoBanana.equals(doubleBanana);
        assertTrue("Merge should have produced logically equal object references", equality);
    }

    @Test
    public void testMergeNegativeQuantity() {
        // take away quantities
        // set up milk
        Item threeGallons = null;
        Item takeTwoGallons = null;
        Item takeFourGallons = null;

        try {
            threeGallons = new Item("milk", 2.99, 3, "Gallon");
            takeTwoGallons = new Item("milk", 2.99, -2, "Gallon");
            takeFourGallons = new Item("milk", 2.99, -4, "Gallon");
        } catch (InvalidItemException e) {
            fail(TestUtil.ERROR_IN_TEST_CASE_INVALID_ITEM_CREATED + e.getMessage());
        }
        final Item nowOneGallon = Item.merge(threeGallons, takeTwoGallons);
        assertNotNull(nowOneGallon);

        assertEquals("Updated Item has wrong name ", threeGallons.getName(), nowOneGallon.getName());
        assertEquals("Updated Item has wrong units ", threeGallons.getUnits(), nowOneGallon.getUnits());
        assertEquals("Updated Item has wrong price", threeGallons.getPrice(), nowOneGallon.getPrice(), 0.0);
        assertEquals("Updated Item has wrong quantity", 1, nowOneGallon.getQuantity());

        // negative quantity is OK
        final Item negativeGallons = Item.merge(threeGallons, takeFourGallons);
        assertNotNull(negativeGallons);

        assertEquals("Updated Item has wrong name ", threeGallons.getName(), negativeGallons.getName());
        assertEquals("Updated Item has wrong units ", threeGallons.getUnits(), negativeGallons.getUnits());
        assertEquals("Updated Item has wrong price", threeGallons.getPrice(), negativeGallons.getPrice(), 0.0);
        assertEquals("Updated Item has wrong quantity", -1, negativeGallons.getQuantity());
    }

    @Test
    public void testMergeUnit() {
        // Change unit measure
        Item bagOfGrapes = null;
        Item lbOfgrapes = null;
        try {
            bagOfGrapes = new Item("grapes", 5.99, 1, "bag");
            lbOfgrapes = new Item("grapes", 2.99, 0, "LB");
        } catch (InvalidItemException e) {
            fail(TestUtil.ERROR_IN_TEST_CASE_INVALID_ITEM_CREATED + e.getMessage());
        }

        Item newUnits = Item.merge(bagOfGrapes, lbOfgrapes);
        assertNotNull(newUnits);
        assertEquals("Updated Item has wrong name ", bagOfGrapes.getName(), newUnits.getName());
        assertEquals("Updated Item has wrong units ", newUnits.getUnits(), lbOfgrapes.getUnits());
        assertEquals("Updated Item has wrong price", lbOfgrapes.getPrice(), newUnits.getPrice(), 0.0);
        assertEquals("Updated Item has wrong quantity", 1, newUnits.getQuantity());
    }
}
