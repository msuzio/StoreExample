package net.suzio.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Models an Item in a store inventory.
 * Item objects may only be created via the public constructor, and are immutable by design
 * to maintain data integrity.  Use cases where a state change is required should be done through creation of a new Item.
 *
 * @author Michael Suzio
 */
public class Item {
    private String name;
    private double price;
    private int quantity;
    private String units;

    private static final Pattern CONTAINS_AlPHA_NUMERIC;

    static {
        // Precompile to reduce overhead
        // Safe to use in multiple threads; the matcher from this is not
        // Can't just use \w, that would match something like "____"
        CONTAINS_AlPHA_NUMERIC = Pattern.compile("[A-Za-z0-9]+");
    }

    // for internal use
    private Item() {

    }

    /**
     * Constructor.Once created, instances are immutable
     *
     * @param name     Name of item
     * @param price    Price per unit of item
     * @param quantity number of units of the Item in question.
     * @param units    Description of unit measure; purely informative
     * @throws InvalidItemException error if item information is invalid
     */
    public Item(String name, double price, int quantity, String units) throws InvalidItemException {
        super();
        if (!validateString(name)) {
            throw new InvalidItemException("Name '" + "' is not valid (must have at least one alphanumeric character)");
        }
        this.name = name;
        // note that zero is a legitimate price (freebies!)
        if (price < 0) {
            throw new InvalidItemException("Price cannot be negative");
        }
        this.price = price;
        this.quantity = quantity;
        if (!validateString(units)) {
            throw new InvalidItemException("units '" + "' are not valid (must have at least one alphanumeric character)");
        }
        this.units = units;
    }

    private boolean validateString(String value) {
        value = value != null ? value.trim() : null;
        return (value != null) && (!value.isEmpty()) && matchesAlphaNumeric(value);
    }

    private boolean matchesAlphaNumeric(String value) {
        Matcher m = CONTAINS_AlPHA_NUMERIC.matcher(value);
        return m.find();
    }

    // Currently no setters; it is assumed we would prefer nothing in our application mutate an Item directly
    public String getUnits() {
        return units;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    /**
     * Merge a new set of data with an existing Item, returning a new Item.
     * Quantities will be added if the result is a positive value
     * Price and units will be updated.
     *
     * @param into Item to merge properties into
     * @param from Item to merge properties from, overriding or modifying the properties of into.
     * @return Merge a new set of data with this Item, returning a new Item.
     * @throws InvalidItemException If the two items are not identically named
     */
    public static Item merge(Item into, Item from) throws InvalidItemException {
        Item item = new Item();
        if (!(into.getName().equals(from.getName()))) {
            throw new InvalidItemException("Merged attempted on two non-identical Items. Original item == '" + into.name + "'"
                    + "Item to merge from = '"
                    + from.name + "'");
        } else {
            item.quantity = into.quantity + from.quantity;
            item.name = from.name;
            item.price = from.price;
            item.units = from.units;
        }
        return item;
    }
}
