package host.server;

import host.HostEnv;
import utils.Host;
import utils.IOUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by fuji on 16-5-26.
 */
public class P2PServer implements Runnable{

    public P2PServer(HostEnv hostEnv, int port) throws IOException {
        executorService=Executors.newCachedThreadPool();
        this.hostEnv=hostEnv;
        this.port=port;
        serverSocket=new ServerSocket(port);
        System.out.println("P2Pserver started successfully on "+port);
    }

    public boolean sendHello(){
        Socket socket=null;
        DataInputStream dis=null;
        DataOutputStream dos=null;
        try {
            socket=IOUtils.getSocket(hostEnv.getRouter());
            dis=IOUtils.getInputStream(socket);
            dos=IOUtils.getOutputStream(socket);
            //send HELLO HOSTNAME HOSTADDR PORT to router
            dos.writeUTF("HELLO "+hostEnv.getName()+" "+IOUtils.getHostAddr()+" "+port);
            String response=dis.readUTF();
            if(response.startsWith("ACCEPT")){
                //HOSTNUM count
                int hostCount=Integer.parseInt(dis.readUTF().split(" ")[1]);
                for(int i=0;i<hostCount;++i){
                    //ADD HOSTNAME HOSTADDR PORT
                    String[] argv=dis.readUTF().split(" ");
                    hostEnv.addHost(argv[1],Host.parseHost(argv[2],argv[3]));
                }
            }
            else {
                System.out.println(response);
            }
        }
        catch (IOException e){
            e.printStackTrace();
            return false;
        }
        finally {
            IOUtils.closeInputStream(dis);
            IOUtils.closeOutputStream(dos);
            IOUtils.closeSocket(socket);
        }
        return true;
    }

    public void sendBye(){
        Socket socket=null;
        DataOutputStream dos=null;
        try {
            socket=IOUtils.getSocket(hostEnv.getRouter());
            dos=IOUtils.getOutputStream(socket);
            //send BYE HOSTNAME
            dos.writeUTF("BYE "+hostEnv.getName());
        }
        catch (IOException e){
            e.printStackTrace();
        }
        finally {
            IOUtils.closeOutputStream(dos);
            IOUtils.closeSocket(socket);
        }
    }

    @Override
    public void run(){
        while (true){
            Socket socket;
            try {
                socket=serverSocket.accept();
                // fixed: 16-5-26 fork a thread to process request
                executorService.execute(new ServerThread(hostEnv,socket));
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    // fixed: 16-5-26 threadpool is needed to process connections
    private ExecutorService executorService;
    private ServerSocket serverSocket;
    private HostEnv hostEnv;
    private int port;

}
