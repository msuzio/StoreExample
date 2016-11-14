package net.suzio.model;

/**
 * Model a Shopper, a Runnable unit that performs a shopping task
 * Created by Michael on 11/13/2016.
 */
public class Shopper implements Runnable {
    /**
     * Alias for the run() method
     *
     * @see #run()
     */
    public void shop() {
        run();
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
        // Do nothing right now
    }
}
