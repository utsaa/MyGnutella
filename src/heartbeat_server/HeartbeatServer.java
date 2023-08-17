package heartbeat_server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class HeartbeatServer extends Thread{

    int port;
    ServerSocket serverSocket;

    public HeartbeatServer(int port){
        this.port=port;
        try {
            serverSocket=new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }



    @Override
    public void run() {
        try {

            while (serverSocket.isBound() && !serverSocket.isClosed()){

//                System.out.println("Inside Started hb server for port "+port);
                Socket socket=serverSocket.accept();
                HeartbeatWorker heartbeatWorker= new HeartbeatWorker(socket);
                heartbeatWorker.start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            if (serverSocket!=null) {
                try {
                    System.out.println("Closing heartbeat server");
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
