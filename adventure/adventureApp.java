
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * adventureApp - threaded for each connection. handles input/output with user and communication with 
 * server
 * @author peter
 *
 */
public class adventureApp implements Runnable {

	private Socket connection;
	private BufferedReader input;
	private PrintWriter output;
	private adventureMessenger messenger;
	private adventureGame game;
	boolean isStarted;

	public adventureApp(Socket socket, adventureMessenger msg) {
		connection = socket;
		messenger = msg;
		isStarted = false;
		
		try {
			// Create input and output streams
			input = new BufferedReader( new InputStreamReader(connection.getInputStream()));
			output = new PrintWriter(connection.getOutputStream(), true);
			// Load game
			game = new adventureGame("zone1.xml", true);
		} catch (Exception e) {
			System.out.println("Error: " + e.toString());
		}
		
		
	}

	public void startGame() {
		isStarted = true;
	}

	private void display(ArrayList<String> display) {
		
		int max_width = 60;
		
		Iterator<String> display_iterator = display.iterator();
		while (display_iterator.hasNext()) {
			String printOutput = display_iterator.next();
			
			// The following code formats the output so it looks neater.
			// Changing max_width (above) will change the maximum width
			// of output lines.
			if (printOutput.length() < max_width) {
				output.println(printOutput);
			} else {
				String[] printSplitOutput = printOutput.split("[ \t\n\f\r]");
				int current_width = 0;
				
				for (String println : printSplitOutput) {
					
					if ((current_width + println.length()) > max_width) {
						output.println();
						current_width = 0;
					} 
					output.print(println + " ");
					current_width += (println.length()+1);
					
						
				}
				
				output.println();	
			}
		}
		
	}
	
	private void combatRun() {
		
		// Change to alter combat tick time in milliseconds
		int combat_sleep = 2000;
		
		while (game.inCombat) {
			
			String inputString = null;
			
			try {
				if (input.ready()) {
					inputString = input.readLine();
					if (inputString.equalsIgnoreCase("quit")) {
						output.println("You can't quit during combat!");
						inputString = null;
					} 
				}
			} catch (Exception e) {
				System.out.println("Error in combatRun" + e.toString());
			}
			
			ArrayList<String> displayBuffer = game.combatAdvance(inputString);
		
			display(displayBuffer);
			// Display in-game prompt
			
			output.print("*fighting*>");
			output.flush();
			try {
				Thread.sleep(combat_sleep);
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	public void run() {
		
		// TODO read news from file
		output.println("**********************************");
		output.println("** Welcome! Type start to begin **");
		output.println("**********************************");
		output.println("Latest News (11AUG10):");
		output.println("-The command parser has been improved.");
		output.println(" It should remove a little of the difficulty");
		output.println(" in figuring out the proper syntax.");
		output.println("-In game help has been updated to be current");
		output.println(" with the available command set.");
		output.println("**********************************");
		output.println("(start) or (help) for more options:");
		Boolean starting = true;
		Boolean running = false;
		output.print(">>");
		output.flush();
		
		while (starting == true) {
			try {
				if (input.ready()) {
					String inputString = input.readLine();
					if (inputString.equalsIgnoreCase("start")) {
						starting = false;
						running = true;
					} else if(inputString.equalsIgnoreCase("quit")) {
						starting = false;
					} else if(inputString.equalsIgnoreCase("help")) {
						output.println("Type start to begin.");
						output.println("Type quit to exit.");
						output.println("You can type help in game for");
						output.println("a list of commands.");
					} else {
						output.println("Not a valid option. Type help for assistance.");
					}
					output.print(">>");
					output.flush();
				}
			} catch(IOException e) {
				System.out.println("Error");
				starting = false;
			}
			if (Thread.interrupted()) {
				output.println("SERVER: SHUTTING DOWN NOW");
				running = false;
				starting = false;
			}
		
		}
		
		// Display intro to the zone
		if (running == true) {
			output.println();
			output.println("Beginning!");
			output.println();
			output.println(game.begin_text);
			output.println();
			output.print("Enter a command>");
			output.flush();
		}
	
		while (running == true) {
			
			try {
				if (input.ready()) {
					output.println();
					String inputString = input.readLine();
					if (inputString.equalsIgnoreCase("quit")) {
						running = false;
					} else {
						ArrayList<String> displayBuffer = game.advance(inputString);
						display(displayBuffer);
						if (game.inCombat) {
							combatRun();
						}
						
					}
					
					// Display in-game prompt
					output.print(">");
					output.flush();
	
				}
			} catch (IOException e) {
				System.out.println("Error");
				running = false;
			}
			if (Thread.interrupted()) {
				output.println("SERVER: SHUTTING DOWN NOW");
				running = false;
			}
			if (!game.inGame) {
				running = false;
				output.println();
				output.println();
				output.println("**You have died.**");
			}
			// Handle messages from server
			ArrayList<String> messages = messenger.getMessages();
			Iterator<String> messageIterator = messages.iterator();
			while (messageIterator.hasNext()) {
				String message = messageIterator.next();
				output.println();
				output.println(message);
				output.println();
				messageIterator.remove();
				
			}
		}
		try {
			output.close();
			input.close();
			connection.close();
		} catch (IOException e) {
			System.out.println("Error shutting down game thread");
		}
		

	}
}
