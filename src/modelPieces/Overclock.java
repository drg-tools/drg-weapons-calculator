package modelPieces;

import guiPieces.ButtonIcons.overclockIcons;

public class Overclock {

	public enum classification{clean, balanced, unstable};
	
	private classification overclockType;
	private String name;
	private String text;
	private overclockIcons icon;
	private int index;
	private boolean implemented;
	
	public Overclock(classification type, String ocName, String ocText, overclockIcons upgradeType, int arrayIndex) {
		this(type, ocName, ocText, upgradeType, arrayIndex, true);
	}
	
	public Overclock(classification type, String ocName, String ocText, overclockIcons upgradeType, int arrayIndex, boolean ocImplemented) {
		overclockType = type;
		name = ocName;
		text = ocText;
		icon = upgradeType;
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
	public overclockIcons getIcon() {
		return icon;
	}
	public boolean isImplemented() {
		return implemented;
	}
	
	public String getShortcutRepresentation() {
		return "" + (index + 1);
	}
}
