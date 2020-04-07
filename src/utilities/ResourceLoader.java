package utilities;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.imageio.ImageIO;

import net.sf.image4j.codec.ico.ICODecoder;

public class ResourceLoader {
	
	// Generic method to load any resource
	// Sourced from https://www.youtube.com/watch?v=rCoed3MKpEA
	public static InputStream load(String path) {
		InputStream input = ResourceLoader.class.getResourceAsStream(path);
		if (input == null) {
			input = ResourceLoader.class.getResourceAsStream("/" + path);
		}
		return input;
	}
	
	// Use this method for .ico files specifically
	public static List<BufferedImage> loadIcoFile(String relativeFilepath) throws IOException {
		return ICODecoder.read(load(relativeFilepath));
	}
	
	// Use this method for virtually every other image type
	public static BufferedImage loadImage(String relativeFilepath) throws IOException {
		return ImageIO.read(load(relativeFilepath));
	}
}
