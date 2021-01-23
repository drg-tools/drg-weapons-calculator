package guiPieces.accuracyEstimator;

import java.awt.Color;
import java.awt.CompositeContext;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Objects;

// This is one of 2 classes sourced from this StackOverflow answer: https://stackoverflow.com/a/42463677
public class AdditiveCompositeContext implements CompositeContext {
    private final Color chromaKey;

    public AdditiveCompositeContext(final Color chromaKey) {
        this.chromaKey = Objects.requireNonNull(chromaKey);
    }

    public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
        int r = chromaKey.getRed(), g = chromaKey.getGreen(), b = chromaKey.getBlue();
        int[] pxSrc = new int[src.getNumBands()];
        int[] pxDst = new int[dstIn.getNumBands()];
        int chans = Math.min(src.getNumBands(), dstIn.getNumBands());

        for (int x = 0; x < dstIn.getWidth(); x++) {
            for (int y = 0; y < dstIn.getHeight(); y++) {
                pxSrc = src.getPixel(x, y, pxSrc);
                pxDst = dstIn.getPixel(x, y, pxDst);

                int alpha = pxSrc.length > 3? alpha = pxSrc[3] : 255;

                if (pxDst[0] == r && pxDst[1] == g && pxDst[2] == b) {
                    pxDst[0] = 0; pxDst[1] = 0; pxDst[2] = 0;
                }

                for (int i = 0; i < 3 && i < chans; i++) {
                    pxDst[i] = Math.min(255, (pxSrc[i] * alpha / 255) + (pxDst[i]));
                    dstOut.setPixel(x, y, pxDst);
                }
            }
        }
    }

    @Override public void dispose() { }
}
