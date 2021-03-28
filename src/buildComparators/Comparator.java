package buildComparators;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import weapons.Weapon;

public abstract class Comparator implements ActionListener {
	protected Weapon baseModel;
	protected String build1, build2, build3, build4;
	
	protected JButton compareBuilds;
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
	
	protected boolean areAllBuildsValid() {
		String build1ErrorMsg="", build2ErrorMsg="", build3ErrorMsg="", build4ErrorMsg="";
		if (!buildInput1.getText().equals("") && !baseModel.isCombinationValid(buildInput1.getText())) {
			build1ErrorMsg = baseModel.getInvalidCombinationErrorMessage();
		}
		if (!buildInput2.getText().equals("") && !baseModel.isCombinationValid(buildInput2.getText())) {
			build2ErrorMsg = baseModel.getInvalidCombinationErrorMessage();
		}
		if (!buildInput3.getText().equals("") && !baseModel.isCombinationValid(buildInput3.getText())) {
			build3ErrorMsg = baseModel.getInvalidCombinationErrorMessage();
		}
		if (!buildInput4.getText().equals("") && !baseModel.isCombinationValid(buildInput4.getText())) {
			build4ErrorMsg = baseModel.getInvalidCombinationErrorMessage();
		}
		
		if (build1ErrorMsg.length() > 0 || build2ErrorMsg.length() > 0 || build3ErrorMsg.length() > 0 || build4ErrorMsg.length() > 0) {
			// Send a pop-up with the error message(s) and then return early for failure state.
			String errorMessage = "<html><body><p style=\"color:red\">";
			
			if (build1ErrorMsg.length() > 0) {
				errorMessage += "Combination 1 errors:<br/>";
				errorMessage += build1ErrorMsg + "<br/>";
			}
			if (build2ErrorMsg.length() > 0) {
				errorMessage += "Combination 2 errors:<br/>";
				errorMessage += build2ErrorMsg + "<br/>";
			}
			if (build3ErrorMsg.length() > 0) {
				errorMessage += "Combination 3 errors:<br/>";
				errorMessage += build3ErrorMsg + "<br/>";
			}
			if (build4ErrorMsg.length() > 0) {
				errorMessage += "Combination 4 errors:<br/>";
				errorMessage += build4ErrorMsg + "<br/>";
			}
			
			errorMessage += "</p></body></html>";
			
			JOptionPane.showMessageDialog(null, errorMessage, "One or more of the builds is invalid", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		else {
			return true;
		}
	}
	
	public abstract JPanel getComparisonPanel();

	public abstract void actionPerformed(ActionEvent arg0);
}
