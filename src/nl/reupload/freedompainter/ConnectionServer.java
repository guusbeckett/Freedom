package nl.reupload.freedompainter;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class ConnectionServer {

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
										Thread.sleep(100);
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
											data[i][0] = handlers.get(i).getImageIcon();
											data[i][1] = handlers.get(i).getUserName();
											send = true;
										}
										else {
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

							private void handleInvite(String invitation,
									String userName) {
								for (ConnectionHandler handle : handlers) {
									if (handle.getUserName().equals(invitation)) {
										handle.recieveInvite("invite " + userName);
									}
								}
							}
						});
						handleImages.start();
						while (true) {
							System.out.println("Server: Waiting for clients...");
							try {
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
}
