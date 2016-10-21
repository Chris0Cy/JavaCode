package pippin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class Assembler {
	/**
	 * lists the mnemonics of the instructions that do not have arguments
	 */
	 public static Set<String> noArgument = new TreeSet<String>();
	 /**
	 * lists the mnemonics of the instructions that allow immediate addressing
	 */
	 public static Set<String> allowsImmediate = new TreeSet<String>();
	 /**
	 * lists the mnemonics of the instructions that allow indirect addressing
	 */
	 public static Set<String> allowsIndirect = new TreeSet<String>(); 
	 
	 static {
		 noArgument.add("HALT");
		 noArgument.add("NOP");
		 noArgument.add("NOT");
		 allowsImmediate.add("LOD");
		 allowsImmediate.add("ADD");
		 allowsImmediate.add("SUB");
		 allowsImmediate.add("MUL");
		 allowsImmediate.add("DIV");
		 allowsImmediate.add("AND");
		 allowsIndirect.add("LOD");	
		 allowsIndirect.add("STO");
		 allowsIndirect.add("JUMP");
		 allowsIndirect.add("JMPZ");
		 allowsIndirect.add("ADD");
		 allowsIndirect.add("SUB");
		 allowsIndirect.add("MUL");
		 allowsIndirect.add("DIV");
	 }
	 
	 /**
	  * Method to assemble a file to its binary representation. If the input has errors
	  * a list of errors will be written to the errors map. If there are errors,
	  * they appear as a map with the line number as the key and the description of the error
	  * as the value. If the input or output cannot be opened, the "line number" key is 0.
	  * @param input the source assembly language file
	  * @param output the binary version of the program if the souce program is
	  * correctly formatted
	  * @param errors the errors map
	  * @return
	  */
	  public static boolean assemble(File input, File output, Map<Integer, String> errors) {
		  ArrayList<String> inputText = new ArrayList<>(); 
		  ArrayList<String> inCode = new ArrayList<>();
		  ArrayList<String> inData = new ArrayList<>();
		  ArrayList<String> outCode = new ArrayList<>();
		  ArrayList<String> outData = new ArrayList<>();
		  int lineNum = 1;
		  int offset = 0;
		  boolean dataFound = false;
		  boolean prevBlank = false;
		  try (Scanner inp = new Scanner(input)) {
			  // while loop reading the lines from input in inputText
			  while(inp.hasNextLine()){
				  lineNum++;
				  String text = inp.nextLine();
				  inputText.add(text);
				  if(text.trim().length() > 0){
					  if(text.charAt(0) == ' ' || text.charAt(0) == '\t'){
						  errors.put(lineNum, "Error on line " + (lineNum) + ": starts with white space");
					  }else if(prevBlank){
						  errors.put(lineNum, "Error on line " + lineNum + ": illegal blank line");
						  prevBlank = false;
					  }
				  }else{
					  prevBlank = true;
				  }
			  }for(int i = 0; i < inputText.size(); i++){
				  if(inputText.get(i).equalsIgnoreCase("DATA")){
					  if(inputText.get(i) == "DATA")
						  dataFound = true;
					  else
						  errors.put(i+1, "Error on line " + (i+1) + ": DATA incorrectly used");
				  }
				  else if(dataFound)
					  inData.add(inputText.get(i));
				  else
					  inCode.add(inputText.get(i));
			  }for(int i = 0; i < inCode.size(); i++){
				  offset++;
				  String[] parts = inCode.get(i).split("\\s+");
				  if(!InstructionMap.opcode.containsKey(parts[0].toUpperCase())){
					  errors.put(i+1, "Error on line " + (i+1) + ": illegal mnemonic");
				  }else{
					  if(!InstructionMap.opcode.containsKey(parts[0]))
						  errors.put(i+1, "Error on line " + (i+1) + ": mnemonics must be uppercase");
					  else{
						  if(noArgument.contains(parts[0]) && parts.length > 1-errors.size()){
							  errors.put(i+1, "Error on line " + (i+1) + ": mnemonics must be uppercase");
						  }
						  else if(noArgument.contains(parts[0]) && parts.length == 1){
							  outCode.add(Integer.toHexString(InstructionMap.opcode.get(parts[0])) + " 0 0");
						  }
						  else{
							  if(parts[1].length() >= 3 && parts[1].charAt(0) == '[' && parts[1].charAt(1) == '['){
								  try{
									  int arg = Integer.parseInt(parts[1].substring(2),16);
									  outCode.add(Integer.toHexString(InstructionMap.opcode.get(parts[0])) + " " +
											  Integer.toHexString(arg).toUpperCase() + " 2");
								  } catch(NumberFormatException e) {
									  errors.put(i+1, "Error on line "+(i+1)+ ": indirect argument is not a hex number");
								  } 								  
							  } else if(parts[1].length() >= 2 && parts[1].charAt(0) == '['){
								  try{
									  int arg = Integer.parseInt(parts[1].substring(1),16);
									  outCode.add(Integer.toHexString(InstructionMap.opcode.get(parts[0])) + " " +
											  Integer.toHexString(arg).toUpperCase() + " 2");
								  } catch(NumberFormatException e) {
									  errors.put(i+1, "Error on line "+(i+1)+ ": direct argument is not a hex number");
								  }
							  } else if(parts[1].length() >= 1 && parts[1].charAt(0) != '['){
								  if(allowsImmediate.contains(parts[0])){
								  	  try{
										  int arg = Integer.parseInt(parts[1].substring(2),16);
										  outCode.add(Integer.toHexString(InstructionMap.opcode.get(parts[0])) + " " +
												  Integer.toHexString(arg).toUpperCase() + " 0");
									  } catch(NumberFormatException e) {
										  errors.put(i+1, "Error on line "+(i+1)+ ": immediate argument is not a hex number");
									  }
								  }
								  else{
									  errors.put(i+1, "Error on line"+(i+1+ ": illegal mnemonic"));
								  }
							  }
						  }
					  }
				  }

			  }for(int i = 0; i < inData.size(); i++){
				  String[] parts = inData.get(i).split("\\s+");
				  if(parts.length < 2){
					  errors.put(offset+i, "Error on line "+(offset+i)+" not an address/value pair");
				  } else{
					  int addr = -1;
					  int val = -1;
					  try{
						  addr = Integer.parseInt(parts[0], 16);
					  } catch(NumberFormatException e){
						  errors.put(offset+i, "Error on line "+(i+offset)+ ": address is not a hex number");
					  }try{
						  val = Integer.parseInt(parts[1], 16);
					  } catch(NumberFormatException e){
						  errors.put(offset+i, "Error on line "+(i+offset)+ ": value is not a hex number");
					  }
					  outData.add(Integer.toHexString(addr).toUpperCase() + " " + Integer.toHexString(val).toUpperCase());
				  }
			  }
			  
		  } catch (FileNotFoundException e) {
			  errors.put(0, "Error: Unable to open the input file");
		  }
		  if(errors.size() == 0) {
			  try (PrintWriter outp = new PrintWriter(output)){
				  for(String str : outCode) outp.println(str);
				  outp.println(-1); // the separator where the source has “DATA”
				  for(String str : outData) outp.println(str);
			  } catch (FileNotFoundException e) {
				  errors.put(0, "Error: Unable to write the assembled program to the output file");
			  }
		  }
		  return errors.size() == 0;
	  } 
}
