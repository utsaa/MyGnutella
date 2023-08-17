import client_thread.ClientThread;
import heartbeat_server.HeartbeatClient;
import heartbeat_server.HeartbeatClientMain;
import heartbeat_server.HeartbeatServer;
import main_server.SuperPeerClient;
import server_download.ServerDownload;
import server_thread.ServerThread;
import utilities.Utilities;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {

    static String fileName;
    public static void main(String[] opts) {
        int id;
        int port;
        int portServer;
        Scanner scanner=new Scanner(System.in);
        String serverName="localhost";
        int peer;
        int count=0;
        int TTLValue;
        String msgId;
        String shareDir;
        ArrayList<Thread> threads= new ArrayList<>();
        ArrayList<ClientThread> peers= new ArrayList<>();
        System.out.println("Enter your Id");
        int yourId=scanner.nextInt();
        String[] args=new String[]{"src/meshtopology.txt", String.valueOf(yourId),"src/Peer"+yourId};
        try{
            int peerId= Integer.parseInt(args[1]);
            shareDir=args[2];
            System.out.println("Peer "+peerId+" stated with sharerd directory "+ shareDir);
            Properties properties= new Properties();
            fileName=args[0];
            System.out.println("Select the "+fileName);
            InputStream inputStream= new FileInputStream(fileName);
            properties.load(inputStream);
            port=Integer.parseInt(properties.getProperty("peer"+peerId+".serverport"));
            System.out.println("port id for peer is "+port);
            ServerDownload serverDownload= new ServerDownload(port, shareDir);
            serverDownload.start();
            portServer=Integer.parseInt(properties.getProperty("peer"+peerId+".port"));
            System.out.println("Server port for peer is "+portServer);
            ServerThread serverThread= new ServerThread(shareDir, portServer, peerId, fileName);
            serverThread.start();
            int hbPort=Integer.valueOf(properties.getProperty("peer"+peerId+".hbPort"));
            HeartbeatServer heartbeatServer=new HeartbeatServer(hbPort);
            heartbeatServer.start();
            System.out.println("Heartbeat Server is started at port "+hbPort);
            List<Integer> peerArr=List.of(properties.getProperty("peer"+peerId+".next").split(",")).stream().map((x)->Integer.valueOf(x)).collect(Collectors.toList());
            System.out.println("Heartbeat clients are started");
            System.out.println("Peer array got first for peer id are "+peerArr);
            HeartbeatClientMain heartbeatClient=new HeartbeatClientMain(peerArr,5000,fileName,peerId);
            heartbeatClient.start();
            SuperPeerClient superPeerClient= new SuperPeerClient(peerId, fileName);
            superPeerClient.start();
            System.out.println("Super peer client is started");
            File f=new File(shareDir);
            if (!f.exists()||!f.isDirectory()) {
                if (f.mkdir()){
                    System.out.println("New directory "+shareDir+" is made");
                };
            }

            while(true) {
                System.out.println("\n     Enter \n\t1 To download a file\n \t2 to Broadcast a invalied message\n");
                int ch = scanner.nextInt();
                scanner.nextLine();
                if (ch == 1) {
                    System.out.println("Enter the file to download");
                } else if (ch == 2) {
                    System.out.println("Enter the file name you want to broadcast as invalid file");
                }else {
                    System.out.println("Input mismatch");
                }
                String f_name = scanner.nextLine();
                count++;
                msgId = peerId + "." + count;
                String[] neighbours = properties.getProperty("peer" + peerId + ".next").split(",");
                TTLValue = neighbours.length;
                for (int i = 0; i < neighbours.length; i++) {
                    int connectingPort = Integer.parseInt(properties.getProperty("peer" + neighbours[i] + ".port"));
                    int neighbouringPeer = Integer.parseInt(neighbours[i]);
                    ClientThread clientThread = new ClientThread(connectingPort, neighbouringPeer, f_name, msgId, peerId, TTLValue);
                    Thread t = new Thread(clientThread);
                    t.start();
                    threads.add(t);
                    peers.add(clientThread);
                }
                for (int i = 0; i < threads.size(); i++) {
                    try {
                        threads.get(i).join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                int[] peersWithFiles;
                if (ch == 1) {
                    boolean found=false;
                    System.out.println("Peers containing the file are: ");
                    for (int i = 0; i < peers.size(); i++) {
                        peersWithFiles = peers.get(i).getPeersArray();
                        if (peersWithFiles==null) continue;
                        System.out.println("Files from peers "+ Utilities.printArray(peersWithFiles));
                        for (int j = 0; j < peersWithFiles.length; j++) {
                            if (peersWithFiles[j] == 0)
                                continue;
                            found=true;
                            System.out.println(peersWithFiles[j]);
                        }
                    }
                    if (found) {
                        System.out.println("Enter the peer from where to download the file: ");
                        int peerFromDownload = scanner.nextInt();
                        int portToDownload = Integer.parseInt(properties.getProperty("peer" + peerFromDownload + ".serverport"));
                        ClientasServer(peerFromDownload, portToDownload, f_name, shareDir);
                        System.out.println("File: " + f_name + " download from peer " + peerFromDownload + " to peer " + peerId);
                    }else {
                        System.out.println("No file found");
                    }

                }
                if (ch == 2) {
                    System.out.println("File modification message broadcast to ");
                    for (int i = 0; i < peers.size(); i++) {
                        peersWithFiles = peers.get(i).getPeersArray();
                        if (peersWithFiles==null) continue;
                        for (int j = 0; j < peersWithFiles.length; j++) {
                            if (peersWithFiles[j]==0)continue;
                            int peerFromDownload = peersWithFiles[j];
                            int portToDownload = Integer.parseInt(properties.getProperty("peer" + peerFromDownload + ".serverport"));
                            BroadcastInvalidMsg(peerFromDownload, portToDownload, f_name);
                        }
                    }
                }
                peers.clear();
                threads.clear();
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    private static void BroadcastInvalidMsg(int peerFromDownload, int portToDownload, String fName) {
    OutputStream outputStream=null;
    InputStream inputStream=null;
    Socket clientasServerSocket=null;
    try {
        clientasServerSocket=new Socket("localhost", portToDownload);
        outputStream=clientasServerSocket.getOutputStream();
        ObjectOutputStream objectOutputStream=new ObjectOutputStream(outputStream);
        objectOutputStream.flush();
        objectOutputStream.writeObject("Invalid file "+fName);
    } catch (UnknownHostException e) {
        e.printStackTrace();
    } catch (IOException e) {
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
        if (clientasServerSocket!=null){
            try {
                System.out.println("closing listener broadcast socket for "+peerFromDownload);
                clientasServerSocket.close();
            } catch (IOException e) {
e.printStackTrace();
            }
        }

    }
    }

    private static void ClientasServer(int peerFromDownload, int portToDownload, String fName, String shareDir) {
        OutputStream outputStream=null;
        InputStream inputStream=null;
        Socket clientServerSocket=null;
        try {
            clientServerSocket=new Socket("localhost", portToDownload);
            outputStream=clientServerSocket.getOutputStream();
            ObjectOutputStream objectOutputStream=new ObjectOutputStream(outputStream);
            objectOutputStream.flush();
            inputStream=clientServerSocket.getInputStream();
            ObjectInputStream objectInputStream= new ObjectInputStream(inputStream);
            objectOutputStream.writeObject(fName);
            int readByte= (int) objectInputStream.readObject();
            System.out.println("bytes transferred: "+readByte);
            byte[] b=new byte[readByte];
            objectInputStream.readFully(b);
            OutputStream fileOutputStream= new FileOutputStream(shareDir+"//"+fName);
            BufferedOutputStream bufferedOutputStream=new BufferedOutputStream(fileOutputStream);
            bufferedOutputStream.write(b,0,readByte);
            System.out.println(fName+" file has be downloaded to your directory "+shareDir);
            bufferedOutputStream.flush();
            objectOutputStream.flush();
            objectOutputStream.writeObject(true);
            System.out.println("Files now became "+List.of(new File(shareDir).listFiles()));
            System.out.println("Exist file "+fName+" is "+new File(shareDir+"//"+fName).exists());
            fileOutputStream.flush();
            fileOutputStream.close();
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
            if (clientServerSocket!=null){
                try {
                    System.out.println("closing listener client as server socket for "+peerFromDownload);
                    clientServerSocket.close();
                } catch (IOException e) {
e.printStackTrace();
                }
            }

        }
    }


}