package net.suzio.store.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Store controller. Houses stock of Items
 * <p>
 * Created by Michael on 11/12/2016.
 */
@SuppressWarnings("WeakerAccess")
public class Store {

    // Just using volatile probably isn't good enough
    private AtomicBoolean open = new AtomicBoolean(false);
    // our waiting shoppers are always in a Queue
    private LinkedBlockingQueue<Shopper> waitingShoppers;

    // Not sure if this is needed; remove until use case is clear
    // private List<Shopper> activeShoppers = new ArrayList<>();

    // use non-concurrent Map, and lock selectively.
    // TODO -- benchmark this against ConcurrentHashMap
    // TODO -- Move this into a Service rather than internal store? JPA or Spring Data storage would be closer to a real model, and separating this out paves the way
    private HashMap<String, Item> stock = new HashMap<>();
    private ReadWriteLock stockLock = new ReentrantReadWriteLock();


    /**
     * Initialize a default Store
     * No limit on the size of the waiting Shopper line
     */
    public Store() {
        // infinite waiting line
        this(0);
    }

    /**
     * Initialize a Store with a set limit on the size of the waiting Shopper line
     *
     * @param waitSize limit on number of Shoppers that can be in waiting line
     */
    public Store(int waitSize) {
        if (waitSize > 0) {
            waitingShoppers = new LinkedBlockingQueue<>(waitSize);
        } else {
            waitingShoppers = new LinkedBlockingQueue<>();
        }
    }

    // Stock management operations

    /**
     * Add an Item to the stock.
     * Items are merged according to rules of {@link Item#merge}
     * Repricing an Item consists of adding a newly priced Item with a quantity of zero
     *
     * @param item The new Item in the stock. in the case of an error during additive case, the existing Item is guaranteed to be preserved.
     */
    public Item addItem(Item item) {
        Lock wLock = stockLock.writeLock();
        Item stockItem;

        wLock.lock();
        try {
            String name = item.getName();
            Item existing = stock.get(name);
            if (existing != null) {
                Item updated = Item.merge(existing, item);
                if (updated.getQuantity() >= 0) {
                    stock.put(name, updated);
                    stockItem = updated;
                } else {
                    stockItem = existing;
                }
            } else {
                stock.put(name, item);
                stockItem = item;
            }
        } finally {
            wLock.unlock();
        }
        return stockItem;
    }

    /**
     * @param name Name of item to query
     * @return Item matching name if in stock, or null
     */
    public Item queryItem(String name) {
        Lock rLock = stockLock.readLock();
        Item item;
        rLock.lock();
        try {
            item = stock.get(name);
        } finally {
            rLock.unlock();
        }
        return item;
    }

    /**
     * Request an Item from the Store stock by name, and record decremented quantity
     *
     * @param itemName          Name of Item to take from stock
     * @param requestedQuantity units of Item to take
     * @return Item  -- null if the Store does not have that Item, or a valid Item reflecting the requested quantity, or a lower quantity if the Store does not have that many units
     */
    public Item takeItem(String itemName, int requestedQuantity) {
        // get Item from stock -- while we do this, nothing else should be modifying the stock
        // so keep this method small
        Lock wLock = stockLock.writeLock();
        Item returnedItem = null;

        wLock.lock();
        try {
            Item stockItem = queryItem(itemName);
            if (stockItem != null) {
                // We have to do our modifications here before we release the lock
                int stockedQuantity = stockItem.getQuantity();
                int diff = stockedQuantity - requestedQuantity;
                if (diff < 0) {
                    // return all we have, and zero out quantity
                    returnedItem = new Item(itemName, stockItem.getPrice(), stockItem.getQuantity(), stockItem.getUnits());
                    stockItem = new Item(itemName, stockItem.getPrice(), 0, stockItem.getUnits());
                    stock.put(itemName, stockItem);
                } else {
                    returnedItem = new Item(itemName, stockItem.getPrice(), requestedQuantity, stockItem.getUnits());
                    stockItem = new Item(itemName, stockItem.getPrice(), diff, stockItem.getUnits());
                    stock.put(itemName, stockItem);
                }
            } // else returnedItem stays null
        } catch (Exception e) {
            // should not happen; if we attach a Logger, log out to that, not standard out
        } finally {
            wLock.unlock();
        }
        // return what we have right now -- so currently if new stock came in after we released the lock and before we return,
        // the requester does not see it. Right now, tough luck for our Shopper
        return returnedItem;
    }

    /**
     * Open the Store
     *
     * TODO -- this is not thread-safe; wrap all this logic in a tight run loop that can deque Shoppers as needed once it is open or just let them pass once open and stop using queue then
     */
    public void open() {
        open.set(true);
        List<Shopper> shoppers = new ArrayList<>(waitingShoppers.size());
        waitingShoppers.drainTo(shoppers);
        shoppers.forEach(Shopper::allowShop);
    }

    /**
     * Let a Shopper start the shopping process.
     * If the store is open, they are accepted immediately
     * If the store is closed, they may choose to wait, in which case they enter the Stores waiting queue.
     * Either way, this method returns immediately.
     *
     * TODO --  as with open(), modify to work with a tight run() loop
     *
     * @param shopper Shopper wanting to shop in this Store
     * @return true if the Shopper was accepted (possibly in a wait state), false if it was not (chose not to wait or wait line is full)
     */
    public boolean startShopper(Shopper shopper) {
        if (isOpen()) {
            shopper.allowShop();
            return true;
        } else if (shopper.isWaitable()) {
            return waitingShoppers.offer(shopper);
        }
        return false;
    }

    /**
     * determines if the store is open
     *
     * @return true if open
     */
    public boolean isOpen() {
        return open.get();
    }

    /**
     * determines if the store is closed
     *
     * @return true if closed
     */
    public boolean isClosed() {
        // just be reflexive for now is case open state gets more complex -- delegate to that to decide overall state
        return !isOpen();
    }

    @SuppressWarnings("unused")
    public void checkoutShopper(Shopper shopper) {
        // do nothing right now -- should be hooked up to a collection of Registers
    }

    @SuppressWarnings("unused")
    public void exitShopper(Shopper shopper) {
        // do nothing right now -- eventually it will clear this Shopper from an active list
    }
}
