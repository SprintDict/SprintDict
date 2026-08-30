package net.bancer.sparkdict;

/**
 * Small, hardcoded feature flags for this build. No settings UI -- flip the
 * constant and rebuild.
 */
public final class AppConfig {

    /**
     * When true, dictionary files are resolved through the Storage Access
     * Framework ({@code SafDictionaryFiles}), using the persisted tree Uri
     * saved under {@code PREF_DICT_ROOT_URI_NAME}, instead of through raw
     * {@code java.io.File} paths ({@code FileDictionaryFiles}).
     *
     * <p>Not yet consulted anywhere -- Shelf/Book/BookInfo still always
     * construct {@code FileDictionaryFiles} regardless of this flag. Wiring
     * that up is the next step.</p>
     */
    public static final boolean USE_SAF_STORAGE = true;

    private AppConfig() {
    }
}
