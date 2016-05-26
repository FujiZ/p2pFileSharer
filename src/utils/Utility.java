package utils;

import java.io.*;
import java.net.Socket;

/**
 * Created by fuji on 16-5-26.
 */
public class Utility {
    public static BufferedReader getBufferedReader(Socket socket) throws IOException {
        return new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public static PrintWriter getPrintWriter(Socket socket) throws IOException {
        return new PrintWriter(socket.getOutputStream());
    }

    public static DataInputStream getInputStream(Socket socket) throws IOException {
        return new DataInputStream(new BufferedInputStream(socket.getInputStream()));
    }

    public static DataOutputStream getOutputStream(Socket socket) throws IOException {
        return new DataOutputStream(socket.getOutputStream());
    }

    public static DataOutputStream getOutputStream(File file) throws IOException {
        return new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
    }

    public static void closeInputStream(InputStream stream){
        if(stream==null)
            return;
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void closeOutputStream(OutputStream stream){
        if(stream==null)
            return;
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void closeWriter(Writer writer){
        if(writer==null)
            return;
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void closeReader(Reader reader){
        if(reader==null)
            return;
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void closeSocket(Socket socket){
        if(socket==null)
            return;
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
