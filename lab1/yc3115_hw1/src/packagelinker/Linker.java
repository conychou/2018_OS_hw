import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Linker {

	private int machineMemorySize = 200;
	
	private class Module {
		int id;
		int base_address;
		int len;
		String errmsg = null;
		ArrayList<Text>	text_list = new ArrayList<Text>();
		Use[]	use_list = null;
	}
	private class Text {
		String type;
		Character opcode;
		int address;
		StringBuilder result = new StringBuilder();
		public Text(String type, String address){
			this.type = type;
			this.opcode = address.charAt(0);
			this.address = Integer.valueOf(address.substring(1));
		}
	}
	private class Symbol {
		String symbol;
		int address;
		boolean used;
		String errmsg;
		int module_id;
		public Symbol(String symbol, int address, int module_id) {
			this.symbol = symbol;
			this.address = address;
			this.module_id = module_id;
		}
	}
	private class Use {
		String symbol = new String();
		boolean used = false;
		public Use(String symbol){
			this.symbol = symbol;
		}
	}
	private ArrayList<Module> modules = new ArrayList<Module>();
	private LinkedHashMap<String,Symbol> symbol_table = new LinkedHashMap<String, Symbol>();
	
	private void DisplayResults() {
		System.out.println("Symbol Table");
		for(Map.Entry<String, Symbol> entry:symbol_table.entrySet()){
			Symbol s = entry.getValue();
			System.out.print(s.symbol+"="+s.address);
			if(s.errmsg != null)
				System.out.println(s.errmsg);
			else
				System.out.print("\n");
		}
		System.out.print("\n");
		System.out.println("Memory Map");
		int index=0;
		for(Module module:modules){
			for(Text text:module.text_list) {
				System.out.print(String.format("%3d:	",index));
				System.out.println(text.result.toString());
				index++;
			}
		}
		System.out.print("\n");
		for(Module module:modules){
			if(module.use_list != null) {
				for(Use use:module.use_list)
					if(!use.used) 
						System.out.println("Warning: In module "+module.id+" "+use.symbol+" appeared in the uselist but was not actually used");
			}
		}
		System.out.print("\n");
		for(Map.Entry<String, Symbol> entry:symbol_table.entrySet()){
			Symbol s = entry.getValue();
			if(!s.used)
				System.out.println("Warning: "+s.symbol+" was defined in module "+s.module_id+" but never used");					
		}
		System.out.print("\n");
		for(Module module:modules){				
			if(module.errmsg != null)
				System.out.println(module.errmsg);
		}
	}
	private void calculateAbsoluteAddress(){
		for(int index=0; index < modules.size();index++){
			modules.get(index).len = modules.get(index).text_list.size();
			if(index > 0)
				modules.get(index).base_address += (modules.get(index-1).base_address+modules.get(index-1).len);
		}
		for(Map.Entry<String, Symbol> entry:symbol_table.entrySet()){
			Symbol s = entry.getValue();
			Module module = modules.get(s.module_id);
			if(s.address >= module.len){ 
				module.errmsg = "Error: In module "+s.module_id+" the def of "+s.symbol+" exceeds the module size;  zero relative"
;
				s.address = 0;
			}

		//	System.out.println(s.symbol+" "+s.module_id+" "+s.address);
			s.address += module.base_address;
		}
	}
	private void processText(){
		for(Module module:modules){
			for(Text text:module.text_list){
				if(text.type.equals("I"))
					text.result.append(text.opcode).append(String.format("%03d",text.address));
				else if(text.type.equals("A")) {
					String s = new String();
					if(text.address >= machineMemorySize){ 
						s = " Error: Absolute address exceeds machine size; zero used";
						text.address=0;
					}
					text.result.append(text.opcode).append(String.format("%03d",text.address)).append(s);	
				} else if (text.type.equals("R")){
					String s = new String();
					if(text.address >= module.len){
						s = "Error: Relative address exceeds module size; zero used";
						text.address=0;
					} else
						text.address += module.base_address;
					text.result.append(text.opcode).append(String.format("%03d",text.address)).append(s);
				} else if (text.type.equals("E")){
					if(text.address >= module.use_list.length)
						text.result.append(text.opcode).append(String.format("%03d",text.address)).append(" Error: External address exceeds length of uselist; treated as immediate");						
					else{
						StringBuilder s = new StringBuilder();
						if(!symbol_table.containsKey(module.use_list[text.address].symbol)) {
							s.append(String.format("%03d",0)).append(" Error: ").append(module.use_list[text.address].symbol).append(" is not defined; zero used");
							module.use_list[0].used = true;
						} else {
							Use use = module.use_list[text.address];
							Symbol symbol = symbol_table.get(use.symbol);
							use.used = true;							
							symbol.used = true;
							s.append(String.format("%03d",symbol.address));								
						}
						text.result.append(text.opcode).append(s);
					}
				} 
			}
		}
	}
	private void scanFile(String filePath) throws FileNotFoundException {
		Scanner scanner = new Scanner(new File(filePath));
		String content = scanner.useDelimiter("\\A").next();

		// Identify the tokens using RegEx
		Matcher matcher = Pattern.compile("[\\d\\w]+").matcher(content);
		String token;
		String previous_token = new String();
		boolean definition = false, use = false, text = false;
		int definition_num = -1, use_num = -1, text_num = -1, module_id = -1;
		Module cur = new Module();
		
		while (matcher.find()) {
			if(definition && use && text) {
				definition = false; use = false; text=false;
				definition_num = -1; use_num = -1; text_num = -1;
				cur = new Module();
				cur.id = modules.size();
			}
			token = matcher.group();
			if(module_id < 0){
				module_id = Integer.valueOf(token);
				continue;
			}
			if(!definition){
				if(definition_num < 0) {
					definition_num = 2*Integer.valueOf(token);
				} else if(definition_num > 0){
					if(definition_num %2 == 0)
						previous_token = token;
					else {
						if(symbol_table.containsKey(previous_token)) {
							symbol_table.get(previous_token).errmsg = " Error: This variable is multiple times defined; first value used";
						} else {
							symbol_table.put(previous_token, new Symbol(previous_token, Integer.valueOf(token), modules.size()));
						}
					}
					definition_num--;
				}
				if(definition_num == 0)
					definition = true;
				continue;
			}
			if(!use) {
				if(use_num < 0) {
					use_num = Integer.valueOf(token);
					if(use_num > 0){
						cur.use_list = new Use[use_num];
						previous_token = token;
					}
				} else if(use_num > 0){
					cur.use_list[Integer.valueOf(previous_token) - use_num] = new Use(token);			
					use_num--;
				}
				if(use_num == 0)
					use = true;
				continue;
			}
			if(!text) {
				if(text_num < 0) {
					text_num = 2*Integer.valueOf(token);
				} else {
					if(text_num %2 == 0)	
						previous_token = token;
					else
						cur.text_list.add(new Text(previous_token, token));				
					text_num--;
				}
				if(text_num == 0) {
					text = true;
					modules.add(cur);
				} else
					continue;
			}
		}		
	}
	public Linker(String filePath) throws FileNotFoundException {
		
		scanFile(filePath);
		
		calculateAbsoluteAddress();
		
		processText();
		
		DisplayResults();
	}
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String filePath;
		if (args.length > 0)
			filePath = args[0];
		else
			throw new IllegalArgumentException(
					"\nExpected path to input series of object modules.\nFor example, \n\njava TwoPass input.txt\n");

		new Linker(filePath);
	}

}
