package router;

import utils.Host;

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
public class RouterServer implements Runnable{

    public RouterServer(int port) throws IOException {
        executorService= Executors.newCachedThreadPool();
        serverSocket=new ServerSocket(port);
        hostSet=Collections.synchronizedSet(new HashSet<>());
        System.out.println("Router server started successfully on "+port);
    }

    @Override
    public void run(){
        while (true){
            Socket socket;
            try {
                socket=serverSocket.accept();
                // fixed: 16-5-26 fork a thread to process request
                executorService.execute(new RouterThread(this,socket));
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] argv){
        try {
            new RouterServer(10240).run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Set<Host> getHostSet() {
        return hostSet;
    }

    private ExecutorService executorService;
    private ServerSocket serverSocket;
    private Set<Host> hostSet;

}
