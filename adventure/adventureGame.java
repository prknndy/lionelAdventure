
import java.util.*;
import java.io.*;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

// TODO: This class is getting way too long. Some of the
//		 tasks should be handed off to new classes.

/**
 * @author - Peter Kennedy
 */
public class adventureGame {
	
	// Containers for game world objects
	private ArrayList<area> areas;	
	private adventurePlayer player;
	private HashMap<Integer, adventureObject> global_objects;
	private HashMap<Integer, area> global_areas;
	private HashMap<Integer, mobile> global_mobs;
	private area current_area;
	// Start message
	String begin_text;
	// Maps for command processing
	private HashMap<String, String> abbreviations;
	private HashMap<String, String> generics;
	// Game status
	public boolean inCombat = false;
	public boolean inGame = true;
	private ArrayList<mobileInstance> combat_mobs;

	/**
	 * class constructor for adventureGame
	 *
	 * @param filename - name of .xml file that contains game data
	 * @param verbose - if true, prints verbose output to server console (not enabled)
	 * @throws XMLStreamException
	 */
	public adventureGame(String filename, boolean verbose) throws XMLStreamException {
		
		areas = new ArrayList<area>();
		player = new adventurePlayer();
		
		global_objects = new HashMap<Integer, adventureObject>();
		global_areas = new HashMap<Integer, area>();
		global_mobs = new HashMap<Integer, mobile>();
		
		combat_mobs = new ArrayList<mobileInstance>();
		
		abbreviations = new HashMap<String, String>();
		generics = new HashMap<String, String>();
		createMaps();
		
		System.out.println("Loading: " + filename);
		loadZone(filename);
		
		current_area = areas.get(0); // FIX THIS!

	}
	
	/**
	 * Adds strings to the abbreviations and generics maps. 
	 */
	public void createMaps() {
		// Directions
		abbreviations.put("n", "north");
		abbreviations.put("e","east");
		abbreviations.put("s","south");
		abbreviations.put("w", "west");
		abbreviations.put("u", "up");
		abbreviations.put("d","down");
		// Other commands
		abbreviations.put("l", "look");
		abbreviations.put("i", "inv");
		// Replace common synonyms
		abbreviations.put("kill", "attack");
		abbreviations.put("grab", "get");
		// Remove unneccisary words
		abbreviations.put("at", " ");
		abbreviations.put("from", " ");
		abbreviations.put("to", " ");
		abbreviations.put("the", " ");
		
		

		generics.put("smile", "You smile. Feeling better?");
		generics.put("frown", "You scrunch your nose and frown.");
	}

	public String render() {
		return current_area.getDesc();
	}
	
	/**
	 * Main function for advancing game play.
	 * 
	 * This function is called every time the player enters new text. It
	 * processes the command and generates output.
	 * @param inputString - player command
	 * @return - Array of strings for printing to the terminal.
	 */
	public ArrayList<String> advance(String inputString) {
		
		ArrayList<String> buffer = new ArrayList<String>();		
		handleCommand(inputString, buffer);
		return buffer;

	}
	
