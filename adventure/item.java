import java.util.*;

public class item extends adventureObject {

	ArrayList<action> actions;
	ArrayList<item> items;
	
	// Item stats
	public boolean stuck; // if true, object cannot be moved
	public boolean container; //if true, object can contain other objects
	public boolean wearableHead; //if true, object can be worn on the head
	public boolean wearableBody; //if true, object can be worn on the body
	public boolean holdable; //if true, object can be held
	public boolean light; //if true, object casts light
	public boolean weapon;
	
	public int damage;
	public int armor;
	
	
	public void setObjectClass(String new_class) {
			object_class = new_class;
			
			// determine other members to set based on class
			// 
			if (object_class.contains("moveable")) {
				stuck = false;
			} 
			if (object_class.contains("key")) {
				stuck = false;
			} 
			if (object_class.contains("container")) {
				container = true;
			} 
			if (object_class.contains("corpse")) {
				container = true;
				stuck = true;
			} 
			if (object_class.contains("weapon")) {
				holdable = true;
				stuck = false;
				weapon = true;
			}
			if (object_class.contains("clothes")) {
				wearableBody = true;
				stuck = false;
			}
			if (object_class.contains("hat")) {
				wearableHead = true;
				stuck = false;
			}
			if (object_class.contains("light")) {
				light = true;
			}
			
	}
	
	
	public item() {
		actions = new ArrayList<action>();
		items = new ArrayList<item>();
		// set item defaults
		stuck = true;
		wearableHead = false;
		wearableBody = false;
		holdable = false;
		container = false;
		light = false;
		weapon = false;
		
		damage = 0;
		armor = 0;
	}
	
	public void setTag(String tag_name, String tag_value) {
		
		if (tag_name.equalsIgnoreCase("damage")) {
			damage = Integer.valueOf(tag_value);
		} else if (tag_name.equalsIgnoreCase("armor")) {
			armor = Integer.valueOf(tag_value);
		} else {
			super.setTag(tag_name, tag_value);
		}
	}

	public void addAction(action new_action) {
		actions.add(new_action);
	}
	
	public void addItem(item new_item) {
		container = true;
		items.add(new_item);
	}
	

}
