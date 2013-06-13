package nl.reupload.freedompainter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.ImageIcon;
import javax.swing.SwingWorker;

public class ConnectionHandler {
	private Socket clientSocket;
	private ImageIcon imageIcon;
	private ObjectOutputStream out;

	public ConnectionHandler(final Socket socket) {
		
		SwingWorker<Integer, Boolean> worker = new SwingWorker<Integer, Boolean>() {

			@Override
			protected Integer doInBackground() throws Exception {
				Thread t = new Thread(new Runnable() {
					
					private ObjectInputStream in;

					@Override
					public void run() {
						clientSocket = socket;
						System.out.println("Server: Accepted "+ socket.getInetAddress());
						try {
							out = new ObjectOutputStream(clientSocket.getOutputStream());
							in = new ObjectInputStream(clientSocket.getInputStream());
							Thread inputListener = new Thread(new Runnable() {
								
								@Override
								 public void run() {
									Object o;
						            while (true) {
						                try {
						                    o = in.readObject();
						                    imageIcon = (ImageIcon) o;
						                } catch (IOException e) {
						                	System.out.println("Server: Client disconnect!");
						                	break;

						                } catch (ClassNotFoundException e) {
						                    e.printStackTrace();
						                }
//						                try {
//											in.reset();
//										} catch (IOException e) {
//											e.printStackTrace();
//										}
						            }
						        }
							});
							inputListener.start();
						} catch (IOException e) {
							// TODO Auto-generated catch block
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
		// TODO Auto-generated method stub
		return imageIcon;
	}

	public void sendImageArray(ImageIcon[] images) {
		if (out != null) {
			try {
				out.writeObject(images);
				out.reset();
			} catch (IOException e) {
				System.err.println("Client connection failed");
			}
		}
	}
}