	private void handleCommand(String inputString, ArrayList<String> buffer) {
		
		
		// Replace abbreviations with proper commands
		// TODO: Add code that will work for compound commands
		inputString = inputString.trim();
		String[] inputStringWords = inputString.split(" ");
		String command = new String();
		for (String word : inputStringWords) {
			
			if (abbreviations.containsKey(word)) {
				word = abbreviations.get(word);
			}
			word = word.trim();
			if (!word.isEmpty()) {
				command = command.concat(word + " ");
			}
		}
		command = command.trim();
		
		
		if (command.startsWith("look")) {
			handleLook(command, buffer);
		} else if (command.startsWith("talk")) {
			handleTalk(command, buffer);
		} else if (command.startsWith("get")) {
			handleGet(command, buffer);
		} else if (command.startsWith("use")) {
			handleUse(command, buffer);
		} else if (command.startsWith("inv")) {
			handleInventory(command, buffer);
		} else if (command.startsWith("exits")) {
			handleExits(command, buffer);
		} else if (command.startsWith("help")) {
			handleHelp(command, buffer);
		} else if (command.startsWith("attack")) {
			handleAttack(command, buffer);
		} else if (command.startsWith("wear")) {
			try {
				String wear_item = command.split(" ", 2)[1];
				buffer.add(player.wear(wear_item));
			} catch (Exception e) {
				buffer.add("What did you want to wear?");
			}
		} else if (command.startsWith("hold")) {
			try {
				String hold_item = command.split(" ", 2)[1];
				buffer.add(player.hold(hold_item));
			} catch (Exception e) {
				buffer.add("What did you want to hold?");
			}
		} else if (command.startsWith("rem")) {
			try {
				String rem_item = command.split(" ", 2)[1];
				buffer.add(player.remove(rem_item));
			} catch (Exception e) {
				buffer.add("What did you want to remove?");
			}
		} else if (command.startsWith("drop")) {
			handleDrop(command, buffer);
			
		} else {
		
			action current_action = findAction(command);
			if (current_action != null) {
				buffer.add(doAction(current_action));
			} else {
		
				if (!handleGeneric(command, buffer)) {
					buffer.add("Ok, but that doesn't do much.");
					buffer.add("(Type help if you are having trouble)");
				}
			}
		}
	}

	/**
	 * combatAdvance - this routine is run instead of the regular advance 
	 * during combat. It handles the combat actions as well as any regular
	 * player commands.
	 * @param inputString
	 * @return
	 */
	public ArrayList<String> combatAdvance(String inputString) {
		// Handle input, if there is any
		ArrayList<String> buffer = new ArrayList<String>();		
		
		if (inputString != null) {
			handleCommand(inputString, buffer);
		}
		
		if (combat_mobs.size() < 1) {
			inCombat = false;
			return buffer;
		}
		
		// TODO: use traits to calculate who attacks first?
		// Handle players attack on mob
		mobileInstance targetMob = combat_mobs.get(0); // <-- fix this?
		mobile targetMobClass = global_mobs.get(targetMob.mob_id);
		int damage = calcDamage(player, targetMobClass);
		
		if (damage > 0) {
			buffer.add(String.format("You attack the %s.", targetMobClass.getName()));
		} else {
			buffer.add("You miss!");
		}
		
		if (targetMob.takeDamage(damage) < 1) {
			buffer.add(String.format("With a gasp, the %s dies.",targetMobClass.getName()));
			current_area.items.add(targetMobClass.getCorpse());
			combat_mobs.remove(targetMob);
			current_area.mobs.remove(targetMob);
			if (combat_mobs.size() < 1) {
				inCombat = false;
				return buffer;
			}
		} 
		
		// Handle mobs attack(s) on player
		for (mobileInstance mob : combat_mobs) {
			mobile this_mobClass = global_mobs.get(mob.mob_id);
			int this_damage = calcDamage(this_mobClass, player);
			player.takeDamage(this_damage);
			if (this_damage > 0) {
				buffer.add(String.format("*The %s hits you!*", this_mobClass.getName()));
			} else {
				buffer.add(String.format("The %s misses you!", this_mobClass.getName()));
			}
			if (player.health < 1) {
				buffer.add("Your vision fades to black.");
				buffer.add(" ");
				buffer.add(" ");
				inGame = false;
				inCombat = false;
			}
		}

		return buffer;
	}
	
	public int calcDamage(mobile attacker, mobile defender) {
		
		Random r = new Random();
		int rand = r.nextInt(6);
		
		int damage = attacker.attackDamage;
		switch(rand) {
			case 0: // attack fails
				damage = 0;
				break;
			case 1: // weak attack
			case 2:
				damage = damage - defender.toughness;
				break;
			case 3: // normal attack
			case 4:
				damage = damage - (defender.toughness/2);
				break;
			case 5: // strong attack
				break;
			case 6: //critical attack
				damage = (int) (damage * 1.5);
			
		}
		
		if (damage < 0) {damage = 0;}
		
		return damage;
	}
	/**
	 * handles the attack command
	 * @param command
	 * @param buffer
	 */
	public void handleAttack(String command, ArrayList<String>buffer) {
		
		if (inCombat) {
			buffer.add("You are already fighting.");
		}
		
		try {
			String target = command.split(" ",2)[1];
			mobileInstance mobTargetInst = findMob(target);
			if (mobTargetInst != null) {
				combat_mobs.add(mobTargetInst);
				inCombat = true;
				buffer.add("*You launch an attack!*");
			} else {
				buffer.add("You can't attack someone that doesn't exist!");
			}
		} catch (Exception e) {
			buffer.add("Who did you want to attack?");
		}
		
	}
	
