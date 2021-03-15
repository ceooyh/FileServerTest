package server;

import java.io.BufferedReader;
/**
 * 파일명 : FileServerMain
 * 작성자 : 오영호
 * 작성일 : 2021-03-15
 * 개요 : 파일 서버 클래스
 * @version 0.0.1
 */
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
public class FileServerMain {
	private static ArrayList<FileServerWorker> list = new ArrayList<FileServerWorker>();
	
	public static void main(String[] args) {
		ServerSocket server = null;
		DataInputStream dis = null;
		FileOutputStream fos = null;
		FileServerWorker sw = null;
		
		try {
			server = new ServerSocket(1234);
			Socket client = server.accept();
			sw = new FileServerWorker(client);
			sw.start();
			list.add(sw);
			dis = new DataInputStream(client.getInputStream());
			//파일명을 읽기
			String fileName = dis.readUTF();
			String parentPath = client.getInetAddress().toString().replace(".", "_");
			//출력할 파일 경로로 fos 셋팅
			File file = new File("c:fileupload\\"+parentPath+"\\" + fileName);
			if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
			fos = new FileOutputStream(file);
			byte[] buf = new byte[1024]; 
			while(true) {
				int count = dis.read(buf);
				if(count == -1) break;
				fos.write(buf,0,count);
			}
			System.out.println("파일 다운로드 완료");
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				if(fos!=null)fos.close();
				if(dis!=null)dis.close();
				if(server!=null)server.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	
	
	/**
	 * 파일명 : FileServerWorer
	 * 작성자 : 오영호
	 * 작성일 : 2021-03-15
	 * 개요 : 파일 서버 스레드 클래스
	 * @version 0.0.1
	 */
	public class FileServerWorker extends Thread{
		private Socket client;
		private BufferedReader br;
		private PrintWriter pw;

		public FileServerWorker(Socket client){
			this.client = client;
			try {
				br = new BufferedReader(new InputStreamReader(client.getInputStream()));
				pw = new PrintWriter(client.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			String msg;
			try {
				while (true) {
					msg = br.readLine();
					if (msg == null || msg.equals("exit"))
						break;
					pw.println(msg);
					pw.flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}finally {
				try {
					if(pw!=null)pw.close();
					if(br!=null)br.close();
					if(client!=null)client.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				//현재 스레드를 리스트에서 제거
				list.remove(this);
				System.out.println(client.getInetAddress()+" 접속 종료 하셨습니다.");
				System.out.println(list.size()+"명 클라이언트 접속 중입니다.");
				
			}

		}

	}
}
