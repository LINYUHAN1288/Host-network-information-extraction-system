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
	private static final long serialVersionUID = 1L;//�Զ����ɵ����л�����
	private static final int max = 8096;//���ץ����
	private static final String IPHost = "/10.242.50.166"; //IP��ַ
	AThread t = null;
	Packet[] packet = new Packet[max];
	JTabbedPane jTabbedPane = new JTabbedPane(JTabbedPane.TOP);

	JPanel ScanMorejp = new JPanel();//�����
	JButton save2;
	JButton start;
	JButton detail;
	JTextField IPjtf2 = new JTextField(11);
	JTextField threadnumjtf = new JTextField(5);//�߳���
	JTextField SPortjtf = new JTextField(5);//��ʼ�˿�
	JTextField EPortjtf = new JTextField(5);//�����˿�
	JTextField Timejtf = new JTextField(5);//��ʱ�ɼ����
	JTextArea resultjta2 =new JTextArea();
	JTextArea resultjta3 = new JTextArea();
	String[] result;
	//��ʱ�������ݼ�ʱ��
	Timer timer;
	
	//��ʱ�ɼ������ʱ��
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
		Label thread = new Label("�߳���");
		Label time = new Label("��ʱ�ɼ����");
		jp21.add(ip2);
		jp21.add(IPjtf2);
		jp21.add(thread);
		jp21.add(threadnumjtf);
		jp21.add(time);
		jp21.add(Timejtf);
		
		
		JPanel jp22 = new JPanel();
		Label sport = new Label("��ʼ�˿�");
		Label eport = new Label("�����˿�");
		jp22.add(sport);
		jp22.add(SPortjtf);
		jp22.add(eport);
		jp22.add(EPortjtf);
		
		JPanel jp23 = new JPanel();
		save2 = new JButton("����");
		start = new JButton("��ʼ");
		detail = new JButton("��ʾͨ����");
		jp23.add(start);
		jp23.add(save2);
		jp23.add(detail);
		
		inputjp2.setLayout(new GridLayout(3, 1));
		inputjp2.add(jp21);
		inputjp2.add(jp22);
		inputjp2.add(jp23);
		
		ScanMorejp.setLayout(new BorderLayout());//�߽�ʽ����
		ScanMorejp.add(inputjp2, BorderLayout.NORTH);
		ScanMorejp.add(Showjsp2,BorderLayout.CENTER);
		ScanMorejp.add(Showjsp3,BorderLayout.SOUTH);
	
		
		jTabbedPane.add("ɨ������Ϣ��ȡ",ScanMorejp);
		this.add(jTabbedPane);
		
		timer = new Timer();
        timer.schedule(new TimerTaskTest(), 1000, 1000*60*1);//1���ӱ���һ��
		timer1 = new Timer();
		timer2 = new Timer();
		this.Monitor();//���ü�����
	}
	
	//��ʱ����0
	public class TimerTaskTest extends TimerTask{
		//@Override
	    public void run() {
			SavePort(resultjta2, "d:\\1.txt");
	    }
	}
	
	//��ʱ����1
	public class TimerTaskTest1 extends TimerTask{
		//@Override
		public void run() {
			try {
				Start();
				timer2.schedule(new TimerTaskTest2(), 1000*10); 
				Scan2();
			} catch (IOException e) {
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			}
		}
	}
	
	//��ʱ����2
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
		this.setTitle("����������Ϣ��ȡϵͳ"); //���ñ���
	    this.setSize(700, 900); //���ô��ڴ�С
	    this.setResizable(true); 
	    this.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width-getSize().width)/2,
		         (Toolkit.getDefaultToolkit().getScreenSize().height-getSize().height)/2); 
	    //�ô�������ʱ��ʾ����Ļ����
	    this.setVisible(true);	//�ô��ڿ���
	}
	public void Monitor() {
		//����ťע�����
		save2.addActionListener(new Controller());
		start.addActionListener(new Controller());
		detail.addActionListener(new Controller());
	}
	public class Controller implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if((e.getSource())instanceof JButton)//�����������ǰ�ť
			{
				JButton button = (JButton)e.getSource(); //ǿ��ת��Ϊ��ť
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
	
	//��ʼץ��
	public void Start() throws IOException {
		NetworkInterface[] devices = JpcapCaptor.getDeviceList();
		JpcapCaptor captor = openDevice(devices, 3);
			
		t = new AThread(captor);
		Thread capThread = new Thread(t);
		capThread.start();
		resultjta2.append("��ʼ\n");
		//t.cancel();	
	}
	
	//����ץ��
	public void End() {
		t.cancel();
		resultjta2.append("����");
	}
	
	//��ʾͨ����
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
		//System.out.println("�������ݰ�����" + count);
		resultjta3.append("TCP���ݰ���:" + String.valueOf(count+count1));	
	}
	
	
	
	
	//���浽redis
	public void SavePort(JTextArea resultjta12,String SavePlace) {
		boolean flag = true;//�Ƿ񱣴�ɹ�
		try {
			FileWriter filterWriter = new FileWriter(SavePlace);//�½��ļ�
			Jedis jedis = new Jedis("localhost"); //redis
			BufferedWriter bufferedWriter = new BufferedWriter(filterWriter);//����IO��
			String[] strings = resultjta12.getText().split("\n");//��ȡ�ı������ݲ��Ұ����з��ид���ַ�������
			for(String string : strings)//����д�뻺����
			{
				jedis.lpush("list", string); //д��redis
				bufferedWriter.write(string);
				bufferedWriter.newLine();
			}
			bufferedWriter.flush();//ˢ���ļ���
			bufferedWriter.close();//�ر�IO��
			jedis.close(); //�ر�redis
		} catch (IOException e) {
			flag = false;//��׽���쳣�򱣴�ʧ��
			JOptionPane.showMessageDialog(null, "����ʧ�ܣ�");
		}
		if (flag) {//δ��׽���쳣����ʾ����ɹ�
			JOptionPane.showMessageDialog(null, "����ɹ����ѱ�����redis");
		}
	}
	
	//�˿�ɨ������Ϣ��ȡ
	public void Scan2() throws IOException {
		//Start();
		packet = t.getPacket();
		boolean flag = true;//��ʽ�����־
		int StartPort = 0;
		int EndPort = 0;
		int ThreadNum = 0;
		ArrayList<Integer> result = new ArrayList<>();
		String IPAddress = IPjtf2.getText().trim();//���ı����ȡIP��ַ
		try {
			StartPort = Integer.parseInt(SPortjtf.getText().trim());
			EndPort = Integer.parseInt(EPortjtf.getText().trim());
			ThreadNum = Integer.parseInt(threadnumjtf.getText().trim());
		} catch (NumberFormatException e) {
			flag = false;
			JOptionPane.showMessageDialog(null, "��ʽ�������������룡");
		}
		if (StartPort<1 || EndPort>65535) {
			JOptionPane.showMessageDialog(null, "�˿ڷ�Χ��1~65535");
		}else if (StartPort>=EndPort) {
			JOptionPane.showMessageDialog(null, "�˿ںŴ�С��������");
		}else if (flag) {
			
			ThreadOfScan threadOfScan = new ThreadOfScan(IPAddress, StartPort, EndPort,ThreadNum);
			threadOfScan.AllScanner();//ʵ����һ��threadofscan����������AllScanner��������
			for (String string : threadOfScan.getResult()) {
				resultjta2.append(string);//����ͨ��getResult����������õĽ���б���������ı���
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
	                		resultjta3.append("�˿�" + String.valueOf(tcp.src_port) + "��" + tcp.dst_ip.toString() + "����" + "\n");
	                	}
	                }
				}
			}
		}
		
	}
	
	
	//ץ���߳�
	private static class AThread implements Runnable{
        JpcapCaptor captor;
        Packet[] packet;
        //�߳��жϱ�־
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
	
	//��ȡ��������
	private static JpcapCaptor openDevice(NetworkInterface[] devices, int choice) throws java.io.IOException{
		JpcapCaptor captor = null;
		try{
	           captor = JpcapCaptor.openDevice(devices[choice], 65535, false, 3000);

	        } catch (IOException e) {
	            e.printStackTrace();
	            System.out.println("������ӿ�ʧ�ܣ�");

	    }
	    return captor;
	}
}

