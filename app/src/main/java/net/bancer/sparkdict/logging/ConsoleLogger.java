package net.bancer.sparkdict.logging;

public class ConsoleLogger implements Logger {

    @Override
    public void error(String tag, String message) {
        System.err.println(message);
    }

    @Override
    public void error(String tag, String message, Throwable throwable) {
        System.err.println(message);
        throwable.printStackTrace(System.err);
    }
}
