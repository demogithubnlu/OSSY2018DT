package tcp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.StringTokenizer;

public class FileServerProcess extends Thread {
	Socket socket;
	private DataInputStream netIn;
	private DataOutputStream netOut;
	private String serverDir;

	public FileServerProcess(Socket socket) {
		this.socket = socket;
		try {
			netIn = new DataInputStream(new BufferedInputStream(
					socket.getInputStream()));
			netOut = new DataOutputStream(new BufferedOutputStream(
					socket.getOutputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void run() {
		try {
			String line;
			while (true) {
				try {
					// đọc lệnh từ client
					line = netIn.readUTF();
					if ("quit".equalsIgnoreCase(line)) {
						break;
					}

					// phân tích lệnh
					phantich(line);
				} catch (Exception e) {
				}
			}
			netIn.close();
			netOut.flush();
			netOut.close();
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void phantich(String line) throws IOException {
		StringTokenizer st = new StringTokenizer(line);
		String command = st.nextToken();
		String sf, df;
		switch (command) {
		case "SET_SERVER_DIR":
			serverDir = st.nextToken();
			// nếu command là SET_SERVER_DIR thì set lại serverDir
			netOut.writeUTF(line);
			break;

		// sẽ không có trường này vì bên client không gởi lện này lên server
		// case "SET_CLIENT_DIR":
		// netOut.writeUTF(line);
		case "SEND":
			df = st.nextToken();
			netOut.writeUTF(command + " " + df);
			receivedFile(df);
			break;
		case "GET":
			sf = st.nextToken();
			// GET sf
			netOut.writeUTF(command + " " + sf);
			sendFile(sf);
			break;

		default:
			break;
		}

	}

	private void sendFile(String sf) throws IOException {
		File file = new File(serverDir + File.separator + sf);
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

	private void receivedFile(String df) throws IOException {
		File file = new File(serverDir + File.separator + df);
		BufferedOutputStream bos = new BufferedOutputStream(
				new FileOutputStream(file));
		long size = netIn.readLong();
		int byteRead, byteMustRead;
		long remain = size;
		byte[] buff = new byte[10 * 1024];
		while (remain > 0) {
			byteMustRead = buff.length > remain ? (int) remain : buff.length;
			byteRead = netIn.read(buff, 0, byteMustRead);
			bos.write(buff, 0, byteRead);
			remain -= byteMustRead;

		}
		bos.close();

	}
}