package net.suzio.model;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.Assert.fail;

public class ItemTest {
    /**
     * As Items will be passed around elements of the system frequently, they should be barred from
     * direct manipulation once created (the creation and inventory population assumed to be
     * implemented in a process outside this scope)
     */
    @Test
    public void testItemImmutable() {
        Class clazz = Item.class;

	/* 
     * naive assumption to start -- look at all fields, ensure there is no Bean type method corresponding to them.
	 * This keeps a robust test that will serve to warn us if we might stray from our contract and make us validate intent.
	 */

        Field[] fields = clazz.getFields();

        for (Field f : fields) {
            String name = f.getName();
            Method[] methods = clazz.getMethods();

            String probeName = "get" +name.substring(0).toUpperCase() + name.substring(1,name.length() - 1);
            for (Method m: methods) {
                if (m.getName().equals(probeName)) {
                    fail("Field '" + name + "' appears to have a setter '" + m.getName());
                }
            }
        }
    }
}
