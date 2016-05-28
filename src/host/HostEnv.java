package host;

import host.client.P2PClient;
import host.server.P2PServer;
import utils.Host;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by fuji on 16-5-28.
 */
public class HostEnv {

    public HostEnv(String dir,Host router) throws Exception {
        this.dir=new File(dir);
        this.router=router;
        hostSet=Collections.synchronizedSet(new HashSet<>());
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

    public void addHost(Host host){
        hostSet.add(host);
    }

    public void removeHost(Host host){
        hostSet.remove(host);
    }

    private File dir;
    private Host router;
    private P2PServer server;
    private P2PClient client;
    private final Set<Host> hostSet;

}
