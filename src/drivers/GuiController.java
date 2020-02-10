package drivers;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import dataGenerator.WeaponStatsGenerator;
import drillerWeapons.EPC_ChargeShot;
import drillerWeapons.EPC_RegularShot;
import drillerWeapons.Subata;
import engineerWeapons.GrenadeLauncher;
import engineerWeapons.SMG;
import engineerWeapons.Shotgun;
import guiPieces.View;
import gunnerWeapons.Autocannon;
import gunnerWeapons.BurstPistol;
import gunnerWeapons.Minigun;
import gunnerWeapons.Revolver;
import modelPieces.EnemyInformation;
import modelPieces.Weapon;
import scoutWeapons.Boomstick;
import scoutWeapons.Classic_FocusShot;
import scoutWeapons.Classic_Hipfire;
import scoutWeapons.Deepcore;
import scoutWeapons.Zhukov;

/*
	Benchmarks: 
		150 Ideal Burst DPS
		100 Ideal Sustained DPS
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
		Weapon[] drillerWeapons = new Weapon[] {new Subata(), new EPC_RegularShot(), new EPC_ChargeShot()};
		Weapon[] engineerWeapons = new Weapon[] {new Shotgun(), new SMG(), new GrenadeLauncher()};
		Weapon[] gunnerWeapons = new Weapon[] {new Minigun(), new Autocannon(), new Revolver(), new BurstPistol()};
		Weapon[] scoutWeapons = new Weapon[] {new Deepcore(), new Classic_Hipfire(), new Classic_FocusShot(), new Boomstick(), new Zhukov()};
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

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Object e = arg0.getSource();
		
		Weapon currentlySelectedWeapon;
		int classIndex = gui.getCurrentClassIndex();
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
			currentlySelectedWeapon.buildFromCombination(calculator.getBestBurstDPSCombination());
		}
		else if (e == gui.getBcmIdealSustained()) {
			currentlySelectedWeapon.buildFromCombination(calculator.getBestSustainedDPSCombination());
		}
		else if (e == gui.getBcmSustainedWeakpoint()) {
			currentlySelectedWeapon.buildFromCombination(calculator.getBestSustainedWeakpointDPSCombination());
		}
		/*
		TODO: re-add this once Accuracy is implemented
		else if (e == gui.getBcmSustainedWeakpointAccuracy()) {
			currentlySelectedWeapon.buildFromCombination(calculator.getBestSustainedWeakpointAccuracyDPSCombination());
		}
		*/
		else if (e == gui.getBcmIdealAdditional()) {
			currentlySelectedWeapon.buildFromCombination(calculator.getBestAdditionalTargetDPSCombination());
		}
		else if (e == gui.getBcmMaxDmg()) {
			currentlySelectedWeapon.buildFromCombination(calculator.getHighestMultiTargetDamageCombination());
		}
		else if (e == gui.getBcmMaxNumTargets()) {
			currentlySelectedWeapon.buildFromCombination(calculator.getMostNumTargetsCombination());
		}
		else if (e == gui.getBcmDuration()) {
			currentlySelectedWeapon.buildFromCombination(calculator.getLongestFiringDurationCombination());
		}
		else if (e == gui.getBcmTTK()) {
			currentlySelectedWeapon.buildFromCombination(calculator.getShortestTimeToKillCombination());
		}
		else if (e == gui.getBcmOverkill()) {
			currentlySelectedWeapon.buildFromCombination(calculator.getLowestOverkillCombination());
		}
		/*
		TODO: re-add this once Accuracy is implemented
		else if (e == gui.getBcmAccuracy()) {
			currentlySelectedWeapon.buildFromCombination(calculator.getHighestAccuracyCombination());
		}
		*/
		/*
		TODO: re-add this once Utility is implemented
		else if (e == gui.getBcmUtility()) {
			currentlySelectedWeapon.buildFromCombination(calculator.getMostUtilityCombination());
		}
		*/
		
		
		else if (e == gui.getExportCurrent()) {
			chooseFolder();
			calculator.runTest(false, true);
		}
		else if (e == gui.getExportAll()) {
			chooseFolder();
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
		}
		
		
		else if (e == gui.getMiscExport()) {
			String combination = currentlySelectedWeapon.getCombination();
			JTextField output = new JTextField(combination);
			output.setFont(new Font("Monospaced", Font.PLAIN, 18));
			JOptionPane.showMessageDialog(null, output, "Current weapon combination:", JOptionPane.INFORMATION_MESSAGE);
		}
		else if (e == gui.getMiscLoad()) {
			String newCombination = JOptionPane.showInputDialog(null, "Enter the comination you want to load:");
			currentlySelectedWeapon.buildFromCombination(newCombination);
		}
	}
}
