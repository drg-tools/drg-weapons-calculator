package modelPieces;

import guiPieces.customButtons.ButtonIcons.overclockIcons;

public class Overclock {

	public enum classification{clean, balanced, unstable};
	
	private classification overclockType;
	private String name;
	private String text;
	private overclockIcons icon;
	private int index;
	private boolean selected;
	private boolean implemented;
	private boolean ignored;
	
	public Overclock(classification type, String ocName, String ocText, overclockIcons upgradeType, int arrayIndex) {
		this(type, ocName, ocText, upgradeType, arrayIndex, true);
	}
	
	public Overclock(classification type, String ocName, String ocText, overclockIcons upgradeType, int arrayIndex, boolean ocImplemented) {
		overclockType = type;
		name = ocName;
		text = ocText;
		icon = upgradeType;
		index = arrayIndex;
		selected = false;
		implemented = ocImplemented;
		ignored = false;
	}
	
	public classification getType() {
		return overclockType;
	}
	public String getName() {
		return name;
	}
	public String getText() {
		return getText(false);
	}
	public String getText(boolean replaceApostrophes) {
		if (replaceApostrophes) {
			// For writing these overclock descriptions into the database, I have to prepend all apostrophes with backslashes so that they don't mess up the INSERT statements.
			// Have to use an escaped backslash so that the literal backslash character gets inserted, which escapes the apostrophe in the INSERTs. It's messy.
			return text.replaceAll("'", "\\\\'");
		}
		else {
			return text;
		}
	}
	public overclockIcons getIcon() {
		return icon;
	}
	public int getIndex() {
		return index;
	}
	
	public void toggleSelected() {
		if (selected) {
			selected = false;
		}
		else {
			selected = true;
			ignored = false;
		}
	}
	public boolean isSelected() {
		return selected;
	}
	
	public boolean isImplemented() {
		return implemented;
	}
	
	public void toggleIgnored() {
		if (ignored) {
			ignored = false;
		}
		else {
			ignored = true;
			selected = false;
		}
	}
	public boolean isIgnored() {
		return ignored;
	}
	
	public String getShortcutRepresentation() {
		return "" + (index + 1);
	}
}
