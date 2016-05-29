package utils;

/**
 * Created by fuji on 16-5-26.
 */
public class Host {
    public Host(String addr,int port){
        this.addr =addr;
        this.port=port;
    }

    public String getAddr() {
        return addr;
    }

    public int getPort() {
        return port;
    }

    public static Host parseHost(String host,String port){
        return new Host(host,Integer.parseInt(port));
    }

    public static String formatHost(String name,Host host){
        return name+"@"+host.addr+":"+host.port;
    }

    @Override
    public int hashCode() {
        return addr.hashCode()^Integer.hashCode(port);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Host other = (Host) obj;
        return addr.equals(other.addr)&&port==other.port;
    }

    @Override
    public String toString(){
        return addr +" "+port;
    }

    private String addr;
    private int port;
}
