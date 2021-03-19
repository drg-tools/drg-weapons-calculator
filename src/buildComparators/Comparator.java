package buildComparators;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JTextField;

import modelPieces.Weapon;

public abstract class Comparator implements ActionListener {
	protected Weapon baseModel;
	protected String build1, build2, build3, build4;
	protected JTextField buildInput1, buildInput2, buildInput3, buildInput4;
	
	// I'm choosing to make this constructor protected, instead of public, so that only child classes can be instantiated.
	protected Comparator(Weapon toUse) {
		baseModel = toUse.clone();
		
		build1 = "";
		build2 = "";
		build3 = "";
		build4 = "";
	}
	
	public void changeWeapon(Weapon toUse) {
		// Check if the new weapon is different than the old one. If they're the same model, do nothing.
		if (baseModel.getFullName().equals(toUse.getFullName())) {
			return;
		}
		
		baseModel = toUse.clone();

		// Because a different weapon will have different build string validation, I'm choosing to clear out all old values.
		build1 = "";
		build2 = "";
		build3 = "";
		build4 = "";
	}
	
	public void setNewBuildAtIndex(int index, String newCombination) {
		// Because this method will only be transferring valid combinations from the GUI into this object, I'm choosing to skip input validation
		switch(index) {
			case 0:{
				build1 = newCombination;
				break;
			}
			case 1:{
				build2 = newCombination;	
				break;
			}
			case 2:{
				build3 = newCombination;
				break;
			}
			case 3:{
				build4 = newCombination;
				break;
			}
		}
	}
	
	public abstract JPanel getComparisonPanel();

	public abstract void actionPerformed(ActionEvent arg0);
}
