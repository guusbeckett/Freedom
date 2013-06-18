/**
 * @author Guus Beckett & Jim van Abkoude
 */

package nl.reupload.freedompainter;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import javax.swing.*;

import nl.reupload.freedompainter.ConnectionClient.inviteListener;
import nl.reupload.freedompainter.ConnectionClient.messageListener;


public class Paint implements inviteListener{
	private PadDraw drawPad;
	private ConnectionServer server;
	protected ConnectionClient client;
	private previewPanel previewPanel;
	private ChatPanel chatPanel;

	public Paint() {
		Icon iconB = new ImageIcon("./img/blue.png");
		//the blue image icon
		Icon iconM = new ImageIcon("./img/magenta.png");
		//magenta image icon
		Icon iconR = new ImageIcon("./img/red.png");
		//red image icon
		Icon iconBl = new ImageIcon("./img/black.png");
		//black image icon
		Icon iconG = new ImageIcon("./img/green.png");
		//finally the green image icon
		//These will be the images for our colors.
		
		final JFrame frame = new JFrame("Paint It");
		//Creates a frame with a title of "Paint it"
		
		Container content = frame.getContentPane();
		//Creates a new container
		content.setLayout(new BorderLayout());
		//sets the layout
		
		chatPanel = new ChatPanel();
		drawPad = new PadDraw();
		previewPanel = new previewPanel();
		//creates a new padDraw, which is pretty much the paint program
		previewPanel.setPreferredSize(new Dimension(100, 300));
		content.add(previewPanel, BorderLayout.EAST);
		content.add(drawPad, BorderLayout.CENTER);
		content.add(chatPanel, BorderLayout.SOUTH);
		//sets the padDraw in the center
		
		JPanel panel = new JPanel();
		//creates a JPanel
		panel.setPreferredSize(new Dimension(32, 68));
		panel.setMinimumSize(new Dimension(32, 68));
		panel.setMaximumSize(new Dimension(32, 68));
		//This sets the size of the panel
		
		JButton clearButton = new JButton("Clear");
		//creates the clear button and sets the text as "Clear"
		clearButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				drawPad.clear();
			}
		});
		//this is the clear button, which clears the screen.  This pretty
		//much attaches an action listener to the button and when the
		//button is pressed it calls the clear() method
		
		JButton redButton = new JButton(iconR);
		//creates the red button and sets the icon we created for red
		redButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				drawPad.red();
			}

		});
		//when pressed it will call the red() method.  So on and so on =]
		
		JButton blackButton = new JButton(iconBl);
		//same thing except this is the black button
		blackButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				drawPad.black();
			}
		});
		
		JButton magentaButton = new JButton(iconM);
		//magenta button
		magentaButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				drawPad.magenta();
			}
		});
		
		JButton blueButton = new JButton(iconB);
		//blue button
		blueButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				drawPad.blue();
			}
		});
		
		JButton greenButton = new JButton(iconG);
		//green button
		greenButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				drawPad.green();
			}
		});
		blackButton.setPreferredSize(new Dimension(16, 16));
		magentaButton.setPreferredSize(new Dimension(16, 16));
		redButton.setPreferredSize(new Dimension(16, 16));
		blueButton.setPreferredSize(new Dimension(16, 16));
		greenButton.setPreferredSize(new Dimension(16,16));
		//sets the sizes of the buttons
		
		panel.add(greenButton);
		panel.add(blueButton);
		panel.add(magentaButton);
		panel.add(blackButton);
		panel.add(redButton);
		panel.add(clearButton);
		//adds the buttons to the panel
		
		content.add(panel, BorderLayout.WEST);
		//sets the panel to the left
		
		frame.setSize(1000, 600);
		//sets the size of the frame
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JMenuBar menubar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		frame.setJMenuBar(menubar);
		menubar.add(fileMenu);
		final Paint paint = this;
		JMenuItem item = new JMenuItem("start server");
		item.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				server = new ConnectionServer();
				String net = "You are running a Freedom server on:\n";
				Enumeration<NetworkInterface> interfaces;
				try {
					interfaces = NetworkInterface.getNetworkInterfaces();
					while (interfaces.hasMoreElements()){
					    NetworkInterface current = interfaces.nextElement();
					    net+=current+"\n";
					    try {
							if (!current.isUp() || current.isLoopback() || current.isVirtual()) continue;
							Enumeration<InetAddress> addresses = current.getInetAddresses();
						    while (addresses.hasMoreElements()){
						        InetAddress current_addr = addresses.nextElement();
						        if (current_addr.isLoopbackAddress()) continue;
						        net+=current_addr.getHostAddress()+"\n";
						    }
						} catch (SocketException e) {
							JOptionPane.showMessageDialog(frame, net, "server notify", JOptionPane.ERROR_MESSAGE);
						}
						JOptionPane.showMessageDialog(frame, net, "server notify", JOptionPane.INFORMATION_MESSAGE);
					    
					}
				} catch (SocketException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				
				
			}
		});
		fileMenu.add(item);
		JMenuItem item2 = new JMenuItem("start client");
		item2.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (client == null) {
					client = new ConnectionClient(paint, JOptionPane.showInputDialog(
						      "Vul het IP adres van de server in: "), JOptionPane.showInputDialog(
								      "Vul een username in: "));
					client.setInviteListener(paint);
					chatPanel.setClient(client);
					drawPad.setClient(client);
					previewPanel.setClient(client);
				}
				else {
					try {
						client.disconnect();
						client = null;
						client = new ConnectionClient(paint, JOptionPane.showInputDialog(
							      "Vul het IP adres van de server in: "), JOptionPane.showInputDialog(
									      "Vul een username in: "));
						client.setInviteListener(paint);
						chatPanel.setClient(client);
						drawPad.setClient(client);
						previewPanel.setClient(client);
					} catch (IOException e) {
						System.err.println("Client: Closing client returned IOException");
					}
					
				}
			}
		});
		fileMenu.add(item2);
		//makes it so you can close
		frame.setVisible(true);
		//makes it so you can see it
	}
	
	public Image getImage() {
		return drawPad.getImage();
	}
	public void mergeImage() {
		drawPad.mergeImage();
	}

	@Override
	public void notifyInvite(String invite) {
		int option = JOptionPane.showConfirmDialog(drawPad, "you have an invite from " + invite + ", accept?");
		switch (option) {
			case (0):
				break;
			case (1):
				break;
			case (2):
				break;
		}
	}
}


