package main_server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SuperWorker extends Thread{
    Socket socket;
    String fileName;


    public SuperWorker(Socket socket,String configFileName) {
        this.socket=socket;
        fileName=configFileName;
    }

    @Override
    public void run() {
        OutputStream outputStream= null;
        InputStream inputStream=null;
        try {
            inputStream=socket.getInputStream();
            ObjectInputStream objectInputStream=new ObjectInputStream(inputStream);
            outputStream=socket.getOutputStream();
            ObjectOutputStream objectOutputStream=new ObjectOutputStream(outputStream);
            int peerId= (int) objectInputStream.readObject();

                if (!AllNodes.contains(peerId)) {
                    AllNodes.add(peerId);
                    System.out.println("Peer id set " + peerId);
                }
                System.out.println("all nodes are" + AllNodes.getNodes());
                objectOutputStream.flush();
                objectOutputStream.writeObject(AllNodes.getNodes());
                System.out.println("sent all nodes "+AllNodes.getNodes());
                objectOutputStream.flush();

        } catch (IOException e) {
             e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }finally {
            if (outputStream!=null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(inputStream!=null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (socket!=null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        }
    }
}
