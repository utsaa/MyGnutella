package server_download;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerDownload extends Thread {
    int portNo;
    String FileDirectory;
    ServerSocket serverSocket;
    Socket socket;

    public ServerDownload(int portNo, String fileDirectory) {
        this.portNo = portNo;
        FileDirectory = fileDirectory;
    }

    @Override
    public void run() {

        try {
            serverSocket = new ServerSocket(portNo);
            while (serverSocket.isBound() && !serverSocket.isClosed()) {
                socket = serverSocket.accept();
                new Downloading(socket, portNo, FileDirectory).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    System.out.println("closing server socket for server download");
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
