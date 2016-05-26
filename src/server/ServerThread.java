package server;

import utils.Utility;

import java.io.*;
import java.net.Socket;

/**
 * Created by fuji on 16-5-26.
 */
public class ServerThread implements Runnable{

    public ServerThread(P2PServer server, Socket socket){
        this.server=server;
        this.socket=socket;
    }

    private void parseCmd(String cmdStr) throws IOException {
        String[] argv=cmdStr.split(" ");
        switch (argv[0].toUpperCase()){
            case "GET":
                processGet(argv);
                break;
            default:
                break;
        }
    }

    private void processGet(String[] argv) throws IOException {
        if(argv.length!=2){
            // TODO: 16-5-26 参数错误
        }
        // fixed: 16-5-26 find the file in cur dir
        File[] files=server.getDir().listFiles();
        for(File file:files){
            if(file.getName().equals(argv[1])){
                if(file.isFile()){
                    sendFile(file);
                    return;
                }
            }
        }
        // TODO: 16-5-26  没有该文件
        dos.writeUTF("ERROR NO SUCH FILE");
    }

    private void sendFile(File file) throws IOException {
        System.out.println("Transfer "+file.getName());
        DataOutputStream dos=new DataOutputStream(socket.getOutputStream());
        FileInputStream fis=new FileInputStream(file);
        byte[] buffer=new byte[BUFSIZE];
        //OK filename
        //SIZE filesize
        dos.writeUTF("OK "+file.getName());
        dos.writeUTF("SIZE "+file.length());
        while (true){
            int read=fis.read(buffer);
            if(read==-1)
                break;
            dos.write(buffer,0,read);
        }
        fis.close();
        System.out.println("Transfer complete");
    }

    @Override
    public void run() {
        System.out.println("New connection from "+socket.getInetAddress()+":"+socket.getPort());
        try {
            dis= Utility.getInputStream(socket);
            dos=Utility.getOutputStream(socket);
            String cmdStr=dis.readUTF();
            parseCmd(cmdStr);
            // TODO: 16-5-26 leave message
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            Utility.closeInputStream(dis);
            Utility.closeOutputStream(dos);
            Utility.closeSocket(socket);
        }
    }

    private P2PServer server;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    private static int BUFSIZE=8192;
}
