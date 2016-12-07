package net.suzio.store.model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
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
    private static final int SHOPPER_WAIT_SLEEP = 5000;

    // our waiting shoppers are always in a Queue
    private final LinkedBlockingQueue<Shopper> waitingShoppers;

    // try concurrent structure here.
    // These are Shoppers that are actively shopping but not yet in a Register queue
    // We need to check in our main loop to try to ensure that all of these Shoppers
    // reach a Register for checkout before we stop
    private final ConcurrentHashMap<Integer, Shopper> shoppingShoppers = new ConcurrentHashMap<>();

    // use non-concurrent Maps, and lock selectively -- possible ConcurrentHashMap is better,
    // but won't optimize just yet -- behavior of adding and removing Registers during run() loop
    // seems to be more understandable with explicit locking right now
    private final HashMap<Integer, Register> registers = new HashMap<>();
    private final ReadWriteLock registerLock = new ReentrantReadWriteLock();
    // As above, could this better be a ConcurrentHashMap?
    // TODO -- Move this into a Service rather than internal store? JPA or Spring Data storage would be closer to a real model, and separating this out paves the way
    private final HashMap<String, Item> stock = new HashMap<>();
    private final ReadWriteLock stockLock = new ReentrantReadWriteLock();

    // control variables
    private volatile boolean open;
    private volatile boolean running = true;
    private volatile boolean registerAdd = true;
    private volatile boolean allowCheckout = true;

    // Constructors

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
        super();
        if (waitSize > 0) {
            waitingShoppers = new LinkedBlockingQueue<>(waitSize);
        } else {
            waitingShoppers = new LinkedBlockingQueue<>();
        }
    }
    // end of constructors

    // Store main loop
    public void run() {
        // should loop around a running state
        // while(running) {
        // take any waiting Shoppers and let them proceed; don't wait on entrance of new Shoppers,
        // since we'll catch them next time around
        Shopper waiting;
        while ((waiting = waitingShoppers.poll()) != null) {
            waiting.allowShop();
        }

        // run each Register's checkout logic.
        // if a Register is added or removed outside this loop, we either get it next
        // time or handle it if we close before then
        Lock rLock = registerLock.readLock();
        rLock.lock();
        try {
            Collection<Register> runningRegisters = registers.values();
            runningRegisters.forEach(Register::checkoutNext);
        } finally {
            rLock.unlock();
        }
        //}

        // no longer running main loop -- perform closing actions
        // no more Shoppers allowed in; no more modifications to our Map of shopping Shoppers
        open = false;

        // We can now know what Shoppers (if any) are still not in a checkout line in our Registers
        // We want them to make them stop shopping and move into the checkout
        if (!shoppingShoppers.isEmpty()) {
            shoppingShoppers.values().forEach(Shopper::stopShopping);

            // That takes some time (at most one more Item taken, then enqueueing Shopper into a Register line).
            // Those actions run inside the Shopper thread, so we can suspend this thread for a short time to allow
            // those actions to happen.
            // After that, the Shopper threads will continue, but they won't  won't checkout and
            // we'll switch to having them put all Items back into stock.
            try {
                Thread.sleep(SHOPPER_WAIT_SLEEP);
            } catch (InterruptedException e) {
                // If we stop early, no big dea; we just won't have given Shoppers full time to complete
            }
        }

        // Son't let any more Shoppers enqueue in a Register
        allowCheckout = false;


        // no more Registers added. If between now and lock acquisition below a Register gets removed by another thread,
        // it will still checkout its line of Shoppers just as we do below.
        registerAdd = false;

        // clear Register pool and checkout the shoppers in each.
        // lock and don't let go until we've processed all Registers.  Only this thread should be allowed to change the Registers now
        Lock wLock = registerLock.writeLock();
        wLock.lock();
        try {
            // Have to capture keys in a new set then call remove method; otherwise just iterating
            // the keys or values gives a ConcurrentModificationException because removeRegister removes the item from the map
            Set<Integer> keySet = new HashSet<>(registers.keySet());
            keySet.forEach(id -> removeRegister(registers.get(id)));
        } finally {
            wLock.unlock();
        }
    }

    // External API -- query and change state

    /**
     * Determines if the store is open. Informative only.
     *
     * @return true if open
     */
    public boolean isOpen() {
        return open;
    }

    /**
     * Determines if the store is closed. Informative only.
     *
     * @return true if closed
     */
    public boolean isClosed() {
        // just be reflexive of method for now is case
        // open state gets more complex -- delegate to that to decide overall state
        return !isOpen();
    }

    /**
     * Perform any logic needed to initialize the Store to a state ready for processing
     */
    public void open() {
        open = true;
        shoppingShoppers.clear();
    }

    /**
     * Shut Store down for the night. After this method is called, all new requests to the Store will be refused and
     * pending operations will be finished as soon as possible
     */
    public void shutdownStore() {
        running = false;
    }

    // Stock management operations

    /**
     * Add an Item to the stock.
     * Items are merged according to rules of {@link Item#merge}
     * Repricing an Item consists of adding a newly priced Item with a quantity of zero
     *
     * @param item The new Item in the stock. in the case of an error during additive case, the existing Item is
     *             guaranteed to be preserved.
     */
    public Item addItem(Item item) {
        Lock wLock = stockLock.writeLock();

        wLock.lock();
        try {
            String name = item.getName();
            Item existing = stock.get(name);
            if (existing != null) {
                Item updated = Item.merge(existing, item);
                if (updated.getQuantity() >= 0) {
                    stock.put(name, updated);
                    return updated;
                } else {
                    return existing;
                }
            } else {
                stock.put(name, item);
                return item;
            }
        } finally {
            wLock.unlock();
        }
    }

    /**
     * @param name Name of item to query
     * @return Item matching name if in stock, or null
     */
    public Item queryItem(String name) {
        Lock rLock = stockLock.readLock();
        rLock.lock();
        try {
            return stock.get(name);
        } finally {
            rLock.unlock();
        }
    }

    /**
     * Request an Item from the Store stock by name, and record decremented quantity
     *
     * @param itemName          Name of Item to take from stock
     * @param requestedQuantity units of Item to take
     * @return Item  -- null if the Store does not have that Item, or a valid Item reflecting the requested quantity, or
     * a lower quantity if the Store does not have that many units
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
    // End of stock management

    // Register control

    /**
     * Add a Register to the Store.
     *
     * @param register Register to add to Store
     * @return register object if Register is successfully added; null otherwise
     */
    public Register addRegister(Register register) {
        if (registerAdd) {
            if (register != null) {
                Lock wLock = registerLock.writeLock();
                wLock.lock();
                try {
                    registers.put(register.getId(), register);
                    return register;
                } finally {
                    wLock.unlock();
                }
            }
        }
        return null;

    }

    /**
     * Remove a Register from the Store's pool.
     *
     * @param register Register to remove
     * @return the removed Register, or null if this Register was not in the register pool
     */
    public Register removeRegister(Register register) {
        // remove from available pool immediately
        Lock wLock = registerLock.writeLock();
        Register remove = null;
        if (register != null) {
            wLock.lock();
            try {
                remove = registers.remove(register.getId());
            } finally {
                wLock.unlock();
            }
        }

        // Register is now independent of Store pool and we don't need a lock
        if (remove != null) {
            remove.checkoutAll();
        }

        return remove;
    }

    // End of store control logic

    // Shopper interactions

    /**
     * Let a Shopper start the shopping process.
     * If the store is open, they are accepted immediately
     * If the store is closed, they may choose to wait, in which case they enter the Stores waiting queue.
     * Either way, this method returns immediately.
     *
     * @param shopper Shopper wanting to shop in this Store
     * @return true if the Shopper was accepted (possibly in a wait state), false if it was not (chose not to wait or
     * wait line is full)
     */
    public boolean startShopper(Shopper shopper) {
        if (isOpen()) {
            // track the Shopper
            shoppingShoppers.put(shopper.getId(), shopper);
            shopper.allowShop();
            return true;
        } else if (shopper.isWaitable()) {
            return waitingShoppers.offer(shopper);
        }
        return false;
    }

    public boolean startShopperCheckout(Shopper shopper) {
        boolean checkoutSuccess = true;
        if (allowCheckout) {
            // just assign all to first Register now -- should still work
            List<Register> availableRegisters = new ArrayList<>(registers.values());
            // If there are no Registers in service, , you cannot checkout
            if (!availableRegisters.isEmpty()) {
                availableRegisters.get(0).addShopper(shopper);
            } else {
                checkoutSuccess = false;
            }
        } else {
            checkoutSuccess = false;
        }

        if (!checkoutSuccess) {
            // We must restock Items -- Shopper handles details of what a failed
            // checkout means to it, best nt to reach into its state but rather we
            // just choose to signal failure
            Cart cart = shopper.getCart();
            if (cart != null) {
                List<Item> cartItems = cart.getItems();
                cartItems.forEach(this::addItem);
            }
        }

        shoppingShoppers.remove(shopper.getId());
        return checkoutSuccess;
    }
    // end of shopper interactions
}
