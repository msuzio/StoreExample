package net.suzio.store.model;

import java.util.HashMap;
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
        waitingShoppers = new LinkedBlockingQueue<>();
    }

    /**
     * Initialize a Store with a set limit on the size of the waiting Shopper line
     *
     * @param waitSize limit on number of Shoppers that can be in waiting line
     */
    public Store(int waitSize) {
        waitingShoppers = new LinkedBlockingQueue<>(waitSize);
    }

    // Stock management operations

    /**
     * Add an Item to the stock.
     * Items are merged according to rules of {@link Item#merge}
     * Repricing an Item consists of adding a newly priced Item with a quantity of zero
     *
     * @param item The new Item in the stock. in the case of an exception during additive case, the existing Item is guaranteed to be preserved.
     */
    public Item addItem(Item item) {
        Lock wLock = stockLock.writeLock();
        // this really should always be initialized somewhere below; if we mess that up,
        // unit tests should catch that and fail
        Item stockItem = null;

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
        Item item = null;
        rLock.lock();
        try {
            item = stock.get(name);
        } finally {
            rLock.unlock();
        }
        return item;
    }

    /**
     * Request an Item fom the Store stockby name, and record decremented quantity
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
        // return what we have right now -- so currently if new stock came in after we releaed the lock and before we return,
        // the requester does not see it. right now, tough luck for our Shopper
        return returnedItem;
    }

    /**
     * Open the Store
     */
    public void open() {
        open.set(true);

        // Later on we should drain our waiting queue
        // and notify waiting Shoppers
    }

    /**
     * lets a Shopper attempt to enter the store
     *
     * todo - can this be removed? Intent was originally to have te Store determine if the Shopper should go into waiting line.
     * @param shopper A Shopper attempting to enter the Store and shop.
     * @return true if the Shopper was accepted (always accepted if open, rejected if closed.
     */
    public boolean enter(Shopper shopper) {
        return isOpen();
    }

    /**
     * Adds shopper to waiting list. Rejects immediately if the number of waiting shoppers has been reached.
     *
     * @param shopper Shopper that wants to wait in line
     * @return true if shopper is allowed to wait, false if waiting line exceeds limit
     */
    public boolean addWaitingShopper(Shopper shopper) {
        return waitingShoppers.offer(shopper);
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
     * @return true if olosed
     */
    public boolean isClosed() {
        // just be reflexive for now is case open state gets more complex -- delegate to that to decide overall state
        return !isOpen();
    }
}
