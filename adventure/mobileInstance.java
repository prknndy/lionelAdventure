

public class mobileInstance {

	public int id;
	public int mob_id;
	
	public boolean initilized = false;
	
	public int health;
	
	public int takeDamage(int damage) {
		health = health - damage;
		if (health < 0) {health = 0;}
		return health;
	}
	
	

}
