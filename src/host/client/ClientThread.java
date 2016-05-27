package host.client;

import utils.thread.IOThread;
import utils.IOUtils;

import java.io.*;
import java.net.Socket;
import java.nio.file.Paths;

/**
 * Created by fuji on 16-5-26.
 */
public class ClientThread extends IOThread{

    public ClientThread(P2PClient client,String cmdStr,String hostname,int port) throws IOException {
        super(new Socket(hostname,port));
        this.client=client;
        this.cmdStr=cmdStr;
        System.out.println("Connection success");
    }

    protected void parseCmd(String cmdStr) throws IOException {
        String[] argv=cmdStr.split(" ");
        switch (argv[0].toUpperCase()){
            case "GET":
                processGet(argv); break;
            case "HELLO":
                processHello(argv); break;
            case "BYE":
                processBye(argv); break;
            default:
                System.out.println("Invalid input");
                break;
        }
    }

    private void processHello(String[] argv) {

    }

    private void processBye(String[] argv) {

    }

    private void processGet(String[] argv) throws IOException {
        if(!checkArg(argv,2))return;

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
        DataOutputStream fileOut= IOUtils.getOutputStream(file);
        System.out.println("File size: "+fileSize);
        System.out.println("Receiving file");
        while (true){
            int read=dis.read(buffer);
            if(read==-1)
                break;
            fileOut.write(buffer,0,read);
            passedlen+=read;
            System.out.println(file.getName()+" received"+(passedlen*100/fileSize)+"%");
        }
        System.out.println("Receive complete");
        fileOut.close();
    }

    @Override
    public void run(){
        try {
            initSocketStream();
            parseCmd(cmdStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            closeSocket();
        }
    }

    private P2PClient client;
    private String cmdStr;
}
