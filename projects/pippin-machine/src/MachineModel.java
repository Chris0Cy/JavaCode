package pippin;

import java.util.Map;
import java.util.TreeMap;
import java.util.Observable;

public class MachineModel extends Observable {

	public final Map<Integer, Instruction> INSTRUCTION_MAP = new TreeMap<Integer,Instruction>();
	private Registers cpu = new Registers();
	private Memory memory = new Memory();
	private Code code; 
	private boolean withGUI = false;
	private boolean running = false;
	private States state;
	
	
	public class Registers{
		private int accumulator;
		private int programCounter;
		public int getProgramCounter() {
			// TODO Auto-generated method stub
			return programCounter;
		}
			
	}
	
	
	
	
	public MachineModel(){
		this(true);

	}
	
	public void setCode(Code code) {
		this.code = code;
	}
	
	public void step() {
		try{
			int pc = cpu.getProgramCounter();
			int arg = code.getArg(pc);
			int indirectionLevel = code.getIndirectionLevel(pc);
			int opCode = code.getOp(pc);
			get(opCode).execute(arg,indirectionLevel);
			
		}catch (Exception e) {
			halt();
			throw e;
		}
		
	}
	
	
	
	public Code getCode() {
		return code;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public void clear() {
		if(this.code != null) {
			memory.clear();
			setAccumulator(0);
			setProgramCounter(0);
		}else{
			memory.clear();
		}
	}
	
	public MachineModel(boolean withGUI) {
		this.withGUI = withGUI;
	
		//INSTRUCTION_MAP entry for "ADD"
				INSTRUCTION_MAP.put(0x3,(arg,level) -> {
					if(level< 0 || level > 2){
						throw new IllegalArgumentException("Illegal indirection level in ADD instruction");
					}
					if(level > 0){
						INSTRUCTION_MAP.get(0x3).execute(memory.getData(arg), level-1);
					}else{
						cpu.accumulator += arg;
						cpu.programCounter ++;
					}
				});
		
		//INSTRUCTION MAP entry for" NOP "
		INSTRUCTION_MAP.put(0x0,(arg, level) -> {
			if( level == 0) {
				cpu.programCounter ++;
			} else {
				throw new IllegalArgumentException(" Illegal indirection level in NOP instruction");
			}
		});
		
		//INSTRUCTION MAP entry for" LOD"
		INSTRUCTION_MAP.put(0x1,(arg, level) -> {
			if( level < 0 || level > 2) {
				throw new IllegalArgumentException(" Illegal indirection level in LOD instruction");
			}
			if(level > 0 ) {
				INSTRUCTION_MAP.get(0x1).execute(memory.getData(arg),level-1);
			} else {
				cpu.accumulator = arg;
				cpu.programCounter ++;
			}
		});
		
		//INSTRUCTION MAP entry for" STO"
		INSTRUCTION_MAP.put(0x2,(arg, level) -> {
			if( level < 1 || level > 2) {
				throw new IllegalArgumentException(" Illegal indirection level in STO instruction");
			}
			if(level == 1 ) {
				memory.setData(arg, cpu.accumulator);
				cpu.programCounter++;
			} else {
				INSTRUCTION_MAP.get(0x2).execute(memory.getData(arg),level-1);
			}
		});
		
		//INSTRUCTION MAP entry for" SUB"
		INSTRUCTION_MAP.put(0x4,(arg, level) -> {
			if( level < 0 || level > 2) {
				throw new IllegalArgumentException(" Illegal indirection level in SUB instruction");
			}
			if(level > 0 ) {
				INSTRUCTION_MAP.get(0x4).execute(memory.getData(arg),level-1);
			} else {
				cpu.accumulator = cpu.accumulator - arg;
				cpu.programCounter ++;
			}
		});
		
		//INSTRUCTION MAP entry for" MUL"
		INSTRUCTION_MAP.put(0x5,(arg, level) -> {
			if( level < 0 || level > 2) {
				throw new IllegalArgumentException(" Illegal indirection level in MUL instruction");
			}
			if(level > 0 ) {
				INSTRUCTION_MAP.get(0x5).execute(memory.getData(arg),level-1);
			} else {
				cpu.accumulator = cpu.accumulator * arg;
				cpu.programCounter ++;
			}
		});
		
		//INSTRUCTION MAP entry for" DIV"
		INSTRUCTION_MAP.put(0x6,(arg, level) -> {
			if( level < 0 || level > 2) {
				throw new IllegalArgumentException(" Illegal indirection level in DIV instruction");
			}
			if(level > 0 ) {
				INSTRUCTION_MAP.get(0x6).execute(memory.getData(arg),level-1);
			} else if(arg == 0) {
				throw new DivideByZeroException("Devision by Zero");
			}else {
				cpu.accumulator = cpu.accumulator / arg;
				cpu.programCounter ++;
			}
		});
		
		//INSTRUCTION MAP entry for" AND"
		INSTRUCTION_MAP.put(0x7,(arg, level) -> {
			if( level < 0 || level > 1) {
				throw new IllegalArgumentException(" Illegal indirection level in ADD instruction");
			}
			if(level > 0 ) {
				INSTRUCTION_MAP.get(0x7).execute(memory.getData(arg),level-1);
			} else {
				if(arg != 0 && cpu.accumulator != 0) {
					cpu.accumulator = 1;
				} else {
					cpu.accumulator = 0;
				}
				cpu.programCounter++;
			}
		});
		
		//INSTRUCTION MAP entry for"JUMP"
		INSTRUCTION_MAP.put(0xB,(arg, level) -> {
			if( level < 0 || level > 1) {
				throw new IllegalArgumentException(" Illegal indirection level in JUMP instruction");
			}
			if(level > 0 ) {
				INSTRUCTION_MAP.get(0xB).execute(memory.getData(arg),level-1);
			} else {
				cpu.programCounter = arg;
			}
		});
		
		//INSTRUCTION MAP entry for"JMPZ"
		INSTRUCTION_MAP.put(0xC,(arg, level) -> {
			if( level < 0 || level > 1) {
				throw new IllegalArgumentException(" Illegal indirection level in JMPZ instruction");
			}
			if(level > 0 ) {
				INSTRUCTION_MAP.get(0xC).execute(memory.getData(arg),level-1);
			}else {
				if ( level == 0 && cpu.accumulator == 0) {
					cpu.programCounter = arg;
				} if(cpu.accumulator !=0) {
					cpu.programCounter++;
				}
			}
		});
		
		//INSTRUCTION MAP entry for"NOT"
		INSTRUCTION_MAP.put(0x8,(arg, level) -> {
			if( level != 0) {
				throw new IllegalArgumentException("Illegal indirection level in NOT instruction");
			}
			if(level > 0 ) {
				INSTRUCTION_MAP.get(0x8).execute(memory.getData(arg),level-1);
			}else {
				if(cpu.accumulator == 0) {
					cpu.accumulator = 1;
				} else {
					cpu.accumulator= 0;
				}
			cpu.programCounter++;
			}
		});

		//INSTRUCTION MAP entry for"CMPZ"
		INSTRUCTION_MAP.put(0x9,(arg, level) -> {
			if( level == 1) {
				if(memory.getData(arg)== 0) {
					cpu.accumulator = 1;
				} else {
					cpu.accumulator = 0;
				}	
			}	else {
					throw new IllegalArgumentException(" Illegal indirection level in CHMPZ instruction");
				
			}cpu.programCounter++;
		});
		
		//INSTRUCTION MAP entry for"CMPL"
		INSTRUCTION_MAP.put(0xA,(arg, level) -> {
			if( level != 1) {
				throw new IllegalArgumentException(" Illegal indirection level in CMPL instruction");	
			}else {
				if(level == 1) {
					cpu.accumulator = 0;
				}
				if(memory.getData(arg) < 0) {
					cpu.accumulator = 1;
				}
				cpu.programCounter++;
			}
			
		});
		
		//INSTRUCTION MAP entry for"HALT"
		INSTRUCTION_MAP.put(0xF,(arg, level) -> {
			halt();
		});
		
		//INSTRUCTION MAP entry for"ROT"
		INSTRUCTION_MAP.put(0xC,(arg, level) -> {
			int start = memory.getData(arg);
			int length = memory.getData(arg+1);
			int move = memory.getData(arg+2);
			if( level != 1) {
				throw new IllegalArgumentException(" Illegal indirection level in ROT instruction");
			}else if(start< 0) {
				throw new IllegalArgumentException(" Illegal indirection level in ROT instruction");
			} else if(length < 0) {
				throw new IllegalArgumentException(" Illegal indirection level in ROT instruction");
			} else if(start + length -1 >= Memory.DATA_SIZE) {
				throw new IllegalArgumentException(" Illegal indirection level in ROT instruction");
			} else if(start <= arg+ 2) {
				throw new IllegalArgumentException(" Illegal indirection level in ROT instruction");
			} else if(length -1 <= arg) {
				throw new IllegalArgumentException(" Illegal indirection level in ROT instruction");
			}
		});
		
	}
	
	
	
	public int getData(int index) {
		return memory.getData(index);
	}
	
	public void setData(int index, int value) {
		memory.setData(index,value);
	}
	
	public Instruction get(Integer key) {
		return INSTRUCTION_MAP.get(key);
	}
	
	int[] getData() {
		return memory.getArray();
	}
	
	public int getProgramCounter() {
		return cpu.programCounter;
	}
	
	public int getAccumulator() {
		return cpu.accumulator;
	}
	
	public void setAccumulator( int i) {
		cpu.accumulator = i;
	}
	
	public void setProgramCounter(int i) {
		cpu.programCounter = i;
	}
	
	public int getchangedIndex() {
		return memory.getChangedIndex();
	}
	
	public void halt() {
		if(withGUI) {
			running =false;
		} else {
			System.exit(0);
		}
	}
	
	public void clearMemory() {
		memory.clear();
	}
	
	/**
	 * Main method that drives the whole simulator
	 * @param args command line arguments are not used
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new MachineView(new MachineModel(true)); 
			}
		});
	}
}
