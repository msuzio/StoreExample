package net.suzio.store.model;

import net.suzio.store.model.util.ItemUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Model a Shopper that performs a shopping task
 * <p>
 * Created by Michael on 11/13/2016.
 */
@SuppressWarnings("WeakerAccess")
public class Shopper implements Runnable {
    private Store store;
    private boolean waitable;
    // probably could just be a regular map; that change only affects #getShoppingList, since shop() operation is otherwise single-threaded if it implements Runnable as expected and the class does not expose internal state
    private final ConcurrentHashMap<String, Item> shoppingMap = new ConcurrentHashMap<>();
    private final Cart cart = new Cart();
    private Receipt receipt;

    // CONCURRENCY BARRIER -- don't need CyclicBarrier reset functionality
    // wait/notify might suffice, since no matter what we need another thread to signal us
    // Not sure if there's performance implications of this -- would be sa good StackOverflow question?
    final CountDownLatch shoppingBarrier = new CountDownLatch(1);

    // Accept a List in our constructors simply because that is more direct to our intent and easier to construct inside our tests;
    // I think it's irrelevant that our internal model uses a HashMap

    /**
     * create default Shopper, without a Store or a shopping list; should not expect it to  doing any shopping
     */
    public Shopper() {
        super();
    }

    /**
     * Create a Shopper to operate with an assigned Store.  waiting behavior defaults to not-waiting, and failing fast
     * against a closed Store
     *
     * @param store    Store we operate on
     * @param itemList List of Items we want to buy
     */
    public Shopper(Store store, List<Item> itemList) {
        super();
        this.store = store;
        if (itemList != null) {
            // Fold repeats and hash by names for easy retrieval
            shoppingMap.putAll(ItemUtil.itemstoMap(itemList));
        }
    }

    /**
     * Create a Shopper to operate with an assigned Store, with a specific waiting behavior
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
     * Perform the steps of shopping throughout a Store
     * This method should be able to execute in a multi-threaded context
     */
    public void shop() {
        if (store != null) {
            // Won't return until shopping is done
            doShopping();
            // In which case we can checkout, then leave -- checkout step returns (relatively) immediately,
            // and we do not explicitly exit Store
            checkout();
        }
    }

    private void checkout() {
        store.startShopperCheckout(this);
    }

    public void allowShop() {
        // flip barrier blocking shopping -- currently nothing waits on that, but this is the logic
        shoppingBarrier.countDown();
    }


    public void doShopping() {
        boolean canShop = store.startShopper(this);
        // We will do this for each Item in our shopping List:
        // -- try to take the Item from the Store
        // -- merge results with our desired quantity
        // -- put merged result back into our shopping list
        // -- put taken amount into Cart
        if (canShop) {
//            //TODO -- MT
//            try {
//                shoppingBarrier.await();
//            } catch (InterruptedException e) {
//                // TODO -- ANY IMPLICATIONS OF THIS HAPPENING?
//                  //consider that if we push shopping code inside try/catch, we are still in a
//                  //possible inconsistent state. I see no good answer right now, but this shouldn't be dismissed
//            }
            shoppingMap.values().forEach(i -> {
                Item takenItem = store.takeItem(i.getName(), i.getQuantity());
                if (takenItem != null) {
                    cart.addItem(takenItem);
                    Item decrement = new Item(takenItem.getName(), takenItem.getPrice(),
                                              takenItem.getQuantity() * -1, takenItem.getUnits());

                    // this merge will effectively decrement our shopping list
                    Item listResult = Item.merge(i, decrement);
                    shoppingMap.put(i.getName(), listResult);
                } // Null case just means store didn't have our Item at all
            });
        }
    }

    /**
     * @return indication if this Shopper waits for Store to open
     */
    public boolean isWaitable() {
        return waitable;
    }

    /**
     * Get our shopping list.  In general, this method should not be called while our shop() method is running, as
     * results are indeterminant.
     *
     * @return Copy of shopping list Items. Multiple instances of a single Item in the initial list will have been folded into a
     * single Item
     */
    protected List<Item> getShoppingList() {

        // Parallelism threshold is basically, none -- this is an operation not truly meant for access while shop() method executes
        // and should be kept protected (only because then it keeps class testable)
        // alternative is read/write locking, which is correct behavior but not needed *yet* and would slowdown shopping which is otherwise single-threaded
        // KEEP AN EYE ON THESE ASSUMPTIONS
        List<Item> listItems = new ArrayList<>();
        shoppingMap.forEachValue(1, listItems::add);
        return listItems;
    }

    /**
     * // TODO: 12/2/2016  -- explain concurrency issues better and guard against them if possible.
     * Get the cart the shopping process has filled Cautions of #getShoppingList apply, although calling this method as
     * shop() executes simply means we will not get a view of the complete shopping run.
     *
     * @return Cart Shopper has put items into as shopping executed.
     */
    protected Cart getCart() {
        return cart;
    }


    public void setReceipt(Receipt receipt) {
        this.receipt = receipt;
    }

    @SuppressWarnings("unused")
    public Receipt getReceipt() {
        return receipt;
    }

    /**
     * run() method of Runnable interface. Default case just calls shop() method
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     * </p>
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        shop();
    }
}
