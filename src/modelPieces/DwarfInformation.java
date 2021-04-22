package modelPieces;

public class DwarfInformation {
	// Health starts at 110, but scales to 125 if all armor upgrades are purchased.
	public static int health = 125;
	public static int shield = 25;
	
	public static double igniteTemperature = 50;
	public static double douseTemperature = 25;
	public static double heatLossPerSec = 12.5;
	
	public static double freezeTemperature = -100;
	public static double thawTemperature = -50;
	public static double heatGainPerSec = 2;
	
	// These are measured in m/sec
	public static double walkSpeed = 3.0;
	public static double runSpeed = 4.35;
	public static double jumpVelocityAdded = 5.0;
}
