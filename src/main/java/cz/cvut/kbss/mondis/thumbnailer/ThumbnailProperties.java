package cz.cvut.kbss.mondis.thumbnailer;

/**
 * Created by myrousz on 11/20/14.
 */
public class ThumbnailProperties {
    public ThumbnailProperties(int width, int height, ThumbnailScaleMode scaleMode) {
        this.width = width;
        this.height = height;
        this.scaleMode = scaleMode;
    }

    public ThumbnailScaleMode getScaleMode() {
        return scaleMode;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    private int width;
    private int height;
    private ThumbnailScaleMode scaleMode;
}
