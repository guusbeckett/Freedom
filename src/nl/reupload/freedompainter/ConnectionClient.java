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
import nl.reupload.freedompainter.ConnectionClient.inviteListener;

public class ConnectionClient {
	
	private Socket clientSocket = null;
	private Paint paint;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private iconListener iconListener;
	private String userName;
	private inviteListener inviteListener;
	private messageListener messageListener;

	public ConnectionClient(Paint paint, final String hostIP, final String userName) {
		if (hostIP != null) {
			if (!hostIP.equals("")) {
				this.paint = paint;
				
				if (userName == null)
					this.userName = "anonymous";
				else
					this.userName = userName;
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
						            out.writeObject("uname "+ConnectionClient.this.userName);
						            //TODO maak thread om op icons te wachten en interface om naar te luisteren
						            Thread iconWaiter = new Thread(new Runnable() {
										
										@Override
										public void run() {
											while (true) {
												if (iconListener != null) {
													System.out.println("Client: Waiting for images");
													iconListener.giveObjects(getImage());
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
		if (in != null) {
			Object o;
			try {
				o = in.readObject();
			} catch (IOException e1) {
				System.err.println("Client: IOException on getImage");
				return null;
			} catch (ClassNotFoundException e1) {
				System.err.println("Client: ClassNotFoundException on getImage");
				return null;
			}
			if (o.getClass() == String.class) {
				if (((String) o).startsWith("invite")) {
					System.out.println("getImage got a string");
					if (inviteListener != null)
						inviteListener.notifyInvite(((String) o).split("invite ")[1]);
				}
				else if (((String) o).startsWith("msg")) {
					if (messageListener != null)
						messageListener.notifyMessage(((String) o).split("msg ")[1]);
				}
				return null;
			}
			else
				return (Object[][]) o;
		} else return null;
	}
	
	public interface iconListener
	{
		public abstract void giveObjects(Object[][] objects);
	}
	
	public interface inviteListener {
		public abstract void notifyInvite(String invite);
	}
	
	public interface messageListener {
		public abstract void notifyMessage(String msg);
	}
	
	public void setInviteListener(inviteListener listener) {
		this.inviteListener = listener;
	}
	
	public void setIconListener(iconListener listener)
	{
		this.iconListener = listener;
		
	}
	
	public void setMessageListener(messageListener listener) {
		this.messageListener = listener;
	}

	public void disconnect() throws IOException {
			clientSocket.close();
			clientSocket = null;
			out = null;
			in = null;
	}
	
	public void sendInvite(String reciever) {
		try {
			out.writeObject("invite "+ reciever);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Object getUserName() {
		// TODO Auto-generated method stub
		return userName;
	}

	public void sendMessage(String text) {
		try {
			out.writeObject("msg " + text);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void sendString(String string) {
		try {
			out.writeObject(string);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
