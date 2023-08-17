package main_server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;

public class SuperPeerClient extends Thread{

    int peerId;

    public SuperPeerClient(int peerId, String fileName) {
        this.peerId = peerId;
        this.fileName = fileName;

    }

    String fileName;


    public static List<Integer> getAllNodes() {
        return allNodes;
    }

    static List<Integer> allNodes=new ArrayList<>();

    public int getPeerId() {
        return peerId;
    }

    public void setPeerId(int peerId) {
        this.peerId = peerId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }


    @Override
    public void run() {
        try {
            OutputStream outputStream1=new FileOutputStream("Log-Peer"+peerId, true);
            InputStream inputStream = new FileInputStream(fileName);
            Properties properties = new Properties();
            properties.load(inputStream);
            inputStream.close();
            String[] superPorts = properties.getProperty("superPorts").split(",");
            int curr=0;
            int superNodeLen=superPorts.length;
            while (true) {
                curr%=superNodeLen;
                for (int i=0;i<superNodeLen;i++) {
                    curr+=1;
                    curr%=superNodeLen;
                    Socket socket=null;
                    OutputStream outputStream=null;
                    InputStream inputStream1=null;
                    try {
                        int port=Integer.parseInt(superPorts[curr]);
                         socket = new Socket("localhost", Integer.valueOf(port));
                         String infos="Port chosen for supernode"+port+"\n";
                         outputStream1.write(infos.getBytes());
                         outputStream = socket.getOutputStream();
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                        objectOutputStream.flush();
                        objectOutputStream.writeObject(peerId);
                        objectOutputStream.flush();
                        inputStream1=socket.getInputStream();
                        InputStream finalInputStream = inputStream1;
                        Callable callable= () -> {
                            ObjectInputStream objectInputStream=new ObjectInputStream(finalInputStream);
                            List<Integer> list= (List<Integer>) objectInputStream.readObject();
                            return list;
                        };
                        FutureTask<List<Integer>> futureTask=new FutureTask<>(callable);
                        Thread thread=new Thread(futureTask);
                        thread.start();
                        allNodes=futureTask.get(1000, TimeUnit.MILLISECONDS);
                        infos="Got allNodes "+allNodes+" for port "+port+"\n";
                        outputStream1.write(infos.getBytes());
                        break;
                    } catch (IOException | InterruptedException | ExecutionException | TimeoutException e) {
                        e.printStackTrace();
                        Thread.sleep(2000);
                    } finally {
                        Thread.sleep(2000);
                        if (outputStream!=null) {
                            try {
                                outputStream.close();
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
        } catch (FileNotFoundException | InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
