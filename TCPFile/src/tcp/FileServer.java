package tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FileServer {
	public static final int PORT = 12345;

	public static void main(String[] args) throws IOException {
		ServerSocket ss = new ServerSocket(PORT);

		while (true) {
			Socket socket = ss.accept();
			FileServerProcess fsp = new FileServerProcess(socket);
			fsp.start();

		}

	}
}