package drivers;

import dataGenerator.WeaponStatsGenerator;
import gunnerWeapons.Autocannon;
import gunnerWeapons.Minigun;
import modelPieces.Weapon;

public class StatisticsGenerator {
	public static void main(String[] args) {
		
		Weapon[] weaponsToGenerateStatisticsAbout = new Weapon[] {
			// Driller weapons
			
			// Engineer weapons
				
			// Gunner weapons
			new Minigun(),
			new Autocannon(),
				
			// Scout weapons
		};
		
		if (weaponsToGenerateStatisticsAbout.length < 1) {
			System.out.println("At least one Weapon must be declared for this driver to work.");
			return;
		}
		
		WeaponStatsGenerator wsg = new WeaponStatsGenerator(weaponsToGenerateStatisticsAbout[0]);
		wsg.setCSVFolderPath("D:\\Files\\DRG_CSV_files");
		boolean printStatsToConsole = true;
		boolean exportStatsToCSV = false;
		for (int i = 0; i < weaponsToGenerateStatisticsAbout.length; i++) {
			if (i > 0) {
				wsg.changeWeapon(weaponsToGenerateStatisticsAbout[i]);
			}
			wsg.runTest(printStatsToConsole, exportStatsToCSV);
		}
	}
}
