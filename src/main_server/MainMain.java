package main_server;

import java.io.File;
import java.util.List;

public class MainMain {
    public static void main(String[] args) {
        String configFile="src/meshtopology.txt";
        MainServer mainServer=new MainServer(configFile);
        mainServer.start();
        try {
            mainServer.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
