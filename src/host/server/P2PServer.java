package host.server;

import utils.Host;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by fuji on 16-5-26.
 */
public class P2PServer implements Runnable{

    public P2PServer(String dir,int port) throws IOException {
        executorService=Executors.newCachedThreadPool();
        hostSet= Collections.synchronizedSet(new HashSet<>());
        this.dir=new File(dir);
        // TODO: 16-5-26 判断dir是否为目录
        serverSocket=new ServerSocket(port);
        System.out.println("P2Pserver started successfully on "+port);
    }

    public void addHost(Host host){
        hostSet.add(host);
    }

    public void removeHost(Host host){
        hostSet.remove(host);
    }

    @Override
    public void run(){
        while (true){
            Socket socket;
            try {
                socket=serverSocket.accept();
                // fixed: 16-5-26 fork a thread to process request
                executorService.execute(new ServerThread(this,socket));
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public File getDir(){
        return dir;
    }

    public static void main(String[] argv){
        try {
            new P2PServer("/home/fuji/tmp/server",12345).run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // fixed: 16-5-26 threadpool is needed to process connections
    private ExecutorService executorService;
    private final Set<Host> hostSet;
    private ServerSocket serverSocket;
    private File dir;

}
