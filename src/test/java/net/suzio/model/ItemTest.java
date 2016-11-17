package net.suzio.model;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.fail;


public class ItemTest {
    /**
     * As Items will be passed around elements of the system frequently, they should be barred from
     * direct manipulation once created
     */
    @Test
    public void testItemImmutable() {
        Class clazz = Item.class;

	/* 
     * naive assumption to start -- look at all fields, ensure there is no Bean type method corresponding to them.
	 * This keeps a robust test that will serve to warn us if we might stray from our contract.
	 */
        Field[] fields = clazz.getDeclaredFields();
        Method[] methods = clazz.getDeclaredMethods();
        Set<String> methodNames = Stream.of(methods)
                .map(Method::getName)
                .filter(n -> n.startsWith("set"))
                .collect(Collectors.toSet());

        for (Field f : fields) {
            String name = f.getName();

            String probeName = "set" + name.substring(0, 1).toUpperCase() + name.substring(1, name.length());

            if (methodNames.contains(probeName)) {
                fail("Field '" + name + "' appears to have a setter '" + probeName + "'");
            }
        }
    }
}
