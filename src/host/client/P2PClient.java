package host.client;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by fuji on 16-5-26.
 */
public class P2PClient implements Runnable{

    public P2PClient(String dir){
        executorService= Executors.newCachedThreadPool();
        this.dir=new File(dir);
        // TODO: 16-5-26 判断dir是否为目录
    }

    public File getDir(){
        return dir;
    }

    @Override
    public void run(){
        // TODO: 16-5-26 读取指令
        Scanner sc=new Scanner(System.in);
        while (true){
            String line=sc.nextLine();
            if(line.toUpperCase().equals("EXIT"))
                break;
            try {
                executorService.execute(new ClientThread(this,line,"127.0.0.1",12345));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // fixed: 16-5-26 每一个命令都创建一个线程
    }

    public static void main(String[] argv){
        new P2PClient("/home/fuji/tmp/client").run();
    }

    private ExecutorService executorService;
    private File dir;
}
