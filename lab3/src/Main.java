import java.io.File;
import java.io.IOException;
import java.util.Scanner;


public class Main {

	public static void main(String[] args) throws IOException{
		// TODO Auto-generated method stub
		String filepath; 	//= "input-07.txt";
		if(args.length > 0)
			filepath = args[0];
		else
            throw new IllegalArgumentException("\nExpected path to input series of object modules.\nFor example, \n\njava Lab3 input.txt\n");
		
		Scanner scanner = new Scanner(new File(filepath));
		String content = scanner.useDelimiter("\\A").next();
		String[] split = content.split("\\s+");
		int index=0;
		int MAX_TASK = Integer.valueOf(split[index++]);
		int MAX_RESOURCE = Integer.valueOf(split[index++]);
		int[] total_resource = new int[MAX_RESOURCE+1];
		
		/* Read input */
		Task[] tasks = new Task[MAX_TASK+1];
		for(int i=1; i<=MAX_TASK; i++)
			tasks[i] = new Task(i, MAX_RESOURCE+1);
		for(int i=1; i <= MAX_RESOURCE; i++) {
			total_resource[i] = Integer.valueOf(split[index++]);
		}
		while(index<split.length) {
			String t_type = split[index++];
			int t_id = Integer.valueOf(split[index++]);
			tasks[t_id].addActivity(new Activity(t_type, Integer.valueOf(split[index++]),Integer.valueOf(split[index++]),Integer.valueOf(split[index++])));			
		}
		scanner.close();
		
		/* creat and execute FIFO Manager */
		Fifo f = new Fifo(tasks, total_resource);
		f.run();
		
		/* creat and execute Banker Manager */
		Banker b = new Banker(tasks, total_resource);
		b.run();
		
		/* Display result */
		if(b.msg != null)		System.out.println(b.msg.toString());
		System.out.format("%20s%37s\n","FIFO","BANKER'S");
		for(int i=1; i<=MAX_TASK; i++) {
			System.out.format("%11s%2d       %-15s%11s%2d       %s\n","Task",i,(f.tasks[i].aborted)?"aborted":f.tasks[i].display(),"Task",i,(b.tasks[i].aborted)?"aborted":b.tasks[i].display());
		}
		System.out.format("%12s        %-15s%12s        %s\n","total",f.display(),"total",b.display());	
	}

}
