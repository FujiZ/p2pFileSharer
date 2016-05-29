package test;

/**
 * Created by fuji on 16-5-29.
 */

import host.HostEnv;
import host.server.P2PServer;
import utils.Host;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

public class Client{

    private JFrame frame;
    private JList userList;
    private JTextArea textArea; //can be used as log window
    private JTable fileTable;
    private JTextField dirField;

    private JTextField txt_serverIp;
    private JTextField txt_serverPort;

    private JTextField txt_hostPort;
    private JTextField txt_name;

    private JButton btn_start;
    private JButton btn_stop;
    private JButton btn_get;
    private JPanel northPanel;
    private JPanel southPanel;
    private JScrollPane rightScroll;
    private JScrollPane leftScroll;
    private JSplitPane centerSplit;

    private DefaultListModel listModel;
    private boolean isConnected = false;

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private MessageThread messageThread;// 负责接收消息的线程
    private Map<String, User> onLineUsers = new HashMap<>();// 所有在线用户

    private HostEnv hostEnv;
    private P2PServer server;

    // 主方法,程序入口
    public static void main(String[] args) {
        new Client();
    }

    // 执行发送
    public void send() {
        if (!isConnected) {
            JOptionPane.showMessageDialog(frame, "还没有连接服务器，无法发送消息！", "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        String message = dirField.getText().trim();
        if (message.equals("")) {
            JOptionPane.showMessageDialog(frame, "消息不能为空！", "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        sendMessage(frame.getTitle() + "@" + "ALL" + "@" + message);
        dirField.setText(null);
    }

    // 构造方法
    public Client() {

        initElements();

        //显示消息
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setForeground(Color.blue);

        initFileTable();
        initNorthPanel();

        rightScroll = new JScrollPane(fileTable);
        rightScroll.setBorder(new TitledBorder("共享文件"));
        leftScroll = new JScrollPane(userList);
        leftScroll.setBorder(new TitledBorder("在线用户"));

        initSouthPanel();

        centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, rightScroll);
        centerSplit.setDividerLocation(140);

        initFrame();
        initActionListener();
    }

    private void initSouthPanel() {
        southPanel = new JPanel(new BorderLayout());
        southPanel.add(dirField, "Center");
        southPanel.add(btn_get, "East");
        southPanel.setBorder(new TitledBorder("共享目录"));
    }

    private void initActionListener() {
        // 写消息的文本框中按回车键时事件
        dirField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                send();
            }
        });

        // 单击发送按钮时事件
        btn_get.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                send();
            }
        });

        // 单击连接按钮时事件
        btn_start.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int serverPort;
                int hostPort;
                if (isConnected) {
                    JOptionPane.showMessageDialog(frame, "已处于连接状态，不要重复连接!",
                            "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    try {
                        serverPort = Integer.parseInt(txt_serverPort.getText().trim());
                        hostPort = Integer.parseInt(txt_hostPort.getText().trim());
                    } catch (NumberFormatException e2) {
                        throw new Exception("端口号应为整数!");
                    }
                    String serverIp = txt_serverIp.getText().trim();
                    String name = txt_name.getText().trim();
                    String dirStr= dirField.getText().trim();
                    if (name.equals("") || serverIp.equals("")){
                        throw new Exception("Name,IP can't be empty!");
                    }
                    if(dirStr.equals("")){
                        throw new Exception("路径不能为空!");
                    }
                    //读取文件夹
                    File dir=new File(dirStr);
                    if(!dir.isDirectory()){
                        throw new Exception("路径必须是目录!");
                    }
                    hostEnv=new HostEnv(name,dir,new Host(serverIp,serverPort));
                    server=new P2PServer(hostEnv,hostPort);
                    if(!server.sendHello()){
                        throw new Exception("与服务器连接失败!");
                    }
                    isConnected=true;

                    frame.setTitle(name);
                    JOptionPane.showMessageDialog(frame, "成功连接!");
                    //todo 将textField设置为不可编辑

                    //初始化用户列表
                    initUserList();

                } catch (Exception exc) {
                    JOptionPane.showMessageDialog(frame, exc.getMessage(),
                            "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 单击断开按钮时事件
        btn_stop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!isConnected) {
                    JOptionPane.showMessageDialog(frame, "已处于断开状态，不要重复断开!",
                            "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    boolean flag = closeConnection();// 断开连接
                    if (flag == false) {
                        throw new Exception("断开连接发生异常！");
                    }
                    JOptionPane.showMessageDialog(frame, "成功断开!");
                } catch (Exception exc) {
                    JOptionPane.showMessageDialog(frame, exc.getMessage(),
                            "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 关闭窗口时事件
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (isConnected) {
                    closeConnection();// 关闭连接
                }
                System.exit(0);// 退出程序
            }
        });

    }

    private void initUserList(){
        listModel.clear();
        synchronized (hostEnv.getHostMap()){
            for(Map.Entry<String,Host> hostEntry:hostEnv.getHostMap().entrySet()){
                listModel.addElement(Host.formatHost(hostEntry.getKey(),hostEntry.getValue()));
            }
        }
    }

    private void initFrame() {
        frame = new JFrame("Client");
        // 更改JFrame的图标：
        //frame.setIconImage(Toolkit.getDefaultToolkit().createImage(Client.class.getResource("qq.png")));
        frame.setLayout(new BorderLayout());
        frame.add(northPanel, "North");
        frame.add(centerSplit, "Center");
        frame.add(southPanel, "South");
        frame.setSize(650, 450);
        int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
        frame.setLocation((screen_width - frame.getWidth()) / 2,
                (screen_height - frame.getHeight()) / 2);
        frame.setVisible(true);
    }

    private void initElements() {
        dirField = new JTextField();
        txt_serverIp = new JTextField("127.0.0.1");
        txt_serverPort = new JTextField("6666");
        txt_hostPort = new JTextField("10240");
        txt_name = new JTextField("xiaoqiang");
        btn_start = new JButton("连接");
        btn_stop = new JButton("断开");
        btn_get = new JButton("下载");
        listModel = new DefaultListModel();
        userList = new JList(listModel);
    }

    private void initFileTable() {
        String[] headers={"文件名","文件大小"};
        Object[][] cellData=null;
        DefaultTableModel model=new DefaultTableModel(cellData,headers){
            public boolean isCellEditable(int row,int column){
                return false;
            }
        };
        fileTable=new JTable(model);
        fileTable.getTableHeader().setReorderingAllowed(false);
    }

    private void initNorthPanel(){
        northPanel = new JPanel();
        northPanel.setLayout(new GridLayout(1, 9));
        northPanel.add(new JLabel("ServerIP"));
        northPanel.add(txt_serverIp);
        northPanel.add(new JLabel("ServerPort"));
        northPanel.add(txt_serverPort);
        northPanel.add(new JLabel("HostPort"));
        northPanel.add(txt_hostPort);
        northPanel.add(new JLabel("Name"));
        northPanel.add(txt_name);
        northPanel.add(btn_start);
        northPanel.add(btn_stop);
        northPanel.setBorder(new TitledBorder("连接信息"));
    }

    /**
     * 连接服务器
     *
     * @param serverPort
     * @param serverIp
     * @param name
     */
    private boolean connectServer(String serverIp,int serverPort, String name) {
        // 连接服务器
        try {
            socket = new Socket(serverIp, serverPort);// 根据端口号和服务器ip建立连接
            writer = new PrintWriter(socket.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(socket
                    .getInputStream()));
            // 发送客户端用户基本信息(用户名和ip地址)
            sendMessage(name + "@" + socket.getLocalAddress().toString());
            // 开启接收消息的线程
            messageThread = new MessageThread(reader, textArea);
            messageThread.start();
            isConnected = true;// 已经连接上了
            return true;
        } catch (Exception e) {
            textArea.append("与端口号为：" + serverPort + "    IP地址为：" + serverIp
                    + "   的服务器连接失败!" + "\r\n");
            isConnected = false;// 未连接上
            return false;
        }
    }

    /**
     * 发送消息
     *
     * @param message
     */
    public void sendMessage(String message) {
        writer.println(message);
        writer.flush();
    }

    /**
     * 客户端主动关闭连接
     */
    @SuppressWarnings("deprecation")
    public synchronized boolean closeConnection() {
        try {
            sendMessage("CLOSE");// 发送断开连接命令给服务器
            messageThread.stop();// 停止接受消息线程
            // 释放资源
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (socket != null) {
                socket.close();
            }
            isConnected = false;
            return true;
        } catch (IOException e1) {
            e1.printStackTrace();
            isConnected = true;
            return false;
        }
    }

    // 不断接收消息的线程
    class MessageThread extends Thread {
        private BufferedReader reader;
        private JTextArea textArea;

        // 接收消息线程的构造方法
        public MessageThread(BufferedReader reader, JTextArea textArea) {
            this.reader = reader;
            this.textArea = textArea;
        }

        // 被动的关闭连接
        public synchronized void closeCon() throws Exception {
            // 清空用户列表
            listModel.removeAllElements();
            // 被动的关闭连接释放资源
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (socket != null) {
                socket.close();
            }
            isConnected = false;// 修改状态为断开
        }

        public void run() {
            String message = "";
            while (true) {
                try {
                    message = reader.readLine();
                    StringTokenizer stringTokenizer = new StringTokenizer(
                            message, "/@");
                    String command = stringTokenizer.nextToken();// 命令
                    if (command.equals("CLOSE"))// 服务器已关闭命令
                    {
                        textArea.append("服务器已关闭!\r\n");
                        closeCon();// 被动的关闭连接
                        return;// 结束线程
                    } else if (command.equals("ADD")) {// 有用户上线更新在线列表
                        String username = "";
                        String userIp = "";
                        if ((username = stringTokenizer.nextToken()) != null
                                && (userIp = stringTokenizer.nextToken()) != null) {
                            User user = new User(username, userIp);
                            onLineUsers.put(username, user);
                            listModel.addElement(username);
                        }
                    } else if (command.equals("DELETE")) {// 有用户下线更新在线列表
                        String username = stringTokenizer.nextToken();
                        User user = (User) onLineUsers.get(username);
                        onLineUsers.remove(user);
                        listModel.removeElement(username);
                    } else if (command.equals("USERLIST")) {// 加载在线用户列表
                        int size = Integer
                                .parseInt(stringTokenizer.nextToken());
                        String username = null;
                        String userIp = null;
                        for (int i = 0; i < size; i++) {
                            username = stringTokenizer.nextToken();
                            userIp = stringTokenizer.nextToken();
                            User user = new User(username, userIp);
                            onLineUsers.put(username, user);
                            listModel.addElement(username);
                        }
                    } else if (command.equals("MAX")) {// 人数已达上限
                        textArea.append(stringTokenizer.nextToken()
                                + stringTokenizer.nextToken() + "\r\n");
                        closeCon();// 被动的关闭连接
                        JOptionPane.showMessageDialog(frame, "服务器缓冲区已满！", "错误",
                                JOptionPane.ERROR_MESSAGE);
                        return;// 结束线程
                    } else {// 普通消息
                        textArea.append(message + "\r\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}