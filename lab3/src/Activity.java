
public class Activity {
	String type;
	int delay;
	int resource_type;
	int resource_num;

	public Activity(String type, int delay, int resource_type, int resource_num) {
		this.type = type;
		this.delay = delay;
		this.resource_type = resource_type;
		this.resource_num = resource_num;
	}
}
