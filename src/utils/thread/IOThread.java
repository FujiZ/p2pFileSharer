package utils.thread;

import utils.Utility;

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
        dis= Utility.getInputStream(socket);
        dos=Utility.getOutputStream(socket);
    }

    protected void closeSocket(){
        Utility.closeInputStream(dis);
        Utility.closeOutputStream(dos);
        Utility.closeSocket(socket);
    }

    protected abstract void parseCmd(String cmdStr) throws IOException;

    @Override
    public void run(){
        System.out.println("New connection from "+socket.getInetAddress()+":"+socket.getPort());
        try {
            initSocketStream();
            String cmdStr=dis.readUTF();
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
