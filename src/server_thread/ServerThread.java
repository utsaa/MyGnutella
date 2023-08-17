package server_thread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class ServerThread extends Thread{
    String fileDirectory;
    int portNo;
    ServerSocket serverSocket=null;
    Socket socket=null;
    int peerId;
    final ArrayList<String> msg;
    String configFileName;

    public ServerThread(String fileDirectory, int portNo, int peerId, String configFileName) {
        this.fileDirectory = fileDirectory;
        this.portNo = portNo;
        this.peerId = peerId;
        msg=new ArrayList<>();
        this.configFileName=configFileName;
    }

    @Override
    public void run() {
        try {
            serverSocket=new ServerSocket(portNo);
            while (serverSocket.isBound() && !serverSocket.isClosed()) {
                socket = serverSocket.accept();
                System.out.println("Connect to client at " + socket.getRemoteSocketAddress() + " with peer " + peerId);
                new Download(socket, fileDirectory, peerId, msg, configFileName).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (serverSocket!=null){
                try {
                    System.out.println("closing server socket for server thread for peer id "+peerId);
                    serverSocket.close();
                } catch (IOException e) {
e.printStackTrace();
                }
            }
        }
    }
}
