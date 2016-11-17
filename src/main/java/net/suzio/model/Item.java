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

    // Precompile to reduce overhead
    // Safe to use in multiple threads; the matcher from this is not
    // Can't just use \w, that would match something like "____"
    private static final Pattern CONTAINS_AlPHA_NUMERIC = Pattern.compile("[A-Za-z0-9]+");

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
     * @return Merge a new set of data with this Item, returning a new Item..  If Item names do not match, returns into Item unchanged
     */
    public static Item merge(Item into, Item from) {
        if (into == null || from == null) {
            return into;
        }

        Item item = new Item();
        if (!(into.getName().equals(from.getName()))) {
            return into;
        } else {
            // For now, negative results are accepted and use cases judge if this makes sense
            item.quantity = into.quantity + from.quantity;
            item.name = from.name;
            item.price = from.price;
            item.units = from.units;
        }
        return item;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Item item = (Item) o;

        //IDEA SUGGESTED THIS TRICKY FORM *AFTER* AUTO-GENERATING A MORE VERBOSE FORM
        //Might be a bad inspection to leave on or perhaps it needs tuning
        return (Double.compare(item.price, price) == 0)
                && (quantity == item.quantity)
                && name.equals(item.name)
                && units.equals(item.units);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = name.hashCode();
        temp = Double.doubleToLongBits(price);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + quantity;
        result = 31 * result + units.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Item{");
        sb.append("name='").append(name).append('\'');
        sb.append(", price=").append(price);
        sb.append(", quantity=").append(quantity);
        sb.append(", units='").append(units).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
