package router;

import utils.Host;
import utils.thread.IOThread;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by fuji on 16-5-26.
 */
public class RouterThread extends IOThread{

    public RouterThread(RouterServer server, Socket socket){
        super(socket);
        this.server=server;
    }

    @Override
    protected void parseCmd(String cmdStr) throws IOException {
        String[] argv=cmdStr.split(" ");
        switch (argv[0].toUpperCase()){
            case "HELLO":
                processHello(argv);
                break;
            default:
                break;
        }
    }

    private void processHello(String[] argv){
        //HELLO HOSTNAME PORT
        if(argv.length!=3){
            // TODO: 16-5-27 参数错误
        }

        Host host=Host.parseHost(argv[1],argv[2]);
        // TODO: 16-5-27 processHello
        //向set中的所有主机广播ADD信息

        //将host加入当前set
        //向host发送更新当前的set
    }

    private void processBye(String[] argv){
        //BYE HOSTNAME PORT
        if(argv.length!=3){
            // TODO: 16-5-27 参数错误
        }

        Host host=Host.parseHost(argv[1],argv[2]);
        // TODO: 16-5-27 processBye
        //向set中的所有主机广播DEL信息
        //将host从当前set中删除
    }

    private RouterServer server;
}
