package heartbeat_server;

import java.io.*;
import java.net.Socket;

public class HeartbeatWorker extends Thread{
    Socket socket;
    public HeartbeatWorker(Socket socket) {
        this.socket=socket;
    }

    @Override
    public void run() {
        InputStream inputStream=null;
        OutputStream outputStream=null;
        try{
            inputStream=socket.getInputStream();
            ObjectInputStream objectInputStream=new ObjectInputStream(inputStream);
            String res= (String) objectInputStream.readObject();
            outputStream=socket.getOutputStream();
            outputStream.flush();
            ObjectOutputStream objectOutputStream=new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(res);
            outputStream.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }finally {
            if (socket!=null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream!=null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
    }
}
}
