package client_thread;

import utilities.MessageFormat;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientThread extends  Thread{
    int portOfConnection;
    int peerToConnect;
    String fileToDownload;
    Socket socket=null;
    int [] peersArray;
    MessageFormat MF= new MessageFormat();
    String msgId;
    int fromPeerId;
    int TTlValue;

    public int[] getPeersArray() {
        return peersArray;
    }

    public ClientThread(int portOfConnection, int peerToConnect, String fileToDownload, String msgId, int fromPeerId, int TTlValue) {
        this.portOfConnection = portOfConnection;
        this.peerToConnect = peerToConnect;
        this.fileToDownload = fileToDownload;
        this.msgId = msgId;
        this.fromPeerId = fromPeerId;
        this.TTlValue = TTlValue;
    }

    @Override
    public void run() {
        OutputStream outputStream=null;
        InputStream inputStream=null;
        try {
            System.out.println("got the request");
            socket=new Socket("localhost", portOfConnection);
            outputStream=socket.getOutputStream();
            ObjectOutputStream objectOutputStream=new ObjectOutputStream(outputStream);
            inputStream= socket.getInputStream();
            ObjectInputStream objectInputStream=new ObjectInputStream(inputStream);
            MF.setFname(fileToDownload);
            MF.setMsgId(msgId);
            MF.setFromPeerId(fromPeerId);
            MF.setTTlValue(TTlValue);
            objectOutputStream.writeObject(MF);
            peersArray= (int[]) objectInputStream.readObject();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }finally {
            if (inputStream!=null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream!=null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (socket!=null){
                try {
                    System.out.println("closing listener client thread socket ");
                    socket.close();
                } catch (IOException e) {
e.printStackTrace();
                }
            }

        }
    }
}
