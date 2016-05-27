package host;

import host.client.P2PClient;
import host.server.P2PServer;

import java.io.File;

/**
 * Created by fuji on 16-5-28.
 */
public class HostEnv {

    public static File getDir() {
        return dir;
    }

    private static File dir;

    private static P2PServer server;
    private static P2PClient client;

}
