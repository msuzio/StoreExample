package net.suzio.store.model;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Test Receipt class
 * Created by Michael on 11/17/2016.
 */
public class ReceiptTest {
    @Test
    public void testOrderByPrice() {
        Receipt receipt = new Receipt();

        String bananas = "Bananas";
        String grapes = "Grapes";
        List<Item> items = new ArrayList<>();
        Item oneBanana = new Item(bananas, 0.99, 1, "LB");
        Item lbOfGrapes = new Item(grapes, 2.99, 1, "LB");
        items.add(oneBanana);
        items.add(lbOfGrapes);
        receipt.addItems(items);

        // expect to be returned in price order, not insertion order
        List<String> itemizedLines = receipt.getItemizedLines();
        assertNotNull("Itemized line list was null", itemizedLines);
        assertFalse("Itemized line list was empty", itemizedLines.isEmpty());
        assertEquals("Itemized line list was not of expected size", items.size(), itemizedLines.size());
        String line1 = itemizedLines.get(0);
        String line2 = itemizedLines.get(1);

        // Needs tests for summed price, but we don't want to bind this test too much to exact formatting
        // until Receipt accepts parametrized formatting we can externally control.
        // Expecting the item name and price at a bare minimum works for now.
        assertTrue("Line item does not contain expected item name " + grapes, line1.contains(grapes));
        assertTrue("line item does not contain expected unit price " + lbOfGrapes.getPrice(), line1.contains(String.valueOf(lbOfGrapes.getPrice())));
        assertTrue("Line item does not contain expected item name " + bananas, line2.contains(bananas));
        assertTrue("line item does not contain expected unit price " + oneBanana.getPrice(), line2.contains(String.valueOf(oneBanana.getPrice())));
    }

    @Test
    public void testOrderByPriceAndName() {
        Receipt receipt = new Receipt();

        String bananas = "Bananas";
        String grapes = "Grapes";
        String milk = "Milk";
        List<Item> items = new ArrayList<>();
        Item oneBanana = new Item(bananas, 0.99, 1, "LB");
        Item lbOfGrapes = new Item(grapes, 2.99, 1, "LB");
        Item gallonOfMilk = new Item(milk, 2.99, 1, "Gallon");
        items.add(oneBanana);
        items.add(lbOfGrapes);
        items.add(gallonOfMilk);
        receipt.addItems(items);

        // expect to be returned in price and Name (descending) order, not insertion order
        List<String> itemizedLines = receipt.getItemizedLines();
        assertNotNull("Itemized line list was null", itemizedLines);
        assertFalse("Itemized line list was empty", itemizedLines.isEmpty());
        assertEquals("Itemized line list was not of expected size", items.size(), itemizedLines.size());
        String line1 = itemizedLines.get(0);
        String line2 = itemizedLines.get(1);
        String line3 = itemizedLines.get(2);

        // Needs tests for summed price, but we don't want to bind this test too much to exact formatting
        // until Receipt accepts parametrized formatting we can externally control.
        // Expecting the item name and price at a bare minimum works for now.
        assertTrue("Line item does not contain expected item name " + grapes, line1.contains(grapes));
        assertTrue("line item does not contain expected unit price " + lbOfGrapes.getPrice(), line1.contains(String.valueOf(lbOfGrapes.getPrice())));
        assertTrue("Line item does not contain expected item name " + milk, line2.contains(milk));
        assertTrue("line item does not contain expected unit price " + gallonOfMilk.getPrice(), line2.contains(String.valueOf(gallonOfMilk.getPrice())));
        assertTrue("Line item does not contain expected item name " + bananas, line3.contains(bananas));
        assertTrue("line item does not contain expected unit price " + oneBanana.getPrice(), line3.contains(String.valueOf(oneBanana.getPrice())));
    }

    @Test
    public void testSumTotal() {
        // We assume the double values are not repeating values like (1 -0.9)
        double[] validPrices = {
                // no pennies
                4.00,
                // one penny
                4.01,
                // just pennies
                0.03,
                // two digits of pennies
                4.99,
        };
        for (double price : validPrices) {
            Receipt receipt = new Receipt();
            List<Item> items = new ArrayList<>();
            Item item1 = new Item("one", price, 1, "LB");
            Item item2 = new Item("two", price, 1, "LB");
            items.add(item1);
            items.add(item2);
            receipt.addItems(items);

            // See if *any* price is in the results
            // Should have a literal $ followed by any number of digits, then pennies
            // *somewhere* in there
            String receiptSum = receipt.getFormattedTotal();
            Pattern p = Pattern.compile("\\$\\d+\\.\\d\\d$");
            Matcher m = p.matcher(receiptSum);
            assertTrue("Formatted total does not match expected pattern " + p, m.matches());
            String expectedSum = String.valueOf(item1.getPrice() + item2.getPrice());
            // assert that the *correct value is in there
            assertTrue(receiptSum.contains(expectedSum));
        }
    }
}
