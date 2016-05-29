package utils.thread;

import utils.IOUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by fuji on 16-5-27.
 */
public abstract class IOThread implements Runnable{
    public IOThread(Socket socket){
        this.socket=socket;
    }

    protected void initSocketStream() throws IOException {
        dis= IOUtils.getInputStream(socket);
        dos= IOUtils.getOutputStream(socket);
    }

    protected void closeSocket(){
        IOUtils.closeInputStream(dis);
        IOUtils.closeOutputStream(dos);
        IOUtils.closeSocket(socket);
    }

    protected void sendErrorMsg(String msg) throws IOException {
        dos.writeUTF("ERROR "+msg);
    }

    protected boolean checkArg(String[] argv,int count) throws IOException {
        if(argv.length!=count) {
            sendErrorMsg("INVALID ARGS");
            return false;
        }
        return true;
    }

    protected abstract void parseCmd(String cmdStr) throws IOException;

    @Override
    public void run(){
        System.out.println("New connection from "+socket.getInetAddress()+":"+socket.getPort());
        try {
            initSocketStream();
            String cmdStr=dis.readUTF();
            System.out.println(cmdStr);
            parseCmd(cmdStr);
        }
        catch (IOException e){
            e.printStackTrace();
        }
        finally {
            closeSocket();
        }
    }

    protected Socket socket;
    protected DataInputStream dis;
    protected DataOutputStream dos;

    protected static int BUFSIZE=8192;
}
