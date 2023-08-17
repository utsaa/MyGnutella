package main_server;

import java.io.*;
import java.util.Properties;

public class CacheInvalid extends Thread{

    boolean stop=false;

    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }



    @Override
    public void run() {

        try {
            while (!stop){
                System.out.println("goes here");
                AllNodes.clear();
                System.out.println("Cache is setting nodes as empty");

                Thread.sleep(30000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
