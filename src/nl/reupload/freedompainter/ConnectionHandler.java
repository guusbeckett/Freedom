package nl.reupload.freedompainter;

import java.net.Socket;

public class ConnectionHandler {
	private Socket clientSocket;

	public ConnectionHandler(Socket socket) {
		clientSocket = socket;
		System.out.println("Server: Accepted "+ socket.getInetAddress());
	}
}
