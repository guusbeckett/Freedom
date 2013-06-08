package nl.reupload.freedompainter;

import java.net.Socket;

import javax.swing.SwingWorker;

public class ConnectionHandler {
	private Socket clientSocket;

	public ConnectionHandler(final Socket socket) {
		
		SwingWorker<Integer, Boolean> worker = new SwingWorker<Integer, Boolean>() {

			@Override
			protected Integer doInBackground() throws Exception {
				Thread t = new Thread(new Runnable() {
					
					@Override
					public void run() {
						clientSocket = socket;
						System.out.println("Server: Accepted "+ socket.getInetAddress());
					}
				});
				t.run();
				return 0;
			}
		};
		worker.execute();
	}
}
