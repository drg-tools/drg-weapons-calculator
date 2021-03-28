package guiPieces;

import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JToolTip;

// This class just overrides the default blue/grey ToolTip text for ModButtons and OverclockButtons
public class HoverText extends JToolTip {
	private static final long serialVersionUID = 1L;
	
	public HoverText(JComponent comp) {
		super();
		this.setComponent(comp);
		this.setFont(GuiConstants.customFont);
		this.setBackground(GuiConstants.drgBackgroundBiege);
		this.setForeground(GuiConstants.drgHighlightedYellow);
		this.setBorder(GuiConstants.orangeLine);
	}
	
	public static String breakLongToolTipString(String input, int maxNumCharsPerLine) {
		 return breakLongToolTipString(input, maxNumCharsPerLine, true); 
	}
	public static String breakLongToolTipString(String input, int maxNumCharsPerLine, boolean encloseWithBodyTags) {
		// First, check if the input string already contains fewer characters than the desired output.
		if (input.length() < maxNumCharsPerLine) {
			return input;
		}
		
		// If it is indeed longer, then run the algorithm.
		ArrayList<String> lines = new ArrayList<String>();
		String[] individualWords = input.split(" ");
		String currentWord;
		int currentWordLength;
		String currentLine = "";
		int currentNumCharsThisLine = 0;
		for (int i = 0; i < individualWords.length; i++) {
			currentWord = individualWords[i];
			currentWordLength = currentWord.length();
			if (currentNumCharsThisLine + currentWordLength < maxNumCharsPerLine) {
				currentLine += currentWord + " ";
				currentNumCharsThisLine += currentWordLength + 1;
			}
			else {
				lines.add(currentLine);
				currentLine = currentWord + " ";
				currentNumCharsThisLine = currentWordLength + 1;
			}
		}
		
		// Don't forget to append the last line
		lines.add(currentLine);
	
		String[] wrappedLines = lines.toArray(new String[lines.size()]);
		
		// I read online that ToolTipText supports HTML, so I'm going to try to leverage that to my advantage.
		String newToolTipText;
		if (encloseWithBodyTags) {
			 newToolTipText = "<html><body><p>" + wrappedLines[0] + "<br/>";
		}
		else {
			 newToolTipText = "<p>" + wrappedLines[0] + "<br/>";
		}
		
		for (int i = 1; i < wrappedLines.length; i++) {
			newToolTipText += wrappedLines[i] + "<br/>";
		}
		
		if (encloseWithBodyTags) {
			newToolTipText += "</p></body></html>";
		}
		else {
			newToolTipText += "</p>";
		}
		
		return newToolTipText;
	}
}
