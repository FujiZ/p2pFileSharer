package router;

import utils.Host;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by fuji on 16-5-26.
 */
public class RouterServer extends JFrame implements Runnable{

    public RouterServer() throws IOException {
        super("Router Server");
        initElements();
        initNorthPanel();
        initCenterPanel();
        initFrame();
        initActionListener();
        setStarted(false);
        executorService= Executors.newCachedThreadPool();
        hostMap=Collections.synchronizedMap(new HashMap<>());
    }

    private void setStarted(boolean flag){
        isStarted=flag;
        txt_port.setEditable(!flag);
        btn_start.setEnabled(!flag);
        btn_stop.setEnabled(flag);
    }

    private void initActionListener() {
        btn_start.addActionListener(e->startServer());
        btn_stop.addActionListener(e->stopServer());
        // 关闭窗口时事件
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (isStarted) {
                    try {
                        serverSocket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                System.exit(0);// 退出程序
            }
        });
    }

    private void stopServer(){
        if(!isStarted){
            JOptionPane.showMessageDialog(this, "服务器还未启动!",
                    "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        JOptionPane.showMessageDialog(this, "停止成功!");
        addLog("Server stoped");
        setStarted(false);
    }

    private void startServer() {
        if(isStarted){
            JOptionPane.showMessageDialog(this, "服务器已经启动!",
                    "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int port;
            try {
                port = Integer.parseInt(txt_port.getText().trim());
                serverSocket = new ServerSocket(port);
            } catch (NumberFormatException e) {
                throw new Exception("端口号必须为整数!");
            }
            setStarted(true);
            executorService.execute(this);
            JOptionPane.showMessageDialog(this, "启动成功!");
            addLog("Server started successfully on port "+port);
        }catch (Exception exc){
            JOptionPane.showMessageDialog(this, exc.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initCenterPanel() {
        centerPanel =new JScrollPane(textArea);
        centerPanel.setBorder(new TitledBorder("System log"));
    }

    private void initElements() {
        txt_port=new JTextField("6666");
        btn_start = new JButton("启动");
        btn_stop = new JButton("停止");
        textArea=new JTextArea();
        textArea.setEditable(false);
    }

    private void initNorthPanel() {
        northPanel=new JPanel();
        northPanel.setLayout(new GridLayout(1, 4));
        northPanel.setBorder(new TitledBorder("连接信息"));
        northPanel.add(new JLabel("Port"));
        northPanel.add(txt_port);
        northPanel.add(btn_start);
        northPanel.add(btn_stop);

    }

    private void initFrame(){
        setLayout(new BorderLayout());
        add(northPanel, "North");
        add(centerPanel, "Center");
        setSize(300, 280);
        int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
        setLocation((screen_width - getWidth()) / 2,
                (screen_height - getHeight()) / 2);
        setVisible(true);
    }

    @Override
    public void run(){
        while (isStarted){
            Socket socket;
            try {
                socket=serverSocket.accept();
                // fixed: 16-5-26 fork a thread to process request
                executorService.execute(new RouterThread(this,socket));
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] argv){
        try {
            new RouterServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Host> getHostMap() {
        return hostMap;
    }

    public void addLog(String log){
        System.out.println(log);
        textArea.append(sdf.format(new Date()));
        textArea.append("> "+log+"\n");
    }

    private ExecutorService executorService;
    private ServerSocket serverSocket;
    private Map<String,Host> hostMap;

    private boolean isStarted;

    private JPanel northPanel;
    private JScrollPane centerPanel;

    private JTextField txt_port;
    private JButton btn_start;
    private JButton btn_stop;

    private JTextArea textArea;

    private SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
}
