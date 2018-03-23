
public class Logger {
	private static boolean isEnable = false;
	public static void log(String s) {
		if(isEnable)
			System.out.println(s);
	}
}
