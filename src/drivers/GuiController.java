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

import buildComparators.CompareAccuracyGraphs;
import buildComparators.CompareMetrics;
import dataGenerator.DatabaseConstants;
import dataGenerator.MetricsCalculator;
import guiPieces.HoverText;
import guiPieces.View;
import modelPieces.EnemyInformation;
import weapons.Weapon;
import weapons.driller.CryoCannon;
import weapons.driller.EPC_ChargeShot;
import weapons.driller.EPC_RegularShot;
import weapons.driller.Flamethrower;
import weapons.driller.Subata;
import weapons.engineer.BreachCutter;
import weapons.engineer.BreachCutter_Projectile;
import weapons.engineer.GrenadeLauncher;
import weapons.engineer.SMG;
import weapons.engineer.Shotgun;
import weapons.gunner.Autocannon;
import weapons.gunner.BurstPistol;
import weapons.gunner.Minigun;
import weapons.gunner.Revolver;
import weapons.scout.AssaultRifle;
import weapons.scout.Boomstick;
import weapons.scout.Classic_FocusShot;
import weapons.scout.Classic_Hipfire;
import weapons.scout.Zhukov;

/*
	Benchmarks: 
		150 Ideal Burst DPS
		100 Ideal Sustained DPS
		125 Sustained + Weakpoint
		8000 Total Damage
*/

public class GuiController implements ActionListener {
	
	private Weapon[] drillerWeapons;
	private Weapon[] engineerWeapons;
	private Weapon[] gunnerWeapons;
	private Weapon[] scoutWeapons;
	private View gui;
	private MetricsCalculator calculator;
	private CompareMetrics metricsComparator;
	private CompareAccuracyGraphs accuracyComparator;
	private JFileChooser folderChooser;
	
	public static void main(String[] args) {
		Weapon[] drillerWeapons = new Weapon[] {new Flamethrower(), new CryoCannon(), new Subata(), new EPC_RegularShot(), new EPC_ChargeShot()};
		Weapon[] engineerWeapons = new Weapon[] {new Shotgun(), new SMG(), new GrenadeLauncher(), new BreachCutter(), new BreachCutter_Projectile()};
		Weapon[] gunnerWeapons = new Weapon[] {new Minigun(), new Autocannon(), new Revolver(), new BurstPistol()};
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
		calculator = new MetricsCalculator(weaponSelected);
		metricsComparator = new CompareMetrics(weaponSelected);
		accuracyComparator = new CompareAccuracyGraphs(weaponSelected);
		folderChooser = new JFileChooser();
		folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	}
	
	private void chooseFolder() {
		int returnVal = folderChooser.showOpenDialog(null);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			calculator.setOutputFolder(folderChooser.getSelectedFile());
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
		
		// Burst DPS
		mysqlCommands.add("    `ideal_burst_dps` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `burst_dps_wp` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `burst_dps_acc` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `burst_dps_aw` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `burst_dps_wp_acc` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `burst_dps_wp_aw` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `burst_dps_acc_aw` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `burst_dps_wp_acc_aw` DOUBLE NOT NULL,\n");
		
		// Sustained DPS
		mysqlCommands.add("    `ideal_sustained_dps` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `sustained_dps_wp` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `sustained_dps_acc` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `sustained_dps_aw` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `sustained_dps_wp_acc` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `sustained_dps_wp_aw` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `sustained_dps_acc_aw` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `sustained_dps_wp_acc_aw` DOUBLE NOT NULL,\n");
		
		// GUI row 2
		mysqlCommands.add("    `ideal_additional_target_dps` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `max_num_targets_per_shot` INT NOT NULL,\n");
		mysqlCommands.add("    `max_multi_target_damage` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `ammo_efficiency` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `damage_wasted_by_armor` DOUBLE NOT NULL,\n");
		
		// GUI row 3
		mysqlCommands.add("    `general_accuracy` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `weakpoint_accuracy` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `firing_duration` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `average_time_to_kill` DOUBLE NOT NULL,\n");
		
		// GUI row 4
		mysqlCommands.add("    `average_overkill` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `breakpoints` INT NOT NULL,\n");
		mysqlCommands.add("    `utility` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `average_time_to_ignite_or_freeze` DOUBLE NOT NULL,\n");
		
		// Metrics not on GUI 
		mysqlCommands.add("    `damage_per_magazine` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `time_to_fire_magazine` DOUBLE NOT NULL,\n");
		
		mysqlCommands.add("    `patch_id` BIGINT UNSIGNED NOT NULL,\n\n");
		mysqlCommands.add("    PRIMARY KEY (`id`),\n\n");
		mysqlCommands.add("    FOREIGN KEY (`character_id`)\n");
		mysqlCommands.add("        REFERENCES characters(`id`),\n\n");
		mysqlCommands.add("    FOREIGN KEY (`gun_id`)\n");
		mysqlCommands.add("        REFERENCES guns(`id`),\n\n");
		mysqlCommands.add("    FOREIGN KEY (`patch_id`)\n");
		mysqlCommands.add("        REFERENCES patches(`id`)\n");
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
			mysqlCommands.add("    `icon` VARCHAR(50) NOT NULL,\n");
			mysqlCommands.add("    `mod_type` VARCHAR(20) NOT NULL,\n");
			
			mysqlCommands.add("    `patch_id` BIGINT UNSIGNED NOT NULL,\n\n");
			mysqlCommands.add("    PRIMARY KEY (`id`),\n\n");
			mysqlCommands.add("    FOREIGN KEY (`character_id`)\n");
			mysqlCommands.add("        REFERENCES characters(`id`),\n\n");
			mysqlCommands.add("    FOREIGN KEY (`gun_id`)\n");
			mysqlCommands.add("        REFERENCES guns(`id`),\n\n");
			mysqlCommands.add("    FOREIGN KEY (`patch_id`)\n");
			mysqlCommands.add("        REFERENCES patches(`id`)\n");
			mysqlCommands.add(");\n\n");
		}
		else {
			filenamePrefix = "changed_";
		}
		
