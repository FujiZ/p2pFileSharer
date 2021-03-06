package host;

import utils.Host;
import utils.IOThread;
import utils.IOUtils;

import java.io.*;
import java.net.Socket;

/**
 * Created by fuji on 16-10-26.
 */
public class ServerThread extends IOThread{

    public ServerThread(HostEnv hostEnv, Socket socket){
        super(socket);
        this.hostEnv=hostEnv;
    }

    @Override
    protected void parseCmd(String cmdStr) throws IOException {
        System.out.println("Received cmd: "+cmdStr);
        String[] argv=cmdStr.split(" ");
        switch (argv[0].toUpperCase()){
            case "GET":
                processGet(argv); break;
            case "ADD":
                processAdd(argv); break;
            case "DEL":
                processDel(argv); break;
            case "LIST":
                processList(argv); break;
            default:
                break;
        }
    }

    private void processList(String[] argv) throws IOException {
        if(!checkArg(argv,1))return;

        int count=0;
        File[] fileList=hostEnv.getDir().listFiles();
        assert fileList!=null;
        for(File file:fileList){
            if(file.isFile())
                ++count;
        }
        dos.writeUTF("FILECOUNT "+count);
        for(File file:fileList){
            if(file.isFile()){
                dos.writeUTF("FILE "+file.getName()+" "+file.length());
            }
        }
    }

    private void processAdd(String[] argv) throws IOException{
        if(!checkArg(argv,4))return;

        Host newHost=Host.parseHost(argv[1],argv[2],argv[3]);
        hostEnv.addHost(argv[1],newHost);
        hostEnv.getClient().addHost(newHost);
    }

    private void processDel(String[] argv) throws IOException{
        if(!checkArg(argv,2))return;

        String name=argv[1];
        Host oldHost=hostEnv.removeHost(name);
        hostEnv.getClient().delHost(oldHost);
    }

    private void processGet(String[] argv) throws IOException {
        if(!checkArg(argv,2))return;

        // fixed: 16-5-26 find the file in cur dir
        File[] fileList=hostEnv.getDir().listFiles();
        assert fileList!=null;
        for(File file:fileList){
            if(file.isFile()&&file.getName().equals(argv[1])){
                sendFile(file);
                return;
            }
        }
        // fixed: 16-5-26  没有该文件
        sendErrorMsg("NO SUCH FILE "+argv[1]);
    }

    private void sendFile(File file) throws IOException {
        System.out.println("Send "+file.getName());
        dos= IOUtils.getOutputStream(socket);
        FileInputStream fis=new FileInputStream(file);
        byte[] buffer=new byte[BUFSIZE];
        //OK filename
        //SIZE filesize
        dos.writeUTF("OK "+file.getName());
        dos.writeUTF("FILESIZE "+file.length());
        while (true){
            int read=fis.read(buffer);
            if(read==-1)
                break;
            dos.write(buffer,0,read);
        }
        fis.close();
        System.out.println("Transfer complete");
    }

    private HostEnv hostEnv;

}
