import java.util.Arrays;

public class Banker extends Manager {

	int[][] claim;
	StringBuilder msg = new StringBuilder();
	
	public Banker(Task[] ts, int[] resources) {
		super(ts, resources);
		// TODO Auto-generated constructor stub
		claim = new int[MAX_TASK+1][MAX_RESOURCE+1];
	}

	/*	Banker's algorithm implementation (permit the resource only when it keep staying in safe state)*/
	@Override
	public boolean checkAllocation(int t_id, Activity act, int cur_t) {
		// TODO Auto-generated method stub	
		int[] remain = Arrays.copyOf(avail_resource, avail_resource.length);
		int[][] posses = new int[MAX_TASK+1][MAX_RESOURCE+1];
		boolean[] finish = new boolean[MAX_TASK+1];
		boolean processing = true;	

		if(tasks[t_id].resource[act.resource_type]+act.resource_num > claim[t_id][act.resource_type]) {
			tasks[t_id].aborted = true;
			msg.append("During cycle "+cur_t+"-"+(cur_t+1)+" of Banker's algorithms\n" + 
					"   Task "+t_id+"'s request exceeds its claim; aborted; "+act.resource_num+" units available next cycle   \n");
			return false;
		}
			
		for(int index=1; index<=MAX_TASK; index++) {
			finish[index] = tasks[index].finish;
			if(!tasks[index].finish&& !tasks[index].aborted)
				posses[index] = Arrays.copyOf(tasks[index].resource, MAX_RESOURCE+1);
		}
		remain[act.resource_type] -= act.resource_num;
		posses[t_id][act.resource_type] += act.resource_num;
		while(processing) {
			processing = false;
			for(int index=1; index<=MAX_TASK; index++) {
				if(!finish[index]&& !tasks[index].aborted) {
					finish[index] = true;
					for(int i=1; i <= MAX_RESOURCE; i++) {
						if(claim[index][i] - posses[index][i] > remain[i])
							finish[index] = false;
					}
					if(finish[index]) {
						for(int i=1; i<=MAX_RESOURCE; i++)
							remain[i] += posses[index][i];
						processing = true;
					}
				}
			}
		}
		for(int index=1; index<=MAX_TASK; index++) 
			if(!finish[index] && !tasks[index].aborted)	return false;
		return true;
	}
	/*	Banker updates task's initial claim info*/
	@Override
	public void initial(int t_id, Activity act) {
		// TODO Auto-generated method stub
		if(total_resource[act.resource_type] < act.resource_num) {
			tasks[t_id].aborted = true;
			Logger.log("Task "+t_id+" is aborted (claim exceeds total in system)");
			msg.append("Banker aborts task "+t_id+" before run begins:\n" + 
					"       claim for resourse "+act.resource_type+" ("+act.resource_num+") exceeds number of units present ("+total_resource[act.resource_type]+")\n");
		} else
			claim[t_id][act.resource_type] = act.resource_num;
	}
	
	/*	Since Banker always stay in safe state, no need to implement deadlockCheck()	*/
	@Override
	public boolean deadlockCheck(int pending_num, Task[] tasks) {
		// TODO Auto-generated method stub
		return false;
	}
}
