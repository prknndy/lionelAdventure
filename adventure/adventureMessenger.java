import java.util.*;


public class adventureMessenger {
	
	public ArrayList<String> messages;
	

	public adventureMessenger() {
		messages = new ArrayList<String>();
	}
	public void addMessage(String message) {
		messages.add(message);
	}
	public ArrayList<String> getMessages() {
		return messages;
	}
}
