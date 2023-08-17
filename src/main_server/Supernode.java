package main_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Supernode implements Runnable{
    int port;
    ServerSocket serverSocket=null;
    String configFileName;
    public Supernode(int port, String configFileName){
        this.port=port;
        try {
            serverSocket=new ServerSocket(port);
            this.configFileName=configFileName;
        } catch (IOException e) {
           e.printStackTrace();
        }
    }


    @Override
    public void run() {
        try {
            System.out.println("Main server Socket with port "+port+" is started");
            while (serverSocket.isBound() && !serverSocket.isClosed()){
                Socket socket=serverSocket.accept();
                SuperWorker superWorker=new SuperWorker(socket, configFileName);
                superWorker.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(serverSocket !=null){
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
