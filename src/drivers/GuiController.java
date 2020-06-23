package drivers;

import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import dataGenerator.DatabaseConstants;
import dataGenerator.WeaponStatsGenerator;
import drillerWeapons.CryoCannon;
import drillerWeapons.EPC_ChargeShot;
import drillerWeapons.EPC_RegularShot;
import drillerWeapons.Flamethrower;
import drillerWeapons.Subata;
import engineerWeapons.BreachCutter;
import engineerWeapons.GrenadeLauncher;
import engineerWeapons.SMG;
import engineerWeapons.Shotgun;
import guiPieces.HoverText;
import guiPieces.View;
import gunnerWeapons.Autocannon;
import gunnerWeapons.BurstPistol;
import gunnerWeapons.Minigun;
import gunnerWeapons.Revolver_FullRoF;
import gunnerWeapons.Revolver_Snipe;
import modelPieces.EnemyInformation;
import modelPieces.Weapon;
import scoutWeapons.Boomstick;
import scoutWeapons.Classic_FocusShot;
import scoutWeapons.Classic_Hipfire;
import scoutWeapons.AssaultRifle;
import scoutWeapons.Zhukov;

/*
	Benchmarks: 
		150 Ideal Burst DPS
		100 Ideal Sustained DPS
		125 Sustained + Weakpoint
		8000 Total Damage
*/

// TODO: manually write up the equipment, grenades, and armor DB files

public class GuiController implements ActionListener {
	
	private Weapon[] drillerWeapons;
	private Weapon[] engineerWeapons;
	private Weapon[] gunnerWeapons;
	private Weapon[] scoutWeapons;
	private View gui;
	private WeaponStatsGenerator calculator;
	private JFileChooser folderChooser;
	
	public static void main(String[] args) {
		Weapon[] drillerWeapons = new Weapon[] {new Flamethrower(), new CryoCannon(), new Subata(), new EPC_RegularShot(), new EPC_ChargeShot()};
		Weapon[] engineerWeapons = new Weapon[] {new Shotgun(), new SMG(), new GrenadeLauncher()};
		Weapon[] gunnerWeapons = new Weapon[] {new Minigun(), new Autocannon(), new Revolver_Snipe(), new Revolver_FullRoF(), new BurstPistol()};
		Weapon[] scoutWeapons = new Weapon[] {new AssaultRifle(), new Classic_Hipfire(), new Classic_FocusShot(), new Boomstick(), new Zhukov()};
		View gui = new View(drillerWeapons, engineerWeapons, gunnerWeapons, scoutWeapons);
		new GuiController(drillerWeapons, engineerWeapons, gunnerWeapons, scoutWeapons, gui);
	}
	
	public GuiController(Weapon[] dWeapons, Weapon[] eWeapons, Weapon[] gWeapons, Weapon[] sWeapons, View inputGui) {
		drillerWeapons = dWeapons;
		engineerWeapons = eWeapons;
		gunnerWeapons = gWeapons;
		scoutWeapons = sWeapons;
		gui = inputGui;
		gui.activateButtonsAndMenus(this);
		Weapon weaponSelected;
		if (drillerWeapons.length > 0) {
			weaponSelected = drillerWeapons[0];
		}
		else if (engineerWeapons.length > 0) {
			weaponSelected = engineerWeapons[0];
		}
		else if (gunnerWeapons.length > 0) {
			weaponSelected = gunnerWeapons[0];
		}
		else if (scoutWeapons.length > 0) {
			weaponSelected = scoutWeapons[0];
		}
		else {
			System.out.println("Error: no weapons in GuiController's arrays");
			weaponSelected = new Minigun();
		}
		calculator = new WeaponStatsGenerator(weaponSelected);
		folderChooser = new JFileChooser();
		folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	}
	
