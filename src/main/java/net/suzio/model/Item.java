package net.suzio.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Models an Item in a store inventory.
 * Item objects may only be created via the public constructor, and are immutable by design 
 * to maintain data integrity
 * 
 * @author Michael Suzio
 *
 */
public class Item {
	private String name;
	private double price;
	private String category;
	private String units;

    private static final Pattern CONTAINS_AlPHA_NUMERIC;
    static
    {
        // Precompile to reduce overhead
        // Safe to use in multiple threads; the matcher from this is not
        // Can't just use \w, that would match something like "____"
        CONTAINS_AlPHA_NUMERIC = Pattern.compile("[A-Za-z0-9]+");
    }

    /**
     * Constructor.Once created, instances are immutable
     * @param name Name of item
     * @param price Price per unit of item
     * @param units Description of unit measure; purely informative
     * @param category Description of item category
     * @throws InvalidItemException error if item information is invalid
     */
	public Item(String name, double price, String units, String category) throws InvalidItemException {
		super();
        if (!validateString(name)) {
            throw new InvalidItemException("Name '" + "' is not valid (must have at least one alphanumeric character)");
        }
		this.name = name;
        // note that zero is a legitimate price (freebies!)
        if (price < 0){
            throw new InvalidItemException("Price cannot be negative");
        }
		this.price = price;
        if (!validateString(units)) {
            throw new InvalidItemException("units '" + "' are not valid (must have at least one alphanumeric character)");
        }
        this.units = units;
        if (!validateString(category)) {
            throw new InvalidItemException("Category '" + "' is not valid (must have at least one alphanumeric character)");
        }
        
		this.category = category;
	}

    private boolean validateString(String value) {
        value = value != null? value.trim() :null;
        return (value != null) && (!value.isEmpty()) && matchesAlphaNumeric(value);
    }

    private boolean matchesAlphaNumeric(String value) {
        Matcher m = CONTAINS_AlPHA_NUMERIC.matcher(value);
        return m.find();
    }

    public String getUnits() {
        return units;
    }

    // Currently no setters; it is assumed we would prefer nothing in our application
	// change the attributes of items once they are loaded into inventory.
	// (i.e., the only process loading Components is the InventoryReader)
	public String getName() {
		return name;
	}
	public double getPrice() {
		return price;
	}
	public String getCategory() {
		return category;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Item item = (Item) o;

        if (Double.compare(item.price, price) != 0) return false;
        if (!name.equals(item.name)) return false;
        if (!category.equals(item.category)) return false;
        return units.equals(item.units);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = name.hashCode();
        temp = Double.doubleToLongBits(price);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + category.hashCode();
        result = 31 * result + units.hashCode();
        return result;
    }
}