		int i;
		for (i = 0; i < drillerWeapons.length; i++) {
			// Skip the EPC Charge Shot since it would have identical info as EPC Regular Shot
			if (i != 4) {
				mysqlCommands.addAll(drillerWeapons[i].exportModsToMySQL(exportAll));
			}
		}
		// Skip BreachCutter_Projectile since it would have identical info as BreachCutter
		for (i = 0; i < engineerWeapons.length - 1; i++) {
			mysqlCommands.addAll(engineerWeapons[i].exportModsToMySQL(exportAll));
		}
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
			mysqlCommands.add("    `icon` VARCHAR(50) NOT NULL,\n");
			
			mysqlCommands.add("    `patch_id` BIGINT UNSIGNED NOT NULL,\n\n");
			mysqlCommands.add("    PRIMARY KEY (`id`),\n\n");
			mysqlCommands.add("    FOREIGN KEY (`character_id`)\n");
			mysqlCommands.add("        REFERENCES characters(`id`),\n\n");
			mysqlCommands.add("    FOREIGN KEY (`gun_id`)\n");
			mysqlCommands.add("        REFERENCES guns(`id`),\n\n");
			mysqlCommands.add("    FOREIGN KEY (`patch_id`)\n");
			mysqlCommands.add("        REFERENCES patches(`id`)\n");
			mysqlCommands.add(");\n\n");
		}
		
		for (i = 0; i < drillerWeapons.length; i++) {
			// Skip the EPC Charge Shot since it would have identical info as EPC Regular Shot
			if (i != 4) {
				mysqlCommands.addAll(drillerWeapons[i].exportOCsToMySQL(exportAll));
			}
		}
		// Skip BreachCutter_Projectile since it would have identical info as BreachCutter
		for (i = 0; i < engineerWeapons.length - 1; i++) {
			mysqlCommands.addAll(engineerWeapons[i].exportOCsToMySQL(exportAll));
		}
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
		String currentlyEquippedCombination;
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
		
		currentlyEquippedCombination = currentlySelectedWeapon.getCombination();
		calculator.changeWeapon(currentlySelectedWeapon);
		// This method is coded to do nothing if it's trying to change the weapon to the one that's already selected
		metricsComparator.changeWeapon(currentlySelectedWeapon);
		accuracyComparator.changeWeapon(currentlySelectedWeapon);
		
		// There are currently 15 options for Best Combinations menus
		int i;
		for (i = 0; i < 15; i++) {
			if (e == gui.getOverallBestCombination(i)) {
				
				gui.activateThinkingCursor();
				
				/*
					Because BCA metrics can be applied to all models, I chose to add 3 checkboxes into the menu that lets users select which of the 8 varieties of DPS they want
					Technically these checkboxes are redundant if only calculating the BCA metric for the currently selected weapon (due to the toggle buttons in the GUI pane)
					but they become necessary when finding the best DPS metric for all 20 models.
				*/
				boolean[] bcaDPSCheckboxValues = gui.getDPSCheckboxValues();
				
				if (gui.calculateBestMetricAllModelsEnabled()) {
					// When this checkbox is selected, then all models in the GUI should run this metric in sequence.
					int j;
					for (j = 0; j < drillerWeapons.length; j++) {
						drillerWeapons[j].setWeakpointDPS(bcaDPSCheckboxValues[0], false);
						drillerWeapons[j].setAccuracyDPS(bcaDPSCheckboxValues[1], false);
						drillerWeapons[j].setArmorWastingDPS(bcaDPSCheckboxValues[2], false);
						
						calculator.changeWeapon(drillerWeapons[j]);
						drillerWeapons[j].buildFromCombination(calculator.getBestMetricCombination(i, false));
					}
					for (j = 0; j < engineerWeapons.length; j++) {
						engineerWeapons[j].setWeakpointDPS(bcaDPSCheckboxValues[0], false);
						engineerWeapons[j].setAccuracyDPS(bcaDPSCheckboxValues[1], false);
						engineerWeapons[j].setArmorWastingDPS(bcaDPSCheckboxValues[2], false);
						
						calculator.changeWeapon(engineerWeapons[j]);
						engineerWeapons[j].buildFromCombination(calculator.getBestMetricCombination(i, false));
					}
					for (j = 0; j < gunnerWeapons.length; j++) {
						gunnerWeapons[j].setWeakpointDPS(bcaDPSCheckboxValues[0], false);
						gunnerWeapons[j].setAccuracyDPS(bcaDPSCheckboxValues[1], false);
						gunnerWeapons[j].setArmorWastingDPS(bcaDPSCheckboxValues[2], false);
						
						calculator.changeWeapon(gunnerWeapons[j]);
						gunnerWeapons[j].buildFromCombination(calculator.getBestMetricCombination(i, false));
					}
					for (j = 0; j < scoutWeapons.length; j++) {
						scoutWeapons[j].setWeakpointDPS(bcaDPSCheckboxValues[0], false);
						scoutWeapons[j].setAccuracyDPS(bcaDPSCheckboxValues[1], false);
						scoutWeapons[j].setArmorWastingDPS(bcaDPSCheckboxValues[2], false);
						
						calculator.changeWeapon(scoutWeapons[j]);
						scoutWeapons[j].buildFromCombination(calculator.getBestMetricCombination(i, false));
					}
					
					// Remember to change back to the weapon showing on the tab when they clicked this action
					calculator.changeWeapon(currentlySelectedWeapon);
				}
				else {
					currentlySelectedWeapon.setWeakpointDPS(bcaDPSCheckboxValues[0], false);
					currentlySelectedWeapon.setAccuracyDPS(bcaDPSCheckboxValues[1], false);
					currentlySelectedWeapon.setArmorWastingDPS(bcaDPSCheckboxValues[2], false);
					
					currentlySelectedWeapon.buildFromCombination(calculator.getBestMetricCombination(i, false));
				}
				
				gui.deactivateThinkingCursor();
				
				// Empty return so that this method doesn't have to finish this for loop or evaluate the if/else block below afterwards
				return;
			}
			else if (e == gui.getSubsetBestCombination(i)) {
				gui.activateThinkingCursor();
				
				// Because Best Combinations (Subset) only runs for the weapon shown on GUI at the moment, I have chosen not to add the 3 checkboxes for DPS metrics in the menu itself.
				// If a user wants to know the subset combination for those metrics, they can toggle the buttons on the GUI and re-run BCS.
				currentlySelectedWeapon.buildFromCombination(calculator.getBestMetricCombination(i, true));
				
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
			calculator.exportMetricsToCSV();
			gui.deactivateThinkingCursor();
		}
		else if (e == gui.getExportAll()) {
			chooseFolder();
			gui.activateThinkingCursor();
			for (i = 0; i < drillerWeapons.length; i++) {
				calculator.changeWeapon(drillerWeapons[i]);
				calculator.exportMetricsToCSV();
			}
			for (i = 0; i < engineerWeapons.length; i++) {
				calculator.changeWeapon(engineerWeapons[i]);
				calculator.exportMetricsToCSV();
			}
			for (i = 0; i < gunnerWeapons.length; i++) {
				calculator.changeWeapon(gunnerWeapons[i]);
				calculator.exportMetricsToCSV();
			}
			for (i = 0; i < scoutWeapons.length; i++) {
				calculator.changeWeapon(scoutWeapons[i]);
				calculator.exportMetricsToCSV();
			}
			gui.deactivateThinkingCursor();
		}
		else if (e == gui.getExportMetricsMySQL()) {
			chooseFolder();
			gui.activateThinkingCursor();
			createMetricsMysqlFile();
			gui.deactivateThinkingCursor();
		}
		/*
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
		*/
		
		else if (e == gui.getCompareBuildMetrics()) {
			// Adapted from https://stackoverflow.com/a/13760416 and https://www.tutorialspoint.com/how-to-display-a-jframe-to-the-center-of-a-screen-in-java
			JOptionPane a = new JOptionPane(metricsComparator.getComparisonPanel(), JOptionPane.INFORMATION_MESSAGE);
			JDialog d = a.createDialog(null, "Compare multiple builds directly, metric-to-metric");
			d.setLocationRelativeTo(gui);
			d.setVisible(true);
		}
		else if (e == gui.getCompareAccuracyGraphs()) {
			if (currentlySelectedWeapon.getGeneralAccuracy() < 0.0) {
				JOptionPane.showMessageDialog(gui, "This weapon doesn't use the Accuracy metrics in a meaningful way. As such, trying to compare builds in this manner is useless.", "Pointless Comparison", JOptionPane.INFORMATION_MESSAGE);
			}
			else {
				// Adapted from https://stackoverflow.com/a/13760416 and https://www.tutorialspoint.com/how-to-display-a-jframe-to-the-center-of-a-screen-in-java
				JOptionPane a = new JOptionPane(accuracyComparator.getComparisonPanel(), JOptionPane.INFORMATION_MESSAGE);
				JDialog d = a.createDialog(null, "Visually compare metrics affected by Accuracy and distance");
				d.setLocationRelativeTo(gui);
				d.setVisible(true);
			}
		}
		else if (e == gui.getCompareLoadCombinationIntoColumn(0)) {
			metricsComparator.setNewBuildAtIndex(0, currentlyEquippedCombination);
			accuracyComparator.setNewBuildAtIndex(0, currentlyEquippedCombination);
		}
		else if (e == gui.getCompareLoadCombinationIntoColumn(1)) {
			metricsComparator.setNewBuildAtIndex(1, currentlyEquippedCombination);
			accuracyComparator.setNewBuildAtIndex(1, currentlyEquippedCombination);
		}
		else if (e == gui.getCompareLoadCombinationIntoColumn(2)) {
			metricsComparator.setNewBuildAtIndex(2, currentlyEquippedCombination);
			accuracyComparator.setNewBuildAtIndex(2, currentlyEquippedCombination);
		}
		else if (e == gui.getCompareLoadCombinationIntoColumn(3)) {
			metricsComparator.setNewBuildAtIndex(3, currentlyEquippedCombination);
			accuracyComparator.setNewBuildAtIndex(3, currentlyEquippedCombination);
		}
		
		else if (e == gui.getMiscScreenshot()) {
			chooseFolder();
			String weaponClass = currentlySelectedWeapon.getDwarfClass();
			String weaponName = currentlySelectedWeapon.getSimpleName();
			File pngOut = new File(calculator.getOutputFolder(), weaponClass + "_" + weaponName + "_" + currentlyEquippedCombination + ".png");
			
			// Sourced from https://stackoverflow.com/a/44019372
			BufferedImage screenshot = gui.getScreenshot();
			try {
				ImageIO.write(screenshot, "png", pngOut);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		else if (e == gui.getMiscExport()) {
			JTextField output = new JTextField(currentlyEquippedCombination);
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
			instructions = HoverText.breakLongToolTipString(instructions, 90, false);
			
			String displayedMessage = "";
			String newCombination;
			boolean isValidCombination = false;
			String errorMessage = "";
			boolean showErrorMessage = false;
			while(true) {
				if (showErrorMessage) {
					displayedMessage = "<html><body>" + instructions + "<span style=\"color: red\">" + errorMessage + "</span></body></html>";
				}
				else {
					displayedMessage = "<html><body>" + instructions + "</body></html>";
				}
				
				newCombination = JOptionPane.showInputDialog(gui, displayedMessage);
				
				if (newCombination != null) {
					isValidCombination = currentlySelectedWeapon.isCombinationValid(newCombination);
					
					if (isValidCombination) {
						currentlySelectedWeapon.buildFromCombination(newCombination);
						break;
					}
					else {
						errorMessage = currentlySelectedWeapon.getInvalidCombinationErrorMessage();
						showErrorMessage = true;
					}
				}
				else {
					// If the user doesn't enter a combination, break the while loop instantly.
					break;
				}
			}
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
