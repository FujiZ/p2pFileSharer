package host.client;

import host.HostEnv;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by fuji on 16-5-26.
 */
public class P2PClient implements Runnable{

    public P2PClient(HostEnv hostEnv){
        executorService= Executors.newCachedThreadPool();
        this.hostEnv=hostEnv;
    }

    @Override
    public void run(){
        // TODO: 16-5-26 读取指令
        Scanner sc=new Scanner(System.in);
        while (true){
            String line=sc.nextLine();
            if(line.toUpperCase().equals("EXIT")){
                hostEnv.getServer().sendBye();
                System.exit(0);
            }
            try {
                // fixed: 16-5-26 每一个命令都创建一个线程
                executorService.execute(new ClientThread(hostEnv,line,"127.0.0.1",12345));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private ExecutorService executorService;
    private HostEnv hostEnv;
}
