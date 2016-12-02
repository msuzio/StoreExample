package net.suzio.store.model;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for Iten.Merge() method; got too big to keep in ItemTest
 *
 * @see Item#merge(Item, Item) Created by Michael on 11/16/2016.
 */
public class ItemMergeTests {
    @Test
    public void testMergeName() {
        // same name is OK
        final String bananas = "Bananas";
        final String grapes = "Grapes";

        Item oneBanana = new Item(bananas, 0.99, 1, "LB");
        Item grapeItem = new Item(grapes, 2.99, 1, "LB");

        // Merging with different names returns original Item untouched
        final Item result = Item.merge(oneBanana, grapeItem);
        assertSame(oneBanana, result);
    }

    @Test
    public void testMergePrice() {
        // change price
        Item almonds = new Item("almonds", 4.99, 1, "jar");
        Item increasePrice = new Item("almonds", 10.99, 0, "jar");

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

        // set up bananas
        Item oneBanana = new Item(bananas, 0.99, 1, "LB");
        Item noBanana = new Item(oneBanana.getName(), oneBanana.getPrice(), 0, oneBanana.getUnits());
        Item anotherBanana = new Item(oneBanana.getName(), oneBanana.getPrice(), 1, oneBanana.getUnits());

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
        final Item doubleBanana = Item.merge(oneBanana, oneBanana);

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
        Item threeGallons = new Item("milk", 2.99, 3, "Gallon");
        Item takeTwoGallons = new Item("milk", 2.99, -2, "Gallon");
        Item takeFourGallons = new Item("milk", 2.99, -4, "Gallon");

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
        Item bagOfGrapes = new Item("grapes", 5.99, 1, "bag");
        Item lbOfgrapes = new Item("grapes", 2.99, 0, "LB");


        Item newUnits = Item.merge(bagOfGrapes, lbOfgrapes);
        assertNotNull(newUnits);
        assertEquals("Updated Item has wrong name ", bagOfGrapes.getName(), newUnits.getName());
        assertEquals("Updated Item has wrong units ", newUnits.getUnits(), lbOfgrapes.getUnits());
        assertEquals("Updated Item has wrong price", lbOfgrapes.getPrice(), newUnits.getPrice(), 0.0);
        assertEquals("Updated Item has wrong quantity", 1, newUnits.getQuantity());
    }
}
