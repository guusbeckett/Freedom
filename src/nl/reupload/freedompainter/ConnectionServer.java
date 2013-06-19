package nl.reupload.freedompainter;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class ConnectionServer {
	
	public static void main(String[] args) {
		new ConnectionServer();
	}

	private ServerSocket serverSocket;
	private ArrayList<ConnectionHandler> handlers;

	public ConnectionServer () {
		handlers = new ArrayList<ConnectionHandler>();
		try {
			System.out.println("Server starting");
			serverSocket = new ServerSocket(3038);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		SwingWorker<Integer, Boolean> worker = new SwingWorker<Integer, Boolean>() {

			@Override
			protected Integer doInBackground() throws Exception {
				Thread t = new Thread(new Runnable() {
					
					@Override
					public void run() {
						Thread handleImages = new Thread(new Runnable() {
							
							@Override
							public void run() {
								System.out.println("Server imageHandler starting");
								boolean send = false;
								while (true) {
									try {
										Thread.sleep(1000);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									Object[][] data = new Object[handlers.size()][2];
//									ImageIcon[] images = new ImageIcon[handlers.size()];
									for (int i=0; i<handlers.size(); i++) {
										if (handlers.get(i).isConnected()) {
											if (handlers.get(i).hasInvitation()) {
												handleInvite(handlers.get(i).getInvitation(), handlers.get(i).getUserName());
												handlers.get(i).setInvite(null);
											}
											if (handlers.get(i).areMessagesAvailable()) {
												handleIncomingMessage(handlers.get(i).getUserName(), handlers.get(i).getMessageCueIn());
												handlers.get(i).emptyMessageCueIn();
											}
											data[i][0] = handlers.get(i).getImageIcon();
											data[i][1] = handlers.get(i).getUserName();
											send = true;
										}
										else {
											broadCastMessage(handlers.get(i).getUserName()+" has lost connection", "server");
											handlers.remove(i);
											System.err.println("Connection to client lost, dropping handler");
											send = false;
											break;
										}
									}
									if (send) {
										System.out.println("Server: Sent "+ data.length+ " images");
										for (ConnectionHandler handle : handlers) {
											if (handle.isConnected())
												handle.sendDataArray(data);
										}
									}
								}
								
							}

							private void handleIncomingMessage(String userName,
									String[] messageCue) {
								for (String msg : messageCue) {
									if (msg.startsWith("pm ")) {
										String[] things = msg.replace("pm ", "").split(" ");
										if (things.length >= 2)
											privateMessage(msg.replace("pm " + things[0] + " ", ""), userName, things[0]);
										else
											privateMessage("there is no reciever or message in this pm", "server", userName);
									}
									else
										broadCastMessage(msg, userName);
								}
								
							}

						

							private void handleInvite(String invitation,
									String userName) {
								if (invitation.equals(userName))
//									broadCastMessage(userName + " tried to invite himself to a joint session", "server");
									privateMessage("You cannot invite yourself!", "server", userName);
								else {
									for (ConnectionHandler handle : handlers) {
										if (handle.getUserName().equals(invitation)) {
											handle.recieveInvite("invite " + userName);
											return;
										}
									}
//									broadCastMessage(invitation + " is not a user", "server");
									privateMessage(invitation + " is not a user", "server", userName);
								}
							}
						});
						handleImages.start();
						while (true) {
							System.out.println("Server: Waiting for clients...");
							try {
//								broadCastMessage("someone joined the server", "server");
								handlers.add(new ConnectionHandler(serverSocket.accept()));
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							
						}
					}
				});
				t.run();
				
				return 0;
			}
		};
		worker.execute();
		
	}
	
	private void broadCastMessage(String msg, String nickName) {
		msg = "<"+nickName+"> "+msg;
		for (ConnectionHandler handle : handlers) {
			handle.sendMessage(msg);
		}
		
	}
	
	private void privateMessage(String msg, String nickNameSend, String nickNameRecieve) {
		msg = "<"+nickNameSend+"> [private] "+msg;
		for (ConnectionHandler handle : handlers) {
			if (handle.getUserName().equals(nickNameRecieve)) {
				handle.sendMessage(msg);
				return;
			}
		}
		privateMessage(nickNameRecieve + " could not be reached", "server", nickNameSend);
	}

	public void stop()  {
		try {
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
