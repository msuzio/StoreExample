# Story List
##Iteration one:

~~* Create an Item object with Name,count,and quantity
   * Items should be immutable
   * Items should validate the name and category contain alphanumerics
   * Items should validate that price and quantity are greater than zero
* Create a Shopper that implements a shop() method and is Runnable
   * the shop() method does not need to perform any actions
* Create a Cart that can hold Items
* Create a Store that can:
   * be open or closed
   * accept Shoppers if open or let them wait if they want to (waiting must be able to be limited to a set number if desired)~~
 
## Iteration Two:

~~* Add Stock population,querying, and modifying methods to Store.~~
~~* All these methods must be synchronized for read/write~~
   ~~* adding an Item to the stock. **(write)**~~
   ~~* querying an Item from the stock list **(read)**~~
   * taking a given quantity of an Item from the stock **(write)** (Moved to Iteration 3)
   ~~* Updating an item price (in place, effective immediately) **(write)**~~
   
## Iteration Three:
### Focus

Implementing skeleton of Store/Shopper interactions

* Store
~~* add taking a given quantity of an Item from the stock **(write lock on stock data)**~~
* Shopper
 ~~* Assembles a shopping list of Items~~
 ~~* tries to take all those Items from Store stock and add them to Cart~~
 ~~* Modifies shopping list to remove Items and quantity it actually managed to buy (we assume checkout completes successfully right now)~~
 ~~* Can report what Items it did not find from list.~~
 
## Iteration Four:
### Focus

Correcting bad design points and poor structure

~~* Item should not throw an Exception in the constructor.
   * This mis-feature is spread out smongst the code base.  Touch all areas as needed.~~
   
## Iteration Five:
### Focus
 
 Correct package structure
 
 ~~* Move all net.suzio.* packages to net.suzio.store.*~~
 
 
## Backlog / Discussion points
 * Register implementation and checkout with Receipt.
     * Implies we also have a method of submitting Shoppers to a pool of Registers
 * Consider if all Item list operations should really be consistently Maps rather than constant implicit folding of quantities 
 * Shoppers should exit the store so Store.close() really knows it can complete close operation
    * This implies a"closing" state
    * Also implies Store keep track of Shoppers currently in the Store. This may be more complex than desirable.
 * Add logging (split into multiple stories as needed)
 * Move stock control out of Store, removing most of the threading concerns
    * methods should be suitable for any datasource, mirorring CRUD focus
    * Start with simple version of this Service/Repository, even possibly just a move of logic from Store keeping existing in-memory map
 * All Producer/Consumer logic remains undone (in favor of getting code committed now)
  * Shopper should be Runnable, and waiting case should really wait (wait()/notify()?) 
  * Store should empty waiting Shopper queue on opening, and signal waiting threads to continue
  * Registers keep a thread-safe queue of Shoppers, and dequeue as needed.
  
  
