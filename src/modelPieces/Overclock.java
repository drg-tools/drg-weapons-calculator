package modelPieces;

public class Overclock {

	public enum classification{clean, balanced, unstable};
	
	private classification overclockType;
	private String name;
	private String text;
	private int index;
	private boolean implemented;
	
	public Overclock(classification type, String ocName, String ocText, int arrayIndex) {
		this(type, ocName, ocText, arrayIndex, true);
	}
	
	public Overclock(classification type, String ocName, String ocText, int arrayIndex, boolean ocImplemented) {
		overclockType = type;
		name = ocName;
		text = ocText;
		index = arrayIndex;
		implemented = ocImplemented;
	}
	
	public classification getType() {
		return overclockType;
	}
	public String getName() {
		return name;
	}
	public String getText() {
		return text;
	}
	public boolean isImplemented() {
		return implemented;
	}
	
	public String getShortcutRepresentation() {
		return "" + (index + 1);
	}
}
