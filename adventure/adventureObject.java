
import java.util.*;

public class adventureObject {

	protected String object_class;
	public int id;
	public int state = 0;
	
	public boolean active;
	public boolean visible = true;

	protected ArrayList<String> desc_states = new ArrayList<String>();
	//protected ArrayList<String> name_states = new ArrayList<String>();
		
	protected String name;
	protected String desc;
	protected String shortDesc;
	

	
	public void setTag(String tag_name, String tag_value) {
		if (tag_name.equalsIgnoreCase("id")) {
			id = Integer.valueOf(tag_value);
		} else if (tag_name.equalsIgnoreCase("class")) {
			setObjectClass(tag_value);
		} 
	}
	
	public String getObjectClass() {
		// This method should be overridden in subclasses
		return object_class;
	}
	public void setObjectClass(String new_class) {
		object_class = new_class;
	}

	public String getName() {
		return name;
	}
	public String getDesc() {
		return desc;
	}
	public void setName(String new_name) {
		name = new_name;
	}
	public void setDesc(String new_desc) {
		desc = new_desc;
		// for lazy map-builders
		if (shortDesc == null) {
			shortDesc = desc;
		}
	}
	public void setNameState(int st, String state_name) {
		//if (st == 0) {setName(state_name);}
		//name_states.add(st, state_name);
		setName(state_name);
	}
	public void setDescState(int st, String state_desc) {
		if (st == 0) {setDesc(state_desc);}
		desc_states.add(st, state_desc);
	}

	public void advanceState() {
		if (desc_states.size() > (state+1)) {
			changeState(state+1);
		}
	}
	public void changeState(int new_state) {
		// TODO: error check
		//name = name_states.get(new_state);
		desc = desc_states.get(new_state);
		state = new_state;
		
	}
	public void setShortDesc(String new_desc) {
		shortDesc = new_desc;
	}
	public String getShortDesc() {
		return shortDesc;
	}
	
	


}
