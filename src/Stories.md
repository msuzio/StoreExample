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
   * taking a given quantity of an Item from the stock **(write)**
   ~~* Updating an item price (in place, effective immediately) **(write)**~~