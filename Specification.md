# Specification
This module models a store with items,Shoppers,registers (checkout lanes) and receipts.

## Behaviors

## Store
### Store states
* A store may be open or closed.
   * When a store is closed, arriving Shoppers who desire to wait may (optionally) queue up sequentially and are allowed inside in that order once the store opens. 
   * An open Store always accepts Shoppers desiring to shop, bypassing any wait step.
   * Shoppers perform their shopping steps in an undefined ~~~concurrent~~~ order
   * A Store may be assumed to be of infinite size and able to accommodate as many Shoppers as desired once open
   * When an open store closes, all existing Shoppers (store wide) are allowed to checkout, but no new Shoppers are allowed in.
   * an explicitly closed Store (one that close() is called on) should then refuse to queue up Shoppers so their shopping attempts fail.
    
## Shopper behavior
* Shoppers desire to enter a Store and shop.  They cannot enter a closed store.
* A Shopper can (optionally) wait if a Store is closed. This request may be refused if the Store desires to limit the size of the wait line.
* A Shopper that is refused entry and does not (or is refused permission to) enter a wait line must terminate the shopping attempt.
* Shoppers have a list of desired items and quantities thereof.
   * Shoppers attempt to purchase all the items on their shopping list.
   * Their behavior when an item is not in stock with a desired quantity should not be assumed 
* After checkout, a Shopper is expected to have an empty Cart and a shopping list with any purchased items removed or decremented


### Store stock
* The Store contains a stock of Items
* Items have a name, price, and unit of measure. (lb,each,bottle -- this is merely descriptive)
* The Store should stock Items before opening.
     * This stocking step is not the responsibility of the Store, it expects to be stocked by an external source entirely decoupled from its logic
     * Stocking the Store is merely desirable, not required; an empty Store simply results in all Shoppers failing to clear their shopping list
* Item quantities are decremented as Shoppers take them off the shelf and add them to their Cart, putting a new Item there
    * Items with zero quantity should be retained in the stock list so Registers can access their information
    * if a Shopper does not checkout, the items should be returned to the stock (this use case is optional)

### Registers and Checkout
* A Store contains multiple Registers
* Registers service a line of Shoppers (a queue).
   * Assignment of Shoppers to a Register is not specified, ***except*** Shoppers will not leave a Register queue once in.
* Registers control the Shopper checkout process
* Checkout is an uninterruptible process; if the process is interrupted, the state of the Store and the Shopper are undefined
* steps of checkout:
    * The Register looks up Items from the Shopper's cart iteratively, but not necessarily in any expected defined ordering
    * All Item information from the Cart is taken as correct; price changes or unit changes made to the Store stock after placement in the Cart are not seen by the Register
    * __The above may not be realistic, but the edge cases severely complicate matters and handling them is unclear__
* At the end of checkout, the Register returns a Receipt the Shopper should retain

### Receipt
* A Receipt takes a collection of Items and generates a summary

```
 Example receipt:
 Fruit -- Banana   2 lbs@ .99/lb    1.98
 Juice -- Orange juice              3.99
 Health foods -- Almonds,Jar        5.99
                                    ----
                       Total:      $11.96 
  ```
