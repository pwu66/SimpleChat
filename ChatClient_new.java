
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
		if(args.length != 2){ // 값이 2개가 아닌 값을 입력하면 아래와 같은 구문 실행.(ip와 port)
			System.out.println("Usage : java ChatClient <username> <server-ip>");
			System.exit(1); // 프로그램 종료
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
		Socket sock = null; // client와 데이터 송수신 역할
		 BufferedReader br = null; 
		 PrintWriter pw = null; // 값을 전달할 때 사용
		 boolean endflag = false; //프로그램 종료를 위한
		try{
				//sock = new Socket(Serverip, 10001); //ip주소를 입력받아(args[1]), 포트번호 10001로 서버소켓 선언
			sock = new Socket(InetAddress.getByName(Serverip), 10001);
			pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream())); // 직렬화 및 socket 통신을 하기 위해 socket의 stream을 가져온다
			br = new BufferedReader(new InputStreamReader(sock.getInputStream())); // server로부터 응답을 받기 위한 부분으로 socket통신을 하기 때문에 socket의 stream 가져온다.
			BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));

			// send username.
			pw.println(username); // ip전달
			pw.flush();//현재 버퍼에 저장되어 있는 내용을 클라이언트로 전송하고 버퍼를 비운다.
			
			InputThread it = new InputThread(sock, br); //동시에 입력 정보처리를 위한 thread 생성
			it.start(); // 
	
			String line = null;
			while((line = keyboard.readLine()) != null){ //서버로부터 메시지 받는 부분 null이 아니면 계속 동작한다.
				pw.println(line); // 데이터 직렬화
				pw.flush(); // 직력화된 데이터 전달
				if(line.equals("/quit")){ // '/quit' 하면 연결 종료
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
	public InputThread(Socket sock, BufferedReader br){ // 생성자 , 함수 호출하면 sock
		this.sock = sock; //ip 및 port 정보
		this.br = br; //서버에 연결된 inputstream object에 연결.
	}
	public void run(){
		try{
			String line = null;
			while((line = br.readLine()) != null){
				System.out.println(line); // 내용 출력
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
  
