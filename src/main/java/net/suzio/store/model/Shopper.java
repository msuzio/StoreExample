package net.suzio.store.model;

import net.suzio.store.model.util.ItemUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Model a Shopper that performs a shopping task
 * <p>
 * Created by Michael on 11/13/2016.
 */
@SuppressWarnings("WeakerAccess")
public class Shopper implements Runnable {
    private static final AtomicInteger ID_COUNTER = new AtomicInteger(0);
    private final Integer id;

    private Store store;
    private boolean waitable;

    private final Map<String, Item> shoppingMap = new HashMap<>();
    private final Cart cart = new Cart();
    private Receipt receipt;

    // CONCURRENCY BARRIER -- don't need CyclicBarrier reset functionality
    // wait/notify might suffice, since no matter what we need another thread to signal us
    // Not sure if there's performance implications of this -- would be a good StackOverflow question?
    final CountDownLatch shoppingBarrier = new CountDownLatch(1);

    // We want to be explicit in our logic
    @SuppressWarnings("RedundantFieldInitialization")
    private volatile boolean canShop = false;


    // Accept a List in our constructors simply because that is more direct to our intent and easier to construct inside our tests;
    // I think it's irrelevant that our internal model uses a HashMap
    /**
     * create default Shopper, without a Store or a shopping list; should not expect it to  doing any shopping
     */
    public Shopper() {
        super();
        this.id = ID_COUNTER.getAndIncrement();
    }

    /**
     * Create a Shopper to operate with an assigned Store.  waiting behavior defaults to not-waiting, and failing fast
     * against a closed Store
     *
     * @param store    Store we operate on
     * @param itemList List of Items we want to buy
     */
    public Shopper(Store store, List<Item> itemList) {
        this();
        this.store = store;
        if (itemList != null) {
            // Fold repeats and hash by names for easy retrieval
            shoppingMap.putAll(ItemUtil.itemsToMap(itemList));
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

            // checkout; checkout step returns (relatively) immediately,
            // and either we were queued up to checkout  or perhaps we had to return some Items
            if (!checkout()) {
                // checkout failed.
                // Adjust our list and Cart accordingly
                List<Item> cartItems = cart.getItems();
                cartItems.forEach(cartReturn ->
                                  {
                                      cart.removeItem(cartReturn);
                                      Item listItem = shoppingMap.get(cartReturn.getName());
                                      Item revisedItem = Item.merge(listItem, cartReturn);
                                      shoppingMap.put(cartReturn.getName(), revisedItem);
                                  });
            }
        }
    }

    private boolean checkout() {
        return store.startShopperCheckout(this);
    }


    public void allowShop() {
        // flip barrier blocking shopping -- currently nothing waits on that, but this is the logic
        shoppingBarrier.countDown();
    }

    public void stopShopping() {
        canShop = false;
    }


    public void doShopping() {
        canShop = store.startShopper(this);
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
                // must check again each time,in case we're signaled to stop getting Items
                if (canShop) {
                    Item takenItem = store.takeItem(i.getName(), i.getQuantity());
                    if (takenItem != null) {
                        cart.addItem(takenItem);
                        Item decrement = new Item(takenItem.getName(), takenItem.getPrice(),
                                                  takenItem.getQuantity() * -1, takenItem.getUnits());

                        // this merge will effectively decrement our shopping list
                        Item listResult = Item.merge(i, decrement);
                        shoppingMap.put(i.getName(), listResult);
                    } // Null case just means store didn't have our Item at all
                }
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
        List<Item> listItems = new ArrayList<>();
        listItems.addAll(shoppingMap.values());
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

    public int getId() {
        return id;
    }

    // general Object methods

    // No idea why IDEA generates this but then complains.
    // Not expected to be used enough to really matter either way
    @SuppressWarnings("StringBufferReplaceableByString")
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Shopper{");
        sb.append("id=").append(id);
        sb.append(", waitable=").append(waitable);
        sb.append(", shoppingMap=").append(shoppingMap);
        sb.append(", cart=").append(cart);
        sb.append(", receipt=").append(receipt);
        sb.append(", canShop=").append(canShop);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Shopper shopper = (Shopper) o;

        return id.equals(shopper.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
