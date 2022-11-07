package drgtools.dpscalc.guiPieces;

import javax.swing.JFrame;

// Adapted from http://www.java2s.com/Tutorial/Java/0240__Swing/ChangeCursorinathreadforanimation.htm
public class ThinkingCursorAnimation implements Runnable {
	
	private JFrame guiToChangeCursor;
	private boolean animate;
	private int msDelayBetweenFrames;
	
	public ThinkingCursorAnimation(JFrame gui) {
		guiToChangeCursor = gui;
		animate = false;
		msDelayBetweenFrames = 100;
	}
	
	public void toggleAnimation() {
		animate = !animate;
	}

	@Override
	public void run() {
		int counter = 0;
		while (animate) {
			guiToChangeCursor.setCursor(CustomCursors.thinkingCursor[counter]);
			counter = (counter + 1) % CustomCursors.thinkingCursor.length;
			
			try {
				Thread.sleep(msDelayBetweenFrames);
			} 
			catch (Exception e) {
			
			}
		}
		
		// After the while loop is done, set it back to the normal Cursor
		guiToChangeCursor.setCursor(CustomCursors.defaultCursor);
	}

}
