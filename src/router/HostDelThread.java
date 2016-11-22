package router;

import utils.Host;
import utils.HostUpdateThread;

import java.io.IOException;

/**
 * Created by fuji on 16-10-27.
 */
public class HostDelThread extends HostUpdateThread{

    public HostDelThread(Host targetHost, String name) throws IOException {
        super(targetHost);
        this.name=name;
    }

    @Override
    protected void update() throws IOException {
        dos.writeUTF("DEL "+name);
    }

    private String name;
}
