
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient_new {
	 static String Serverip;
	 static String username;
	 
	
	public static void main(String[] args) {
		//BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
		Scanner keyboard = new Scanner(System.in);
		/*
		if(args.length != 2){ // ���� 2���� �ƴ� ���� �Է��ϸ� �Ʒ��� ���� ���� ����.(ip�� port)
			System.out.println("Usage : java ChatClient <username> <server-ip>");
			System.exit(1); // ���α׷� ����
		}
		*/
		
		System.out.println("<username> <server-ip>");
		String userinput = keyboard.nextLine();
		username = userinput.split("\\s")[0];
		Serverip = userinput.split("\\s")[1];
		
		ChatClient_new ChatClient = new ChatClient_new(Serverip,username);

		ChatClient.start();
	} // main
	
	public ChatClient_new(String Serverip,String username) {
		
		 this.Serverip = Serverip;
		 this.username = username;
	}
	
	public void start() {
		Socket sock = null; // client�� ������ �ۼ��� ����
		 BufferedReader br = null; 
		 PrintWriter pw = null; // ���� ������ �� ���
		 boolean endflag = false; //���α׷� ���Ḧ ����
		try{
				//sock = new Socket(Serverip, 10001); //ip�ּҸ� �Է¹޾�(args[1]), ��Ʈ��ȣ 10001�� �������� ����
			sock = new Socket(InetAddress.getByName(Serverip), 10001);
			pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream())); // ����ȭ �� socket ����� �ϱ� ���� socket�� stream�� �����´�
			br = new BufferedReader(new InputStreamReader(sock.getInputStream())); // server�κ��� ������ �ޱ� ���� �κ����� socket����� �ϱ� ������ socket�� stream �����´�.
			BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));

			// send username.
			pw.println(username); // ip����
			pw.flush();//���� ���ۿ� ����Ǿ� �ִ� ������ Ŭ���̾�Ʈ�� �����ϰ� ���۸� ����.
			
			InputThread it = new InputThread(sock, br); //���ÿ� �Է� ����ó���� ���� thread ����
			it.start(); // 
	
			String line = null;
			while((line = keyboard.readLine()) != null){ //�����κ��� �޽��� �޴� �κ� null�� �ƴϸ� ��� �����Ѵ�.
				pw.println(line); // ������ ����ȭ
				pw.flush(); // ����ȭ�� ������ ����
				if(line.equals("/quit")){ // '/quit' �ϸ� ���� ����
					endflag = true;
					break;
				}
			}
			System.out.println("Connection closed.");
		}catch(Exception ex){
			if(!endflag)
				System.out.println(ex);
		}finally{
			try{
				if(pw != null)
					pw.close();
			}catch(Exception ex){}
			try{
				if(br != null)
					br.close();
			}catch(Exception ex){}
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
	}
} // class

class InputThread extends Thread{
	private Socket sock = null;
	private BufferedReader br = null;
	public InputThread(Socket sock, BufferedReader br){ // ������ , �Լ� ȣ���ϸ� sock
		this.sock = sock; //ip �� port ����
		this.br = br; //������ ����� inputstream object�� ����.
	}
	public void run(){
		try{
			String line = null;
			while((line = br.readLine()) != null){
				System.out.println(line); // ���� ���
			}
		}catch(Exception ex){
		}finally{
			try{
				if(br != null)
					br.close();
			}catch(Exception ex){}
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		}
	} // InputThread
}
}
  
