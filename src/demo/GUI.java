package demo;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;

//import java.text.SimpleDateFormat;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import jpcap.*;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;
import redis.clients.jedis.*;

import java.util.*;
import java.util.Timer;
import java.util.TimerTask;



public class GUI extends JFrame
{
	private static final long serialVersionUID = 1L;//自动生成的序列化机制
	private static final int max = 8096;//最大抓包数
	private static final String IPHost = "/10.242.50.166"; //IP地址
	AThread t = null;
	Packet[] packet = new Packet[max];
	JTabbedPane jTabbedPane = new JTabbedPane(JTabbedPane.TOP);

	JPanel ScanMorejp = new JPanel();//主面板
	JButton save2;
	JButton start;
	JButton detail;
	JTextField IPjtf2 = new JTextField(11);
	JTextField threadnumjtf = new JTextField(5);//线程数
	JTextField SPortjtf = new JTextField(5);//开始端口
	JTextField EPortjtf = new JTextField(5);//结束端口
	JTextField Timejtf = new JTextField(5);//定时采集间隔
	JTextArea resultjta2 =new JTextArea();
	JTextArea resultjta3 = new JTextArea();
	String[] result;
	//定时保存数据计时器
	Timer timer;
	
	//定时采集间隔计时器
	Timer timer1;
	
	Timer timer2;
	
	public GUI() 
	{  

		JPanel inputjp2 = new JPanel();
		resultjta2.setEditable(false);
		resultjta2.setLineWrap(true);
		resultjta3.setEditable(false);
		resultjta3.setLineWrap(true);
		JScrollPane Showjsp2 = new JScrollPane(resultjta2);
		JScrollPane Showjsp3 = new JScrollPane(resultjta3);
		Showjsp3.setPreferredSize(new Dimension(300, 200));
		
		
		JPanel jp21 = new JPanel();
		Label ip2 = new Label("IP");
		Label thread = new Label("线程数");
		Label time = new Label("定时采集间隔");
		jp21.add(ip2);
		jp21.add(IPjtf2);
		jp21.add(thread);
		jp21.add(threadnumjtf);
		jp21.add(time);
		jp21.add(Timejtf);
		
		
		JPanel jp22 = new JPanel();
		Label sport = new Label("开始端口");
		Label eport = new Label("结束端口");
		jp22.add(sport);
		jp22.add(SPortjtf);
		jp22.add(eport);
		jp22.add(EPortjtf);
		
		JPanel jp23 = new JPanel();
		save2 = new JButton("保存");
		start = new JButton("开始");
		detail = new JButton("显示通信量");
		jp23.add(start);
		jp23.add(save2);
		jp23.add(detail);
		
		inputjp2.setLayout(new GridLayout(3, 1));
		inputjp2.add(jp21);
		inputjp2.add(jp22);
		inputjp2.add(jp23);
		
		ScanMorejp.setLayout(new BorderLayout());//边界式布局
		ScanMorejp.add(inputjp2, BorderLayout.NORTH);
		ScanMorejp.add(Showjsp2,BorderLayout.CENTER);
		ScanMorejp.add(Showjsp3,BorderLayout.SOUTH);
	
		
		jTabbedPane.add("扫描与信息提取",ScanMorejp);
		this.add(jTabbedPane);
		
		timer = new Timer();
        timer.schedule(new TimerTaskTest(), 1000, 1000*60*1);//1分钟保存一次
		timer1 = new Timer();
		timer2 = new Timer();
		this.Monitor();//放置监听器
	}
	
	//定时任务0
	public class TimerTaskTest extends TimerTask{
		//@Override
	    public void run() {
			SavePort(resultjta2, "d:\\1.txt");
	    }
	}
	
	//定时任务1
	public class TimerTaskTest1 extends TimerTask{
		//@Override
		public void run() {
			try {
				Start();
				timer2.schedule(new TimerTaskTest2(), 1000*10); 
				Scan2();
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}
	}
	
