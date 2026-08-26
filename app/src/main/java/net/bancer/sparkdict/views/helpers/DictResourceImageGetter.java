package net.bancer.sparkdict.views.helpers;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html.ImageGetter;

import net.bancer.sparkdict.domain.core.LexicalEntry;

/**
 * {@link ImageGetter} implementation that retrieves images stored as dictionary
 * resources and converts them into {@link Drawable} instances for rendering
 * inside HTML content displayed by a {@link android.widget.TextView}.
 *
 * <p>The images are scaled to fit the specified maximum width while preserving
 * their original aspect ratio.</p>
 */
public class DictResourceImageGetter implements ImageGetter {

    /**
     * Dictionary entry containing the image resources.
     */
    private final LexicalEntry lexicalEntry;

    /**
     * Maximum width of the image in pixels.
     */
    private final int maxWidth;

    private final Resources resources;

    /**
     * Creates an image getter for the specified lexical entry.
     *
     * @param lexicalEntry lexical entry containing image resources.
     * @param maxWidth     maximum width of returned images in pixels.
     * @param resources    Application's resources.
     */
    public DictResourceImageGetter(LexicalEntry lexicalEntry, int maxWidth, Resources resources) {
        this.lexicalEntry = lexicalEntry;
        this.maxWidth = maxWidth;
        this.resources = resources;
    }

    /**
     * Retrieves and creates a drawable for the requested dictionary image.
     *
     * <p>The image is loaded from the resources of the associated lexical entry
     * and resized to fit {@code maxWidth} while keeping its aspect ratio.</p>
     *
     * @param sourceName name of the image resource referenced by the HTML {@code img} tag.
     * @return drawable representing the requested image, or {@code null} if the resource does not exist.
     */
    @Override
    public Drawable getDrawable(String sourceName) {
        byte[] resource = lexicalEntry.getResource(sourceName);
        if (resource.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(resource, 0, resource.length);
            if (bitmap == null) {
                return null;
            }
            BitmapDrawable drawable = new BitmapDrawable(resources, bitmap);
            int width = drawable.getIntrinsicWidth();
            int height = drawable.getIntrinsicHeight();
            if (width <= 0 || height <= 0) {
                return null;
            }
            int bottom = height * maxWidth / width;
            drawable.setBounds(0, 0, maxWidth, bottom);
            return drawable;
        } else {
            return null;
        }
    }
}
