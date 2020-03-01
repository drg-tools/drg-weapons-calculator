package guiPieces;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class GlossaryTextAndFAQText {

	private static JPanel createScrollableTextPanel(String title, JPanel scrollableText) {
		 JPanel toReturn = new JPanel();
		 toReturn.setLayout(new BorderLayout());
		 toReturn.setBackground(GuiConstants.drgBackgroundBrown);
		 
		 JLabel header = new JLabel(title);
		 header.setForeground(GuiConstants.drgRegularOrange);
		 header.setFont(new Font("Dialogue", Font.PLAIN, 18));
		 JPanel centerAlignHeader = new JPanel();
		 centerAlignHeader.setLayout(new FlowLayout(FlowLayout.CENTER));
		 centerAlignHeader.setBackground(GuiConstants.drgBackgroundBrown);
		 centerAlignHeader.add(header);
		 toReturn.add(centerAlignHeader, BorderLayout.NORTH);
		 
		 toReturn.add(new JScrollPane(scrollableText), BorderLayout.CENTER);
		 
		 return toReturn;
	}
	
	// This method can pull double-duty as both a Q&A box as well as a Term/Definition box
	private static JPanel createQandAPanel(String question, String answer) {
		JPanel toReturn = new JPanel();
		toReturn.setLayout(new BoxLayout(toReturn, BoxLayout.PAGE_AXIS));
		
		JLabel questionOrTerm = new JLabel(question);
		questionOrTerm.setForeground(GuiConstants.drgRegularOrange);
		// Set the Label to be almost flush with the left side
		JPanel leftAlignLabel = new JPanel();
		leftAlignLabel.setLayout(new FlowLayout(FlowLayout.LEFT));
		leftAlignLabel.setBackground(GuiConstants.drgBackgroundBrown);
		leftAlignLabel.add(questionOrTerm);
		toReturn.add(leftAlignLabel);
		
		JTextArea answerOrDefinition = new JTextArea(answer);
		answerOrDefinition.setBackground(GuiConstants.drgBackgroundBrown);
		answerOrDefinition.setForeground(GuiConstants.drgHighlightedYellow);
		// Left-pad the answer a bit for visual clarity
		answerOrDefinition.setMargin(new Insets(0, 30, 0, 0));
		toReturn.add(answerOrDefinition);
		
		return toReturn;
	}
	
	public static JPanel getFAQText() {
		// TODO: add more FAQs
		String[][] FAQtext = {
			{"Why do some Mods and Overclocks have a Red outline?", "Mods or Overclocks with a Red outline either are not implemented yet, or how they work in-game can't be represented by the Weapon's stats."},
		};
		
		JPanel panelContainedWithinScrollPane = new JPanel();
		panelContainedWithinScrollPane.setBackground(GuiConstants.drgBackgroundBrown);
		// panelContainedWithinScrollPane.setLayout(new GridLayout(FAQtext.length, 1));
		panelContainedWithinScrollPane.setLayout(new BoxLayout(panelContainedWithinScrollPane, BoxLayout.PAGE_AXIS));
		
		for (int i = 0; i < FAQtext.length; i++) {
			panelContainedWithinScrollPane.add(createQandAPanel(FAQtext[i][0], FAQtext[i][1]));
		}
		
		return createScrollableTextPanel("Frequently Asked Questions", panelContainedWithinScrollPane);
	}

	public static JPanel getGlossaryText() {
		// Perhaps these should be sorted alphabetically?
		// TODO: add more terms
		String[][] glossaryText = {
			{"DoT", "Damage Over Time"},
			{"Status Effect", "A conditional effect that can be applied to enemies. Sometimes it's a DoT, other times it's a crowd control effect."},
		};
		
		JPanel panelContainedWithinScrollPane = new JPanel();
		panelContainedWithinScrollPane.setBackground(GuiConstants.drgBackgroundBrown);
		// panelContainedWithinScrollPane.setLayout(new GridLayout(glossaryText.length, 1));
		panelContainedWithinScrollPane.setLayout(new BoxLayout(panelContainedWithinScrollPane, BoxLayout.PAGE_AXIS));
		
		for (int i = 0; i < glossaryText.length; i++) {
			panelContainedWithinScrollPane.add(createQandAPanel(glossaryText[i][0], glossaryText[i][1]));
		}
		
		return createScrollableTextPanel("Glossary of Terms", panelContainedWithinScrollPane);
	}
}
