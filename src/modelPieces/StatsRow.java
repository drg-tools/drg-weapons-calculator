package modelPieces;

import utilities.GuiConstants;
import utilities.MathUtils;

public class StatsRow {

	private String statName;
	private String statValue;
	private boolean highlightModifiedValue;
	
	public StatsRow(String name, int value, boolean valueIsModified) {
		statName = name;
		statValue = "" + value;
		highlightModifiedValue = valueIsModified;
	}
	
	public StatsRow(String name, double value, boolean valueIsModified) {
		statName = name;
		statValue = "" + MathUtils.round(value, GuiConstants.numDecimalPlaces);
		highlightModifiedValue = valueIsModified;
	}
	
	public StatsRow(String name, String value, boolean valueIsModified) {
		statName = name;
		statValue = value;
		highlightModifiedValue = valueIsModified;
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
}
