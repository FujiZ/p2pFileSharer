package utils;

/**
 * Created by fuji on 16-5-26.
 */
public class Host {
    public Host(String name,String addr,int port){
        this.name=name;
        this.ip =addr;
        this.port=port;
    }

    public String getIP() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getName(){
        return name;
    }

    public String getAddr(){
        return ip+" "+port;
    }

    public static Host parseHost(String name,String host,String port){
        return new Host(name,host,Integer.parseInt(port));
    }

    public static String formatHost(String name,Host host){
        return name+"@"+host.ip +":"+host.port;
    }

    @Override
    public int hashCode() {
        return ip.hashCode()^Integer.hashCode(port);
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
        return name.equals(other.name)&& ip.equals(other.ip)&&port==other.port;
    }

    @Override
    public String toString(){
        return name+"@"+ ip +":"+port;
    }

    private String name;
    private String ip;
    private int port;
}
