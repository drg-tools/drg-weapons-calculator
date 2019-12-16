package modelPieces;

public class StatsRow {

	private String statName;
	private String statValue;
	private boolean highlightModifiedValue;
	
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