	public String handleFlee(area target) {
		
		int flee_chance = 3;
		
		Random r = new Random();
		int rand = r.nextInt(10);
		
		// only allow players a certain change to flee to prevent it from
		// being an easy escape, odds can be changed by changing flee_chance
		if (rand > flee_chance) {
			inCombat = false;
			// TODO: make combat_mobs aggresive so player can't flee and reenter
			combat_mobs.clear();
			handleMove(target); // if there are aggresive mobs in next area, this will handle it
			return "You flee!";
		} else {
			return "You fail to flee!";
		}
		
	}
	
	/**
	 * handles the drop command
	 * @param command
	 * @param buffer
	 */
	public void handleDrop(String command, ArrayList<String>buffer) {
		// TODO: handle items that can't be dropped
		// rooms that can't be dropped into. Show item desc
		// when dropped into room so player can find.
		try {
			String dropped_item_name = command.split(" ",2)[1];
			for (item dropped_item : player.getItems()) {
				if (dropped_item.getName().equalsIgnoreCase(dropped_item_name)) {
					player.getItems().remove(dropped_item);
					current_area.items.add(dropped_item);
					buffer.add(String.format("You dropped the %s.",dropped_item_name));
					return;
				}
			}
			buffer.add("You do not have that item.");
		} catch(Exception e) {
			buffer.add("What did you want to drop?");
		}
		
	}
	
	/**
	 * handles the exits command
	 * @param command -player command
	 * @param buffer -buffer for storing output
	 */
	public void handleExits(String command, ArrayList<String> buffer) {
		
		buffer.add("The following exits are visible: ");
		for (item exit : current_area.exits) {
			for (action exit_action : exit.actions) {
				if (exit_action.type.equalsIgnoreCase("exit")) {
					buffer.add(" -" + exit_action.name);
				}
			}
		}
	}
	
	/**
	 * handles generic commands (commands that don't do anything)
	 * @param command -player command string
	 * @param buffer -buffer for storing output
	 * @return
	 */
	public boolean handleGeneric(String command, ArrayList<String> buffer) {
		
		if (generics.containsKey(command)) {
			buffer.add(generics.get(command));
			return true;
		}
		return false;
	}
	
	/**
	 * handles the use command
	 * @param command 
	 * @param buffer
	 */
	public void handleUse(String command, ArrayList<String> buffer) {
		// TODO: Check to make sure target of action is in the room before allowing action
		try {
			String target = command.split(" ", 2)[1];
			item itemTarget = findItem(target);
			boolean foundAction = false;
			if (itemTarget != null) {
				for (action a : itemTarget.actions) {
					if (a.use) {
						buffer.add(doAction(a));
						foundAction = true;
					}
				}
				if (!foundAction) {
					buffer.add("You cannot use that.");
				}
			} else {
				buffer.add("What did you want to use?");
			}
		} catch (Exception e) {
			buffer.add("That is not how you do that.");
		}
		
		
	}
	
