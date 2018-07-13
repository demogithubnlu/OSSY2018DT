package tcp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.StringTokenizer;

public class FileClient {
	private Socket socket;
	private DataInputStream netIn;
	private DataOutputStream netOut;
	private BufferedReader userIn;
	private static final String host = "127.0.0.1";
	private static final int PORT = 12345;
	private String clientDir;

	public FileClient() {
		clientDir = "C:\\source";
	}

	// mo ket noi
	public void request() {
		try {
			this.socket = new Socket(host, PORT);
			netIn = new DataInputStream(new BufferedInputStream(
					socket.getInputStream()));
			netOut = new DataOutputStream(new BufferedOutputStream(
					socket.getOutputStream()));
			userIn = new BufferedReader(new InputStreamReader(System.in));
			String line;
			//System.out.println("welcome NLU ");
			
			//vòng lặp vô tận để nhận request của client
			while (true) {
				
				try {
					// đọc lệnh người dùng
					line = userIn.readLine();

					// phân tích lệnh
					phantich(line);
					if ("quit".equalsIgnoreCase(line)) {
						break;
					}
				} catch (Exception e) {
				}
			}
			netIn.close();
			netOut.flush();
			netOut.close();
			userIn.close();
			socket.close();
		} catch (Exception e) {
		}

	}

	private void phantich(String line) throws IOException {

		StringTokenizer st = new StringTokenizer(line);
		String command = st.nextToken();
		String sf, df;
		switch (command) {
		case "SET_SERVER_DIR":

			// phía server xử lý.chỉ cần gởi luôn dòng lệnh lên server là dc
			netOut.writeUTF(line);

			break;
		case "SET_CLIENT_DIR":

			// nếu command là set_client_dir thì token tiếp theo là thư mục gốc
			// của client
			// chỉ cần thay thế ở thư mục gốc client thôi không cần đưa lên
			// server
			clientDir = st.nextToken();

			break;
		case "SEND":
			sf = st.nextToken();
			df = st.nextToken();
			// gởi lệnh lên server với caaustrucs "SEND df"
			netOut.writeUTF(command + " " + df);
			sendFile(sf);

			break;
		case "GET":
			sf = st.nextToken();
			df = st.nextToken();
			// GET sf
			netOut.writeUTF(command + " " + sf);
			receivedFile(df);
			break;

		case "QUIT":
			netOut.writeUTF(line);
			break;

		default:
			System.out.println("khong hop le");
			break;
		}
		netOut.flush();
	}

	private void receivedFile(String df) throws IOException {
		File file   = new File(clientDir + File.separator +df);
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
		long size = netIn.readLong();
		int byteRead,byteMustRead;
		long remain = size;
		byte[] buff = new byte[10*1024];
		while(remain>0){
			byteMustRead = buff.length>remain ? (int) remain:buff.length;
			byteRead = netIn.read(buff, 0, byteMustRead);
			bos.write(buff, 0, byteRead);
			remain-= byteMustRead;
			
		}
		bos.close();
	}

	private void sendFile(String sf) throws IOException {

		File file = new File(clientDir + File.separator + sf);
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(
				file));
		netOut.writeLong(file.length());
		byte[] buff = new byte[10 * 1024];
		int data;
		while ((data = bis.read(buff)) != -1) {
			netOut.write(buff, 0, data);

		}
		netOut.flush();
		bis.close();
	}
	public static void main(String[] args) {
		FileClient fileclient = new FileClient();
		fileclient.request();
	}
}
