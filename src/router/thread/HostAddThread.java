package router.thread;

import utils.Host;
import utils.thread.HostUpdateThread;

import java.io.IOException;

/**
 * Created by fuji on 16-5-27.
 */
public class HostAddThread extends HostUpdateThread{
    public HostAddThread(Host targetHost,Host newHost) throws IOException {
        super(targetHost);
        host=newHost;
    }

    @Override
    protected void update() throws IOException {
        dos.writeUTF("ADD "+host);
    }

    private Host host;

}
