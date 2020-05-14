package drivers;

import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
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
			File selectedFolder = folderChooser.getSelectedFile();
			calculator.setCSVFolderPath(selectedFolder.getAbsolutePath());
		}
	}
	
	private void createMysqlFile() {
		ArrayList<String> mysqlCommands = new ArrayList<String>();
		mysqlCommands.add(String.format("USE `%s`;\n\n", DatabaseConstants.databaseName));
		mysqlCommands.add(String.format("DROP TABLE IF EXISTS `%s`;\n\n", DatabaseConstants.tableName));
		mysqlCommands.add(String.format("CREATE TABLE `%s` (\n", DatabaseConstants.tableName));
		mysqlCommands.add("    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,\n");
		mysqlCommands.add("    `characters_id` BIGINT UNSIGNED NOT NULL,\n");
		mysqlCommands.add("    `guns_id` BIGINT UNSIGNED NOT NULL,\n");
		mysqlCommands.add("    `weaponShortName` VARCHAR(20) NOT NULL,\n");
		mysqlCommands.add("    `buildCombination` VARCHAR(6) NOT NULL,\n");
		mysqlCommands.add("    `idealBurstDPS` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `idealSustainedDPS` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `sustainedWeakpointDPS` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `sustainedWeakpointAccuracyDPS` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `idealAdditionalTargetDPS` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `maxMultiTargetDamage` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `maxNumTargetsPerShot` INT NOT NULL,\n");
		mysqlCommands.add("    `firingDuration` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `averageTimeToKill` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `averageOverkill` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `generalAccuracy` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    `utility` DOUBLE NOT NULL,\n");
		mysqlCommands.add("    PRIMARY KEY (`id`),\n\n");
		mysqlCommands.add("    FOREIGN KEY (`characters_id`)\n");
		mysqlCommands.add("        REFERENCES characters(`id`),\n\n");
		mysqlCommands.add("    FOREIGN KEY (`guns_id`)\n");
		mysqlCommands.add("        REFERENCES guns(`id`)\n");
		mysqlCommands.add(");\n\n");
		
		int i;
		for (i = 0; i < drillerWeapons.length; i++) {
			calculator.changeWeapon(drillerWeapons[i]);
			mysqlCommands.addAll(calculator.dumpToMySQL());
		}
		for (i = 0; i < engineerWeapons.length; i++) {
			calculator.changeWeapon(engineerWeapons[i]);
			mysqlCommands.addAll(calculator.dumpToMySQL());
		}
		for (i = 0; i < gunnerWeapons.length; i++) {
			calculator.changeWeapon(gunnerWeapons[i]);
			mysqlCommands.addAll(calculator.dumpToMySQL());
		}
		for (i = 0; i < scoutWeapons.length; i++) {
			calculator.changeWeapon(scoutWeapons[i]);
			mysqlCommands.addAll(calculator.dumpToMySQL());
		}
		
		// Open the MySQL file once, then dump the accumulated ArrayList of lines all at once to minimize I/O time
		try {
			// Set append=False so that it clears out the old file
			FileWriter MySQLwriter = new FileWriter(calculator.getCSVFolderPath() + "\\buildStatistics.sql", false);
			for (String line: mysqlCommands) {
				MySQLwriter.append(line);
			}
			MySQLwriter.flush();
			MySQLwriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		
		if (e == gui.getBcmIdealBurst()) {
			gui.activateThinkingCursor();
			currentlySelectedWeapon.buildFromCombination(calculator.getBestMetricCombination(0));
			gui.deactivateThinkingCursor();
		}
		else if (e == gui.getBcmIdealSustained()) {
			gui.activateThinkingCursor();
			currentlySelectedWeapon.buildFromCombination(calculator.getBestMetricCombination(1));
			gui.deactivateThinkingCursor();
		}
		else if (e == gui.getBcmSustainedWeakpoint()) {
			gui.activateThinkingCursor();
			currentlySelectedWeapon.buildFromCombination(calculator.getBestMetricCombination(2));
			gui.deactivateThinkingCursor();
		}
		else if (e == gui.getBcmSustainedWeakpointAccuracy()) {
			gui.activateThinkingCursor();
			currentlySelectedWeapon.buildFromCombination(calculator.getBestMetricCombination(3));
			gui.deactivateThinkingCursor();
		}
		else if (e == gui.getBcmIdealAdditional()) {
			gui.activateThinkingCursor();
			currentlySelectedWeapon.buildFromCombination(calculator.getBestMetricCombination(4));
			gui.deactivateThinkingCursor();
		}
		else if (e == gui.getBcmMaxDmg()) {
			gui.activateThinkingCursor();
			currentlySelectedWeapon.buildFromCombination(calculator.getBestMetricCombination(5));
			gui.deactivateThinkingCursor();
		}
		else if (e == gui.getBcmMaxNumTargets()) {
			gui.activateThinkingCursor();
			currentlySelectedWeapon.buildFromCombination(calculator.getBestMetricCombination(6));
			gui.deactivateThinkingCursor();
		}
		else if (e == gui.getBcmDuration()) {
			gui.activateThinkingCursor();
			currentlySelectedWeapon.buildFromCombination(calculator.getBestMetricCombination(7));
			gui.deactivateThinkingCursor();
		}
		else if (e == gui.getBcmTTK()) {
			gui.activateThinkingCursor();
			currentlySelectedWeapon.buildFromCombination(calculator.getBestMetricCombination(8));
			gui.deactivateThinkingCursor();
		}
		else if (e == gui.getBcmOverkill()) {
			gui.activateThinkingCursor();
			currentlySelectedWeapon.buildFromCombination(calculator.getBestMetricCombination(9));
			gui.deactivateThinkingCursor();
		}
		else if (e == gui.getBcmAccuracy()) {
			gui.activateThinkingCursor();
			currentlySelectedWeapon.buildFromCombination(calculator.getBestMetricCombination(10));
			gui.deactivateThinkingCursor();
		}
		else if (e == gui.getBcmUtility()) {
			gui.activateThinkingCursor();
			currentlySelectedWeapon.buildFromCombination(calculator.getBestMetricCombination(11));
			gui.deactivateThinkingCursor();
		}
		
		else if (e == gui.getDSHaz1()) {
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
		else if (e == gui.getExportMySQL()) {
			chooseFolder();
			gui.activateThinkingCursor();
			createMysqlFile();
			gui.deactivateThinkingCursor();
		}
		
		else if (e == gui.getMiscScreenshot()) {
			chooseFolder();
			String weaponPackage = currentlySelectedWeapon.getDwarfClass();
			String weaponClassName = currentlySelectedWeapon.getSimpleName();
			String filePath = calculator.getCSVFolderPath() + "\\" + weaponPackage + "_" + weaponClassName + "_" + currentlySelectedWeapon.getCombination() +".png";
			
			// Sourced from https://stackoverflow.com/a/44019372
			BufferedImage screenshot = gui.getScreenshot();
			try {
				ImageIO.write(screenshot, "png", new File(filePath));
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
