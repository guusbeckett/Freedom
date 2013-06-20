/**
 * @author Guus Beckett & Jim van Abkoude
 */

package nl.reupload.freedompainter;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.Savepoint;
import java.util.Enumeration;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import nl.reupload.freedompainter.ConnectionClient.inviteListener;
import nl.reupload.freedompainter.ConnectionClient.messageListener;


public class Paint implements inviteListener{
	private PadDraw drawPad;
	private ConnectionServer server;
	protected ConnectionClient client;
	private previewPanel previewPanel;
	private ChatPanel chatPanel;
	private String jointPartner;

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
		
		chatPanel = new ChatPanel(this);
		drawPad = new PadDraw();
		previewPanel = new previewPanel(this);
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
				startServer();
			}
		});
		fileMenu.add(item);
		JMenuItem item2 = new JMenuItem("start client");
		item2.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (client != null) {
					disconnect();
				}
				connect(JOptionPane.showInputDialog(
					      "Vul het IP adres van de server in: "), JOptionPane.showInputDialog(
							      "Vul een username in: "));
			}
		});
		JMenuItem item3 = new JMenuItem("Save Image");
		item3.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileFilter(new PNGFilter());
				String fileName = null;
				int returnVal = chooser.showSaveDialog(drawPad);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					fileName = chooser.getSelectedFile().getAbsolutePath();
				}
				try {
					writeImage(getImage(), new File(fileName));
					chatPanel.notifyMessage("<system> image written to " + fileName);
				} catch (IOException e) {
					chatPanel.notifyMessage("<system> writing image failed, IOException occured");
				}
				
			}
		});
		fileMenu.add(item2);
		fileMenu.add(item3);
		//makes it so you can close
		frame.setVisible(true);
		//makes it so you can see it
	}
	
	public Image getImage() {
		return drawPad.getImage();
	}
	public void mergeImage(Image image) {
		drawPad.mergeImage(image, true);
	}
	
	public void writeImage(Image image, File file) throws IOException {
		BufferedImage dest = new BufferedImage(
			    image.getWidth(null), image.getHeight(null),
			    BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = dest.createGraphics();
		g2.drawImage(image, 0, 0, null);
		g2.dispose();
		ImageIO.write(dest, "png", file);
	}
	
	public boolean startServer() {
		if (server == null) {
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
						JOptionPane.showMessageDialog(drawPad, net, "server notify", JOptionPane.ERROR_MESSAGE);
					}
					JOptionPane.showMessageDialog(drawPad, net, "server notify", JOptionPane.INFORMATION_MESSAGE);
				    
				}
			} catch (SocketException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return true;
		}
		else return false;
	}
	public boolean stopServer() {
		if (server != null) {
			server.stop();
			server = null;
			return true;
		} else return false;
	}
	
	public void connect(String ip, String userName) {
		client = new ConnectionClient(this, ip, userName);
		client.setInviteListener(this);
		setClient(client);
	}
	
	public void disconnect() {
		try {
			client.disconnect();
			client = null;
			setClient(null);
		} catch (IOException e) {
			System.err.println("Client: Closing client returned IOException");
		}
	}
	
	public void setClient(ConnectionClient client) {
		chatPanel.setClient(client);
		drawPad.setClient(client);
		previewPanel.setClient(client);
	}

	@Override
	public void notifyInvite(String invite) {
		int option = JOptionPane.showConfirmDialog(drawPad, "you have an invite from " + invite + ", accept?");
//		chatPanel.notifyMessage("<system> invite code " + option + " from " + invite);
		switch (option) {
			case (0):
				jointPartner = invite;
				break;
			case (1):
				break;
			case (2):
				break;
		}
	}

	public String getJointPartner() {
		return jointPartner;
	}

	public void setJointPartner(String jointPartner) {
		this.jointPartner = jointPartner;
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
	
	public void mergeImage(Image image2, boolean callRepaint) {
		if (image != null) {
			Image oldImage = createImage(getSize().width-300, getSize().height);
			Graphics2D g2dOld = (Graphics2D) oldImage.getGraphics();
			g2dOld.drawImage(image, 0, 0, null);
			Graphics2D g2d = (Graphics2D) image.getGraphics();
			image2 = TransformWhiteToTransparency(image2);
			g2d.drawImage(oldImage, 0, 0, null);
			g2d.drawImage(image2, 0, 0, null);
		}
		if (callRepaint)
			repaint();
	//	this.image.getGraphics().drawImage(image, 1, 1, null);
	}
	
	  private Image TransformWhiteToTransparency(Image image) {
	  BufferedImage dest = new BufferedImage(
			    image.getWidth(null), image.getHeight(null),
			    BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = dest.createGraphics();
		g2.drawImage(image, 0, 0, null);
		g2.dispose();
		
	    RGBImageFilter filter = new RGBImageFilter()
	    {
	    public int markerRGB = Color.white.getRGB() | 0xFFFFFFFF;
	      public final int filterRGB(int x, int y, int rgb)
	      {
	    	  if ((rgb | 0xFF000000) == markerRGB)  
	            {  
	               // Mark the alpha bits as zero - transparent  
	               return 0x00FFFFFF & rgb;  
	            }  
	            else  
	            {  
	               // nothing to do  
	               return rgb;  
	            }  
	      }
	    };

	    ImageProducer ip = new FilteredImageSource(dest.getSource(), filter);
	    return Toolkit.getDefaultToolkit().createImage(ip);
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
		new Timer(1000, new ActionListener() {
			
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
//				mergeImage();
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
	private Paint paint;

	public previewPanel(Paint paint) {
		this.paint = paint;
		x=0;
		y=0;
		contextMenu = new JPopupMenu();
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	public void paintComponent(Graphics g){
		String partner = paint.getJointPartner();
		if (partner == null)
			partner = "";
		Graphics2D g2 = (Graphics2D)g;
		g.clearRect(0, 0, this.getHeight(), this.getWidth());
		if (servedObjects != null) {
			int y = 50;
			for (Object[] objs : servedObjects) {
				if (objs[1] != null) {
					if (partner.equals((String)objs[1])) {
						paint.mergeImage(((ImageIcon)objs[0]).getImage());
						g2.drawString((String)objs[1] + " (joint session partner)", 0, y-10);
					}
					else
						g2.drawString((String)objs[1], 0, y-10);
				}
				if (objs[0]!= null)
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
									paint.setJointPartner((String) objs[1]);
									
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
	private Paint paint;
	private static int i = 0;

	public ChatPanel (Paint paint) {
		this.paint = paint;
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
		notifyMessage("<system> Welcome to Freedom chat, want to know how to use the console, type /help");
	}
	
	public void setClient(ConnectionClient client) {
		this.connectionClient = client;
		client.setMessageListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		handleInput(input.getText());
		input.setText("");
	}

	@Override
	public void notifyMessage(String msg) {
		view.setText(view.getText()+"\n"+msg);
	}
	
	
	
	public void handleInput(String input) {
		if (input.startsWith("/")) {
			notifyMessage("<"+((connectionClient != null)?connectionClient.getUserName():"anonymous")+"> "+ input);
			if (input.startsWith("/nickname")) {
				if (connectionClient != null)
					connectionClient.sendString("uname " + input.split(" ")[1]);
				else
					notifyMessage("<system> cannot change nickname in offline mode");
			}
			else if (input.startsWith("/disconnect")) {
				if (connectionClient != null)
					try {
						connectionClient.disconnect();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				else
					notifyMessage("<system> cannot disconnect in offline mode");
			}
			else if (input.startsWith("/host")) {
				if (paint.startServer())
					notifyMessage("<system> starting server");
				else
					notifyMessage("<system> server could not start, maybe it is already running");
			}
			else if (input.startsWith("/stophost")) {
				if (paint.stopServer())
					notifyMessage("<system> stopping server");
				else
					notifyMessage("<system> server isn't running");
			}
			else if (input.startsWith("/connect")) {
				String[] items = input.split(" ");
				paint.connect((items.length>=2)?items[1]:null, (items.length>=3)?items[2]:null);
				if (items.length>=2)
					notifyMessage("<system> connecting to " + items[1] + ((items.length>=3)?" with username "+items[2]:""));
				else
					notifyMessage("<system> cannot connect to nothing");
			}
			else if (input.startsWith("/invite")) {
				if (input.split(" ").length >=2) {
					connectionClient.sendInvite(input.split(" ")[1]);
					paint.setJointPartner(input.split(" ")[1]);
				}
				else
					notifyMessage("<system> you must specify a username");
			}
			else if (input.startsWith("/help")) {
				notifyMessage(
						"<system> Welcome to Freedom help" + "\n" +
						"<system> " + "\n" +
						"<system> possible commands:" + "\n" +
						"<system> " + "\n" +
						"<system> /connect [ip] (username)\tconnect to a Freedom server at [ip] (with username as nickname)" + "\n" +
						"<system> /host\tstart hosting a Freedom server on your system" + "\n" +
						"<system> /stophost\tstop hosting a Freedom server on your system" + "\n" +
						"<system> /disconnect\tdisconnect from currently connected Freedom server" + "\n" +
						"<system> /nickname [nickname]\tchange nickname to [nickname]" + "\n" +
						"<system> /invite [username]\tinvite [username] to a joint session" + "\n" +
						"<system> /clear\tclear chat history" + "\n" +
						"<system> /pm [nickName] [msg]\tsend a private message containing [msg] to [nickName]" + "\n" +
						"<system> /saveimg [filename]\tsave current image to [filename]" + "\n" +
						"<system> /sendreverse [string]\tsends a reversed version of [string] as message" + "\n" +
						"<system>" + "\n" +
						"<system> Freedom by Guus Beckett and Jim van Abkoude. 2013"
						);
			}
			else if (input.startsWith("/clear")) {
				view.setText("");
			}
			
			else if (input.startsWith("/sendreverse")) {
				String string = input.split(" ")[1];
				if (connectionClient != null) {
					connectionClient.sendMessage(reverse(string));
				}else notifyMessage("<system> you are offline");
			}
			else if (input.startsWith("/saveimg")) {
				String[] data = input.split(" ");
				if (data.length >= 2)
					try {
						paint.writeImage(paint.getImage(), new File(data[1]));
						notifyMessage("<system> image written to " + data[1]);
					} catch (IOException e) {
						notifyMessage("<system> writing image failed, IOException occured");
					}
				else
					notifyMessage("<system> writing image failed, command syntax incorrect. see /help");
			}
			else if (input.startsWith("/pm")) {
				if (connectionClient != null) {
					connectionClient.sendMessage(input.substring(1));
				}
			}
			else
				notifyMessage("<system> " + input + " is not a command, try /help");
		}
		else
		{
			if (connectionClient != null) {
				connectionClient.sendMessage(input);
			}
			else notifyMessage("<system> you are offline");
		}
	}

		public static String reverse(String str) {
	        int localI = i++;
	        if ((null == str) || (str.length()  <= 1)) {
	            return str;
	        }
	        String reversed = reverse(str.substring(1)) + str.charAt(0);

	        return reversed;
	    }
}

class PNGFilter extends FileFilter {
	public boolean accept(File f) {
		return f.isDirectory() || f.getName().toLowerCase().endsWith(".png");
	}

	public String getDescription() {
		return ".png files";
	}
	
	

}
