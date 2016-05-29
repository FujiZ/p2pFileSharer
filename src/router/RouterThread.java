package router;

import router.thread.HostAddThread;
import router.thread.HostDelThread;
import utils.Host;
import utils.thread.IOThread;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
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
        //HELLO HOSTNAME HOSTADDR PORT
        if(argv.length!=4){
            // TODO: 16-5-27 参数错误
            return;
        }

        String name=argv[1];
        Host newHost=Host.parseHost(name,argv[2],argv[3]);
        //检查昵称是否已经存在
        if(server.getHostMap().containsKey(name)){
            sendErrorMsg("NAME EXISTS");
            return;
        }

        //向set中的所有主机广播ADD信息
        synchronized (server.getHostMap()){
            for(Map.Entry<String,Host> hostEntry:server.getHostMap().entrySet()){
                executorService.execute(new HostAddThread(hostEntry.getValue(),newHost));
            }
        }
        //将host加入当前set
        server.getHostMap().put(name,newHost);
        //向host发送更新当前的set
        sendHostMap(newHost);
    }

    private void sendHostMap(Host host) throws IOException {
        dos.writeUTF("ACCEPT "+host);
        dos.writeUTF("HOSTNUM "+server.getHostMap().size());
        synchronized (server.getHostMap()){
            for(Map.Entry<String,Host> hostEntry:server.getHostMap().entrySet()){
                dos.writeUTF("ADD "+hostEntry.getKey()+" "+hostEntry.getValue().getAddr());
            }
        }
    }

    private void processBye(String[] argv) throws IOException {
        //BYE HOSTNAME
        if(argv.length!=2){
            // TODO: 16-5-27 参数错误
            return;
        }

        String name=argv[1];
        //将host从当前set中删除
        server.getHostMap().remove(name);
        //向set中的所有主机广播DEL信息
        synchronized (server.getHostMap()){
            for(Map.Entry<String,Host> hostEntry:server.getHostMap().entrySet()){
                executorService.execute(new HostDelThread(hostEntry.getValue(),name));
            }
        }
    }

    private ExecutorService executorService;
    private RouterServer server;
}
