package utils.thread;

import utils.Host;
import utils.IOUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by fuji on 16-5-27.
 */
public abstract class HostUpdateThread implements Runnable{
    //需要tarHost,updateInfo这两个信息
    public HostUpdateThread(Host targetHost) throws IOException {
        socket= IOUtils.getSocket(targetHost);
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

    protected abstract void update() throws IOException;

    //需要重载更新信息的操作
    @Override
    public void run() {
        try {
            initSocketStream();
            update();
        }catch (IOException e){
            e.printStackTrace();
        }
        finally {
            closeSocket();
        }
    }

    protected Socket socket;
    protected DataInputStream dis;
    protected DataOutputStream dos;

}
