package heartbeat_server;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class HeartbeatClient extends Thread{
    int port;
    int duration;
    int peerId;
    AtomicBoolean present=new AtomicBoolean(false);

    boolean stop=false;
    Socket socket;
    OutputStream fileOutputStream;

    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getPeerId() {
        return peerId;
    }

    public void setPeerId(int peerId) {
        this.peerId = peerId;
    }

    public boolean isPresent(long duration) {
        long t1 = System.currentTimeMillis();
        while (((System.currentTimeMillis() - t1) < duration)&& !present.get()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return present.get();
    }


    public HeartbeatClient(int port, int duration, int peerId){
        this.port=port;
        this.duration=duration;
        this.peerId=peerId;
    }

    @Override
    public void run() {
        InputStream inputStream=null;
        OutputStream outputStream=null;
        try {
             socket = new Socket("localhost", port);
             String infos="Heartbeatclient got request for "+peerId+"\n";
//            System.out.println(infos);
            fileOutputStream=new FileOutputStream("Log-Peer"+peerId, true);
            fileOutputStream.write(infos.getBytes());
                outputStream=socket.getOutputStream();
            ObjectOutputStream objectOutputStream=new ObjectOutputStream(outputStream);
            String res="ok";
            objectOutputStream.flush();
            objectOutputStream.writeObject(res);
            present.set(false);
            objectOutputStream.flush();
            inputStream =socket.getInputStream();
            ObjectInputStream objectInputStream=new ObjectInputStream(inputStream);
            String res2= (String) objectInputStream.readObject();
            present.set(true);
            fileOutputStream.write(infos.getBytes());
            fileOutputStream.flush();
            infos="present became true\n";
//            System.out.println(infos);
            fileOutputStream.write(infos.getBytes());


        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
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
