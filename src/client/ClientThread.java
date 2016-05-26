package client;

import utils.Utility;

import java.io.*;
import java.net.Socket;
import java.nio.file.Paths;

/**
 * Created by fuji on 16-5-26.
 */
public class ClientThread implements Runnable{

    public ClientThread(P2PClient client,String cmdStr,String hostname,int port) throws IOException {
        this.client=client;
        this.cmdStr=cmdStr;
        socket=new Socket(hostname,port);
        System.out.println("Connection success");
    }

    private void parseCmd(String cmdStr) throws IOException {
        String[] argv=cmdStr.split(" ");
        switch (argv[0].toUpperCase()){
            case "GET":
                processGet(argv);
                break;
            default:
                System.out.println("Invalid input");
                break;
        }
    }

    private void processGet(String[] argv) throws IOException {
        if(argv.length!=2){
            // TODO: 16-5-26 参数错误
            return;
        }
        File file=Paths.get(client.getDir().getAbsolutePath(),argv[1]).toFile();
        if(file.exists()){
            System.out.println("File already exists!");
            return;
        }

        dos.writeUTF("GET "+argv[1]);
        dos.flush();
        String response=dis.readUTF();
        if(response.startsWith("OK")) {
            receiveFile(file);
        }
        else {
            System.out.println(response);
        }
    }

    private void receiveFile(File file) throws IOException {
        //SIZE 12345
        long fileSize=Long.parseLong(dis.readUTF().split(" ")[1]);
        byte[] buffer=new byte[BUFSIZE];
        long passedlen=0;
        DataOutputStream fileOut= Utility.getOutputStream(file);
        System.out.println("文件长度为: "+fileSize);
        System.out.println("开始接收文件");
        while (true){
            int read=dis.read(buffer);
            if(read==-1)
                break;
            fileOut.write(buffer,0,read);
            passedlen+=read;
            System.out.println("文件接收了"+(passedlen*100/fileSize)+"%");
        }
        System.out.println("接收完成");
        fileOut.close();
    }

    @Override
    public void run(){
        try {
            dis=Utility.getInputStream(socket);
            dos=Utility.getOutputStream(socket);
            parseCmd(cmdStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            Utility.closeInputStream(dis);
            Utility.closeOutputStream(dos);
            Utility.closeSocket(socket);
        }
    }

    private static int BUFSIZE=8192;

    private P2PClient client;
    private String cmdStr;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
}
