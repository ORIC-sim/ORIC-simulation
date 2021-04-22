package oric;

import org.junit.Assert;
import org.junit.Test;

import oric.Cache;

import java.util.ArrayList;

public class CacheTest {

    @Test
    public void test1(){
        Cache<Integer> cache = new Cache<>(4);
        ArrayList<Integer> array = new ArrayList<>();

        cache.putIntoCache(1);
        cache.putIntoCache(2);
        cache.putIntoCache(3);
        cache.putIntoCache(4);

        cache.putIntoCache(5);

        array.add(3);
        array.add(4);
        array.add(5);

        Assert.assertEquals(array,cache.getArrayListTest());
        Assert.assertFalse(cache.hasElements(1));
        Assert.assertTrue(cache.hasElements(3));

    }
}
