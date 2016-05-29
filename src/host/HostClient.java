package host;


import host.client.P2PClient;
import host.server.P2PServer;
import utils.Host;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by fuji on 16-5-28.
 */
public class HostClient {
    private static HostEnv hostEnv;
    private static P2PClient client;
    private static P2PServer server;
    private static ExecutorService executorService;

    public static void main(String[] argv){
        Host router=new Host("127.0.0.1",10240);
        try {
            hostEnv=new HostEnv("zhp",new File("/home/fuji/tmp/server"),router);
            server=new P2PServer(hostEnv,12450);
            client=new P2PClient(hostEnv);
            hostEnv.setServer(server);
            executorService= Executors.newCachedThreadPool();
            executorService.execute(server);
            executorService.execute(client);
            executorService.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
