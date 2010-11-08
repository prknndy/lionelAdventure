
public class action extends adventureObject {

	public boolean used = false;
	public int requires = 0; //requires action owner is at state before enabling
	
	protected int target;
	
	protected String usage;
	protected boolean use = false;
	protected boolean useOnce = false;
	protected String type;

	public int getTarget() {
		return target;
	}
	public void setTarget(int new_target) {
		target = new_target;
	}
	public void setState(int new_state) {
		state = new_state;
	}
	public void setRequires(int new_requires) {
		requires = new_requires;
	}
	public int getRequires() {
		return requires;
	}

	public String getUsage() {
		return usage;
	}
	public void setUsage(String new_usage) {
		usage = new_usage;
		if (usage.equalsIgnoreCase("use_once")) {
			use = true;
			useOnce = true;
		} else if (usage.equalsIgnoreCase("use")) {
			use = true;
		}
	}
	public String getType() {
		return type;
	}
	public void setType(String new_type) {
		type = new_type;
	}
	

}
