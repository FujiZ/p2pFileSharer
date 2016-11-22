package router;

import utils.Host;
import utils.HostUpdateThread;

import java.io.IOException;

/**
 * Created by fuji on 16-10-27.
 */
public class HostAddThread extends HostUpdateThread{
    public HostAddThread(Host targetHost,Host newHost) throws IOException {
        super(targetHost);
        host=newHost;
    }

    @Override
    protected void update() throws IOException {
        dos.writeUTF("ADD "+host.getName()+" "+host.getAddr());
    }

    private Host host;

}
