package net.bancer.sparkdict.views.helpers;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.text.Editable;
import android.text.Html.TagHandler;
import android.text.Selection;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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

    /**
     * Currently processed image or audio element.
     */
    private Href currentHref;

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
        currentHref = new Href();
        output.setSpan(currentHref, len, len, Spannable.SPAN_MARK_MARK);
    }

    /**
     * Handles `</object>` tag.
     *
     * @param output SpannableStringBuilder - string that has been parsed up to the
     *               present call.
     */
    private void handleObjectEndTag(Editable output) {
        int len = output.length();
        Href href = currentHref;
        currentHref = null;
        if (href == null) {
            return;
        }
        int where = output.getSpanStart(href);
        output.removeSpan(href);
        if (where == len) {
            return;
        }
        char[] resourceName = new char[len - where];
        output.getChars(where, len, resourceName, 0);
        String src = new String(resourceName);
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
        AudioImageSpan audioImageSpan = new AudioImageSpan(audioIcon, src, context);
        output.setSpan(audioImageSpan, len, output.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        // create clickable span
        AudioButtonSpan audioButtonSpan = new AudioButtonSpan(src, lexicalEntry, audioImageSpan);
        output.setSpan(audioButtonSpan, where, output.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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
    public static class AudioButtonSpan extends ClickableSpan {

        /**
         * Name of the audio resource to play.
         */
        private final String resourceName;

        /**
         * Lexical entry containing the audio resource.
         */
        private final LexicalEntry lexicalEntry;

        /**
         * Audio image span associated with this button.
         */
        private final AudioImageSpan audioImageSpan;

        /**
         * Creates an audio button span.
         *
         * @param resourceName name of the audio resource to play.
         * @param lexicalEntry lexical entry containing the audio resource.
         * @param audioImageSpan audio image span associated with this button.
         */
        public AudioButtonSpan(String resourceName, LexicalEntry lexicalEntry, AudioImageSpan audioImageSpan) {
            this.resourceName = resourceName;
            this.lexicalEntry = lexicalEntry;
            this.audioImageSpan = audioImageSpan;
        }

        /**
         * Updates the text drawing state to remove the underline.
         *
         * @param ds text paint used to draw the span.
         */
        @Override
        public void updateDrawState(@androidx.annotation.NonNull TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
        }

        /**
         * Handles clicks on the audio button by animating the associated image,
         * clearing text selection, and playing the audio resource when available.
         *
         * @param widget view on which the span was clicked.
         */
        @Override
        public void onClick(View widget) {
            animateAudioImage(widget);
            clearTextSelection(widget);
            playResourceAudio();
        }

        /**
         * Animates the associated audio image when one can be found.
         *
         * @param widget view on which the span was clicked.
         */
        private void animateAudioImage(View widget) {
            AudioImageSpan targetSpan = audioImageSpan;
            if (targetSpan == null) {
                targetSpan = findAudioImageSpan(widget);
            }
            if (targetSpan != null) {
                targetSpan.animateBackground(widget);
            }
        }

        /**
         * Finds the audio image span associated with this button.
         *
         * @param widget view on which the span was clicked.
         * @return associated audio image span, or {@code null} if none is found.
         */
        private AudioImageSpan findAudioImageSpan(View widget) {
            if (!(widget instanceof TextView)) {
                return null;
            }
            TextView textView = (TextView) widget;
            if (!(textView.getText() instanceof Spanned)) {
                return null;
            }
            Spanned spanned = (Spanned) textView.getText();
            int start = spanned.getSpanStart(this);
            int end = spanned.getSpanEnd(this);
            if (start < 0 || end < start) {
                return null;
            }
            AudioImageSpan[] spans = spanned.getSpans(start, end, AudioImageSpan.class);
            if (spans.length == 0) {
                return null;
            }
            return spans[0];
        }

        /**
         * Clears the text selection immediately and after the view's pending operations.
         *
         * @param widget view on which the span was clicked.
         */
        private void clearTextSelection(View widget) {
            if (!(widget instanceof TextView)) {
                return;
            }
            TextView textView = (TextView) widget;
            removeTextSelection(textView);
            textView.post(() -> removeTextSelection(textView));
        }

        /**
         * Removes the selection from the text view's text when it is mutable.
         *
         * @param textView text view whose selection should be removed.
         */
        private void removeTextSelection(TextView textView) {
            if (textView.getText() instanceof Spannable) {
                Spannable spannable = (Spannable) textView.getText();
                Selection.removeSelection(spannable);
            }
        }

        /**
         * Retrieves and plays the audio resource when available.
         */
        private void playResourceAudio() {
            if (lexicalEntry == null || resourceName == null) {
                return;
            }
            byte[] audio = lexicalEntry.getResource(resourceName);
            if (audio == null || audio.length == 0) {
                return;
            }
            try {
                playAudio(audio);
            } catch (IOException e) {
                String message = String.format(
                    "Cannot play audio file %s. Audio length: %s",
                    resourceName,
                    audio.length
                );
                Log.e(TAG, message, e);
            }
        }

        /**
         * Provides audio data from a byte array to a media player.
         */
        private class ByteArrayMediaDataSource extends MediaDataSource {

            /**
             * Audio data provided to the media player.
             */
            private final byte[] data;

            /**
             * Creates a media data source backed by a byte array.
             *
             * @param data audio data to provide.
             */
            ByteArrayMediaDataSource(byte[] data) {
                this.data = data;
            }

            /**
             * Reads audio data from the specified position into the buffer.
             *
             * @param position position in the audio data from which to read.
             * @param buffer buffer into which the data is read.
             * @param offset offset in the buffer at which to start writing.
             * @param size maximum number of bytes to read.
             * @return number of bytes read, or {@code -1} if the end of the data is reached.
             */
            @Override
            public int readAt(long position, byte[] buffer, int offset, int size) {
                if (position >= data.length) {
                    return -1; // end of stream
                }
                int length = Math.min(size, (int) (data.length - position));
                System.arraycopy(data, (int) position, buffer, offset, length);
                return length;
            }

            /**
             * Returns the size of the audio data.
             *
             * @return audio data size in bytes.
             */
            @Override
            public long getSize() {
                return data.length;
            }

            /**
             * Closes the data source.
             *
             * <p>No resources need to be released because the data is held in memory.</p>
             */
            @Override
            public void close() {
                // nothing to release
            }
        }

        /**
         * Plays audio file.
         *
         * @param audio audio to be played.
         * @throws IOException on failure to play the audio
         */
        private void playAudio(byte[] audio) throws IOException {
            MediaPlayer player = new MediaPlayer();
            player.setDataSource(new ByteArrayMediaDataSource(audio));
            player.setOnCompletionListener(MediaPlayer::release);
            player.prepare();
            player.start();
        }
    }
}
