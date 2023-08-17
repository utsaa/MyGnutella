package heartbeat_server;

import main_server.SuperPeerClient;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;


public class HeartbeatClientMain extends Thread{

    int duration;
    static List<Integer> peers=new ArrayList<>();
    String fileName;


    int peerId;

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public static synchronized List<Integer> getPeers() {
        return new ArrayList<>(List.copyOf(peers));
    }

    public static void setPeers(List<Integer> peers) {
        HeartbeatClientMain.peers = peers;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getPeerId() {
        return peerId;
    }

    public void setPeerId(int peerId) {
        this.peerId = peerId;
    }
    OutputStream fileOutputStream;

    public HeartbeatClientMain(List<Integer> peers, int duration, String fileName, int peerId){
        this.peers=peers;
        this.duration=duration;
        this.fileName=fileName;
        this.peerId=peerId;
        System.out.println("For peerId "+peerId+" Hb client main started with peers "+peers);
        try {
            String name="Log-Peer"+peerId;
            File f=new File(name);
            if (f.exists()) {
                f.delete();
                System.out.println("FIle "+name+" existed so deleted");
            }
            fileOutputStream=new FileOutputStream(name);
            System.out.println("Created file "+name);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try {
            boolean first=true;
            while (true) {
                List<HeartbeatClient> heartbeatClients = new ArrayList<>();
                Properties properties = new Properties();
                InputStream inputStream = new FileInputStream(fileName);
                properties.load(inputStream);
                List<Integer> allPeers=new ArrayList<>();
//                System.out.println("coming after breaking loop");
                if (!first) {
//                    System.out.println("coming here also");
                    allPeers=new ArrayList<>(SuperPeerClient.getAllNodes());
                    int idx=allPeers.indexOf(peerId);
                    allPeers.remove(idx);
//                    System.out.println("Connected nodes are "+allPeers);
                    String infos="Connected nodes are "+allPeers+"\n";

                    fileOutputStream.write(infos.getBytes());
                    fileOutputStream.flush();
                    int index;
                    Random random1=new Random();
                    int seed=random1.nextInt(100);
                    Random random=new Random(seed);
                    synchronized (this) {
                        if (allPeers.size() <= 3) {
                            peers.addAll(allPeers);
                        } else {
                            int i = 0;
                            while (i < 3) {
                                index = random.nextInt(allPeers.size());
                                if (peers.contains(allPeers.get(index))) continue;
                                peers.add(allPeers.get(index));
                                i++;
                            }
                        }
                    }
                    }
                first=false;
                for (int peerId_i : peers) {
                    String port = properties.getProperty("peer" + peerId_i + ".hbPort");
                    int port_i = Integer.parseInt(port);
//                    System.out.println("For peer id "+peerId+" heart beat clients started for "+port);
                    String infos="For peer id "+peerId+" heart beat clients started for "+port+"\n";
                    fileOutputStream.write(infos.getBytes());
                    fileOutputStream.flush();
                    HeartbeatClient heartbeatClient = new HeartbeatClient(port_i, duration, peerId_i);
                    heartbeatClient.start();
                    heartbeatClients.add(heartbeatClient);
                }

//                inputStream.close();
                Thread.sleep(duration + 1000);
                while (true) {
//                    inputStream=new FileInputStream(fileName);
//                    properties=new Properties();
//                    properties.load(inputStream);
                        Iterator<HeartbeatClient> heartbeatClientIterator = heartbeatClients.iterator();
                        for (Iterator<HeartbeatClient> it = heartbeatClientIterator; it.hasNext(); ) {
                            HeartbeatClient client = it.next();
                            client.setStop(true);
                        }
                        for (Iterator<HeartbeatClient> it = heartbeatClientIterator; it.hasNext(); ) {
                            HeartbeatClient client = it.next();
//                            System.out.println("joining client ");
                            fileOutputStream.write("joining client".getBytes());
                            client.join(duration);

                        }
                    synchronized (this) {
                        for (HeartbeatClient client : heartbeatClients) {
                            if (!client.isPresent(1000)) {
                                int idx = peers.indexOf(client.getPeerId());
                                if (idx>=0) peers.remove(idx);
                                String infos = "Clien is not present for " + client.getPeerId()+"\n";
                                fileOutputStream.write(infos.getBytes());
                                fileOutputStream.flush();

                            } else {
                                if (!peers.contains(client.getPeerId())) {
                                    peers.add(client.getPeerId());
                                    String infos = "Adding " + client.getPeerId() + " to peer list\n";
                                    fileOutputStream.write(infos.getBytes());
                                    fileOutputStream.flush();
                                }
                            }
                        }
                    }
//                        System.out.println("For peer id " + peerId + " Connected peers are " + peers);
                    String infos="For peer id " + peerId + " Connected peers are " + peers+"\n";
                        fileOutputStream.write(infos.getBytes());
                        fileOutputStream.flush();

//                    inputStream.close();
                        if (peers.size() <= 2) {
//                            System.out.println("Inside the size 0");
                            peers.clear();
                            infos="Inside the size 0"+"\n";
                            fileOutputStream.write(infos.getBytes());
                            fileOutputStream.flush();
                            heartbeatClients.clear();
//                            System.out.println("breaking peer loop");
                            infos="breaking peer loop"+"\n";
                            fileOutputStream.write(infos.getBytes());
                            fileOutputStream.flush();
                            break;
                        }

                    Thread.sleep(duration + 1000);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();

    }
    }
}
