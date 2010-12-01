
import java.io.*;
import java.util.*;
import java.net.*;



/**
 * Class implementing the server. Main entry point for the application.
 * @author peter kennedy
 *
 */
public class adventureServer implements Runnable {
	
	ArrayList<String> messages;
	
	public adventureServer(ArrayList<String> msgs) {
		messages = msgs;
	}
	
	/**
	 * Main server method. Initializes the socket and listens for connections, passing
	 * off the socket and generating a new one as needed. Also handles messaging between
	 * the server console and the connections. 
	 */
	public void run() {
		
		// Init server socket
		ServerSocket server = null;
		ArrayList<Thread> appList = new ArrayList<Thread>();
		ArrayList<adventureMessenger> msgList = new ArrayList<adventureMessenger>();

		try {
			server = new ServerSocket(2080);
		} catch (IOException e) {
			System.out.println("Could not open socket...exiting");
			System.exit(-1);
		}
		System.out.println("Server started.");
		try {
			// Set a timeout to loop every second to check for messages/interrupts
			server.setSoTimeout(1000);
		} catch (SocketException e) {
			System.out.println("Could not set socket timeout");
		}
		Boolean serverStatus = true;
		while (serverStatus == true) {
			try {
				// Wait for connection
				Socket connection = server.accept();
				System.out.println("Connection.");
				// Start app instance
				adventureMessenger messenger = new adventureMessenger();
				Thread t = new Thread(new adventureApp(connection, messenger));
				t.start();
				// Add app and messenger to queue
				appList.add(t);
				msgList.add(messenger);

			} catch (IOException e) {
				// Do nothing
			} 
			if (Thread.interrupted()) {
				serverStatus = false;
			}
			Iterator<String> messageIterator = messages.iterator();
			while(messageIterator.hasNext()) {
				String message = messageIterator.next();
				for (adventureMessenger messenger : msgList) {
					messenger.addMessage(message);
				}
				messageIterator.remove();
			}

		}

		System.out.println("Attempting to close connections.");
		for (Thread app : appList) {
			app.interrupt();	
		}

		System.out.println("Shutting down server.");
		try {server.close();} catch (IOException e) { System.out.println("Failed to close socket"); }
		System.out.println("Exiting");
		
	}

	private static void printHelp() {
		System.out.println("Commands: ");
		System.out.println("quit-> shuts down server immediately");
		System.out.println("shutdown %m-> shuts down server in %m seconds");
		System.out.println("message %s-> broadcasts %s to all users");
		System.out.println("help-> prints this list of commands");

	}

	public static void main(String args[]) {

		Boolean serverStatus = true;
		System.out.println("KTK Adventure Server v0.1");
		System.out.println("By Peter Kennedy");
		System.out.println("Type help for a list of commands");
		System.out.println();
		System.out.println("Starting server...");

		// Initialize messages
		ArrayList<String> messages = new ArrayList<String>();

		// Initialize server thread
		Thread t = new Thread(new adventureServer(messages));
		t.start();

		// Capture input stream
		BufferedReader cin = new BufferedReader(new InputStreamReader(System.in));

		// Listen for commands
		while (serverStatus) {
			try {
				System.out.print(">>?");
				String inputString = cin.readLine();
				String command = inputString.substring(0, inputString.length());
				// TODO: make sure commands have proper syntax to avoid errors
				if (command.equalsIgnoreCase("quit")) { 
					serverStatus = false;
				} else if (command.equalsIgnoreCase("help")) {
					printHelp();
				} else if (command.startsWith("shutdown")) {
					int minutes = Integer.valueOf(command.split(" ",2)[1]);
					System.out.println(String.format("Shutting down in %d seconds.", minutes));
					messages.add(String.format("SERVER: Shutting down in %d seconds.", minutes)); 
					// TODO: for release change this to minutes?
					serverStatus = false;	
					try {
						Thread.sleep(minutes*1000);
					} catch (InterruptedException e) {
						System.out.println("Unable to sleep, shutting down now.");
					}
									
				} else if (command.startsWith("message")) {
					String message = command.split(" ",2)[1];
					messages.add(message);
					System.out.println("Message: " + message);
				} else {
					System.out.println("Unknown command: " + command + ". Type help for a list of commands.");
				}
				
			} catch (IOException e) {
				System.out.println("Error");
			}
		}
		System.out.println("Shutting down NOW!");
		t.interrupt();
		
		
	}

		
}

