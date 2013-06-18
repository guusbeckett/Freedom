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
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.Array;

import javax.swing.ImageIcon;
import javax.swing.SwingWorker;

import nl.reupload.freedompainter.ConnectionClient.iconListener;

public class ConnectionClient {
	
	private Socket clientSocket = null;
	private Paint paint;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private iconListener listener;

	public ConnectionClient(Paint paint, final String hostIP, final String userName) {
		if (hostIP != null) {
			if (!hostIP.equals("")) {
				this.paint = paint;
				
		        SwingWorker<Integer, Boolean> worker = new SwingWorker<Integer, Boolean>() {

					@Override
					protected Integer doInBackground() throws Exception {
						Thread t = new Thread(new Runnable() {
							
							@Override
							public void run() {
								try {
						            clientSocket = new Socket(hostIP, 3038);
						            out = new ObjectOutputStream(clientSocket.getOutputStream());
						            in = new ObjectInputStream(clientSocket.getInputStream());
						            out.writeObject(userName);
						            //TODO maak thread om op icons te wachten en interface om naar te luisteren
						            Thread iconWaiter = new Thread(new Runnable() {
										
										@Override
										public void run() {
											while (true) {
												if (listener != null) {
													System.out.println("Client: Waiting for images");
													listener.giveObjects(getImage());
													System.out.println("Client: images recieved");
												}
											}
											
											
										}
									});
						            iconWaiter.start();
								} catch (UnknownHostException e) {
						            System.err.println("Cannot find host, socket init failed");
						            //System.exit(1);
						        } catch (IOException e) {
						            System.err.println("Couldn't get I/O for socket");
						            e.printStackTrace();
//						            System.exit(1);
						        }
							}
						});
						t.run();
						return 0;
					}
				};
				worker.execute();
			}
			else throw new IllegalArgumentException("hostIP is empty!!");
		}
		else throw new IllegalArgumentException("hostIP is null!!");
	}

	public void sendImage(ImageIcon image) throws IOException {
		if (image != null && out != null) {
			out.writeObject(image);
			out.reset();
		}
	}

	public Object[][] getImage() {
		try {
			return (Object[][]) in.readObject();
		} catch (ClassNotFoundException e) {
			System.err.println("No Class Found");
		} catch (IOException e) {
			System.err.println("IOException occured");
		}
		return null;
	}
	
	public interface iconListener
	{
		public abstract void giveObjects(Object[][] objects);
	}
	
	public void setListener(iconListener listener)
	{
		this.listener = listener;
		
	}

	public void disconnect() throws IOException {
			clientSocket.close();
	}
}
