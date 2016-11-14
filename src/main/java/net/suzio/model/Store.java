package net.suzio.model;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Store controller.
 * <p>
 * Created by Michael on 11/12/2016.
 */
public class Store {

    private AtomicBoolean open = new AtomicBoolean(false);
    // our waiting shoppers are always in a Queue
    private LinkedBlockingQueue<Shopper> waitingShoppers;
    // Not sure if this is needed; remove until use case is clear
    // private List<Shopper> activeShoppers = new ArrayList<>();

    // use non-concurrent Map, and lock selectively
    // in a real application, we would benchmark this against ConcurrentHashMap
    HashMap<String, Item> stock = new HashMap<>();
    ReadWriteLock stockLock = new ReentrantReadWriteLock();


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
        // this really shuld be initialized somewhere below; if we mess that up,
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
        } catch (InvalidItemException e) {
            // essentially cannot happen; if it does for some reason, we haven't updated our stock anyway
        } finally {
            wLock.unlock();
        }
        return stockItem;
    }

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

    // Shopper management operations

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
     * @param shopper A Shopper attempting to enter the Store and shop
     * @return true if the Shopper was accepted (always accepted if open, rejected if closed.
     */
    public boolean enter(Shopper shopper) {
        if (isOpen()) {
            // activeShoppers.add(shopper);
            return true;
        }
        return false;
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
