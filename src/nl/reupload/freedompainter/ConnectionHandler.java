package nl.reupload.freedompainter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.SwingWorker;

public class ConnectionHandler {
	private Socket clientSocket;
	private ImageIcon imageIcon;
	private ObjectOutputStream out;
	private boolean connect;
	private String userName;
	private String invited;
	private ArrayList<String> messageListIn;

	public ConnectionHandler(final Socket socket) {
		
		SwingWorker<Integer, Boolean> worker = new SwingWorker<Integer, Boolean>() {

			@Override
			protected Integer doInBackground() throws Exception {
				Thread t = new Thread(new Runnable() {
					
					private ObjectInputStream in;

					@Override
					public void run() {
						messageListIn = new ArrayList<String>();
						clientSocket = socket;
						System.out.println("Server: Accepted "+ socket.getInetAddress());
						try {
							out = new ObjectOutputStream(clientSocket.getOutputStream());
							in = new ObjectInputStream(clientSocket.getInputStream());
							connect = true;
							Thread inputListener = new Thread(new Runnable() {
								

								@Override
								 public void run() {
									Object o;
						            while (true) {
						                try {
						                    o = in.readObject();
											if (o.getClass() == ImageIcon.class)
						                    	imageIcon = (ImageIcon) o;
						                    else if (o.getClass() == String.class) {
						                    	if (((String) o).startsWith("invite "))
						                    		setInvite(((String) o).split("invite ")[1]);
						                    	else if (((String) o).startsWith("uname ")) {
						                    		if (userName != null)
						                    			addMessageCueIn(userName +" changed nickname to " + ((String) o).split("uname ")[1]);
						                    		else
						                    			addMessageCueIn("logged on");
						                    		setUserName(((String) o).split("uname ")[1]);
						                    	}
						                    	else if (((String) o).startsWith("msg "))
						                    		addMessageCueIn(((String) o).split("msg ")[1]);
						                    }
						                } catch (IOException e) {
						                	System.out.println("Server: Client disconnect!");
						                	connect = false;
						                	break;
						                } catch (ClassNotFoundException e) {
						                    e.printStackTrace();
						                }
						            }
						        }
							});
							inputListener.start();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
				t.run();
				return 0;
			}
		};
		worker.execute();
	}

	public ImageIcon getImageIcon() {
		return imageIcon;
	}

	public void sendDataArray(Object[][] data) {
		if (out != null) {
			try {
				out.writeObject(data);
				out.reset();
			} catch (IOException e) {
				System.err.println("Client connection failed");
			}
		}
	}
	
	public boolean isConnected() {
		return connect;
	}

	public String getIP() {
		return clientSocket.getInetAddress().toString();
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public void setInvite(String targetUserName) {
		invited = targetUserName;
	}
	
	public boolean hasInvitation() {
		return (invited != null);
	}
	
	public String getInvitation() {
		return invited;
	}

	public void recieveInvite(String string) {
		try {
			out.writeObject(string);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void addMessageCueIn(String string) {
		messageListIn.add(string);
		
	}
	
	public String[] getMessageCueIn() {
		return messageListIn.toArray(new String[0]);
	}
	
	public void emptyMessageCueIn() {
		messageListIn.clear();
	}
	
	public boolean areMessagesAvailable() {
		return (!messageListIn.isEmpty());
	}

	public void sendMessage(String msg) {
		if (msg != null)
		try {
			out.writeObject("msg "+msg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
