package net.bancer.sparkdict.views.helpers;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import net.bancer.sparkdict.R;
import net.bancer.sparkdict.domain.core.LexicalEntry;

import org.xml.sax.XMLReader;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.text.Editable;
import android.text.Html.TagHandler;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;

/**
 * UnrecognizedTagsHandler parses HTML/XML tags that are not recognised by 
 * android.text.Html.fromHtml() method.
 * 
 * @see android.text.Html
 * @author valera
 *
 */
public class UnrecognizedTagsHandler implements TagHandler {
	
	private static final String TAG = "UnrecognizedTagsHandler";
	
	private LexicalEntry lexicalEntry;
	
	private Context context;
	
	public UnrecognizedTagsHandler(LexicalEntry lexicalEntry, Context context) {
		this.lexicalEntry = lexicalEntry;
		this.context = context;
	}

	@Override
	public void handleTag(boolean opening, String tag, Editable output,
			XMLReader xmlReader) {
		if(tag.equalsIgnoreCase("object")) {
			if(opening) {
				handleObjectStartTag(output);
			} else {
				handleObjectEndTag(output);
			}
		}
	}

	/**
	 * Handles `<object data="">` tag.
	 * 
	 * @param output
	 *            SpannableStringBuilder - string that has been parsed up to the
	 *            present call.
	 */
	private void handleObjectStartTag(Editable output) {
		int len = output.length();
		output.setSpan(new Href(), len, len, Spannable.SPAN_MARK_MARK);
	}

	/**
	 * Handles `</object>` tag.
	 * 
	 * @param output
	 *            SpannableStringBuilder - string that has been parsed up to the
	 *            present call.
	 */
	private void handleObjectEndTag(Editable output) {
		int len = output.length();
		Object obj = getLastSpanObj(output, Href.class);
		int where = output.getSpanStart(obj);
		output.removeSpan(obj);
		if (where != len) {
			Href h = (Href) obj;
			char[] resourceName = new char[len - where];
			output.getChars(where, len, resourceName, 0);
			h.mHref = resourceName;
			if (h.mHref != null) {
				String src = new String(h.mHref);
				// remove resource name from output
				output.delete(where, len);
				// insert audio image span
				Drawable d = context.getResources().getDrawable(
						R.drawable.ic_audio_vol);
				d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
				len = output.length();
				output.append("\uFFFC");
				output.setSpan(new ImageSpan(d, src), len, output.length(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				// create clickable span
				output.setSpan(new AudioButtonSpan(src), where,
						output.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
	}
	
	/**
	 * Retrieves the last spanned object of `kind` class from the spanned text.
	 * 
	 * @param text
	 *            text that has spannable elements.
	 * @param kind
	 *            spanned element object's class.
	 * @return last spanned object of specified class.
	 */
	private Object getLastSpanObj(Spanned text, Class<?> kind) {
		Object[] objs = text.getSpans(0, text.length(), kind);
		if (objs.length == 0) {
			return null;
		} else {
			return objs[objs.length - 1];
		}
	}

	/**
	 * Container for the resource name.
	 * 
	 * @author valera
	 *
	 */
	private class Href {
		
		public char[] mHref;
		
	}
	
	/**
	 * AudioButtonSpan emulates HTML image inside anchor: 
	 * `<a href="#"><img src="..."></a>`
	 * 
	 * @author valera
	 *
	 */
	private class AudioButtonSpan extends URLSpan {
		
		private AudioButtonSpan(String url) {
			super(url);
		}

		@Override
		public void onClick(View widget) {
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				Uri uri = Uri.parse(getURL());
				String resourceName = uri.toString();
				byte[] audio = lexicalEntry.getResource(resourceName);
				String dir = Environment.getExternalStorageDirectory()
						+ "/Android/data/net.bancer.sparkdict/cache";
				String fileExtension = resourceName.substring(resourceName
						.lastIndexOf('.'));
				String filePath = dir + "/temp" + fileExtension;
				writeTempFile(audio, filePath);
				playAudio(filePath);
			}
		}

		/**
		 * Creates a file. If the destination folder does not exist it will be
		 * created together with its parents.
		 * 
		 * @param data
		 *            bytes to be saved.
		 * @param filePath
		 *            full file name.
		 */
		private void writeTempFile(byte[] data, String filePath) {
			File f = new File(filePath);
			File d = f.getParentFile();
			if (!f.exists()) {
				d.mkdirs();
			} else {
				f.delete();
			}
			if (d.exists()) {
				BufferedOutputStream out = null;
				try {
					out = new BufferedOutputStream(new FileOutputStream(f));
					out.write(data);
				} catch (FileNotFoundException e) {
					Log.e(TAG, "Cannot write the file", e);
				} catch (IOException e) {
					Log.e(TAG, "Cannot write the file", e);
				} finally {
					if (out != null) {
						try {
							out.close();
						} catch (IOException e) {
							Log.e(TAG, "Cannot close the file", e);
						}
					}
				}
			}
		}

		/**
		 * Plays audio file.
		 * 
		 * @param filePath file to be played.
		 */
		private void playAudio(String filePath) {
			MediaPlayer player = new MediaPlayer();
			try {
				player.setDataSource(filePath);
				player.prepare();
				player.start();
			} catch (IllegalArgumentException e) {
				Log.e(TAG, "Cannot play audio file", e);
			} catch (IllegalStateException e) {
				Log.e(TAG, "Cannot play audio file", e);
			} catch (IOException e) {
				Log.e(TAG, "Cannot play audio file", e);
			}
		}
	}
}