	/**
	 * handles the help command
	 * @param command
	 * @param buffer
	 */
	public void handleHelp(String command, ArrayList<String> buffer) {
		// TODO add help for diff commands
		buffer.add("********************************");
		buffer.add("Basic game commands: ");
		buffer.add("*Looking and exploring*");
		buffer.add("	look (at _target_)");
		buffer.add("	look in _container_");
		buffer.add("	look self");
		buffer.add("	exits");
		buffer.add("*Items*");
		buffer.add("	get _item_ (_container_)");
		buffer.add("	drop _item_");
		buffer.add("	use _item_ (_target_)");
		buffer.add("	wear _item_");
		buffer.add("	hold _item_");
		buffer.add("	remove _item_");
		buffer.add("	inv(entory)");
		buffer.add("*Interacting with others*");
		buffer.add("	talk _someone_ (_speech_)");
		buffer.add("	attack _somone_");
		buffer.add("*Other options*");
		buffer.add("	quit *exits from the game*");
		buffer.add("	help *prints this message*");
		buffer.add("*******************************");
		buffer.add("Move between areas by typing a");
		buffer.add("direction. Most rooms have exits");
		buffer.add("in a cardinal direction:");
		buffer.add("north, east, south, west, up,");
		buffer.add("down.");
		buffer.add("Other times they might be more");
		buffer.add("specific (ex. enter cave, climb");
		buffer.add("ladder). Typing exits will display");
		buffer.add("all visible exits. Some exits may");
		buffer.add("be hidden.");
		buffer.add("***");
		buffer.add("Sometimes there are special actions");
		buffer.add("in a room. You might get hints by");
		buffer.add("looking at something.");
		buffer.add("***");
		buffer.add("Actions, dialogue and using items can");
		buffer.add("trigger changes in your surrondings");
		buffer.add("and open up new opportunities.");
		buffer.add("You might need to do one of these");
		buffer.add("before you can proceed with your goals.");
		buffer.add("***");
		buffer.add("Commands are NOT case sensitive.");
		buffer.add("If you're having trouble, try a");
		buffer.add("a similar word.");
		buffer.add("Many commands and cardinal directions");
		buffer.add("can be abbreviated with the letter");
		buffer.add("they start with.");
		buffer.add("********************************");
		
		
	}
	
	/**
	 * handles the inventory command
	 * @param command
	 * @param buffer
	 */
	public void handleInventory(String command, ArrayList<String> buffer) {
		buffer.add("You currently have: ");
		boolean has_something = false;
		for (item inv_item : player.getItems()) {
			buffer.add("  -" + inv_item.getDesc());
			has_something = true;
		}
		if (!has_something) {
			buffer.add("Nothing. But possesions aren't everything.");
		}
	}
	
	/**
	 * handles the look, look at and look in commands
	 * @param command
	 * @param buffer
	 */
	public void handleLook(String command, ArrayList<String> buffer) {
		
		if (command.equalsIgnoreCase("look")) {
			// Just reprint area
			buffer.add("You look around.");
			buffer.add(" ");
			buffer.add(current_area.getName());
			buffer.add("~");
			buffer.add(current_area.getDesc());
			for (item current_item : current_area.items) {
				if (current_item.visible) {
					buffer.add(current_item.getShortDesc());
				}
			}
			for (mobileInstance mob : current_area.mobs) {
				buffer.add(global_mobs.get(mob.mob_id).getShortDesc());
			}
		} else if (command.startsWith("look in")) {
			try {
				String target = command.split(" ", 3)[2];
				item itemTarget = findItem(target);
				if (itemTarget != null) {
					if (itemTarget.container) {
						buffer.add(String.format("You look in the %s.", itemTarget.getName()));
						buffer.add(" ");
						buffer.add("You see:");
						for (item container_item : itemTarget.items) {
							buffer.add(container_item.getShortDesc());
						}
					} else {
						buffer.add("You cannot look in that!");
					}
				} else {
					buffer.add("You can't look in something that doesn't exist!");
				}
			} catch (Exception e) {
					buffer.add("Look in what?");
			}
				
		} else {
			String target;
			try {
				if (command.equalsIgnoreCase("look at")) {
					target = command.split(" ",3)[2];
				} else {
					target = command.split(" ", 2)[1];
				}
			} catch (Exception e) {
				buffer.add("What did you want to look at?");
				return;
			}
			if (target.equalsIgnoreCase("self")) {
				buffer.addAll(player.lookSelf());
				return;
			}
			item itemTarget = findItem(target);
			if (itemTarget != null) {
				buffer.add(String.format("You look at the %s.", itemTarget.getName()));
				buffer.add(" ");
				buffer.add(itemTarget.getDesc());
			} else {
				mobileInstance mobTarget = findMob(target);
				if (mobTarget != null) {
					mobile currentMob = global_mobs.get(mobTarget.mob_id);
					buffer.add(String.format("You look at the %s.", currentMob.getName()));
					buffer.add(" ");
					buffer.add(currentMob.getDesc());
					
				} else {
					buffer.add("You don't see that here.");
				}
			}
		}
		
	}
	/**
	 * handles the get command
	 * @param command
	 * @param buffer
	 */
	public void handleGet(String command, ArrayList<String> buffer) {
		// Check item container status
		String[] stringArray = command.split(" ", 3);
		String target_container = null;
		String target_item = null;
		try {
			target_item = stringArray[1];
		} catch (Exception e) {
			buffer.add("Get what?");
			return;
		}
		try {
			target_container = stringArray[2];
			item container_item = findItem(target_container);
			// get from container
			if (container_item != null) {
				for (item our_item : container_item.items) {
					if (our_item.getName().equalsIgnoreCase(target_item)) {
						if (our_item.stuck == false) {
							// remove from container
							container_item.items.remove(our_item);
							// add to inventory
							player.addItem(our_item);
							buffer.add(String.format("You take the %s.", target_item));
						} else {
							buffer.add("You can't take that!");
						}
						return;
					}
				}
				buffer.add(String.format("There is no %s in that.", target_item));
				
			} else {
				buffer.add("You can't get something from something that doesn't exist!");
			}
			
		} catch (Exception e) {
			// get from room
			item our_item = findItem(target_item);
			if (our_item != null) {
				if (our_item.stuck == false) {
					// remove from area
					current_area.items.remove(our_item);
					// add to inventory
					player.addItem(our_item);
					buffer.add(String.format("You take the %s.", target_item));
				} else {
					buffer.add("You can't take that!");
				}
			} else {
				buffer.add("You can't take something that doesn't exist!");
			}

		}
		
		
	}
	
