package net.suzio.model;

/**
 * Created by Michael on 11/12/2016.
 */
public enum ItemUnit {
    LB("lb"), EACH("each");

    String name;
    ItemUnit(String name) {
        this.name = name;
    }
}