	//定时任务2
	public class TimerTaskTest2 extends TimerTask{
		//@Override
	    public void run() {
			End();
	    }
	}
	
	
	public void LaunchFrame() {
		IPjtf2.setText("10.242.50.166");
		threadnumjtf.setText("500");
		SPortjtf.setText("1");
		EPortjtf.setText("65535");
		Timejtf.setText("1");
		this.setTitle("主机网络信息提取系统"); //设置标题
	    this.setSize(700, 900); //设置窗口大小
	    this.setResizable(true); 
	    this.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width-getSize().width)/2,
		         (Toolkit.getDefaultToolkit().getScreenSize().height-getSize().height)/2); 
	    //让窗口启动时显示在屏幕中央
	    this.setVisible(true);	//让窗口可视
	}
	public void Monitor() {
		//给按钮注册监听
		save2.addActionListener(new Controller());
		start.addActionListener(new Controller());
		detail.addActionListener(new Controller());
	}
	public class Controller implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if((e.getSource())instanceof JButton)//如果这个操作是按钮
			{
				JButton button = (JButton)e.getSource(); //强制转换为按钮
				if (button == save2) {
					SavePort(resultjta2, "d:\\1.txt");
				}else if (button == start) {
						//Start();
						timer1.schedule(new TimerTaskTest1(), 0, 1000*60*Integer.parseInt(Timejtf.getText().trim()));
				}else if (button == detail) {
					AnalyzePacket(packet);
				}
			}
		}	
	}
	
	//开始抓包
	public void Start() throws IOException {
		NetworkInterface[] devices = JpcapCaptor.getDeviceList();
		JpcapCaptor captor = openDevice(devices, 3);
			
		t = new AThread(captor);
		Thread capThread = new Thread(t);
		capThread.start();
		resultjta2.append("开始\n");
		//t.cancel();	
	}
	
	//结束抓包
	public void End() {
		t.cancel();
		resultjta2.append("结束");
	}
	
	//显示通信量
	public void AnalyzePacket(Packet[] packet) {
		int count = 0;
		int count1 = 0;
		ArrayList<TCPPacket> tcpPacketArray = new ArrayList<TCPPacket>();
		Map<String, Integer> tcp_ip = new HashMap<String, Integer>();
		for(int i = 0; packet[i] != null && i < max; i++) {
			if(packet[i] instanceof TCPPacket){
                TCPPacket tcp = (TCPPacket) packet[i];
                tcpPacketArray.add(tcp);
                if (String.valueOf(tcp.src_ip).equals(IPHost)) {
                	count++;
                	if(tcp_ip.containsKey(String.valueOf(tcp.dst_ip)))
                	{
                		int value = tcp_ip.get(String.valueOf(tcp.dst_ip));
                		value++;
                		tcp_ip.put(String.valueOf(tcp.dst_ip), value);
                	} else {
                		tcp_ip.put(String.valueOf(tcp.dst_ip), 1);
                	}
                		
                }
                if (String.valueOf(tcp.dst_ip).equals(IPHost)) {
                	count1++;
                	if(tcp_ip.containsKey(String.valueOf(tcp.src_ip)))
                	{
                		int value = tcp_ip.get(String.valueOf(tcp.src_ip));
                		value++;
                		tcp_ip.put(String.valueOf(tcp.src_ip), value);
                	} else {
                		tcp_ip.put(String.valueOf(tcp.src_ip), 1);
                	}
                }	            
			}
		}
		for (Entry<String, Integer> entry : tcp_ip.entrySet()) {
		    System.out.println(entry.getKey() + ":" + entry.getValue());
		    resultjta3.append(entry.getKey() + ":" + entry.getValue() + "\n");
		}
		//System.out.println("所有数据包数：" + count);
		resultjta3.append("TCP数据包数:" + String.valueOf(count+count1));	
	}
	
	
	
	
	//保存到redis
	public void SavePort(JTextArea resultjta12,String SavePlace) {
		boolean flag = true;//是否保存成功
		try {
			FileWriter filterWriter = new FileWriter(SavePlace);//新建文件
			Jedis jedis = new Jedis("localhost"); //redis
			BufferedWriter bufferedWriter = new BufferedWriter(filterWriter);//缓冲IO流
			String[] strings = resultjta12.getText().split("\n");//获取文本区内容并且按换行符切割，写入字符数组中
			for(String string : strings)//逐行写入缓冲区
			{
				jedis.lpush("list", string); //写入redis
				bufferedWriter.write(string);
				bufferedWriter.newLine();
			}
			bufferedWriter.flush();//刷入文件中
			bufferedWriter.close();//关闭IO流
			jedis.close(); //关闭redis
		} catch (IOException e) {
			flag = false;//捕捉到异常则保存失败
			JOptionPane.showMessageDialog(null, "保存失败！");
		}
		if (flag) {//未捕捉到异常则提示保存成功
			JOptionPane.showMessageDialog(null, "保存成功，已保存至redis");
		}
	}
	
	//端口扫描与信息提取
	public void Scan2() throws IOException {
		//Start();
		packet = t.getPacket();
		boolean flag = true;//格式错误标志
		int StartPort = 0;
		int EndPort = 0;
		int ThreadNum = 0;
		ArrayList<Integer> result = new ArrayList<>();
		String IPAddress = IPjtf2.getText().trim();//从文本框获取IP地址
		try {
			StartPort = Integer.parseInt(SPortjtf.getText().trim());
			EndPort = Integer.parseInt(EPortjtf.getText().trim());
			ThreadNum = Integer.parseInt(threadnumjtf.getText().trim());
		} catch (NumberFormatException e) {
			flag = false;
			JOptionPane.showMessageDialog(null, "格式错误请重新输入！");
		}
		if (StartPort<1 || EndPort>65535) {
			JOptionPane.showMessageDialog(null, "端口范围：1~65535");
		}else if (StartPort>=EndPort) {
			JOptionPane.showMessageDialog(null, "端口号从小至大输入");
		}else if (flag) {
			
			ThreadOfScan threadOfScan = new ThreadOfScan(IPAddress, StartPort, EndPort,ThreadNum);
			threadOfScan.AllScanner();//实例化一个threadofscan并调用它的AllScanner（）方法
			for (String string : threadOfScan.getResult()) {
				resultjta2.append(string);//遍历通过getResult（）方法获得的结果列表并将其加入文本区
			}
			for (int i = 0; packet[i] != null && i < max; i++) {
				if (packet[i] instanceof TCPPacket) {
					TCPPacket tcp = (TCPPacket) packet[i];
	                //String data = new String(tcp.data);
					result = threadOfScan.getResult1();
					String ip = String.valueOf(tcp.src_ip);
					System.out.println(ip);
	                for (int j = 0; j < threadOfScan.getResult1().size(); j++) {
	                	if (tcp.src_port == result.get(j) || (String.valueOf(tcp.src_ip).equals(IPHost))) {
	                		resultjta3.append("端口" + String.valueOf(tcp.src_port) + "与" + tcp.dst_ip.toString() + "连接" + "\n");
	                	}
	                }
				}
			}
		}
		
	}
	
	
	//抓包线程
	private static class AThread implements Runnable{
        JpcapCaptor captor;
        Packet[] packet;
        //线程中断标志
        volatile boolean cancel;

        AThread(JpcapCaptor captor) throws IOException{
            this.captor = captor;
            this.packet = new Packet[max];
            this.cancel = false;
            new Thread(this);
        }


		@Override
        public void run() {
            packet = new Packet[max];
            for(int i = 0; i < max && cancel == false; i++){
                packet[i] = captor.getPacket();
            }
        }

        public void cancel(){
            cancel = true;
        }

        public Packet[] getPacket(){
            return packet;
        }

    }
	
	//获取并打开网卡
	private static JpcapCaptor openDevice(NetworkInterface[] devices, int choice) throws java.io.IOException{
		JpcapCaptor captor = null;
		try{
	           captor = JpcapCaptor.openDevice(devices[choice], 65535, false, 3000);

	        } catch (IOException e) {
	            e.printStackTrace();
	            System.out.println("打开网络接口失败！");

	    }
	    return captor;
	}
}

