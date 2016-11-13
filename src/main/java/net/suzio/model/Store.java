package net.suzio.model;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Store controller.
 *
 * Created by Michael on 11/12/2016.
 */
public class Store {

    private AtomicBoolean open = new AtomicBoolean(false);

    private static final Store STORE;

    private Store() {
    }

    static {
        STORE = new Store();
    }

    public static Store getInstance() {
        return STORE;
    }

    /**
     * determines if the store is open
     * @return true if open
     */
    public boolean isOpen(){
        return open.get();
    }

    /**
     * determines if the store is closed
     * @return true if olosed
     */
    public boolean isClosed() {
        // just be reflexive for now is case open state gets more complex -- delegate to that to decide overall state
        return !isOpen();
    }
}
