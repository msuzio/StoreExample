package net.suzio.store.model;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Register that services Shoppers in checkout process
 * Created by Michael on 11/17/2016.
 */
public class Register {
    // optionally bounded queue of Shoppers waiting for this register
    // our waiting shoppers are always in a Queue
    private LinkedBlockingQueue<Shopper> waitingShoppers;
    private static AtomicInteger idCounter = new AtomicInteger(0);
    private Integer id;

    public Register() {
        this(0);
    }

    public Register(int lineLimit) {
        if (lineLimit > 0) {
            waitingShoppers = new LinkedBlockingQueue<>(lineLimit);
        } else {
            waitingShoppers = new LinkedBlockingQueue<>();
        }
        this.id = idCounter.getAndIncrement();
    }

    public void checkoutShopper() {
        // No waiting timeout right now; adjust as desired behavior becomes clear
        Shopper shopper = waitingShoppers.poll();
        if (shopper != null) {
            Receipt receipt = new Receipt();
            Cart cart = shopper.getCart();
            final List<Item> validItems = cart.getItems().stream().filter(i -> i.getQuantity() > 0).collect(Collectors.toList());
            receipt.addItems(validItems);
            shopper.setReceipt(receipt);
        }
    }


    public boolean addWaitingShopper(Shopper shopper) {
        return waitingShoppers.offer(shopper);
    }

    /**
     * @return number of Shoppers waiting. In a multithreaded context, this may change after querying, so this is just informational and #addWaitingShopper may still refuse requests
     */
    public int getWaitingCount() {
        return waitingShoppers.size();
    }

    public int getRemainingWaitLimit() {
        return waitingShoppers.remainingCapacity();
    }

    public Integer getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Register register = (Register) o;

        return id.equals(register.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
