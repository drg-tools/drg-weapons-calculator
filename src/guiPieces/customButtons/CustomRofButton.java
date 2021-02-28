package guiPieces.customButtons;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import guiPieces.GuiConstants;
import modelPieces.Weapon;
import utilities.MathUtils;

public class CustomRofButton extends JButton implements ActionListener, ChangeListener  {
	private static final long serialVersionUID = 1L;
	
	private JComponent parentComponent;
	private Weapon toUpdate;
	private JButton useRecommendation;
	private JSlider customRoFSlider;

	public CustomRofButton(JComponent parent, Weapon weaponWithCustomizableRoF) {
		parentComponent = parent;
		toUpdate = weaponWithCustomizableRoF;
		
		this.setForeground(GuiConstants.drgHighlightedYellow);
		this.setBackground(GuiConstants.drgBackgroundBrown);
		this.setBorder(GuiConstants.orangeLine);
		
		this.setText("Set the RoF");
		this.setFont(GuiConstants.customFontHeader);
		this.setHorizontalAlignment(SwingConstants.CENTER);
		this.addActionListener(this);
	}
	
	// Adapted from https://docs.oracle.com/javase/tutorial/uiswing/components/slider.html
	private JPanel getCustomRofPanel() {
		JPanel toReturn = new JPanel();
		toReturn.setLayout(new BoxLayout(toReturn, BoxLayout.Y_AXIS));
		
		double maxRoF = toUpdate.getRateOfFire();
		double currentRoF = toUpdate.getCustomRoF();
		double recommendedRoF = toUpdate.getRecommendedRateOfFire();
		
		// Add a button to automatically jump to "recommended" RoF on the slider
		useRecommendation = new JButton("Use recommended Rate of Fire (" + MathUtils.round(recommendedRoF, GuiConstants.numDecimalPlaces) + ")");
		useRecommendation.addActionListener(this);
		toReturn.add(useRecommendation);
		
		// Add slider
		int currentTick = 1 + (int) Math.round(currentRoF * 1000.0);
		// Because JSlider only supports integers, I have to use 1000 integers to approximate doubles to 3 decimal places.
		int numberOfTicks = 1 + (int) Math.round(maxRoF * 1000.0);
		customRoFSlider = new JSlider(JSlider.HORIZONTAL, 1, numberOfTicks, currentTick);
		customRoFSlider.setMajorTickSpacing(1000);  // Every 1.0
		customRoFSlider.setMinorTickSpacing(250);  // Every 0.25
		customRoFSlider.setPaintTicks(true);
		customRoFSlider.setPaintLabels(true);
		
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		for (int i = 0; i <= maxRoF; i ++) {
			labelTable.put(1 + i * 1000, new JLabel(i + ""));
		}
		
		// If the above for-loop doesn't automatically create the recommended RoF as a label, this snippet will be a catch-all
		int keyForRecommendation = 1 + (int) Math.round(recommendedRoF * 1000.0);
		if (labelTable.get(keyForRecommendation) == null) {
			labelTable.put(keyForRecommendation, new JLabel(MathUtils.round(recommendedRoF, 2) + ""));
		}
		customRoFSlider.setLabelTable(labelTable);
		customRoFSlider.setBorder(new EmptyBorder(2*GuiConstants.paddingPixels, 0, GuiConstants.paddingPixels, 0));
		
		toReturn.add(customRoFSlider);
		
		// Enable listener for slider to make it update model in background
		customRoFSlider.addChangeListener(this);
		
		return toReturn;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		Object e = arg0.getSource();
		
		if (e == this) {
			// Adapted from https://stackoverflow.com/a/13760416
			JOptionPane a = new JOptionPane(getCustomRofPanel(), JOptionPane.INFORMATION_MESSAGE);
			JDialog d = a.createDialog(null, "Customize what RoF you want the model to use");
			d.setLocationRelativeTo(parentComponent);
			d.setVisible(true);
		}
		else if (e == useRecommendation) {
			double recommendedRoF = toUpdate.getRecommendedRateOfFire();
			// I have to move the slider before setting the precise value, because the stateChanged() just below this will round down to 3 decimals if the slider moves afterwards.
			customRoFSlider.setValue(1 + (int) Math.round(recommendedRoF * 1000.0));
			toUpdate.setCustomRoF(recommendedRoF);
		}
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
	    JSlider source = (JSlider) e.getSource();
	    if (!source.getValueIsAdjusting()) {
	    	double convertedRoFValue = (((double) source.getValue()) - 1.0) / 1000.0;
	        toUpdate.setCustomRoF(convertedRoFValue);
	    }
	}
}
