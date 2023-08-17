package main_server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class MainServer extends Thread{
    String fileName;
    public MainServer(String fileName){
        this.fileName=fileName;
    }

    @Override
    public void run() {
        CacheInvalid cacheInvalid=null;
        ExecutorService service=null;
        try {

            Properties properties= new Properties();
            InputStream inputStream=new FileInputStream(fileName);
            properties.load(inputStream);
            String[] ports=properties.getProperty("superPorts").split(",");
            System.out.println("Supernode ports are"+ports);
            inputStream.close();
            cacheInvalid= new CacheInvalid();
            cacheInvalid.start();
            System.out.println("Invalidate cache is started");
            service= Executors.newFixedThreadPool(3);
            for (String port: ports) {
                service.submit(new Supernode(Integer.valueOf(port), fileName));
            }
            cacheInvalid.join();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                cacheInvalid.setStop(true);
                cacheInvalid.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            service.shutdown();
        }
    }
}