	/**
	 * handles the talk command
	 * @param command
	 * @param buffer
	 */
	public void handleTalk(String command, ArrayList<String> buffer) {
		String[] stringArray = command.split(" ", 3);
		String target = null;
		String trigger = null;
		try {
			target = stringArray[1];
		} catch (Exception e) {
			
		}
		try {
			trigger = stringArray[2];
		} catch (Exception e) {

		}
		mobile mob = findMobile(target);
		if (mob != null) {
			buffer.add(String.format("You talk to the %s.",mob.getName()));
			String response;
			if (trigger == null) {
				trigger = "start";
			} 
			response = mob.getDialogue(trigger);
			if (response != null) {
				buffer.add(" ");
				buffer.add(String.format("The %s responds: \"",mob.getName())+ response + "\"");
			} else {
				buffer.add(" ");
				buffer.add(String.format("The %s doesn't response to that.",mob.getName()));
			}
		} else {
			buffer.add("You can't talk to someone that doesn't exist!");
		}
		
	}
	
	public String handleMove(area target)
	{
		
		current_area = target;
		
		// check mobs in new area and initilize them if they are new
		// TODO automatic dialog, aggresive mobs, etc
		for (mobileInstance mob : current_area.mobs) {
			if (!mob.initilized) {
				mob.health = global_mobs.get(mob.mob_id).maxHealth;
			}
		}
		
		return "You move in that direction.";
	}
	
	/**
	 * executes action
	 * @param current_action - action to be executed
	 * @return text for output
	 */
	public String doAction(action current_action) {
		// TODO CHECK USAGE
		
		if((current_action.useOnce) && (current_action.used)) {
			return "You can't do that anymore.";
		}
		
		try {
			
			if (current_action.state != current_action.getRequires()) {
				return "You can't do that right now. Maybe you need to do something else first?";
			}
			String action_type = current_action.getType(); 
			if (action_type.equalsIgnoreCase("state_advance")) {
				adventureObject target = global_objects.get(current_action.getTarget());
				target.advanceState();
				current_action.used = true;
				return "Ok.";
			} else if (action_type.equalsIgnoreCase("exit")) {
				area target = global_areas.get(current_action.getTarget());
				current_action.used = true;
				
				if (inCombat) {
					return handleFlee(target);
				}
				//current_area = target;

				
				return handleMove(target);
			}

		} catch (Exception e) {
			// whatever
		}
		return "You can't do that right now. Maybe you need to do something else first?";
	}

