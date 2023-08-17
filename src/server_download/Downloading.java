package server_download;

import java.io.*;
import java.net.Socket;

class Downloading extends Thread{

    int portNo;
    String sharedDirectory;
    Socket socket;
    String fileName;
    public Downloading(Socket socket, int portNo, String fileDirectory) {
        this.socket=socket;
        this.portNo=portNo;
        sharedDirectory=fileDirectory;
    }

    @Override
    public void run() {
        InputStream inputStream= null;
        OutputStream outputStream=null;
        try {
            inputStream = socket.getInputStream();
            ObjectInputStream objectInputStream= new ObjectInputStream(inputStream);
            outputStream=socket.getOutputStream();
            ObjectOutputStream objectOutputStream=new ObjectOutputStream(outputStream);
            fileName= (String) objectInputStream.readObject();
            String fileLocation;
            if (fileName.startsWith("Invalid File")){
                System.out.println(fileName+" modified by user");
            }else {
                while (true){
                    File myFile=new File(sharedDirectory+"//"+fileName);
                    long length=myFile.length();
                    byte[] myByteArray=new byte[(int)length];
//                   //Sending the file length to be downloaded to the client
                    objectOutputStream.writeObject((int)myFile.length());
                    objectOutputStream.flush();
                    FileInputStream fileInputStream=new FileInputStream(myFile);
                    BufferedInputStream bufferedInputStream=new BufferedInputStream(fileInputStream);
                    bufferedInputStream.read(myByteArray, 0, (int) myFile.length());
                    System.out.println("sending file of "+ myByteArray.length+" bytes");
                    objectOutputStream.write(myByteArray, 0, myByteArray.length);
                    objectOutputStream.flush();
                    boolean done= (boolean) objectInputStream.readObject();
                    if (done) {
                        System.out.println("File sending complete");
                        fileInputStream.close();
                        break;
                    }
                }
            }
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
                    System.out.println("closing listener socket for server download");
                    socket.close();
                } catch (IOException e) {

                }
            }


        }
}
