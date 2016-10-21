package pippin;

import java.util.ArrayList;

public class Code {
	public static int CODE_MAX = 256;	
	private ArrayList<IntTriple> program = new ArrayList<IntTriple>();
	
	public class IntTriple {
		private int op;
		private int arg;
		private int indirectionLevel;
		
		public IntTriple(int o, int ar, int ind) {
			this.op=o;
			this.arg = ar;
			this.indirectionLevel = ind;	
			}
			
			public int getOp(){
				return this.op;
			}
			
			public int getArg(){
				return this.arg;
			}
			
			public int getIndirectionLevel(){
				return this.indirectionLevel;
			}
		}
	
	
		public int getProgramSize() {
			return program.size();
		}
		
		public int getOp(int i) {
			return program.get(i).getOp();
		}
		
		public int getArg(int i) {
			return program.get(i).getArg();
		}
		
		public int getIndirectionLevel(int i) {
			return program.get(i).getIndirectionLevel();
		}
		
		public void clear() {
			program.clear();
		}
		
		public void setCode(int op, int arg, int indirectionLevel ) {
			program.add(new IntTriple(op,arg,indirectionLevel));
		}
	
		public String getCodeText(int i){
			StringBuilder builder = new StringBuilder();
			if(i < program.size()){
				builder.append(InstructionMap.mnemonics.get(program.get(i).op));
				builder.append(' ');
				for(int j = 0; j < program.get(i).indirectionLevel; j++){
					builder.append('[');
				}
				builder.append(program.get(i).arg);
			}
			return builder.toString();
		}
}
