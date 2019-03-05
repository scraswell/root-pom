package net.craswell.commands.streams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * An output stream that writes to an SLF4J logger.
 */
// JUSTIFICATION: Overriding only this method is sufficient.
@SuppressWarnings("squid:S4349")
public class OutputLogStream
        extends OutputStream {
    /**
     * The encoding used for strings.
     */
    static final String ENCODING = "UTF-8";

    /**
     * New lines represented as a string.
     */
    static final String NEW_LINE = "\n";

    /**
     * The class logger.
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * The string builder.
     */
    private final StringBuilder stringBuilder = new StringBuilder();

    /**
     * Tracks the number of times the stream was flushed.
     */
    private long flushCount = 0;

    /**
     * The log level for this output stream.
     */
    private final Level level;

    /**
     * Initializes a new instance of the OutputLogStream class. 
     * @param level The logging level for this stream.
     */
    @SuppressWarnings("WeakerAccess")
    public OutputLogStream(Level level) {
        this.level = level;
    }

    /**
     * Writes a byte to the stream.
     *
     * @param b The byte to be written.
     * @throws IOException Thrown when a problem occurs during the write.
     */
    @Override
    public void write(int b)
            throws IOException {

        final String toWrite = new String(
                new byte[]{(byte) (b & 0xff)},
                ENCODING);

        if (toWrite.equals(NEW_LINE)
                && this.getStringBuilder().length() > 0) {
            this.flush();

        } else {
            this.getStringBuilder()
                    .append(toWrite);
        }
    }

    /**
     * Flushes the stream (to the logger).  This clears the StringBuilder.
     */
    @Override
    public void flush()
            throws IOException {
        super.flush();

        final String stringToWrite = this
                .getStringBuilder()
                .toString();

        this.getStringBuilder().delete(
                0,
                this.getStringBuilder().length());

        try {
            this.getLoggingMethod().invoke(
                    this.getLogger(),
                    stringToWrite);
        } catch (
                IllegalAccessException
                | InvocationTargetException
                | NoSuchMethodException e) {
            this.getLogger().error(
                    "An exception occurred while attempting to write to the logger.",
                    e);
        }

        this.flushCount++;
    }

    /**
     * Gets the number of times the stream was flushed.  Facilitates testing.
     * @return The number of times the stream was flushed.  Facilitates testing.
     */
    long getFlushCount() {
        return this.flushCount;
    }

    /**
     * Gets the logger.
     *
     * @return The logger.
     */
    private Logger getLogger() {
        return this.logger;
    }

    /**
     * Gets the logging level.
     * @return The logging level.
     */
    private Level getLevel() {
        return this.level;
    }

    /**
     * Gets the string builder.
     *
     * @return The string builder.
     */
    private StringBuilder getStringBuilder() {
        return this.stringBuilder;
    }

    /**
     * Gets the logging method to be used.
     * @return the logging method to be used.
     * @throws NoSuchMethodException Thrown when the method specified does not exist.
     */
    // JUSTIFICATION: later expansion.
    @SuppressWarnings("squid:S1301")
    private Method getLoggingMethod()
            throws NoSuchMethodException {
        final Method method;

        switch(this.getLevel()) {
            case ERROR:
                method = this.getLogger()
                        .getClass()
                        .getMethod("error", String.class);
                break;
            default:
                method = this.getLogger()
                        .getClass()
                        .getMethod("info", String.class);
        }

        return method;
    }
}
