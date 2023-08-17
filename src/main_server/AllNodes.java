package main_server;

import javax.xml.stream.FactoryConfigurationError;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class AllNodes {

    public static List<Integer> nodes=new ArrayList<>();

    public synchronized static void add(Integer i){
        nodes.add(i);
    }

    public static synchronized boolean contains(Integer i){
        return nodes.contains(i);
    }

    public static synchronized boolean remove(Integer i){
        if (!nodes.contains(i)) return false;
        int idx=nodes.indexOf(i);
        nodes.remove(idx);
        return true;
    }

    public synchronized static void clear(){
        nodes.clear();
    }

    public static synchronized List<Integer> getNodes(){
        return List.copyOf(nodes);
    }
}
