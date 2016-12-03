# Story List
##Iteration one:

* ~~Create an Item object with Name,count,and quantity~~
   * ~~Items should be immutable~~
   * ~~Items should validate the name and category contain alphanumerics~~
   * ~~Items should validate that price and quantity are greater than zero~~
* ~~Create a Shopper that implements a shop() method and is Runnable~~
   * ~~the shop() method does not need to perform any actions~~
* ~~Create a Cart that can hold Items~~
* ~~Create a Store that can:~~
   * ~~be open or closed~~
   * ~~accept Shoppers if open or let them wait if they want to (waiting must be able to be limited to a set number if desired)~~
 
## Iteration Two:

* ~~Add Stock population,querying, and modifying methods to Store.~~
* ~~All these methods must be synchronized for read/write~~
    * ~~adding an Item to the stock. **(write)**~~
    * ~~querying an Item from the stock list **(read)**~~
    * taking a given quantity of an Item from the stock **(write)** (Moved to Iteration 3)
    * ~~Updating an item price (in place, effective immediately) **(write)**~~
   
## Iteration Three:
### Focus

Implementing skeleton of Store/Shopper interactions

* Store
 * ~~add taking a given quantity of an Item from the stock **(write lock on stock data)**~~
* Shopper
 * ~~Assembles a shopping list of Items~~
 * ~~Tries to take all those Items from Store stock and add them to Cart~~
 * ~~Modifies shopping list to remove Items and quantity it actually managed to buy (we assume checkout completes successfully right now)~~
   * ~~Can report what Items it did not find from list.~~
 
## Iteration Four:
### Focus

Correcting bad design points and poor structure

* ~~Item should not throw an Exception in the constructor.~~
   * ~~This mis-feature is spread out amongst the code base.  Touch all areas as needed.~~
  
   
## Iteration Five:
### Focus
 
 Correct package structure
 
 * ~~Move all net.suzio.* packages to net.suzio.store.*~~
 
## Iteration Six:
### Focus
Checkout steps, minor cleanups
 
* Register
      * ~~Accepts Shoppers into queue~~
      * ~~Checks out Shoppers~~
      * ~~examines Items in the Cart, generating a Receipt~~
* Receipt
      ~~* generates a line-item list of purchased Items~~
      ~~* returns a summarized total~~
      * ~~Shopper takes Receipt as final step of checkout~~
      * ~~Receipt generate line items and formatted total~~
* Cleanup
    * ~~Remove unused classes~~
    * ~~Correct Markdown in all documents~~
    * ~~Resolve typos where flagged~~

### Comment

Git commit was severely delayed by lone developer having Thanksgiving week off. 

## Iteration Seven
### Focus

Design and documentation

* ~~Update Dataflow document with textual dissection of the moving parts.~~
 ~~* Add Stories from Backlog to target Iteration Eight in a coherent manner~~
* __NO__ code changes

## Iteration Eight

### Focus

Convert Shopper to fit updated design

~~* shopping steps go in correct order~~
   ~~* _shop_~~
   * ~~_checkout_~~
   * ~~_exit_~~
* ~~stub out code relevant to running in a multithreaded context~~
*~~execute correctly in a single-threaded context~~
* ~~Store logic changes to suit this and allow unit tests to run, but no more than that~~
   * ~~Store need not track known Shoppers yet~~

## Iteration Nine

### Focus

Convert Store and Register to fit updated design

* ~~Link Registers to Store, closing the total Shopping loop~~
* ~~Register checkout calls Shopper exit logic~~
* Store tracks active shoppers __(unclear on design goal; moved to next Iteration)__
   * Adds them when they start shopping
   * Removes them when they exit
* ~~All components must still be able to execute in single-threaded mode for testing.~~
* ~~cleanup Store -- methods should be logically grouped.~~

## Iteration Ten
### Focus

Clean up tests , address code inspections, and revisit specification to review concerns from Iteration Nine

* Cleanup overly coupled tests (use Mockito mocks)
   * Register Test
   * ShopperTest
   * SimpleShoppingProcessTests
* Review all inspections in project and fix or (very selectively) suppress where appropriate.
* Check logic in Store/Shopper/Register against dataflow and choose if Store needs to track Shoppers
   * this is directly to address dangling story from Iteration Nine

 
## Backlog / Discussion points
 * Assignment of Shoppers to Registers should be done from a pool of available Registers
     * This should be arbitrated somehow; specification right now says this should  be a behavior of the Shopper
 * Consider if all Item list operations should really be consistently Maps rather than constant implicit folding of quantities 
 * Shoppers should exit the store so Store.close() really knows it can complete close operation
    * This implies a"closing" state
    * Also implies Store keep track of Shoppers currently in the Store. This may be more complex than desirable.
    * All Producer/Consumer logic remains undone (in favor of getting code committed now)
      * Shopper should be Runnable, and waiting case should really wait (wait()/notify()?) 
      * Store should empty waiting Shopper queue on opening, and signal waiting threads to continue
      * Registers keep a thread-safe queue of Shoppers, and dequeue as needed.
 * Move stock control out of Store, removing most of the threading concerns
     * methods should be suitable for any datasource, mirroring CRUD focus
     * Start with simple version of this Service/Repository, even possibly just a move of logic from Store keeping existing in-memory map
 * Add logging (split into multiple stories as needed)
  
  