class PadDraw extends JComponent{
	Image image;
	//this is gonna be your image that you draw on
	Graphics2D graphics2D;
	//this is what we'll be using to draw on
	int currentX, currentY, oldX, oldY;
	//these are gonna hold our mouse coordinates
	private ConnectionClient recient;

	public ConnectionClient getClient() {
		return recient;
	}

	public void setClient(ConnectionClient client) {
		this.recient = client;
	}

	public Image getImage() {
		return image;
	}
	
	public void mergeImage() {
		if (image != null && recient != null) {
			//Image image = recient.getImage();
			if (image != null) {
				Graphics2D g2d = (Graphics2D) image.getGraphics();
				g2d.drawImage(image, 0, 0, null);
				g2d.drawImage(this.image, 0, 0, null);
			}
		}
	//	this.image.getGraphics().drawImage(image, 1, 1, null);
	}
	//Now for the constructors
	public PadDraw(){
		setDoubleBuffered(false);
		addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent e){
				oldX = e.getX();
				oldY = e.getY();
			}
		});
		new Timer(100, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (recient != null) {
					try {
						recient.sendImage(new ImageIcon(image));
					} catch (IOException e) {
						recient = null;
						System.err.println("Connection to server no longer available, client dropped");
					}
				}
				
			}
		}).start();
		addMouseMotionListener(new MouseMotionAdapter(){
			public void mouseDragged(MouseEvent e){
				currentX = e.getX();
				currentY = e.getY();
				if(graphics2D != null)
				graphics2D.drawLine(oldX, oldY, currentX, currentY);
				mergeImage();
				repaint();
				oldX = currentX;
				oldY = currentY;
			}

		});
	}

	public void paintComponent(Graphics g){
		if(image == null){
			image = createImage(getSize().width-300, getSize().height);
			graphics2D = (Graphics2D)image.getGraphics();
			graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			clear();

		}
		
		g.drawImage(image, 0, 0, null);
		
	}

	public void clear(){
		graphics2D.setPaint(Color.white);
		graphics2D.fillRect(0, 0, getSize().width-300, getSize().height);
		graphics2D.setPaint(Color.black);
		repaint();
	}
	public void red(){
		graphics2D.setPaint(Color.red);
		repaint();
	}
	public void black(){
		graphics2D.setPaint(Color.black);
		repaint();
	}
	public void magenta(){
		graphics2D.setPaint(Color.magenta);
		repaint();
	}
	public void blue() {
		graphics2D.setPaint(Color.blue);
		repaint();
	}
	public void green(){
		graphics2D.setPaint(Color.green);
		repaint();
	}


}

