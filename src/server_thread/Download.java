package server_thread;

import client_thread.ClientThread;
import com.sun.tools.javac.Main;
import heartbeat_server.HeartbeatClientMain;
import utilities.MessageFormat;
import utilities.Utilities;

import javax.sound.midi.Soundbank;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

class Download extends Thread{

    protected Socket socket;
    String fileDirectory;
    int port;
    String fileToDownload;
    int peerId;
    ArrayList<String> peerMsg;
    ArrayList<Thread> threads=new ArrayList<>();
    ArrayList<ClientThread> peersWithFiles=new ArrayList<>();
    int[] peersArray_list=new int[20];
    int []a=new int[20];
    int countOfPeers=0;
    int messageId;
    int set=0;
    int TTLValue;
    MessageFormat MF= new MessageFormat();
    String configFileName;
    public Download(Socket socket, String fileDirectory, int peerId, final ArrayList<String> msg, String configFileName) {
    this.socket=socket;
    this.fileDirectory=fileDirectory;
    this.peerId=peerId;
    peerMsg=msg;
    this.configFileName=configFileName;
    }

    @Override
    public void run() {

        System.out.println("Server thread for peer "+ peerId);
        InputStream inputStream= null;
        OutputStream outputStream=null;
        try {
            inputStream = socket.getInputStream();
            ObjectInputStream objectInputStream=new ObjectInputStream(inputStream);
            outputStream=socket.getOutputStream();
            ObjectOutputStream objectOutputStream=new ObjectOutputStream(outputStream);
            boolean peerDuplicate;
            MF= (MessageFormat) objectInputStream.readObject();
            System.out.println("got request from "+MF.getFromPeerId());
            peerDuplicate=peerMsg.contains(MF.getMsgId());
            if (peerDuplicate){
                System.out.println("duplicate");
            }else {
                System.out.println(MF.getMsgId());
                peerMsg.add(MF.getMsgId());
            }
            fileToDownload=MF.getFileToDownload();
            System.out.println("Found: "+fileToDownload);
            if (!peerDuplicate){
                File newFile;
                File directoryObj=new File(fileDirectory);
                String[] fileList= directoryObj.list();
                System.out.println("for file directory "+fileDirectory+" list of files "+List.of(fileList));
                for (String file:fileList){
                    newFile=new File(file);
                    if (newFile.getName().equals(fileToDownload)){
                        System.out.println("Adding peer id "+peerId+" to peer lis");
                        peersArray_list[countOfPeers++]=peerId;
                        break;
                    }
                }
                System.out.println("Local search complete");
                Properties properties= new Properties();
                InputStream inputStream1=new FileInputStream(configFileName);
                properties.load(inputStream1);
                String temp=null;

                while (temp==null || temp.split(",").length==0){
                    temp= String.join(",",HeartbeatClientMain.getPeers().stream().map((x)->String.valueOf(x)).collect(Collectors.toList()));
                    Thread.sleep(5000);
                }

                if (temp!=null && MF.getTTlValue()>0){
                    String[] neighbours=temp.split(",");
                    for (int i=0; i<neighbours.length;i++){
                        if (MF.getFromPeerId()==Integer.parseInt(neighbours[i])){
                            continue;
                        }
                        int connectingPort= Integer.parseInt(properties.getProperty("peer"+neighbours[i]+".port"));
                        int neighbouringPeer=Integer.parseInt(neighbours[i]);
                        System.out.println("Sending to "+neighbouringPeer);
                        int ttlValue= MF.getTTlValue();
                        ttlValue--;
                        MF.setTTlValue(ttlValue);
                        ClientThread clientThread=new ClientThread(connectingPort, neighbouringPeer, fileToDownload, MF.getMsgId(), peerId, MF.getTTlValue());
                        Thread t= new Thread(clientThread);
                        t.start();
                        threads.add(t);
                        peersWithFiles.add(clientThread);
                    }
                }
                for (int i=0; i<threads.size();i++){
                    threads.get(i).join();
                }
                for (ClientThread peer:peersWithFiles){
                    a=peer.getPeersArray();
                    if (a==null) continue;
                    System.out.println("peers array from client "+List.of(a));
                    for (int j=0; j<a.length;j++){
                        if (a[j]==0)continue;
                        boolean found=false;
                        for (int p: peersArray_list) {
                            if (p==a[j]){
                                    found=true;
                                    break;
                            }
                        }
                        if (!found)peersArray_list[countOfPeers++] = a[j];
                    }
                }
                System.out.println("Sending peers array list to client "+Utilities.printArray(peersArray_list));
                objectOutputStream.writeObject(peersArray_list);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
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
                    System.out.println("closing listener server thread socket for peer id "+peerId);
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();

                }
            }

        }
    }
}