	private void chooseFolder() {
		int returnVal = folderChooser.showOpenDialog(null);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			calculator.changeOutputFolder(folderChooser.getSelectedFile());
		}
	}
	
	private void createMetricsMysqlFile() {
		ArrayList<String> mysqlCommands = new ArrayList<String>();
		mysqlCommands.add(String.format("USE `%s`;\n\n", DatabaseConstants.databaseName));
		mysqlCommands.add(String.format("DROP TABLE IF EXISTS `%s`;\n\n", DatabaseConstants.statsTableName));
		mysqlCommands.add(String.format("CREATE TABLE `%s` (\n", DatabaseConstants.statsTableName));
		mysqlCommands.add("    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,\n");
		mysqlCommands.add("    `character_id` BIGINT UNSIGNED NOT NULL,\n");
		mysqlCommands.add("    `gun_id` BIGINT UNSIGNED NOT NULL,\n");
		mysqlCommands.add("    `weapon_short_name` VARCHAR(20) NOT NULL,\n");
		mysqlCommands.add("    `build_combination` VARCHAR(6) NOT NULL,\n");
		mysqlCommands.add("    `ideal_burst_dps` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `ideal_sustained_dps` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `sustained_weakpoint_dps` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `sustained_weakpoint_accuracy_dps` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `ideal_additional_target_dps` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `max_num_targets_per_shot` INT NOT NULL,\n");
		mysqlCommands.add("    `max_multi_target_damage` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `ammo_efficiency` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `general_accuracy` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `weakpoint_accuracy` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `firing_duration` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `average_overkill` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `average_time_to_kill` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `breakpoints` INT NOT NULL,\n");
		mysqlCommands.add("    `utility` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `damage_per_magazine` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `time_to_fire_magazine` DOUBLE NOT NULL,\n\n");
		mysqlCommands.add("    PRIMARY KEY (`id`),\n\n");
		mysqlCommands.add("    FOREIGN KEY (`character_id`)\n");
		mysqlCommands.add("        REFERENCES characters(`id`),\n\n");
		mysqlCommands.add("    FOREIGN KEY (`gun_id`)\n");
		mysqlCommands.add("        REFERENCES guns(`id`)\n");
		mysqlCommands.add(");\n\n");
		
		int i;
		for (i = 0; i < drillerWeapons.length; i++) {
			calculator.changeWeapon(drillerWeapons[i]);
			mysqlCommands.addAll(calculator.dumpMetricsToMySQL());
		}
		for (i = 0; i < engineerWeapons.length; i++) {
			calculator.changeWeapon(engineerWeapons[i]);
			mysqlCommands.addAll(calculator.dumpMetricsToMySQL());
		}
		for (i = 0; i < gunnerWeapons.length; i++) {
			calculator.changeWeapon(gunnerWeapons[i]);
			mysqlCommands.addAll(calculator.dumpMetricsToMySQL());
		}
		for (i = 0; i < scoutWeapons.length; i++) {
			calculator.changeWeapon(scoutWeapons[i]);
			mysqlCommands.addAll(calculator.dumpMetricsToMySQL());
		}
		
		// Open the MySQL file once, then dump the accumulated ArrayList of lines all at once to minimize I/O time
		// Set append=False so that it clears out the old file
		calculator.writeFile(mysqlCommands, DatabaseConstants.statsTableName + ".sql", false);
	}
	
	private void createModsOCsMysqlFiles(boolean exportAll) {
		ArrayList<String> mysqlCommands = new ArrayList<String>();
		mysqlCommands.add(String.format("USE `%s`;\n\n", DatabaseConstants.databaseName));
		
		String filenamePrefix = "";
		if (exportAll) {
			mysqlCommands.add(String.format("DROP TABLE IF EXISTS `%s`;\n\n", DatabaseConstants.modsTableName));
			mysqlCommands.add(String.format("CREATE TABLE `%s` (\n", DatabaseConstants.modsTableName));
			mysqlCommands.add("    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,\n");
			mysqlCommands.add("    `character_id` BIGINT UNSIGNED NOT NULL,\n");
			mysqlCommands.add("    `gun_id` BIGINT UNSIGNED NOT NULL,\n");
			mysqlCommands.add("    `mod_tier` TINYINT UNSIGNED NOT NULL,\n");
			mysqlCommands.add("    `mod_index` VARCHAR(1) NOT NULL,\n");
			mysqlCommands.add("    `mod_name` VARCHAR(50) NOT NULL,\n");
			mysqlCommands.add("    `credits_cost` SMALLINT UNSIGNED NOT NULL,\n");
			mysqlCommands.add("    `magnite_cost` TINYINT UNSIGNED NOT NULL,\n");
			mysqlCommands.add("    `bismor_cost` TINYINT UNSIGNED NOT NULL,\n");
			mysqlCommands.add("    `umanite_cost` TINYINT UNSIGNED NOT NULL,\n");
			mysqlCommands.add("    `croppa_cost` TINYINT UNSIGNED NOT NULL,\n");
			mysqlCommands.add("    `enor_pearl_cost` TINYINT UNSIGNED NOT NULL,\n");
			mysqlCommands.add("    `jadiz_cost` TINYINT UNSIGNED NOT NULL,\n");
			
			mysqlCommands.add("    `text_description` VARCHAR(1000) NOT NULL,\n");
			mysqlCommands.add("    `json_stats` VARCHAR(1000) NOT NULL,\n");
			mysqlCommands.add("    `icon` VARCHAR(1000) NOT NULL,\n");
			mysqlCommands.add("    `mod_type` VARCHAR(1000) NOT NULL,\n");
			
			mysqlCommands.add("    `patch_number_index` BIGINT UNSIGNED NOT NULL,\n\n");
			mysqlCommands.add("    PRIMARY KEY (`id`),\n\n");
			mysqlCommands.add("    FOREIGN KEY (`character_id`)\n");
			mysqlCommands.add("        REFERENCES characters(`id`),\n\n");
			mysqlCommands.add("    FOREIGN KEY (`gun_id`)\n");
			mysqlCommands.add("        REFERENCES guns(`id`)\n");
			mysqlCommands.add(");\n\n");
		}
		else {
			filenamePrefix = "changed_";
		}
		
		// Breach Cutter isn't fully fleshed out; I just have a skeleton written for mod/OC costs used in this method.
		Weapon bc = new BreachCutter();
		int i;
		for (i = 0; i < drillerWeapons.length; i++) {
			// Skip the EPC Charge Shot since it would have identical info as EPC Regular Shot
			if (i != 4) {
				mysqlCommands.addAll(drillerWeapons[i].exportModsToMySQL(exportAll));
			}
		}
		for (i = 0; i < engineerWeapons.length; i++) {
			mysqlCommands.addAll(engineerWeapons[i].exportModsToMySQL(exportAll));
		}
		mysqlCommands.addAll(bc.exportModsToMySQL(exportAll));
		for (i = 0; i < gunnerWeapons.length; i++) {
			// Skip Revolver Snipe since it would have identical info as Revolver Max RoF
			if (i != 2) {
				mysqlCommands.addAll(gunnerWeapons[i].exportModsToMySQL(exportAll));
			}
		}
		for (i = 0; i < scoutWeapons.length; i++) {
			// Skip M1000 Hipfire since it would have identical info as M1000 Focused Shots
			if (i != 1) {
				mysqlCommands.addAll(scoutWeapons[i].exportModsToMySQL(exportAll));
			}
		}
		
		// Open the MySQL file once, then dump the accumulated ArrayList of lines all at once to minimize I/O time
		// Set append=False so that it clears out the old file
		calculator.writeFile(mysqlCommands, filenamePrefix + DatabaseConstants.modsTableName + ".sql", false);
		
		mysqlCommands = new ArrayList<String>();
		mysqlCommands.add(String.format("USE `%s`;\n\n", DatabaseConstants.databaseName));
		if (exportAll) {
			mysqlCommands.add(String.format("DROP TABLE IF EXISTS `%s`;\n\n", DatabaseConstants.OCsTableName));
			mysqlCommands.add(String.format("CREATE TABLE `%s` (\n", DatabaseConstants.OCsTableName));
			mysqlCommands.add("    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,\n");
			mysqlCommands.add("    `character_id` BIGINT UNSIGNED NOT NULL,\n");
			mysqlCommands.add("    `gun_id` BIGINT UNSIGNED NOT NULL,\n");
			mysqlCommands.add("    `overclock_type` VARCHAR(20) NOT NULL,\n");
			mysqlCommands.add("    `overclock_index` TINYINT UNSIGNED NOT NULL,\n");
			mysqlCommands.add("    `overclock_name` VARCHAR(50) NOT NULL,\n");
			mysqlCommands.add("    `credits_cost` SMALLINT UNSIGNED NOT NULL,\n");
			mysqlCommands.add("    `magnite_cost` TINYINT UNSIGNED NOT NULL,\n");
			mysqlCommands.add("    `bismor_cost` TINYINT UNSIGNED NOT NULL,\n");
			mysqlCommands.add("    `umanite_cost` TINYINT UNSIGNED NOT NULL,\n");
			mysqlCommands.add("    `croppa_cost` TINYINT UNSIGNED NOT NULL,\n");
			mysqlCommands.add("    `enor_pearl_cost` TINYINT UNSIGNED NOT NULL,\n");
			mysqlCommands.add("    `jadiz_cost` TINYINT UNSIGNED NOT NULL,\n");
			
			mysqlCommands.add("    `text_description` VARCHAR(1000) NOT NULL,\n");
			mysqlCommands.add("    `json_stats` VARCHAR(1000) NOT NULL,\n");
			mysqlCommands.add("    `icon` VARCHAR(1000) NOT NULL,\n");
			
			mysqlCommands.add("    `patch_number_index` BIGINT UNSIGNED NOT NULL,\n\n");
			mysqlCommands.add("    PRIMARY KEY (`id`),\n\n");
			mysqlCommands.add("    FOREIGN KEY (`character_id`)\n");
			mysqlCommands.add("        REFERENCES characters(`id`),\n\n");
			mysqlCommands.add("    FOREIGN KEY (`gun_id`)\n");
			mysqlCommands.add("        REFERENCES guns(`id`)\n");
			mysqlCommands.add(");\n\n");
		}
		
		for (i = 0; i < drillerWeapons.length; i++) {
			// Skip the EPC Charge Shot since it would have identical info as EPC Regular Shot
			if (i != 4) {
				mysqlCommands.addAll(drillerWeapons[i].exportOCsToMySQL(exportAll));
			}
		}
		for (i = 0; i < engineerWeapons.length; i++) {
			mysqlCommands.addAll(engineerWeapons[i].exportOCsToMySQL(exportAll));
		}
		mysqlCommands.addAll(bc.exportOCsToMySQL(exportAll));
		for (i = 0; i < gunnerWeapons.length; i++) {
			// Skip Revolver Snipe since it would have identical info as Revolver Max RoF
			if (i != 2) {
				mysqlCommands.addAll(gunnerWeapons[i].exportOCsToMySQL(exportAll));
			}
		}
		for (i = 0; i < scoutWeapons.length; i++) {
			// Skip M1000 Hipfire since it would have identical info as M1000 Focused Shots
			if (i != 1) {
				mysqlCommands.addAll(scoutWeapons[i].exportOCsToMySQL(exportAll));
			}
		}
		
		calculator.writeFile(mysqlCommands, filenamePrefix + DatabaseConstants.OCsTableName + ".sql", false);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Object e = arg0.getSource();
		
		Weapon currentlySelectedWeapon;
		int classIndex = gui.getCurrentClassIndex();
		
		// Have these commands disabled when Information is at the front.
		if (classIndex > 3) {
			return;
		}
		
		int weaponIndex = gui.getCurrentWeaponIndex();
		if (classIndex == 0) {
			currentlySelectedWeapon = drillerWeapons[weaponIndex];
		}
		else if (classIndex == 1) {
			currentlySelectedWeapon = engineerWeapons[weaponIndex];
		}
		else if (classIndex == 2) {
			currentlySelectedWeapon = gunnerWeapons[weaponIndex];
		}
		else if (classIndex == 3) {
			currentlySelectedWeapon = scoutWeapons[weaponIndex];
		}
		else {
			System.out.println("Error: no weapons in GuiController's arrays");
			currentlySelectedWeapon = new Minigun();
		}
		calculator.changeWeapon(currentlySelectedWeapon);
		
		int[] tier1Subset, tier2Subset, tier3Subset, tier4Subset, tier5Subset, ocsSubset;
		int t1 = currentlySelectedWeapon.getSelectedModAtTier(1);
		if (t1 > -1) {
			tier1Subset = new int[] {t1, t1};
		}
		else {
			// Have to subtract 1 from the length since the for loop this gets fed to uses <= instead of just <
			tier1Subset = new int[] {-1, currentlySelectedWeapon.getModsAtTier(1).length - 1};
		}
		int t2 = currentlySelectedWeapon.getSelectedModAtTier(2);
		if (t2 > -1) {
			tier2Subset = new int[] {t2, t2};
		}
		else {
			tier2Subset = new int[] {-1, currentlySelectedWeapon.getModsAtTier(2).length - 1};
		}
		int t3 = currentlySelectedWeapon.getSelectedModAtTier(3);
		if (t3 > -1) {
			tier3Subset = new int[] {t3, t3};
		}
		else {
			tier3Subset = new int[] {-1, currentlySelectedWeapon.getModsAtTier(3).length - 1};
		}
		int t4 = currentlySelectedWeapon.getSelectedModAtTier(4);
		if (t4 > -1) {
			tier4Subset = new int[] {t4, t4};
		}
		else {
			tier4Subset = new int[] {-1, currentlySelectedWeapon.getModsAtTier(4).length - 1};
		}
		int t5 = currentlySelectedWeapon.getSelectedModAtTier(5);
		if (t5 > -1) {
			tier5Subset = new int[] {t5, t5};
		}
		else {
			tier5Subset = new int[] {-1, currentlySelectedWeapon.getModsAtTier(5).length - 1};
		}
		int oc = currentlySelectedWeapon.getSelectedOverclock();
		if (oc > -1) {
			ocsSubset = new int[] {oc, oc};
		}
		else {
			ocsSubset = new int[] {-1, currentlySelectedWeapon.getOverclocks().length - 1};
		}
		
		for (int i = 0; i < currentlySelectedWeapon.getBaselineStats().length; i++) {
			if (e == gui.getOverallBestCombination(i)) {
				gui.activateThinkingCursor();
				currentlySelectedWeapon.buildFromCombination(calculator.getBestMetricCombination(i));
				gui.deactivateThinkingCursor();
				
				// Empty return so that this method doesn't have to finish this for loop or evaluate the if/else block below afterwards
				return;
			}
			else if (e == gui.getSubsetBestCombination(i)) {
				gui.activateThinkingCursor();
				currentlySelectedWeapon.buildFromCombination(calculator.getBestMetricCombination(i, tier1Subset, tier2Subset, tier3Subset, tier4Subset, tier5Subset, ocsSubset));
				gui.deactivateThinkingCursor();
				
				// Empty return so that this method doesn't have to finish this for loop or evaluate the if/else block below afterwards
				return;
			}
		}
		
		if (e == gui.getDSHaz1()) {
			EnemyInformation.setHazardLevel(1);
			gui.updateDifficultyScaling();
		}
		else if (e == gui.getDSHaz2()) {
			EnemyInformation.setHazardLevel(2);
			gui.updateDifficultyScaling();
		}
		else if (e == gui.getDSHaz3()) {
			EnemyInformation.setHazardLevel(3);
			gui.updateDifficultyScaling();
		}
		else if (e == gui.getDSHaz4()) {
			EnemyInformation.setHazardLevel(4);
			gui.updateDifficultyScaling();
		}
		else if (e == gui.getDSHaz5()) {
			EnemyInformation.setHazardLevel(5);
			gui.updateDifficultyScaling();
		}
		else if (e == gui.getDSPC1()) {
			EnemyInformation.setPlayerCount(1);
			gui.updateDifficultyScaling();
		}
		else if (e == gui.getDSPC2()) {
			EnemyInformation.setPlayerCount(2);
			gui.updateDifficultyScaling();
		}
		else if (e == gui.getDSPC3()) {
			EnemyInformation.setPlayerCount(3);
			gui.updateDifficultyScaling();
		}
		else if (e == gui.getDSPC4()) {
			EnemyInformation.setPlayerCount(4);
			gui.updateDifficultyScaling();
		}
		
		else if (e == gui.getExportCurrent()) {
			chooseFolder();
			gui.activateThinkingCursor();
			calculator.runTest(false, true);
			gui.deactivateThinkingCursor();
		}
		else if (e == gui.getExportAll()) {
			chooseFolder();
			gui.activateThinkingCursor();
			int i;
			for (i = 0; i < drillerWeapons.length; i++) {
				calculator.changeWeapon(drillerWeapons[i]);
				calculator.runTest(false, true);
			}
			for (i = 0; i < engineerWeapons.length; i++) {
				calculator.changeWeapon(engineerWeapons[i]);
				calculator.runTest(false, true);
			}
			for (i = 0; i < gunnerWeapons.length; i++) {
				calculator.changeWeapon(gunnerWeapons[i]);
				calculator.runTest(false, true);
			}
			for (i = 0; i < scoutWeapons.length; i++) {
				calculator.changeWeapon(scoutWeapons[i]);
				calculator.runTest(false, true);
			}
			gui.deactivateThinkingCursor();
		}
		else if (e == gui.getExportMetricsMySQL()) {
			chooseFolder();
			gui.activateThinkingCursor();
			createMetricsMysqlFile();
			gui.deactivateThinkingCursor();
		}
		else if (e == gui.getExportModsOCsMySQL()) {
			chooseFolder();
			gui.activateThinkingCursor();
			createModsOCsMysqlFiles(true);
			gui.deactivateThinkingCursor();
		}
		else if (e == gui.getExportChangedModsOCsMySQL()) {
			chooseFolder();
			gui.activateThinkingCursor();
			createModsOCsMysqlFiles(false);
			gui.deactivateThinkingCursor();
		}
		
		else if (e == gui.getMiscScreenshot()) {
			chooseFolder();
			String weaponClass = currentlySelectedWeapon.getDwarfClass();
			String weaponName = currentlySelectedWeapon.getSimpleName();
			File pngOut = new File(calculator.getOutputFolder(), weaponClass + "_" + weaponName + "_" + currentlySelectedWeapon.getCombination() + ".png");
			
			// Sourced from https://stackoverflow.com/a/44019372
			BufferedImage screenshot = gui.getScreenshot();
			try {
				ImageIO.write(screenshot, "png", pngOut);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		else if (e == gui.getMiscExport()) {
			String combination = currentlySelectedWeapon.getCombination();
			JTextField output = new JTextField(combination);
			output.setFont(new Font("Monospaced", Font.PLAIN, 18));
			
			// Adapted from https://stackoverflow.com/a/13760416 and https://www.tutorialspoint.com/how-to-display-a-jframe-to-the-center-of-a-screen-in-java
			JOptionPane a = new JOptionPane(output, JOptionPane.INFORMATION_MESSAGE);
			JDialog d = a.createDialog(null, "Current weapon combination:");
			d.setLocationRelativeTo(gui);
			d.setVisible(true);
		}
		else if (e == gui.getMiscLoad()) {
			String instructions = "Enter the combination you want to load for this weapon. It should consist of 5 capital letters, A-C, and 1 number, 1-7. Each capital letter "
					+ "corresponds to a mod tier and the number corresponds to the desired overclock. If you do not want to use a mod tier or overclock, substitute the "
					+ "corresponding character with a hyphen.";
			instructions = HoverText.breakLongToolTipString(instructions, 90);
			String newCombination = JOptionPane.showInputDialog(gui, instructions);
			currentlySelectedWeapon.buildFromCombination(newCombination);
		}
		else if (e == gui.getMiscSuggestion()) {
			openWebpage("https://github.com/drg-tools/drg-weapons-calculator/issues/new/choose");
		}
	}
	
	// These methods sourced from https://stackoverflow.com/a/10967469
	private static boolean openWebpage(URI uri) {
	    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
	    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
	        try {
	            desktop.browse(uri);
	            return true;
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	    return false;
	}
	private static boolean openWebpage(URL url) {
	    try {
	        return openWebpage(url.toURI());
	    } catch (URISyntaxException e) {
	        e.printStackTrace();
	    }
	    return false;
	}
	private static boolean openWebpage(String url) {
		try {
			return openWebpage(new URL(url));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return false;
	}
}