class previewPanel extends JPanel implements ConnectionClient.iconListener , MouseListener, MouseMotionListener
{
	private ConnectionClient connectionClient;
	private Object[][] servedObjects;
	private int x,y;
	private JPopupMenu contextMenu;

	public previewPanel() {
		x=0;
		y=0;
		contextMenu = new JPopupMenu();
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	public void paintComponent(Graphics g){
			Graphics2D g2 = (Graphics2D)g;
			g.clearRect(0, 0, this.getHeight(), this.getWidth());
			if (servedObjects != null) {
				int y = 50;
				for (Object[] objs : servedObjects) {
					if (objs[1] != null)
						g2.drawString((String)objs[1], 0, y-10);
					g2.drawImage(((ImageIcon)objs[0]).getImage(), 0, y, 100, 100, null);
					y+=150;
				}
			}
		}

	@Override
	public void giveObjects(Object[][] objects) {
		if (objects != null) {
			servedObjects = objects;
			System.out.println(servedObjects.length);
			repaint();
		}
	}
	
	public void setClient(ConnectionClient connectionClient)
	{
		this.setConnectionClient(connectionClient);
		connectionClient.setIconListener(this);
	}

	public ConnectionClient getConnectionClient() {
		return connectionClient;
	}

	public void setConnectionClient(ConnectionClient connectionClient) {
		this.connectionClient = connectionClient;
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		int localy = 50;
		if (arg0.getButton() == MouseEvent.BUTTON3)
			if (servedObjects != null) {
				for (final Object[] objs : servedObjects) {
					if (y >= localy-10 && y <= localy+100) {
						contextMenu.removeAll();
						if (!objs[1].equals(connectionClient.getUserName())) {
							JMenuItem item = new JMenuItem("Invite " + objs[1]);
							item.addActionListener(new ActionListener() {
								
								@Override
								public void actionPerformed(ActionEvent arg0) {
									connectionClient.sendInvite((String) objs[1]);
									
								}
							});
							contextMenu.add(item);
						}
						
						else
							contextMenu.add(new JMenuItem("You cannot invite yourself!"));
						contextMenu.show(this, x, y);
					}
					localy+=150;
				}
			}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		x = e.getX();
		y = e.getY();
	}
			
	
	
}

class ChatPanel extends JPanel implements ActionListener, messageListener {
	
	private JTextArea view;
	private JTextField input;
	private ConnectionClient connectionClient;

	public ChatPanel () {
//		this.setPreferredSize(new Dimension(200, 500));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(Box.createRigidArea(new Dimension(0,10)));
		setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		view = new JTextArea();
		view.setEditable(false);
		input = new JTextField();
		input.addActionListener(this);
//		add(view, BorderLayout.CENTER);
		add(view);
		add(input);
	}
	
	public void setClient(ConnectionClient client) {
		this.connectionClient = client;
		client.setMessageListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
//		view.setText(view.getText()+"\n"+input.getText());
		if (connectionClient != null) {
			connectionClient.sendMessage(input.getText());
		}
		input.setText("");
	}

	@Override
	public void notifyMessage(String msg) {
		view.setText(view.getText()+"\n"+msg);
	}
}
