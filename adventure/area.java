import java.util.*;

public class area extends adventureObject {

	// items in the room	
	public ArrayList<item> items;
	public ArrayList<item> exits;
	public ArrayList<action> actions;
	public ArrayList<mobileInstance> mobs;

	public area() {
		items = new ArrayList<item>();
		exits = new ArrayList<item>();
		actions = new ArrayList<action>();
		mobs = new ArrayList<mobileInstance>();
	}
	public void addMobile(mobileInstance mob) {
		mobs.add(mob);
	}
	public void addItem(item new_item) {
		items.add(new_item);
	}
	public void addExit(item exit) {
		exits.add(exit);
	}
	public void addAction(action new_action) {
		actions.add(new_action);
	}
		





}
