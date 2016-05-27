package utils;

/**
 * Created by fuji on 16-5-26.
 */
public class Host {
    public Host(String name,int port){
        this.name=name;
        this.port=port;
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    public static Host parseHost(String host,String port){
        return new Host(host,Integer.parseInt(port));
    }

    @Override
    public int hashCode() {
        return name.hashCode()^Integer.hashCode(port);
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
        return name.equals(other.name)&&port==other.port;
    }

    @Override
    public String toString(){
        return name+" "+port;
    }

    private String name;
    private int port;
}
