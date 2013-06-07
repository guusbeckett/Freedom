package nl.reupload.freedompainter;

import java.io.IOException;
import java.net.ServerSocket;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class ConnectionServer {

	private ServerSocket serverSocket;

	public ConnectionServer () {
		try {
			serverSocket = new ServerSocket(3038);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
//			try {
//				new ConnectionHandler(serverSocket.accept());
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			SwingUtilities.invokeLater(new Runnable() {
			
				@Override
				public void run() {
					while (true) {
						try {
							new ConnectionHandler(serverSocket.accept());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				}
			});
//		Thread t = new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				while (true) {
//					try {
//						new ConnectionHandler(serverSocket.accept());
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//				
//			}
//		});
//		t.run();
	}
}
