package modelPieces;

import guiPieces.GuiConstants;
import guiPieces.customButtons.ButtonIcons.modIcons;
import utilities.MathUtils;

public class StatsRow {

	private String statName;
	private String statValue;
	private modIcons icon;
	private boolean highlightModifiedValue;
	private boolean shouldBeDisplayed;
	
	public StatsRow(String name, int value, modIcons statType, boolean valueIsModified) {
		statName = name;
		statValue = "" + value;
		icon = statType;
		highlightModifiedValue = valueIsModified;
		shouldBeDisplayed = true;
	}
	public StatsRow(String name, int value, modIcons statType, boolean valueIsModified, boolean display) {
		statName = name;
		statValue = "" + value;
		icon = statType;
		highlightModifiedValue = valueIsModified;
		shouldBeDisplayed = display;
	}
	
	public StatsRow(String name, double value, modIcons statType, boolean valueIsModified) {
		statName = name;
		statValue = "" + MathUtils.round(value, GuiConstants.numDecimalPlaces);
		icon = statType;
		highlightModifiedValue = valueIsModified;
		shouldBeDisplayed = true;
	}
	public StatsRow(String name, double value, modIcons statType, boolean valueIsModified, boolean display) {
		statName = name;
		statValue = "" + MathUtils.round(value, GuiConstants.numDecimalPlaces);
		icon = statType;
		highlightModifiedValue = valueIsModified;
		shouldBeDisplayed = display;
	}
	
	public StatsRow(String name, String value, modIcons statType, boolean valueIsModified) {
		statName = name;
		statValue = value;
		icon = statType;
		highlightModifiedValue = valueIsModified;
		shouldBeDisplayed = true;
	}
	public StatsRow(String name, String value, modIcons statType, boolean valueIsModified, boolean display) {
		statName = name;
		statValue = value;
		icon = statType;
		highlightModifiedValue = valueIsModified;
		shouldBeDisplayed = display;
	}
	
	public String getName() {
		return statName;
	}
	public String getValue() {
		return statValue;
	}
	public modIcons getIcon() {
		return icon;
	}
	public boolean shouldValueBeHighlighted() {
		return highlightModifiedValue;
	}
	public boolean shouldBeDisplayed() {
		return shouldBeDisplayed;
	}
}
