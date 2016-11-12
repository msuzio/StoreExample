package net.suzio.model;

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
	private ItemUnit units;

    /**
     * Constructor.
     * Once created, instances are immutable.
     * There is no validation of arguments currently; the Inventory object is the generally single point of responsiblity
     * in the application for creating new instances. This may change in the future.
     * @see net.suzio.service.Inventory
     * @param name
     * @param price
     * @param units
     * @param category
     */
	public Item(String name, double price, ItemUnit units, String category) {
		super();
		this.name = name;
		this.price = price; // we're OK with a zero price; loading process should vsalidate that
        this.units = units;
		this.category = category;
	}

    public ItemUnit getUnits() {
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
        return units == item.units;

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
