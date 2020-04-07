package modelPieces;

import guiPieces.GuiConstants;
import utilities.MathUtils;

public class StatsRow {

	private String statName;
	private String statValue;
	private boolean highlightModifiedValue;
	private boolean shouldBeDisplayed;
	
	public StatsRow(String name, int value, boolean valueIsModified) {
		statName = name;
		statValue = "" + value;
		highlightModifiedValue = valueIsModified;
		shouldBeDisplayed = true;
	}
	public StatsRow(String name, int value, boolean valueIsModified, boolean display) {
		statName = name;
		statValue = "" + value;
		highlightModifiedValue = valueIsModified;
		shouldBeDisplayed = display;
	}
	
	public StatsRow(String name, double value, boolean valueIsModified) {
		statName = name;
		statValue = "" + MathUtils.round(value, GuiConstants.numDecimalPlaces);
		highlightModifiedValue = valueIsModified;
		shouldBeDisplayed = true;
	}
	public StatsRow(String name, double value, boolean valueIsModified, boolean display) {
		statName = name;
		statValue = "" + MathUtils.round(value, GuiConstants.numDecimalPlaces);
		highlightModifiedValue = valueIsModified;
		shouldBeDisplayed = display;
	}
	
	public StatsRow(String name, String value, boolean valueIsModified) {
		statName = name;
		statValue = value;
		highlightModifiedValue = valueIsModified;
		shouldBeDisplayed = true;
	}
	public StatsRow(String name, String value, boolean valueIsModified, boolean display) {
		statName = name;
		statValue = value;
		highlightModifiedValue = valueIsModified;
		shouldBeDisplayed = display;
	}
	
	public String getName() {
		return statName;
	}
	public String getValue() {
		return statValue;
	}
	public boolean shouldValueBeHighlighted() {
		return highlightModifiedValue;
	}
	public boolean shouldBeDisplayed() {
		return shouldBeDisplayed;
	}
}
