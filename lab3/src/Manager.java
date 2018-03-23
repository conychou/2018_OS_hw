import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/* 
 * General Manager Superclass:
 *		Implement general constructor() and run() function 
 *		Subclasses need to implement their own checkAllocation(), deadlockCheck(), initial();
 */

public abstract class Manager {
	Task[] tasks;
	int[] total_resource;
	int[] avail_resource;
	int MAX_TASK, MAX_RESOURCE;

	
	public Manager(Task[] ts, int[] resources) {
		MAX_TASK = ts.length-1;
		MAX_RESOURCE = resources.length-1;
		tasks = new Task[MAX_TASK+1];
		for(int index=1; index<=MAX_TASK; index++) 
			tasks[index] = new Task(ts[index]);
		total_resource = resources;
		avail_resource = Arrays.copyOf(resources, resources.length);
	}
	public void run() {
		Deque<Task> pending_que = new ArrayDeque<Task>();
		int cur_t=0;
		int all_finish = 0;

		while(all_finish < MAX_TASK) {	
			Logger.log("During  "+cur_t+" - "+(cur_t+1));
			Deque<Task> new_pending = new ArrayDeque<Task>();
			int[] new_avail_resource = new int[MAX_RESOURCE+1];
			
			/* Check blocked tasks */
			if(!pending_que.isEmpty())
				Logger.log("First check blocked tasks.");
			for(Task task: pending_que) {
				Activity act = task.getRecentActivity();
				if(checkAllocation(task.id, act, cur_t)) {
					/*	Unblock and completes its request */
					avail_resource[act.resource_type] -= act.resource_num;
					task.resource[act.resource_type] += act.resource_num;
					Logger.log("	  task "+task.id+" completes its request (resource["+act.resource_type+"]: requested = "+act.resource_num+", remaining = "+avail_resource[act.resource_type]+")");
					task.removeRecentActivity();
				} else {
					/* Still till blocked */
					Logger.log("	  task "+task.id+" still blocked");
					task.wait_t++;
					new_pending.add(task);
				}
			}
			
			/* Handling task's Activity */
			for(int index=1; index <= MAX_TASK; index++) {
				Task task = tasks[index];
				if(!task.aborted && !task.finish && !pending_que.contains(task)) {
					Activity act = task.getRecentActivity();
					/*	If Activity having delay time, delay it first	*/
					if(act.delay > 0) {
						Logger.log("Task "+task.id+" in "+act.type+" delayed "+act.delay+".");
						act.delay--;		
						continue;
					}else{
						/*	Handling initiate activity 	*/
						if(act.type.equals("initiate")) { 
							Logger.log("Task "+task.id+" does initialization.");
							initial(index, act);
							if(task.aborted)		all_finish++;
						/*	Handling request activity 	*/
						} else if(act.type.equals("request")) {
							/*	If task requiring resources more than total, abort the task and release its resource	*/
							if(task.resource[act.resource_type]+act.resource_num > total_resource[act.resource_type]) {
								for(int i=1; i <= MAX_RESOURCE; i++)
									new_avail_resource[i] +=task.resource[i];
								task.aborted = true;
								all_finish++;
								Logger.log("According to the spec task "+task.id+" is aborted now and its resources are\n" + 
										"available next cycle ("+cur_t+" - "+(cur_t+1)+")");
								continue;
							}
			
							if(checkAllocation(task.id, act, cur_t)) {
								avail_resource[act.resource_type] -= act.resource_num;
								task.resource[act.resource_type] += act.resource_num;
								Logger.log("Task "+task.id+" completes its request (resource["+act.resource_type+"]: requested = "+act.resource_num+", remaining = "+avail_resource[act.resource_type]+")");
							} else {
								/*	If task aborted after checkAllocation(), release its resources	
								 *		else, make task to pending_que 
								 */
								if(task.aborted) {
									for(int i=1; i <= MAX_RESOURCE; i++)
										new_avail_resource[i] += tasks[task.id].resource[i];
									all_finish++;
									Logger.log("Task "+task.id+"'s request exceeds its claim; aborted; "+act.resource_num+" units available next cycle");
								} else {
									Logger.log("Task "+task.id+"'s request cannot be granted.");
									task.wait_t++;
									new_pending.add(task);
								}
								continue;
							}
							
							/*	Handling release	 activity */
						} else if(act.type.equals("release")) {
							new_avail_resource[act.resource_type] +=act.resource_num;
							task.resource[act.resource_type] -= act.resource_num;
							Logger.log("Task "+task.id+" completes its release (resource["+act.resource_type+"]: released = "+act.resource_num+", available next cycle = "+(cur_t+1)+")");
						
							/*	Handling terminate activity 	*/
						} else if(act.type.equals("terminate")) {
							task.finish = true;
							task.end_t = cur_t;
							Logger.log("Task "+task.id+" terminate");
							all_finish++;
						}
						task.removeRecentActivity();
					}
				}
			}
			/*	Deadlock checking.
			 * 	If deadlock, removing the lowest number numbered tasks and its resources until no-deadlock.
    			*/
			while(deadlockCheck(new_pending.size(), tasks)) {
				Logger.log("deadlock !!!");
				new_pending.clear();
				for(int index=1; index<=MAX_TASK; index++) {
					Task task = tasks[index];
					if(!task.aborted && !task.finish) {
						for(int i=1; i<= MAX_RESOURCE; i++)
							avail_resource[i] +=task.resource[i];
						task.aborted = true;
						Logger.log("According to the spec task "+task.id+" is aborted now and its resources are\n" + 
								"available next cycle ("+cur_t+" - "+(cur_t+1)+")");
						all_finish++;
						break;
					}
				}
				for(int index=1; index<=MAX_TASK; index++) {
					Task task = tasks[index];
					if(!task.aborted && !task.finish) {
						Activity act = task.getRecentActivity();
						if(!checkAllocation(task.id, act, cur_t))
							new_pending.add(task);
					}
				}
			}
			/*	Update avaible resource	*/
			for(int i=1; i <= MAX_RESOURCE; i++)
				avail_resource[i] += new_avail_resource[i];
			pending_que = new_pending;
			cur_t++;
		}
	}
	public String display() {
		int sum_t=0, sum_wait_t=0;
		for(int index=1; index<=MAX_TASK; index++) {
			if(!tasks[index].aborted) {
				sum_t += tasks[index].end_t;
				sum_wait_t += tasks[index].wait_t;
			}
		}
		return String.format("%2d%4d%6d%%",sum_t,sum_wait_t,Math.round(sum_wait_t*100.0/sum_t));
	}
	public abstract boolean checkAllocation(int t_id, Activity act, int cur_t);
	public abstract boolean deadlockCheck(int pending_num, Task[] tasks);
	public abstract void initial(int t_id, Activity act);
}
