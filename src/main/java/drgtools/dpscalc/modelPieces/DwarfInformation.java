package drgtools.dpscalc.modelPieces;

import drgtools.dpscalc.modelPieces.temperature.CreatureTemperatureComponent;

public class DwarfInformation {
	// Health starts at 110, but scales to 125 if all armor upgrades are purchased.
	public static int health = 125;
	public static int shield = 25;

	public static CreatureTemperatureComponent playerTemperature = new CreatureTemperatureComponent(50, 25, 12.5, 1.5, -100, -50, 2, 1);
	
	// These are measured in m/sec
	public static double walkSpeed = 3.0;
	public static double runSpeed = 4.35;
	public static double jumpVelocityAdded = 5.0;
}
