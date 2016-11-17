package net.suzio.model;

import net.suzio.model.util.ItemUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Model a Shopper that performs a shopping task
 *
 * Created by Michael on 11/13/2016.
 */
//TODO -- EVENTUALLY THIS MUST BE MULTI-THREADED AND THREAD-SAFE
public class Shopper { //implements Runnable {

    private Store store = null;
    private boolean waitable = false;
    // probably could just be a regular map; that change only affects #getShoppingList, since shop() operation is otherwise single-threaded if it implements Runnable as expected and nothing outside this class has writeable accessors
    private ConcurrentHashMap<String, Item> shoppingMap = new ConcurrentHashMap<>();
    private Cart cart = new Cart();

    // Accept a List in our constructors simply because that is more direct to our intent and easier to construct inside our tests;
    // I think it's irrelevant that our internal model uses a HashMap

    /**
     * Create a Shopper to operate with an assigned Store.  waiting behavior defaults to not-waiting, and failing fast against a closed Store
     *
     * @param store    Store we operate on
     * @param itemList List of Items we want to buy
     */
    public Shopper(Store store, List<Item> itemList) {
        this.store = store;
        if (itemList != null) {
            // Fold repeats and hash by names for easy retrieval
            shoppingMap.putAll(ItemUtil.itemstoMap(itemList));
        }
    }

    /**
     * Create a Shopper to operate with an assigned Store, with a set waiting behavior
     *
     * @param store    Store we operate on
     * @param itemList List of Items we want to buy
     * @param waitable true/false indicator of whether this Shopper waits in line at a closed Store
     */
    public Shopper(Store store, List<Item> itemList, boolean waitable) {
        this(store, itemList);
        this.waitable = waitable;
    }

    /**
     * @return indication if this Shopper waits for Store to open
     */
    public boolean isWaitable() {
        return waitable;
    }

    /**
     * Get our shopping list.  In general, this method should not be called while our shop() method is running, as results are indeterminant
     *
     * @return Copy of (immutable) shopping list Items. Multiple instances of a single Item will have been folded into a single Item
     */
    protected List<Item> getShoppingList() {
        // Parallelism threshold is basically, none -- this is an operation  not truly meant for access while shop() method executes
        // and should be kept protected (only because then it keeps class testable)
        // alternative is read/write locking, which is correct behavior but not needed *yet* and would slowdown shopping which is otherwise single-threaded
        //RTODO - KEEP AN EYE ON THESE ASSUMPTIONS
        List<Item> listItems = new ArrayList<>();
        shoppingMap.forEachValue(1, listItems::add);
        return listItems;
    }

    /**
     * Get the cart the shopping process has filled
     * Cautions of #getShoppingList apply, although calling this method as shop() executes simply means we will not get a view of the complete shopping run.
     *
     * @return Cart Shopper has put items into as shopping executed. cart state should not be mutated by callers (@see Cart#addItem)
     */
    protected Cart getCart() {
        return cart;
    }

    /**
     * Perform the steps of shopping throughout a Store
     * This method should be able to execute in a multi-threaded context
     */
    public void shop() {
        // try to enter;
        final boolean allowedEntrance = store.enter(this);
        if (allowedEntrance) {

            // Defer all actual multi-threaded behaviors of this clas itself, although data structures must be thread-safe
//            if (waitable) {
//                store.addWaitingShopper(this);
//                // eventually we want to block until we get to go into Store
//                // consider how this affects case of Store never opening
//                try {
//                    Thread.currentThread().wait();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }

            // We will do this for each Item in our shopping List:
            // -- try to take the Item from the Store
            // -- merge results with our desired quantity
            // -- put merged result back into our shopping list
            // -- put taken amount into Cart
            shoppingMap.values().forEach(i -> {
                Item takenItem = store.takeItem(i.getName(), i.getQuantity());
                if (takenItem != null) {
                    cart.addItem(takenItem);
                    Item decrement = null;
                    try {
                        decrement = new Item(takenItem.getName(), takenItem.getPrice(), takenItem.getQuantity() * -1, takenItem.getUnits());
                    } catch (InvalidItemException e) {
                        //TODO -- FORGET IT, WE'RE NOT GOING TO THROW EXCEPTIONS ANYMORE
                    }
                    // this merge will effectively decrement our shopping list
                    Item listResult = Item.merge(i, decrement);
                    shoppingMap.put(i.getName(), listResult);
                } // Null case just means store didn't have our Item at all
            });

            // TODO missing checkout steps
            //  - Get in line for a Register
            //  - Register will check us out
            //  - exit store?
            //  - can return results of our shopping -- list now has anything we could not get
        }

    }

//    TODO we're not multi-threaded yet, although nothing in our implementation rules that out
//    /**
//     * run() method of Runnable interface. Default case just calls shop() method
//     * <p>
//     * The general contract of the method <code>run</code> is that it may
//     * take any action whatsoever.
//     * </p>
//     *
//     * @see Thread#run()
//     */
//    @Override
//    public void run() {
//       shop();
//    }
}
