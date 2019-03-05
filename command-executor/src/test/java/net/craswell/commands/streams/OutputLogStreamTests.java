package net.craswell.commands.streams;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.io.IOException;

class OutputLogStreamTests {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(OutputLogStreamTests.class);

    private static final String TEST_STRING = "Hello World!".concat(OutputLogStream.NEW_LINE)
            .concat("Hello World, again!").concat(OutputLogStream.NEW_LINE)
            .concat("Hello World, thrice!").concat(OutputLogStream.NEW_LINE);

    @Test
    void canWrite()
            throws IOException {
        LOGGER.info("Test starting...  You should see three lines appended (at the error level) before the test completion message.");

        try (final OutputLogStream outputLogStream = new OutputLogStream(Level.ERROR)) {
            outputLogStream.write(
                    TEST_STRING.getBytes(OutputLogStream.ENCODING));

            Assertions.assertEquals(
                    3,
                    outputLogStream.getFlushCount());
        }

        LOGGER.info("Test completed.");
    }
}
