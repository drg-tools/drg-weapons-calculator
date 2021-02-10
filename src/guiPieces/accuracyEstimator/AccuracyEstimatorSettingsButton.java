package guiPieces.accuracyEstimator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import guiPieces.GuiConstants;
import guiPieces.HoverText;
import modelPieces.Weapon;

public class AccuracyEstimatorSettingsButton extends JButton implements ActionListener, ChangeListener {
	private static final long serialVersionUID = 1L;
	
	private JComponent parentComponent;
	private Weapon toUpdate;
	
	private JCheckBox modelRecoilSetting;
	private JRadioButton setDwarfStationary, setDwarfMoving;
	private JRadioButton visualizeGeneralAccuracy, visualizeWeakpointAccuracy;

	public AccuracyEstimatorSettingsButton(JComponent parent, String textToDisplay, Weapon weaponWithAccuracy) {
		parentComponent = parent;
		toUpdate = weaponWithAccuracy;
		
		// Font color will be set by the parent WeaponTab, in constructCalculationsPanel()
		this.setBackground(GuiConstants.drgBackgroundBrown);
		this.setBorder(GuiConstants.orangeLine);
		
		this.setText(textToDisplay);
		this.setFont(GuiConstants.customFontBold);
		this.setHorizontalAlignment(SwingConstants.LEFT);
		this.addActionListener(this);
	}
	
	// Adapted from https://docs.oracle.com/javase/tutorial/uiswing/components/slider.html
	private JPanel getAccuracySettingsPanel() {
		JPanel toReturn = new JPanel();
		toReturn.setLayout(new BoxLayout(toReturn, BoxLayout.Y_AXIS));
		
		ButtonGroup stationaryOrMoving = new ButtonGroup();
		boolean currentlyMoving = toUpdate.isDwarfMoving();
		setDwarfStationary = new JRadioButton("Dwarf is Standing Still", !currentlyMoving);
		setDwarfStationary.addActionListener(this);
		stationaryOrMoving.add(setDwarfStationary);
		toReturn.add(setDwarfStationary);
		setDwarfMoving = new JRadioButton("Dwarf is Moving", currentlyMoving);
		setDwarfMoving.addActionListener(this);
		stationaryOrMoving.add(setDwarfMoving);
		toReturn.add(setDwarfMoving);
		
		modelRecoilSetting = new JCheckBox("Model Recoil in Accuracy Estimations", toUpdate.isRecoilModeledInAccuracy());
		modelRecoilSetting.addActionListener(this);
		toReturn.add(modelRecoilSetting);
		
		toReturn.add(new JSeparator());
		
		ButtonGroup generalOrWeakpointSetting = new ButtonGroup();
		boolean currentlyShowingGeneralAccuracy = toUpdate.accuracyVisualizerShowsGeneralAccuracy();
		visualizeGeneralAccuracy = new JRadioButton("Visualize General Accuracy", currentlyShowingGeneralAccuracy);
		visualizeGeneralAccuracy.addActionListener(this);
		generalOrWeakpointSetting.add(visualizeGeneralAccuracy);
		toReturn.add(visualizeGeneralAccuracy);
		visualizeWeakpointAccuracy = new JRadioButton("Visualize Weakpoint Accuracy", !currentlyShowingGeneralAccuracy);
		visualizeWeakpointAccuracy.addActionListener(this);
		generalOrWeakpointSetting.add(visualizeWeakpointAccuracy);
		toReturn.add(visualizeWeakpointAccuracy);
		
		toReturn.add(new JSeparator());
		
		// Add JLabel for instructions/description
		String longText = "Change the distance that's used to estimate the Accuracy metrics";
		JLabel description = new JLabel(HoverText.breakLongToolTipString(longText, 40));
		description.setBorder(new EmptyBorder(GuiConstants.paddingPixels, 0, 2*GuiConstants.paddingPixels, 0));
		toReturn.add(description);
		
		// Add slider
		int currentDistance = (int) toUpdate.getAccuracyDistance();
		JSlider accuracyDistanceSlider = new JSlider(JSlider.HORIZONTAL, 1, 19, currentDistance);
		accuracyDistanceSlider.setMajorTickSpacing(3);
		accuracyDistanceSlider.setMinorTickSpacing(1);
		accuracyDistanceSlider.setPaintTicks(true);
		accuracyDistanceSlider.setPaintLabels(true);

		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put(1, new JLabel("1m"));
		labelTable.put(4, new JLabel("4m"));
		labelTable.put(7, new JLabel("7m"));
		labelTable.put(10, new JLabel("10m"));
		labelTable.put(13, new JLabel("13m"));
		labelTable.put(16, new JLabel("16m"));
		labelTable.put(19, new JLabel("19m"));
		accuracyDistanceSlider.setLabelTable(labelTable);
		
		toReturn.add(accuracyDistanceSlider);
		
		// Enable listener for slider to make it update model in background
		accuracyDistanceSlider.addChangeListener(this);
		
		return toReturn;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		Object e = arg0.getSource();
		
		if (e == this) {
			// Adapted from https://stackoverflow.com/a/13760416
			JOptionPane a = new JOptionPane(getAccuracySettingsPanel(), JOptionPane.INFORMATION_MESSAGE);
			JDialog d = a.createDialog(null, "AccuracyEstimator Settings");
			d.setLocationRelativeTo(parentComponent);
			d.setVisible(true);
		}
		else if (e == modelRecoilSetting) {
			toUpdate.setModelRecoilInAccuracy(modelRecoilSetting.isSelected());
		}
		else if (e == setDwarfStationary) {
			if (setDwarfStationary.isSelected()) {
				toUpdate.setDwarfMoving(false);
			}
		}
		else if (e == setDwarfMoving) {
			if (setDwarfMoving.isSelected()) {
				toUpdate.setDwarfMoving(true);
			}
		}
		else if (e == visualizeGeneralAccuracy) {
			if (visualizeGeneralAccuracy.isSelected()) {
				toUpdate.setAccuracyVisualizerToShowGeneralAccuracy(true);
			}
		}
		else if (e == visualizeWeakpointAccuracy) {
			if (visualizeWeakpointAccuracy.isSelected()) {
				toUpdate.setAccuracyVisualizerToShowGeneralAccuracy(false);
			}
		}
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
	    JSlider source = (JSlider) e.getSource();
	    if (!source.getValueIsAdjusting()) {
	        toUpdate.setAccuracyDistance(source.getValue());
	    }
	}
}
