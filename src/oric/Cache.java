package oric;

import java.util.ArrayList;
import java.util.HashMap;

public class Cache<T> {
    final int max_cache_size;

    int now_cache_size;
    final HashMap<T, Integer> cache;
    final ArrayList<T> array;

    public Cache(int max_size){
        this.max_cache_size = max_size;
        this.now_cache_size = 0;
        this.cache = new HashMap<T,Integer>();
        this.array = new ArrayList<T>();
    }

    public void putIntoCache(T e){
        // if cache_size is over
        if( this.now_cache_size+1 > this.max_cache_size){
            // oversize , delete half size
            for(int i = 0; i < this.max_cache_size/2 ; i++){
                this.cache.remove(array.get(0));
                this.array.remove(0);
                this.now_cache_size --;
            }
        }
        // if already cached
        if (hasElements(e))
            return;

        // Put elements into cache
        this.array.add(e);
        this.cache.put(e,1);
        this.now_cache_size ++;
    }

    public boolean hasElements(T e){
        return this.cache.containsKey(e);
    }

    public int getSize(){
        return this.array.size();
    }

    public T getElementByIndex(int index){
        if(index>= now_cache_size){
            return null;
        }
        return array.get(index);
    }

    public ArrayList<T> getArrayListTest(){
        return array;
    }
}
