package net.craswell.commands;

import org.apache.commons.exec.Executor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

class ExecutorTests {
    private static final String TEST_PASS_INPUT = "TEST_passPhRaSe";
    private static final String TEST_KEY_OUTPUT = "/tmp/test.key";
    private static final String TEST_PUB_OUTPUT = "/tmp/test.pub";
    private static final String TEST_CA = "/tmp/test-ca.pem";

    private static final ExecutorFactory EXECUTOR_FACTORY = new ExecutorFactory();
    private static final OpenSSLCommandFactory OPEN_SSL_COMMAND_FACTORY = new OpenSSLCommandFactory();

    @Test
    void canCreateRSAPrivateKey()
            throws IOException {
        final Path privateKeyOutputPath = Paths.get(TEST_KEY_OUTPUT);
        final Path publicKeyOutputPath = Paths.get(TEST_PUB_OUTPUT);

        final File privateKeyFile = privateKeyOutputPath.toFile();
        privateKeyFile.deleteOnExit();

        final File publicKeyFile = publicKeyOutputPath.toFile();
        publicKeyFile.deleteOnExit();

        final Executor executor = EXECUTOR_FACTORY.build();

        final int createPrivateKeyResult = executor.execute(OPEN_SSL_COMMAND_FACTORY.createRSAPrivateKey(
                TEST_PASS_INPUT,
                privateKeyFile.getAbsolutePath()));

        Assertions.assertEquals(
                0,
                createPrivateKeyResult);

        Assertions.assertTrue(
                privateKeyFile.exists());

        final int createPublicKeyResult = executor.execute(OPEN_SSL_COMMAND_FACTORY.createRSAPublicKey(
                TEST_PASS_INPUT,
                privateKeyFile.getAbsolutePath(),
                publicKeyFile.getAbsolutePath()));

        Assertions.assertEquals(
                0,
                createPublicKeyResult);

        Assertions.assertTrue(
                publicKeyFile.exists());
    }

    @Test
    void canCreateCertificateAuthority()
            throws IOException {
        final Path privateKeyOutputPath = Paths.get(TEST_KEY_OUTPUT);
        final Path certificateOutputPath = Paths.get(TEST_CA);

        final File privateKeyFile = privateKeyOutputPath.toFile();
        privateKeyFile.deleteOnExit();

        final File caFile = certificateOutputPath.toFile();
        caFile.deleteOnExit();

        final Executor executor = EXECUTOR_FACTORY.build();

        final int createPrivateKeyResult = executor.execute(OPEN_SSL_COMMAND_FACTORY.createRSAPrivateKey(
                TEST_PASS_INPUT,
                privateKeyFile.getAbsolutePath()));

        Assertions.assertEquals(
                0,
                createPrivateKeyResult);

        Assertions.assertTrue(
                privateKeyFile.exists());

        final int createCertificateAuthorityResult = executor.execute(OPEN_SSL_COMMAND_FACTORY.createCertificateAuthority(
                TEST_PASS_INPUT,
                privateKeyFile.getAbsolutePath(),
                caFile.getAbsolutePath(),
                3650));

        Assertions.assertEquals(
                0,
                createCertificateAuthorityResult);

        Assertions.assertTrue(
                caFile.exists());

        final int viewCertificateResult = executor.execute(
                OPEN_SSL_COMMAND_FACTORY.viewCertificate(caFile.getAbsolutePath()));

        Assertions.assertEquals(
                0,
                viewCertificateResult);
    }
}
