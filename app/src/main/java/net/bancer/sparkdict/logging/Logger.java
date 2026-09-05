package net.bancer.sparkdict.logging;

public interface Logger {

    void error(String tag, String message);

    void error(String tag, String message, Throwable throwable);
}
