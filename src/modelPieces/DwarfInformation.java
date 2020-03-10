package modelPieces;

public class DwarfInformation {
	// Health starts at 110, but scales to 125 if all armor upgrades are purchased.
	public static int health = 125;
	public static int shield = 25;
	
	// TODO: verify these "temperature" numbers
	public static double igniteTemperature = 150;
	public static double douseTemperature = 75;
	public static double heatLossPerSec = 40;
	
	public static double freezeTemperature = -150;
	public static double thawTemperature = -75;
	public static double heatGainPerSec = 3;
	
	// These are measured in m/sec
	public static double walkSpeed = 3.0;
	public static double runSpeedModifier = 1.4;
}