	// TODO: not return actions that must be used?
	/**
	 * finds an action give it's name
	 * returns action
	 */
	public action findAction(String name) {
		Iterator<action> area_actions = current_area.actions.iterator();
		while(area_actions.hasNext()) {
			action current_action = area_actions.next();
			if (current_action.getName().equalsIgnoreCase(name)) {
				current_action.setState(current_area.state);
				return current_action;
			}
		}
		Iterator<item> exit_iterator = current_area.exits.iterator();
		while(exit_iterator.hasNext()) {
			item current_item = exit_iterator.next();
			Iterator<action> item_actions = current_item.actions.iterator();
			while(item_actions.hasNext()) {
				action current_action = item_actions.next();
				if (current_action.getName().equalsIgnoreCase(name)) {
					current_action.setState(current_item.state);
					return current_action;
				}
			}
		}
		Iterator<item> item_iterator = current_area.items.iterator();
		while(item_iterator.hasNext()) {
			item current_item = item_iterator.next();
			Iterator<action> item_actions = current_item.actions.iterator();
			while(item_actions.hasNext()) {
				action current_action = item_actions.next();
				if (current_action.getName().equalsIgnoreCase(name)) {
					current_action.setState(current_item.state);
					return current_action;
				}
			}
		}
		return null;
	
	}
	
	/**
	 * finds an item given it's name
	 * @param name string of item name
	 * @return the item found, or null if none found
	 */
	public item findItem(String name) {
		Iterator<item> items = current_area.items.iterator();
		while(items.hasNext()) {
			item current_item = items.next();
			if (current_item.getName().equalsIgnoreCase(name)) {
				return current_item;
			}
		}
		Iterator<item> exits = current_area.exits.iterator();
		while(exits.hasNext()) {
				item current_item = exits.next();
				if (current_item.getName().equalsIgnoreCase(name)) {
					return current_item;
				}
		}
		for (item current_item : player.getItems()) {
			if (current_item.getName().equalsIgnoreCase(name)) {
				return current_item;
			}
		}

		return null;
		 
	}
	/**
	 * find's a mobile given it's name
	 * @param name String of mobile
	 * @return mobile if found or null
	 */
	public mobile findMobile(String name) {
		mobileInstance mobInstance = findMob(name);
		if (mobInstance != null) {
			return global_mobs.get(mobInstance.mob_id);
		}
		return null;
	}
	/**
	 * finds a mobile instance given a name
	 * @param name 
	 * @return mobile instance if found otherwise null
	 */
	public mobileInstance findMob(String name) {
		for (mobileInstance mob : current_area.mobs) {
			try {
				String mname = global_mobs.get(mob.mob_id).getName();
				if (name.equalsIgnoreCase(mname)) {
					return mob;
				}
			} catch (Exception e) {
			
			}
		}
		return null;
	}

	// should really put loading methods into new class to clean up code
	
	/**
	 * loads a zone file
	 */
	public void loadZone(String filename) throws XMLStreamException {
		
		// Create event factory	
		XMLInputFactory factory = XMLInputFactory.newInstance();
		// Create event reader
		try {
			
			XMLEventReader r = factory.createXMLEventReader(filename, new FileInputStream(filename));
			
			;
			// Create event iterator
			while (r.hasNext()) {
				XMLEvent event = r.nextEvent();
				if (event.isStartElement()) {
					
					StartElement startElement = event.asStartElement();
					if (startElement.getName().getLocalPart().equalsIgnoreCase("area")) {
					
						area new_area = loadArea(r, startElement);
						areas.add(new_area);
						global_objects.put(new_area.id ,new_area);
						global_areas.put(new_area.id, new_area);
					} else if (startElement.getName().getLocalPart().equalsIgnoreCase("mobile")) {
						mobile new_mob = loadMobile(r, startElement);
						global_mobs.put(new_mob.id, new_mob);
					} else if (startElement.getName().getLocalPart().equalsIgnoreCase("begin")) {
						XMLEvent begin_event = r.nextEvent();
						begin_text = begin_event.asCharacters().getData();
					}
				}
			}

		} catch (IOException e) {
			System.out.println("Error!:" + e.toString());
			return;
		}
	}
	
