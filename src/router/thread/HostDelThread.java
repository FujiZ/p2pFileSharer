package router.thread;

import utils.Host;
import utils.thread.HostUpdateThread;

import java.io.IOException;

/**
 * Created by fuji on 16-5-27.
 */
public class HostDelThread extends HostUpdateThread{

    public HostDelThread(Host targetHost, Host oldHost) throws IOException {
        super(targetHost);
        host=oldHost;
    }

    @Override
    protected void update() throws IOException {
        dos.writeUTF("DEL "+host);
    }

    private Host host;
}
