package net.suzio.service;

import net.suzio.model.Item;

/** Interface for an Inventory manager
 *  Just a stub right now
 * Created by Michael on 11/12/2016.
 */
public interface Inventory {
    void loadInventory();
    Item getItem(String name);
    boolean addItem();
}
