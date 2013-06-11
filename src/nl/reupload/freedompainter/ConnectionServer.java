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
						while (true) {
							System.out.println("Server: Waiting for clients...");
							try {
								handlers.add(new ConnectionHandler(serverSocket.accept()));
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							ImageIcon[] images = new ImageIcon[handlers.size()];
							for (int i=0; i<handlers.size(); i++) {
								images[i] = handlers.get(i).getImageIcon();
							}
							for (ConnectionHandler handle : handlers)
								handle.sendImageArray(images);
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
