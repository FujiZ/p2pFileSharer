package router;

import router.thread.HostAddThread;
import router.thread.HostDelThread;
import utils.Host;
import utils.thread.IOThread;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by fuji on 16-5-26.
 */
public class RouterThread extends IOThread{

    public RouterThread(RouterServer server, Socket socket){
        super(socket);
        this.executorService= Executors.newCachedThreadPool();
        this.server=server;
    }

    @Override
    protected void parseCmd(String cmdStr) throws IOException {
        String[] argv=cmdStr.split(" ");
        switch (argv[0].toUpperCase()){
            case "HELLO":
                processHello(argv); break;
            case "BYE":
                processBye(argv); break;
            default:
                break;
        }
    }

    private void processHello(String[] argv) throws IOException {
        //HELLO HOSTNAME PORT
        if(argv.length!=3){
            // TODO: 16-5-27 参数错误
            return;
        }

        Host newHost=Host.parseHost(argv[1],argv[2]);
        //向set中的所有主机广播ADD信息
        synchronized (server.getHostSet()){
            for(Host oldHost:server.getHostSet()){
                executorService.execute(new HostAddThread(oldHost,newHost));
            }
        }
        //将host加入当前set
        server.getHostSet().add(newHost);
        //向host发送更新当前的set
        sendHostSet(newHost);
    }

    private void sendHostSet(Host host) throws IOException {
        dos.writeUTF("ACCEPT "+host);
        dos.writeUTF("HOSTNUM "+server.getHostSet().size());
        synchronized (server.getHostSet()){
            for(Host h:server.getHostSet()){
                dos.writeUTF("ADD "+h);
            }
        }
    }

    private void processBye(String[] argv) throws IOException {
        //BYE HOSTNAME PORT
        if(argv.length!=3){
            // TODO: 16-5-27 参数错误
            return;
        }

        Host oldHost=Host.parseHost(argv[1],argv[2]);
        //将host从当前set中删除
        server.getHostSet().remove(oldHost);
        //向set中的所有主机广播DEL信息
        synchronized (server.getHostSet()){
            for(Host host:server.getHostSet()){
                executorService.execute(new HostDelThread(host,oldHost));
            }
        }
    }

    private ExecutorService executorService;
    private RouterServer server;
}
