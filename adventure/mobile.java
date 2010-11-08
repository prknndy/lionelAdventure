import java.util.*;

/**
 * the mobile class holds the information
 * for each type of mobile. individual mobiles
 * use the mobileInstance class
 * @author peter
 *
 */
public class mobile extends adventureObject {

	
	// Dialogue members
	public boolean talks;	
	private int dialogueState;
	private HashMap<String, dialogue> dialogues;
	
	// Mobile items
	protected ArrayList<item> items;
	public ArrayList<item> heldItems; // items held in hands/appendages
	public item headItem = null; // item worn on head
	public item bodyItem = null; // item worn on body
	
	// Mobile "stats"
	public int maxHealth = 1;
	public int toughness = 1;
	

	public int attackDamage = 5;
	public String attackString;
	
	public mobile() {
		dialogues = new HashMap<String,dialogue>();
		items = new ArrayList<item>();
		heldItems = new ArrayList<item>();
		dialogueState = 0;
		talks = false;
		
	}
	
	/**
	 * getCorpse - returns the corpse of a slain mobile
	 * TODO: Should this be in the mobileInstance class?
	 * @return the corpse item
	 */
	public item getCorpse() {
		item corpse = new item();
		corpse.setObjectClass("corpse");
		corpse.setName("corpse");
		corpse.setDesc(String.format("The corpse of a %s.", name));
		corpse.setShortDesc(String.format("The bloody corpse of a %s lies here.", name));
		for (item i : items) {
			corpse.addItem(i);
		}
		return corpse;
	}
	
	public ArrayList<item> getItems() {
		return items;
	}
	
	public void addItem(item new_item) {
		items.add(new_item);
	}
	
	public void setTag(String tag_name, String tag_value) {
		
		if (tag_name.equalsIgnoreCase("max_health")) {
			maxHealth = Integer.valueOf(tag_value);
		} else if (tag_name.equalsIgnoreCase("attack_damage")) {
			attackDamage = Integer.valueOf(tag_value);
		} else if (tag_name.equalsIgnoreCase("toughness")) {
			toughness = Integer.valueOf(tag_value);
		} else {
			super.setTag(tag_name, tag_value);
		}
	}
	
	public void addDialogue(dialogue new_dialogue) {
		dialogues.put(new_dialogue.trigger, new_dialogue);
		talks = true;
	}
	
	public boolean hasDialogue(String trigger) {
		return dialogues.containsKey(trigger);
	}
	
	public String getDialogue(String trigger) {
		if (hasDialogue(trigger)) {
			dialogue current_dialogue = dialogues.get(trigger);
			if (current_dialogue.state != dialogueState) {
				return null;
			} else {
				dialogueState = current_dialogue.nextState;
				return current_dialogue.text;
			}
		} else {
			return null;
		}
	}	



}
