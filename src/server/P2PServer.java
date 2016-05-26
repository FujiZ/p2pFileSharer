package server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by fuji on 16-5-26.
 */
public class P2PServer implements Runnable{

    public P2PServer(String dir,int port) throws IOException {
        executorService =Executors.newCachedThreadPool();
        this.dir=new File(dir);
        // TODO: 16-5-26 判断dir是否为目录
        serverSocket=new ServerSocket(port);
        System.out.println("P2Pserver started successfully on "+port);
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
    private ServerSocket serverSocket;
    private File dir;

}
