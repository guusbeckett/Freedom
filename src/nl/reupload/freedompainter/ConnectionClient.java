package nl.reupload.freedompainter;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class ConnectionClient {
	
	private Socket clientSocket = null;
	private Paint paint;
	private ObjectOutputStream out;
	private ObjectInputStream in;

	public ConnectionClient(Paint paint) {
		this.paint = paint;
		try {
            clientSocket = new Socket("localhost", 3038);
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());
        } catch (UnknownHostException e) {
            System.err.println("Cannot find host, socket init failed");
            //System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for socket");
            e.printStackTrace();
//            System.exit(1);
        }
	}

	public void sendImage(Image image) {
		try {
			out.writeObject(image);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Image getImage() {
		// TODO stub method
		try {
			if (in.available() > 0)
				return (Image) in.readObject();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
