package nl.reupload.freedompainter;

import java.net.ConnectException;

public class Main {

	private static Paint paint;
	private static ConnectionClient client;

	public static void main(String[] args){
		paint = new Paint();
		client = new ConnectionClient();
	}
}
