package host.client;

/**
 * Created by fuji on 16-5-29.
 */

import host.HostEnv;
import host.server.P2PServer;
import utils.Host;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

public class P2PClient {

    public P2PClient() {
        initElements();
        initFileTable();

        initNorthPanel();
        initCenterPanel();
        initSouthPanel();

        centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, rightScroll);
        centerSplit.setDividerLocation(140);

        initFrame();
        initActionListener();
        setConnected(false);
    }

    private void initElements() {
        dirField = new JTextField("/home/fuji/tmp/client");
        txt_serverIp = new JTextField("127.0.0.1");
        txt_serverPort = new JTextField("6666");
        txt_hostPort = new JTextField("10240");
        txt_name = new JTextField("zhp");
        btn_start = new JButton("连接");
        btn_stop = new JButton("断开");
        btn_stop.setEnabled(false);
        btn_get = new JButton("下载");
        btn_get.setEnabled(false);
        listModel = new DefaultListModel<>();
        userList = new JList<>(listModel);
    }

    private void initFileTable() {
        String[] headers={"文件名","文件大小"};
        tableModel=new DefaultTableModel(null,headers){
            public boolean isCellEditable(int row,int column){
                return false;
            }
        };
        fileTable=new JTable(tableModel);
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

    private void initCenterPanel() {
        rightScroll = new JScrollPane(fileTable);
        rightScroll.setBorder(new TitledBorder("共享文件"));
        leftScroll = new JScrollPane(userList);
        leftScroll.setBorder(new TitledBorder("在线用户"));
    }

    private void initSouthPanel() {
        southPanel = new JPanel(new BorderLayout());
        southPanel.add(dirField, "Center");
        southPanel.add(btn_get, "East");
        southPanel.setBorder(new TitledBorder("共享目录"));
    }

    private void initFrame() {
        frame = new JFrame("P2PClient");
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

    private void initActionListener() {

        // 单击下载按钮时事件
        btn_get.addActionListener(e -> getFile());

        // 单击连接按钮时事件
        btn_start.addActionListener(e -> connectToRouter());

        // 单击断开按钮时事件
        btn_stop.addActionListener(e -> disconectFromRouter());

        // 选择用户时事件
        userList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                if(!event.getValueIsAdjusting()){
                    //向对应主机发送list请求
                    Host targetHost=userList.getSelectedValue();
                    if(targetHost==null)
                        return;
                    try {
                        executorService.execute(new ClientThread(hostEnv,"LIST",targetHost));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        // 关闭窗口时事件
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (isConnected) {
                    server.sendBye();
                }
                System.exit(0);// 退出程序
            }
        });

    }

    // 执行下载
    public void getFile() {
        if (!isConnected) {
            JOptionPane.showMessageDialog(frame, "未连接服务器", "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        Host targetHost=userList.getSelectedValue();
        if(targetHost==null){
            JOptionPane.showMessageDialog(frame, "未选择主机", "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        int index=fileTable.getSelectedRow();
        if(index<0){
            JOptionPane.showMessageDialog(frame, "未选择文件", "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        String filename=(String)tableModel.getValueAt(index,0);

        try {
            executorService.execute(new ClientThread(hostEnv,"GET "+filename,targetHost));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connectToRouter(){
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
            hostEnv=new HostEnv(name,dir,new Host("server",serverIp,serverPort));
            server=new P2PServer(hostEnv,hostPort);
            hostEnv.setServer(server);
            hostEnv.setClient(this);
            if(!server.sendHello()){
                throw new Exception("与服务器连接失败!");
            }
            //启动server线程
            executorService.execute(server);

            frame.setTitle(name);
            JOptionPane.showMessageDialog(frame, "成功连接!");
            //将textField设置为不可编辑
            setConnected(true);

            //初始化用户列表
            initUserList();

        } catch (Exception exc) {
            JOptionPane.showMessageDialog(frame, exc.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
            if(server!=null)
                server.close();
        }
    }

    private void setTextFieldEditable(boolean flag){
        txt_hostPort.setEditable(flag);
        txt_name.setEditable(flag);
        txt_serverIp.setEditable(flag);
        txt_serverPort.setEditable(flag);
        dirField.setEditable(flag);
    }

    private void setConnected(boolean flag){
        isConnected=flag;
        setTextFieldEditable(!flag);
        btn_get.setEnabled(flag);
        btn_start.setEnabled(!flag);
        btn_stop.setEnabled(flag);
    }

    private void disconectFromRouter(){
        if (!isConnected) {
            JOptionPane.showMessageDialog(frame, "已处于断开状态，不要重复断开!",
                    "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            server.sendBye();
            JOptionPane.showMessageDialog(frame, "成功断开!");
            setConnected(false);
            listModel.clear();
            tableModel.setRowCount(0);
            frame.setTitle("P2PClient");
        } catch (Exception exc) {
            exc.printStackTrace();
            JOptionPane.showMessageDialog(frame, exc.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void showMessage(String msg,String type){
        JOptionPane.showMessageDialog(frame,msg,type,JOptionPane.INFORMATION_MESSAGE);
    }

    public void clearFileList(){
        tableModel.setRowCount(0);
    }

    public void addFile(String name,String size){
        String[] row={name,size};
        tableModel.addRow(row);
    }

    private void initUserList(){
        listModel.clear();
        synchronized (hostEnv.getHostMap()){
            for(Map.Entry<String,Host> hostEntry:hostEnv.getHostMap().entrySet()){
                listModel.addElement(hostEntry.getValue());
            }
        }
    }

    public void addHost(Host host){
        listModel.addElement(host);
    }

    public void delHost(Host host){
        listModel.removeElement(host);
    }

    // 主方法,程序入口
    public static void main(String[] args) {
        new P2PClient();
    }

    private HostEnv hostEnv;
    private P2PServer server;
    private ExecutorService executorService=Executors.newCachedThreadPool();

    private boolean isConnected = false;

    //GUI
    private JFrame frame;
    private JList<Host> userList;
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

    private DefaultListModel<Host> listModel;
    private DefaultTableModel tableModel;
}