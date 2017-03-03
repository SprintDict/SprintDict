package net.bancer.sparkdict.views.helpers;

import net.bancer.sparkdict.domain.core.LexicalEntry;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html.ImageGetter;

/**
 * DictResourceImageGetter retrieves android.graphics.drawable.Drawable for a
 * specific image from the dictionary res folder.
 * 
 * @author valera
 *
 */
public class DictResourceImageGetter implements ImageGetter {
	
	private final LexicalEntry lexicalEntry;

	public DictResourceImageGetter(LexicalEntry lexicalEntry) {
		this.lexicalEntry = lexicalEntry;
	}

	@Override
	public Drawable getDrawable(String sourceName) {
		byte[] resource = lexicalEntry.getResource(sourceName);
		if (resource.length > 0) {
			BitmapDrawable drawable = new BitmapDrawable(
					BitmapFactory.decodeByteArray(resource, 0, resource.length));
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
					drawable.getIntrinsicHeight());
			return drawable;
		} else {
			return null;
		}
	}
}
