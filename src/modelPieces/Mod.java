package modelPieces;

public class Mod {

	private String name;
	private String text;
	private int tier;
	private int index;
	private boolean implemented;
	
	public Mod(String modName, String modText, int tierNumber, int arrayIndex) {
		this(modName, modText, tierNumber, arrayIndex, true);
	}
	
	public Mod(String modName, String modText, int tierNumber, int arrayIndex, boolean modImplemented) {
		name = modName;
		text = modText;
		tier = tierNumber;
		index = arrayIndex;
		implemented = modImplemented;
	}
	
	public String getName() {
		return name;
	}
	public String getText() {
		return text;
	}
	public int getTier() {
		return tier;
	}
	public boolean isImplemented() {
		return implemented;
	}
	
	public char getLetterRepresentation() {
		switch (index) {
			case 0: {
				return 'A';
			}
			case 1: {
				return 'B';
			}
			case 2: {
				return 'C';
			}
			default: {
				return '-';
			}
		}
	}
	
	public String getShortcutRepresentation() {
		return "" + tier + getLetterRepresentation();
	}
}
