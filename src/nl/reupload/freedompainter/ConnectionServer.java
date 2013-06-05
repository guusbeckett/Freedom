package nl.reupload.freedompainter;

import java.io.IOException;
import java.net.ServerSocket;

public class ConnectionServer {

	private ServerSocket serverSocket;

	public ConnectionServer () {
		  try {
			serverSocket = new ServerSocket(3038);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
