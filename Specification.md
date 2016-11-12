# Specification
This module models a store with items,customers,registers (checkout lanes) and receipts.

## Behaviors

### Inventory
Item information is stored in a central inventory.
+ Items have a name, price, and unit of measure. (lb,each,bottle -- this is merely descriptive)

#### Inventory loading
+ Inventory must be loaded up front to start with a known set of stocked items and quantities of those items
+ The programmatic loading of this data must be flexible, as datasources may change

### Inventory State Changes
+ Customers shop for items.
+ Inventory is marked as depleted as customers take them off the shelf and add them to their cart
+ if a customer does not checkout, the items are assumed to be returned to the Inventory by stock boys
 + The restocking takes an undefined amount of time

### Registers and Inventory
The registers can access the inventory and look up items to get pricing.

## Store behavior
A store may be open or closed.

When a store is closed, arriving customers queue up sequentially and are allowed inside in that order once the 
store opens. When an open store closes, all existing customers (storewide) are allowed to checkout, but 
no new customers are allowed in.

## Customer behavior
Customers shop for varying (unspecified) periods of time, taking items off the shelf, until they decide to checkout and
pay at a register.

## Registers and customer checkout
+ Registers service a line of customers (a queue).
+ Customers may choose a register line to join in a variety of behaviors that can change over time.
+ A register generates a receipt summarizing the list of items, pricing per item, and final total.
+ Multiple items of the same name are aggregated into multiple counts of the item on the receipt

'Example receipt:
 Fruit -- Banana   2 lbs@ .99/lb    1.98
 Juice -- Orange juice              3.99
 Health foods -- Almonds,Jar        5.99
                                    ----
                       Total:      $11.96'
