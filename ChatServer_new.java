import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
public class ChatServer_new {
		public static void main(String[] args) {
			try{
				ServerSocket server = new ServerSocket(10001);// 10001�� ��Ʈ�� �ϴ� server socket ����
				
				System.out.println("Waiting connection...");
				HashMap hm = new HashMap(); // ������ ���� ���� ��������. hashmap key ���̵�, value�� ��� ��Ʈ��
				while(true){ //Ŭ���̾�Ʈ ���� �׻� ���� �� �ֵ��� ���ѷ���
					Socket sock = server.accept(); //�� ���������� ������ ����ϸ� ������  �Ʒ��� ������ ����
					ChatThread chatthread = new ChatThread(sock, hm); // ������ �����ϸ鼭 ������ �ۼ����� ���� ������ ��ü ����
					chatthread.start(); //������ ���� run()�޼ҵ� ����
				} // while
			}catch(Exception e){ //���� ���� �� ���� ���� �� ��� ���� �޼���
				System.out.println(e);
			}
		} // main
	

	static class ChatThread extends Thread{
		private Socket sock; //Ŭ���̾�Ʈ�� ����ϱ� ���� ����
		private String id; // ������ ���̵� ����
		private BufferedReader br;
		private HashMap hm; // 
		private boolean initFlag = false;
		boolean banFlag = true;
		String ban[] = {"fuck","sibal","dogbaby","michin","ssyang"};
		ArrayList spamlist = new ArrayList();
		public ChatThread(Socket sock, HashMap hm){
			this.sock = sock; //����� ���� ������
			this.hm = hm; //����� ���� ������
			try{// ������ ������, ���� ��� �� printwriter�� bufferedreader �����, ������ ������  ���̵�� ��½�Ʈ��(printwriter)�� hashmap�� �־�
				// ������ ���� ���� ����
				PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream())); // Ŭ���̾�Ʈ�鿡�� ���� �޽����� ���� ����
				br = new BufferedReader(new InputStreamReader(sock.getInputStream())); // Ŭ���̾�Ʈ�� �Է��� ���� �б� ���� ����
				id = br.readLine(); //id �Է�
				broadcast("["+CurrentTime()+"]"+id + " entered."); // �ٸ� ���� ��ο��� broadcast�� �޽��� ������.
				System.out.println("[Server] User (" + id + ") entered.");
				synchronized(hm){ // �������� �����尡 �Ѱ��� �ڿ��� ����ϰ��� �� �� ��Ƽ�������� ���������� ����ڰ��� ����ȭ�� �����ش�.
					//���� �����͸� ����ϰ� �ִ� �ش� �����带 �����ϰ� ������ ��������� ������ ������ ����
					//���ϵ��� �Ѵ�.
					hm.put(this.id, pw);//������ ���̵�� printwriter�� �־��ش�. �ش� ������ ���� ������ printwriter�� �����ؼ�
					// ������ Ŭ���̾�Ʈ���� ����ϵ��� �ִ� ���̴�. �׷��� constructor�� ������ ���ش�.
				}
				initFlag = true;
			}catch(Exception ex){
				System.out.println(ex);
				}
			} // construcor
		
		public void run(){// Ŭ���̾�Ʈ�κ��� ���� ������ Ŭ�����Ϳ��� �۽���
			try{ 
				File();
				String line = null; //���� ���� ������ ���� ����
				while((line = br.readLine()) != null){//Ŭ���̾�Ʈ�� �Է��� �����о�´�.
					banFlag = true;
					for(int i =0;i<spamlist.size();i++) {
						if(line.equals(spamlist.get(i))){//"/quit"�Է½� break
							send_error(line);
							banFlag = false;	
						}
					}
					if(banFlag){
					if(line.equals("/quit")) //"/quit"�Է½� break
						break;
					else if(line.indexOf("/to") == 0){ 
						sendmsg(line); // "/to" �Է½� �ӼӸ���� ���
					}else if(line.equals("/userlist")){ 
						send_userlist(); // 
					}else if(line.equals("/spamlist")){ 
						spam_list() ; // 
					}
					else if(line.contains("/addspam")) {
					    String[] content = line.split(" ");
						if(spamlist.size()==0) {
							spamlist.add(content[1]);
						}
						else{
							add_spam(content[1]);
							file_store();
						}
					}
					else
						broadcast("["+CurrentTime()+"]"+id + " : " + line); // �׷��� ���� ��� ����� ���ο��� ��ȭ������ ������.
					}
					}
				}catch(Exception ex){ // ����� �Է� �� �о�ð�� ����
				System.out.println(ex);
			}finally{ // quit �Է½� 
				synchronized(hm){ // ���� �����尡 �����ϴ� �ؽ����� ����ȭ ��Ų��.
					hm.remove(id); // ���� ���, hashmap���� id ����.
					//����
				}
				broadcast("["+CurrentTime()+"]"+id + " exited."); // ������ Ŭ���̾�Ʈ���� �������� �˸���.
				try{
					if(sock != null)
						sock.close();
				}catch(Exception ex){}
			}
		} // run
		
		public void spam_list(){ //������ ��� ���
			Object obj = hm.get(id);
			PrintWriter pw = (PrintWriter)obj;
			pw.println("spamlist");
			pw.flush();
			for(int i =0; i<spamlist.size();i++) {
				pw.println(spamlist.get(i)); //�޼��� ������
				pw.flush(); // ���� ����
				}
			}
		
		public void add_spam(String line){ //������ �߰�
		     Object obj = hm.get(id);
			PrintWriter pw = (PrintWriter)obj;
				for(int i =0; i<spamlist.size();i++) {
					if(line.equals(spamlist.get(i))) {
						pw.println(line+" is already existed"); //�޼��� ������
						pw.flush(); // ���� ����
						return;
					}
				}
				spamlist.add(line);
				pw.println(line+"is new word");
				pw.flush();
		}
		
		public void File(){ //���� ������ �ܾ� �ҷ�����, ������ ���� ������ �����
			 String file = "spamlist.txt";
			 File f = new File(file);
			 if (f.isFile()) {
				 Scanner inputStream = null;
				 try {
					 inputStream = new Scanner(f);
				 }
				 catch(FileNotFoundException e) {
					 System.out.println("Error opening the file " + file); 
					 System.exit(0);
				 }
				 while (inputStream.hasNextLine()){ 
					String line = inputStream.nextLine();
					spamlist.add(line); 
					} 
			    }
			 else{
					PrintWriter outputStream = null; 
					 try{outputStream = new PrintWriter(file);} 
					 catch(FileNotFoundException e){ 
						 System.out.println("Error opening the file" + file); 
						 System.exit(0); 
						 }   
						 outputStream.println(""); //������ �����
				} 
			}		 
			 
		public void file_store(){ //���Ͽ� �ܾ� �����ϴ�method
			 String file = "spamlist.txt"; 
			PrintWriter outputStream = null; 
			 try{outputStream = new PrintWriter(file);} 
			 catch(FileNotFoundException e){ 
				 System.out.println("Error opening the file" + file); 
				 System.exit(0); 
				 } 
			 for (int count = 0; count < spamlist.size(); count++){  
				 outputStream.println(spamlist.get(count)); 
				 } 
			 outputStream.close(); 
			 }	
		
		public void send_error(String msg){ //�ݱ�� ���� ����� �޼ҵ�
			Object obj = hm.get(id);
			PrintWriter pw = (PrintWriter)obj;
			pw.println("I love you, God loves you"); //�޼��� ������
			pw.flush(); // ���� ����
			}
		
		public void send_userlist() { //���� ��ü ��� �����ִ� �޼ҵ�
			synchronized(hm){
			Iterator<String> iter = hm.keySet().iterator();
			Object obj = hm.get(id);
			PrintWriter pw = (PrintWriter)obj;
			pw.println("userlist");
			pw.flush();
				while(iter.hasNext()) {
					String username = (String)iter.next();
					pw.println(username);
					pw.flush();
					}
				pw.println("total users");
				pw.println(hm.size());
				pw.flush();
				}
		}
		 
		public void sendmsg(String msg){//�ӼӸ��ϴ� �޼ҵ�
			int start = msg.indexOf(" ") +1;//ó�� ���� ���� ���ĺ��� ����
			int end = msg.indexOf(" ", start); //�ι�° ���� ������ ���ڰ� ���̵��̴�.
			if(end != -1){
				String to = msg.substring(start, end); //���̵� �κ� ���
				String msg2 = msg.substring(end+1); // ��ȭ ���� ���
				Object obj = hm.get(to); // ���̵� �ش��ϴ� ��½�Ʈ���� �����´�.
				if(obj != null){
					PrintWriter pw = (PrintWriter)obj;
					pw.println("["+CurrentTime()+"]"+id+ " whisphered. : " + msg2);
					pw.flush();
					} // if
				}
			} // sendmsg
		
		public String CurrentTime() { //����ð������ִ� �޼ҵ�
			long time= System.currentTimeMillis();
			SimpleDateFormat daytime = new SimpleDateFormat("a hh:mm");
			String strtime =daytime.format(new Date(time));
			return strtime;
		}//
		
		public void broadcast(String msg){ //���� �޼����� Ŭ���̾�Ʈ���� �������ִ� �޼���
			synchronized(hm){ //����ȭ//������ ���� ���� �����ϰ� ������ �� �ִ� ������ ���¸� ����� ���� �����尣�� ����ȭ�� ���� data thread-safe�� �Ѵ�.
				//�������� ��Ų��. // �Ѱ��� �����尡 ���� �����͸� ����ϰ� �ִٸ�, ������ �����带 �����Ϳ� ������ �� ������ ���´�.
				Collection collection = hm.values(); //hm�� ����� ��� ��Ʈ���� �޾ƿ´�.
				Iterator iter = collection.iterator();//�÷��� �ȿ��� iterator method ����� ���� ���� 
				Object obj = hm.get(id);
				PrintWriter pw1 = (PrintWriter)obj;
				while(iter.hasNext()){ // 
					PrintWriter pw = (PrintWriter)iter.next(); 
					if(pw1 != pw) {
					pw.println(msg); //�޼��� ������
					pw.flush(); // ���� ����
						}
					}
				}
			} // broadcast
	}
}
