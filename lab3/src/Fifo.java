
public class Fifo extends Manager{

	public Fifo(Task[] ts, int[] total_resource) {
		super(ts, total_resource);
		// TODO Auto-generated constructor stub
	}

	/*	Fifo algorithm implementation*/
	@Override
	public boolean checkAllocation(int t_id, Activity act, int cur_t) {
		// TODO Auto-generated method stub
		return (avail_resource[act.resource_type] >= act.resource_num)?true:false;
	}

	/*	DeadlockCheck return true if all active tasks are pending*/
	@Override
	public boolean deadlockCheck(int pending_num, Task[] tasks) {
		// TODO Auto-generated method stub
		if(pending_num == 0)		return false;

		int active_count =0;
		for(int index=1; index<=MAX_TASK; index++) {
			Task t = tasks[index];
			if(!t.aborted && !t.finish)
				active_count++;
		}
		return (active_count == pending_num);
	}

	/*	Fifo doesn't need initial claim info*/
	@Override
	public void initial(int t_id, Activity act) {
		// TODO Auto-generated method stub	
	}
}
