package drgtools.dpscalc.guiPieces;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import drgtools.dpscalc.utilities.ResourceLoader;

public class CustomCursors {
	public static Cursor defaultCursor = Toolkit.getDefaultToolkit().createCustomCursor(ResourceLoader.loadIcoFile("/images/drg_default_cursor.ico").get(0), new Point(2, 13), "drg_default");
	public static Cursor defaultCursorPlusQuestionMark = Toolkit.getDefaultToolkit().createCustomCursor(ResourceLoader.loadIcoFile("/images/drg_cursor_with_qmark.ico").get(0), new Point(2, 13), "drg_question_mark");
	
	private static Cursor[] thinkingCursorFrames() {
		Toolkit tk = Toolkit.getDefaultToolkit();
		// The custom animated cursor that I made only has 8 frames
		Cursor[] toReturn = new Cursor[8];
		
		BufferedImage rawFrame;
		
		for (int i = 0; i < 8; i++) {
			rawFrame = ResourceLoader.loadIcoFile("/images/thinkingCursorFrames/loadingFrame_" + i + ".ico").get(0);
			toReturn[i] = tk.createCustomCursor(rawFrame, new Point(2, 13), "drg_loading_" + i);
		}
		
		return toReturn;
	}
	public static Cursor[] thinkingCursor = thinkingCursorFrames();
}
