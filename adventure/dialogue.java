

public class dialogue {

	public int state;
	public int nextState;
	public String trigger;
	public String text;

	public void setText(String new_text) {
		text = new_text;
	}
	public String getText() {
		return text;
	}

}