	/**
	 * loads content within an "area" tag
	 * @param r 
	 * @param sE
	 * @return the area loaded
	 * @throws XMLStreamException
	 */
	public area loadArea(XMLEventReader r, StartElement sE) throws XMLStreamException {
		boolean in_area_tag = true;
		area new_area = new area();
		loadTagData(sE, new_area);
		while ((in_area_tag) && (r.hasNext())) {
			XMLEvent event = r.nextEvent();
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				String type = startElement.getName().getLocalPart();
			
				if (type.equalsIgnoreCase("item")) {
					item new_item = loadItem(r, startElement, "item");
					new_area.addItem(new_item);
					global_objects.put(new_item.id ,new_item);
					
				} else if (type.equalsIgnoreCase("exit")) {
					item exit = loadItem(r, startElement, "exit");
					new_area.addExit(exit);
					global_objects.put(exit.id, exit);
				} else if (type.equalsIgnoreCase("action")) {
					action new_action = loadAction(r, startElement);
					new_area.addAction(new_action);
				} else if (type.equalsIgnoreCase("mob_instance")) {
					mobileInstance mob = loadMobileInstance(r, startElement);
					new_area.addMobile(mob);
				} else {
					loadAttribute(r, startElement, new_area);
				}

			} else if (event.isEndElement()) {
				EndElement endElement = event.asEndElement();
				if (endElement.getName().getLocalPart().equalsIgnoreCase("area")) {
					in_area_tag = false;
				}
			}

		}
		return new_area;


	}
	public mobile loadMobile(XMLEventReader r, StartElement sE) throws XMLStreamException {
		boolean in_tag = true;
		mobile new_mob = new mobile();
		loadTagData(sE, new_mob);
		while ((in_tag) && (r.hasNext())) {
			XMLEvent event = r.nextEvent();
			if (event.isStartElement()) {

				StartElement startElement = event.asStartElement();
				String type = startElement.getName().getLocalPart();

				if (type.equalsIgnoreCase("dialogue")) {
					dialogue new_dialogue = loadDialogue(r, startElement);
					new_mob.addDialogue(new_dialogue);
				} else {
					loadAttribute(r, startElement, new_mob);
				}
				
			} else if (event.isEndElement()) {
				EndElement endElement = event.asEndElement();
				
				if (endElement.getName().getLocalPart().equalsIgnoreCase("mobile")) {
					
					in_tag = false;
				}
			} 

		}
		return new_mob;

	}

	public dialogue loadDialogue(XMLEventReader r, StartElement startElement) throws XMLStreamException {

		dialogue new_dialogue = new dialogue();
		// load tag data
		Iterator<Attribute> attributes = startElement.getAttributes();
		while (attributes.hasNext()) {
			Attribute a = attributes.next();
			String a_name = a.getName().toString();
			if (a_name.equalsIgnoreCase("state")) {
				String value = a.getValue();
				new_dialogue.state = Integer.valueOf(value);
			} else if (a_name.equalsIgnoreCase("next_state")) {
				String value = a.getValue();
				new_dialogue.nextState = Integer.valueOf(value);
			} else if (a_name.equalsIgnoreCase("trigger")) {
				new_dialogue.trigger = a.getValue();
			} 	
		}
		XMLEvent event = r.nextEvent();
		new_dialogue.setText(event.asCharacters().getData());
		return new_dialogue;
		
	}

	public mobileInstance loadMobileInstance(XMLEventReader r, StartElement sE) throws XMLStreamException {
		mobileInstance mob = new mobileInstance();
		Iterator<Attribute> attributes = sE.getAttributes();
		while (attributes.hasNext()) {
			Attribute a = attributes.next();
			String a_name = a.getName().toString();
			if (a_name.equalsIgnoreCase("id")) {
				String value = a.getValue();
				mob.id = Integer.valueOf(value);
			} else if (a_name.equalsIgnoreCase("mob")) {
				String value = a.getValue();
				mob.mob_id = Integer.valueOf(value);
			}
		}
		return mob;
	}
	public item loadItem(XMLEventReader r, StartElement sE, String tag) throws XMLStreamException {
		boolean in_tag = true;
		item new_item = new item();
		loadTagData(sE, new_item);
		while ((in_tag) && (r.hasNext())) {
			XMLEvent event = r.nextEvent();
			if (event.isStartElement()) {

				StartElement startElement = event.asStartElement();
				String type = startElement.getName().getLocalPart();

				if (type.equalsIgnoreCase("action")) {
					action new_action = loadAction(r, startElement);
					new_item.addAction(new_action);
				} else if (type.equalsIgnoreCase("item")) {
					item new_container_item = loadItem(r, startElement,"item");
					new_item.addItem(new_container_item);
				} else {
					loadAttribute(r, startElement, new_item);
				}
				
			} else if (event.isEndElement()) {
				EndElement endElement = event.asEndElement();
				
				if (endElement.getName().getLocalPart().equalsIgnoreCase(tag)) {
					
					in_tag = false;
				}
			} 

		}
		return new_item;
	
	}
	public adventureObject loadAdventureObject(XMLEventReader r, StartElement sE, String tag) throws XMLStreamException {

		boolean in_tag = true;
		adventureObject new_item = new item();
		loadTagData(sE, new_item);
		while ((in_tag) && (r.hasNext())) {
			XMLEvent event = r.nextEvent();
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				loadAttribute(r, startElement, new_item);
				
			} else if (event.isEndElement()) {
				EndElement endElement = event.asEndElement();
				if (endElement.getName().getLocalPart().equalsIgnoreCase(tag)) {
					in_tag = false;
				}
			}

		}
		return new_item;

	}
	public void loadTagData(StartElement startElement, adventureObject object) throws XMLStreamException {

		Iterator<Attribute> attributes = startElement.getAttributes();
		while (attributes.hasNext()) {
			Attribute a = attributes.next();
			String a_name = a.getName().toString();
			String a_value = a.getValue();
			object.setTag(a_name, a_value);
		}
		

	}

	public action loadAction(XMLEventReader r, StartElement startElement) throws XMLStreamException {

		action new_action = new action();
		// load tag data
		Iterator<Attribute> attributes = startElement.getAttributes();
		while (attributes.hasNext()) {
			Attribute a = attributes.next();
			String a_name = a.getName().toString();
			if (a_name.equalsIgnoreCase("id")) {
				String value = a.getValue();
				new_action.id = Integer.valueOf(value);
			} else if (a_name.equalsIgnoreCase("class")) {
				new_action.setObjectClass(a.getValue());
			} else if (a_name.equalsIgnoreCase("target")) {
				String value = a.getValue();
				new_action.setTarget( Integer.valueOf(value));
			} else if (a_name.equalsIgnoreCase("usage")) {
				new_action.setUsage(a.getValue());
			} else if (a_name.equalsIgnoreCase("type")) {
				new_action.setType(a.getValue());
			} else if (a_name.equalsIgnoreCase("requires")) {
				String value = a.getValue();
				new_action.setRequires(Integer.valueOf(value));
			}
		}
		XMLEvent event = r.nextEvent();
		new_action.setName(event.asCharacters().getData());
		return new_action;
		
	}
	// Poor choice of name for this function, should be called loadMembers
	public void loadAttribute(XMLEventReader r, StartElement startElement, adventureObject object) throws XMLStreamException {

		int state = 0;
		String name = startElement.getName().getLocalPart();
		Iterator<Attribute> attributes = startElement.getAttributes();
		while (attributes.hasNext()) {
			Attribute a = attributes.next();
			String a_name = a.getName().toString();
			if (a_name.equalsIgnoreCase("state")) {
				String value = a.getValue();
				state = Integer.valueOf(value);
			}
		}
		if (name.equalsIgnoreCase("name")) {
			XMLEvent event = r.nextEvent();
			object.setNameState(state, event.asCharacters().getData());
		} else if (name.equalsIgnoreCase("desc")) {
			XMLEvent event = r.nextEvent();
			object.setDescState(state, event.asCharacters().getData());
		} else if (name.equalsIgnoreCase("short_desc")) {
			XMLEvent event = r.nextEvent();
			object.setShortDesc(event.asCharacters().getData());
		}
		

	}


}
