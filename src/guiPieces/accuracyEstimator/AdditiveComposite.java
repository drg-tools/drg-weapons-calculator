package guiPieces.accuracyEstimator;

import java.awt.Color;
import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.util.Objects;

//This is one of 2 classes sourced from this StackOverflow answer: https://stackoverflow.com/a/42463677
public class AdditiveComposite implements Composite {
    private final Color chromaKey;

    public AdditiveComposite(final Color chromaKey) {
        this.chromaKey = Objects.requireNonNull(chromaKey);
    }

    public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
        return new AdditiveCompositeContext(chromaKey);
    }
}
