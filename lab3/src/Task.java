import java.util.ArrayDeque;
import java.util.Deque;

public class Task {
	int id=0, end_t=0, wait_t=0;
	int[] resource;
	boolean aborted = false, finish = false;	
	private Deque<Activity> que = new ArrayDeque<Activity>();
	
	public Task(int id, int resource_type) {
		this.id = id;
		resource = new int[resource_type];
	}
	public Task(Task t) {
		if(t==null)	
			 throw new IllegalArgumentException("Task cannot be null");
		this.id = t.id;
		this.que = new ArrayDeque<Activity>();
		this.resource = new int[t.resource.length];
		for(Activity a: t.que)
			this.que.addLast(new Activity(a.type, a.delay, a.resource_type, a.resource_num));
	}
	public void addActivity(Activity act) {
		que.addLast(act);
	}
	public Activity getRecentActivity() {
		return que.peekFirst();
	}
	public void removeRecentActivity() {
		que.removeFirst();
	}
	public void release(int t) {
		if(que.peekFirst().type != "terminate")
			System.out.println("Task "+id+" releases");
		else
			System.out.println("Task "+id+" releases and is finished (at "+(t+1)+")");
	}	
	public void printallAct() {
		System.out.println("Task: "+id+" ==================");
		for(Activity act:que)
			System.out.println(act.type+" "+act.delay+" "+act.resource_type+" "+act.resource_num);
	}
	public void printlastAct() {
		System.out.println(que.getLast().type+" "+id+" "+que.getLast().delay+" "+que.getLast().resource_type+" "+que.getLast().resource_num);
	}
	public String display() {
		return String.format("%2d%4d%6d%%",end_t,wait_t,Math.round(wait_t*100.0/end_t));
	}
}
