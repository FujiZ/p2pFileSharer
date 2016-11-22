package host;

import utils.Host;

import java.io.File;
import java.util.*;

/**
 * Created by fuji on 16-10-28.
 */
public class HostEnv {

    public HostEnv(String name,File dir,Host router) throws Exception {
        this.name=name;
        this.dir=dir;
        this.router=router;
        hostMap=Collections.synchronizedMap(new HashMap<>());
        if(!this.dir.isDirectory()){
            throw new Exception("Not a directory");
        }
    }

    public File getDir() {
        return dir;
    }

    public P2PServer getServer() {
        return server;
    }

    public void setServer(P2PServer server) {
        this.server = server;
    }

    public P2PClient getClient() {
        return client;
    }

    public void setClient(P2PClient client) {
        this.client = client;
    }

    public Host getRouter() {
        return router;
    }

    public void addHost(String name,Host host){
        hostMap.put(name,host);
    }

    public Host removeHost(String name){
        return hostMap.remove(name);
    }

    public String getName(){
        return name;
    }

    public Map<String,Host> getHostMap(){
        return hostMap;
    }

    private File dir;
    private Host router;
    private String name;
    private P2PServer server;
    private P2PClient client;
    private final Map<String,Host> hostMap;

}
