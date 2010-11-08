import java.util.*;


public class adventurePlayer extends mobile {
	
	public int health;
	private int defaultDamage = 5;
	
	public adventurePlayer() {
		health = 100;
		toughness = 10;
		bodyItem = null;
		headItem = null;
	}
	
	public void takeDamage(int damage) {
		health = health - damage;
	}
	
	public String remove(String rem_item) {
		
		
		if (headItem != null) {
			if (headItem.getName().equalsIgnoreCase(rem_item)) {
				toughness -= headItem.armor;
				headItem = null;
				return String.format("You remove the %s.", rem_item);
				
			}
		}
		if (bodyItem != null) {
			if (bodyItem.getName().equalsIgnoreCase(rem_item)) {
				toughness -= bodyItem.armor;
				bodyItem = null;
				return String.format("You remove the %s.", rem_item);
			}
		}
		for (item i : heldItems) {
			if (i.getName().equalsIgnoreCase(rem_item)) {
				if (i.weapon) {
					attackDamage = defaultDamage;
				}
				heldItems.remove(i);
				// TODO check for weapon status and update damage;
				return String.format("You remove the %s.", rem_item);
			}
		}
		return "You aren't using that item.";
		
	}
	
	public ArrayList<String> lookSelf() {
		ArrayList<String> buffer = new ArrayList<String>();
		
		if (health < 10) {
			buffer.add("You are mortally wounded and will die soon without aid.");
		} else if (health < 25) {
			buffer.add("You are seriously wounded and should seek help.");
		} else if (health < 50) {
			buffer.add("You have several wounds that could use some attention.");
		} else if (health < 75) {
			buffer.add("You have a few wounds, but nothing serious.");
		} else {
			buffer.add("You are in good health.");
		}
		boolean naked = true;
		if (headItem != null) {
			buffer.add(String.format("You are wearing a %s on your head.", headItem.getName()));
			naked = false;
		}
		if (bodyItem != null) {
			buffer.add(String.format("You are wearing a %s on your body.", bodyItem.getName()));
			naked = false;
		}
		boolean left = false;
		for (item i : heldItems) {
			if (left) {
				buffer.add(String.format("You are holding a %s in your left hand.", i.getName()));
				
			} else {
				buffer.add(String.format("You are holding a %s in your right hand.", i.getName()));
				naked = false;
				left = true;
			}
		}
		if (naked) {
			buffer.add("You are naked! How embarassing!");
		}
		return buffer;
		
	}
	
	public String wear(String wear_item) {
		for (item i : items) {
			if (i.getName().equalsIgnoreCase(wear_item)) {
				if (i.wearableBody) {
					if (bodyItem == null) {
						bodyItem = i;
						toughness += bodyItem.armor;
						return String.format("You wear the %s.", wear_item);
					} else {
						return "You are already wearing something on your body.";
					}
				} else if (i.wearableHead) {
					if (headItem == null) {
						headItem = i;
						toughness += headItem.armor;
						return String.format("You put on the %s.", wear_item);
					} else {
						return "You are already wearing something on your head.";
					}
					
				} else {
					return "You cannot wear that.";
				}
			}
		}
		return "You cannot wear something you don't have.";
	}
	
	public String hold(String hold_item) {
		
	
		for (item i : items) {
			if (i.getName().equalsIgnoreCase(hold_item)) {
				if (i.holdable) {
					if (heldItems.size() < 2) {
						
						if (i.weapon) {
							if (attackDamage > defaultDamage) {
								return "You are already holding a weapon.";
							}
							attackDamage = i.damage;
						}
						heldItems.add(i);
						// Check for weapon status and update damage
						
						return String.format("You hold the %s.", hold_item);
					} else {
						return "Both your hands are full. You must remove something before holding that.";
					}
				} else {
					return "You cannot hold that.";
				}
			}
		}
		
		return "You can't wear something you don't have!";
		
	}
	
	

}
