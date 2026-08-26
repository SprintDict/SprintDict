package net.bancer.sparkdict.views.helpers;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.Editable;
import android.text.Html.TagHandler;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;

import net.bancer.sparkdict.R;
import net.bancer.sparkdict.domain.core.LexicalEntry;

import org.xml.sax.XMLReader;

import java.io.IOException;

/**
 * UnrecognizedTagsHandler parses HTML/XML tags that are not recognised by
 * android.text.Html.fromHtml() method.
 */
public class UnrecognizedTagsHandler implements TagHandler {

    private static final String TAG = "UnrecognizedTagsHandler";

    private final LexicalEntry lexicalEntry;

    private final Context context;

    public UnrecognizedTagsHandler(LexicalEntry lexicalEntry, Context context) {
        this.lexicalEntry = lexicalEntry;
        this.context = context;
    }

    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        if (tag.equalsIgnoreCase("object")) {
            if (opening) {
                handleObjectStartTag(output);
            } else {
                handleObjectEndTag(output);
            }
        }
    }

    /**
     * Handles `<object data="">` tag.
     *
     * @param output SpannableStringBuilder - string that has been parsed up to the
     *               present call.
     */
    private void handleObjectStartTag(Editable output) {
        int len = output.length();
        output.setSpan(new Href(), len, len, Spannable.SPAN_MARK_MARK);
    }

    /**
     * Handles `</object>` tag.
     *
     * @param output SpannableStringBuilder - string that has been parsed up to the
     *               present call.
     */
    private void handleObjectEndTag(Editable output) {
        int len = output.length();
        Object obj = getLastSpanObj(output);
        int where = output.getSpanStart(obj);
        output.removeSpan(obj);
        if (where == len) {
            return;
        }
        Href h = (Href) obj;
        char[] resourceName = new char[len - where];
        output.getChars(where, len, resourceName, 0);
        if (h == null) {
            return;
        }
        h.mHref = resourceName;
        String src = new String(h.mHref);
        // remove resource name from output
        output.delete(where, len);
        // insert audio image span
        Drawable audioIcon = context.getDrawable(R.drawable.ic_audio_vol);
        if (audioIcon == null) {
            return;
        }
        audioIcon.setBounds(0, 0, audioIcon.getIntrinsicWidth(), audioIcon.getIntrinsicHeight());
        len = output.length();
        // Append Unicode Object Replacement Character U+FFFC which acts as a stand-in for non-text
        // items (like images, icons, or embedded files) that cannot be shown in plain text.
        output.append("￼");
        output.setSpan(new ImageSpan(audioIcon, src), len, output.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        // create clickable span
        output.setSpan(new AudioButtonSpan(src), where, output.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    /**
     * Retrieves the last spanned object of Href class from the spanned text.
     *
     * @param text text that has spannable elements.
     * @return last spanned object of specified class.
     */
    private Object getLastSpanObj(Spanned text) {
        Object[] objs = text.getSpans(0, text.length(), (Class<?>) Href.class);
        if (objs.length == 0) {
            return null;
        } else {
            return objs[objs.length - 1];
        }
    }

    /**
     * Container for the resource name.
     */
    private static class Href {

        public char[] mHref;

    }

    /**
     * AudioButtonSpan emulates HTML image inside anchor:
     * `<a href="#"><img src="..."></a>`
     */
    private class AudioButtonSpan extends URLSpan {

        private AudioButtonSpan(String url) {
            super(url);
        }

        @Override
        public void onClick(View widget) {
            Uri uri = Uri.parse(getURL());
            String resourceName = uri.toString();
            byte[] audio = lexicalEntry.getResource(resourceName);
            playAudio(audio);
        }

        private class ByteArrayMediaDataSource extends MediaDataSource {
            private final byte[] data;

            ByteArrayMediaDataSource(byte[] data) {
                this.data = data;
            }

            @Override
            public int readAt(long position, byte[] buffer, int offset, int size) {
                if (position >= data.length) {
                    return -1; // end of stream
                }
                int length = Math.min(size, (int) (data.length - position));
                System.arraycopy(data, (int) position, buffer, offset, length);
                return length;
            }

            @Override
            public long getSize() {
                return data.length;
            }

            @Override
            public void close() {
                // nothing to release
            }
        }

        /**
         * Plays audio file.
         *
         * @param audio audio to be played.
         */
        private void playAudio(byte[] audio) {
            MediaPlayer player = new MediaPlayer();
            try {
                player.setDataSource(new ByteArrayMediaDataSource(audio));
                player.prepare();
                player.start();
            } catch (IllegalArgumentException | IOException | IllegalStateException e) {
                Log.e(TAG, "Cannot play audio file", e);
            }
        }
    }
}
