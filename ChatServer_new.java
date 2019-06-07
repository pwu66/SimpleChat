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
				ServerSocket server = new ServerSocket(10001);// 10001번 포트로 하는 server socket 생성
				
				System.out.println("Waiting connection...");
				HashMap hm = new HashMap(); // 쓰레드 간의 정보 공유역할. hashmap key 아이디, value는 출력 스트림
				while(true){ //클라이언트 접속 항상 받을 수 있도록 무한루프
					Socket sock = server.accept(); //이 서버소켓의 연결을 대기하며 들어오면  아래의 쓰레드 실행
					ChatThread chatthread = new ChatThread(sock, hm); // 접속을 유지하면서 데이터 송수신을 위한 스레드 객체 생성
					chatthread.start(); //스레드 내에 run()메소드 실행
				} // while
			}catch(Exception e){ //서버 소켓 및 연결 오류 날 경우 에러 메세지
				System.out.println(e);
			}
		} // main
	

	static class ChatThread extends Thread{
		private Socket sock; //클라이언트와 통신하기 위한 소켓
		private String id; // 접속자 아이디 저장
		private BufferedReader br;
		private HashMap hm; // 
		private boolean initFlag = false;
		boolean banFlag = true;
		String ban[] = {"fuck","sibal","dogbaby","michin","ssyang"};
		ArrayList spamlist = new ArrayList();
		public ChatThread(Socket sock, HashMap hm){
			this.sock = sock; //연결된 소켓 가져옴
			this.hm = hm; //사용자 정보 가져옴
			try{// 쓰레드 생성시, 소켓 통신 할 printwriter와 bufferedreader 만들고, 접속한 유저의  아이디와 출력스트림(printwriter)을 hashmap에 넣어
				// 스레드 간의 정보 공유
				PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream())); // 클라이언트들에게 날릴 메시지를 위한 세팅
				br = new BufferedReader(new InputStreamReader(sock.getInputStream())); // 클라이언트가 입력한 것을 읽기 위한 세팅
				id = br.readLine(); //id 입력
				broadcast("["+CurrentTime()+"]"+id + " entered."); // 다른 유저 모두에게 broadcast로 메시지 보낸다.
				System.out.println("[Server] User (" + id + ") entered.");
				synchronized(hm){ // 여러개의 스레드가 한개의 자원을 사용하고자 할 때 멀티쓰레드인 상태임으로 사용자간의 동기화를 시켜준다.
					//현재 데이터를 사용하고 있는 해당 스레드를 제외하고 나머지 스레드들은 데이터 접근을 하지
					//못하도록 한다.
					hm.put(this.id, pw);//유저의 아이디와 printwriter을 넣어준다. 해당 스레드 마다 각각의 printwriter을 저장해서
					// 각각의 클라이언트들이 사용하도록 주는 것이다. 그래서 constructor에 선언을 해준다.
				}
				initFlag = true;
			}catch(Exception ex){
				System.out.println(ex);
				}
			} // construcor
		
		public void run(){// 클라이언트로부터 받은 데이터 클라인터에게 송신함
			try{ 
				File();
				String line = null; //수신 받은 데이터 저장 변수
				while((line = br.readLine()) != null){//클라이언트가 입력한 내용읽어온다.
					banFlag = true;
					for(int i =0;i<spamlist.size();i++) {
						if(line.equals(spamlist.get(i))){//"/quit"입력시 break
							send_error(line);
							banFlag = false;	
						}
					}
					if(banFlag){
					if(line.equals("/quit")) //"/quit"입력시 break
						break;
					else if(line.indexOf("/to") == 0){ 
						sendmsg(line); // "/to" 입력시 귓속말기능 사용
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
						broadcast("["+CurrentTime()+"]"+id + " : " + line); // 그렇지 않을 경우 사용자 전부에게 대화내용을 보낸다.
					}
					}
				}catch(Exception ex){ // 사용자 입력 못 읽어올경우 에러
				System.out.println(ex);
			}finally{ // quit 입력시 
				synchronized(hm){ // 여러 스레드가 공유하는 해쉬맵을 동기화 시킨다.
					hm.remove(id); // 나올 경우, hashmap에서 id 제거.
					//저장
				}
				broadcast("["+CurrentTime()+"]"+id + " exited."); // 나머지 클라이언트에게 나갔음을 알린다.
				try{
					if(sock != null)
						sock.close();
				}catch(Exception ex){}
			}
		} // run
		
		public void spam_list(){ //금지어 목록 출력
			Object obj = hm.get(id);
			PrintWriter pw = (PrintWriter)obj;
			pw.println("spamlist");
			pw.flush();
			for(int i =0; i<spamlist.size();i++) {
				pw.println(spamlist.get(i)); //메세지 보내기
				pw.flush(); // 버퍼 비우기
				}
			}
		
		public void add_spam(String line){ //금지어 추가
		     Object obj = hm.get(id);
			PrintWriter pw = (PrintWriter)obj;
				for(int i =0; i<spamlist.size();i++) {
					if(line.equals(spamlist.get(i))) {
						pw.println(line+" is already existed"); //메세지 보내기
						pw.flush(); // 버퍼 비우기
						return;
					}
				}
				spamlist.add(line);
				pw.println(line+"is new word");
				pw.flush();
		}
		
		public void File(){ //파일 있으면 단어 불러오기, 없으면 새로 빈파일 만들기
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
						 outputStream.println(""); //빈파일 만들기
				} 
			}		 
			 
		public void file_store(){ //파일에 단어 저장하는method
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
		
		public void send_error(String msg){ //금기어 사용시 사랑의 메소드
			Object obj = hm.get(id);
			PrintWriter pw = (PrintWriter)obj;
			pw.println("I love you, God loves you"); //메세지 보내기
			pw.flush(); // 버퍼 비우기
			}
		
		public void send_userlist() { //유저 전체 목록 보여주는 메소드
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
		 
		public void sendmsg(String msg){//귓속말하는 메소드
			int start = msg.indexOf(" ") +1;//처음 공백 문자 이후부터 시작
			int end = msg.indexOf(" ", start); //두번째 공백 사이의 문자가 아이디이다.
			if(end != -1){
				String to = msg.substring(start, end); //아이디 부분 얻기
				String msg2 = msg.substring(end+1); // 대화 내용 얻기
				Object obj = hm.get(to); // 아이디에 해당하는 출력스트림을 가져온다.
				if(obj != null){
					PrintWriter pw = (PrintWriter)obj;
					pw.println("["+CurrentTime()+"]"+id+ " whisphered. : " + msg2);
					pw.flush();
					} // if
				}
			} // sendmsg
		
		public String CurrentTime() { //현재시간보여주는 메소드
			long time= System.currentTimeMillis();
			SimpleDateFormat daytime = new SimpleDateFormat("a hh:mm");
			String strtime =daytime.format(new Date(time));
			return strtime;
		}//
		
		public void broadcast(String msg){ //받은 메세지를 클라이언트에게 전파해주는 메서드
			synchronized(hm){ //동기화//쓰레드 간의 서로 공유하고 수정할 수 있는 안전한 상태를 만들기 위해 스레드간의 동기화를 시켜 data thread-safe를 한다.
				//안전성를 시킨다. // 한개의 스레드가 현재 데이터를 사용하고 있다면, 나머지 스레드를 데이터에 접근할 수 없도록 막는다.
				Collection collection = hm.values(); //hm에 저장된 출력 스트림을 받아온다.
				Iterator iter = collection.iterator();//컬렉션 안에서 iterator method 사용을 위한 선언 
				Object obj = hm.get(id);
				PrintWriter pw1 = (PrintWriter)obj;
				while(iter.hasNext()){ // 
					PrintWriter pw = (PrintWriter)iter.next(); 
					if(pw1 != pw) {
					pw.println(msg); //메세지 보내기
					pw.flush(); // 버퍼 비우기
						}
					}
				}
			} // broadcast
	}
}
