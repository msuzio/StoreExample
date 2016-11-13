package net.suzio.model;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Store controller.
 * <p>
 * Created by Michael on 11/12/2016.
 */
public class Store {

    private AtomicBoolean open = new AtomicBoolean(false);
    //our shoppers are always in a Queue
    private LinkedBlockingQueue<Shopper> waitingShoppers;


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

    // Shopper management operations

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
