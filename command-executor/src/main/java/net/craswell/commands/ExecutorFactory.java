package net.craswell.commands;

import net.craswell.commands.streams.OutputLogStream;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.event.Level;

/**
 * Factory responsible for the creation of command executors.
 */
// JUSTIFICATION: later use.
@SuppressWarnings("WeakerAccess")
public class ExecutorFactory {
    /**
     * The default execution timeout is 60 seconds.
     */
    private static final long DEFAULT_TIMEOUT = 60L * 1000L;

    /**
     * Builds an executor from which a command can be executed using the specified execution timeout value.
     * @param executionTimeout The execution timeout value.
     * @return The built executor.
     */
    public Executor build(final long executionTimeout) {
        final DefaultExecutor executor = new DefaultExecutor();

        final ExecuteWatchdog executeWatchdog = new ExecuteWatchdog(executionTimeout);

        final PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(
                new OutputLogStream(Level.INFO),
                new OutputLogStream(Level.ERROR));

        executor.setWatchdog(executeWatchdog);
        executor.setStreamHandler(pumpStreamHandler);

        return executor;
    }

    /**
     * Builds an executor from which a command can be executed using the default execution timeout value.
     * @return The built executor.
     */
    public Executor build() {
        return this.build(DEFAULT_TIMEOUT);
    }
}
