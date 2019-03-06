package net.craswell.commands;

import org.apache.commons.exec.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

/**
 * Creates OpenSSL Commands.
 */
// JUSTIFICATION: Later use.
@SuppressWarnings("WeakerAccess")
public final class OpenSSLCommandFactory {
    private static final String OPENSSL_BIN = "/usr/bin/openssl";
    private static final String ENCODING = "UTF-8";
    private static final int KEY_LENGTH = 3072;

    /**
     * Generates a command line that, when invoked, creates the corresponding public key to a private key.
     *
     * @param passPhrase     The passphrase with which the private key file was encrypted.
     * @param privateKeyFile The private key file path.
     * @param outputFile     The output file used to store the public key.
     * @return The command line that, when invoked, creates the corresponding public key to a private key.
     * @throws IOException Thrown when a problem with file IO occurs.
     */
    public CommandLine createRSAPublicKey(
            final String passPhrase,
            final String privateKeyFile,
            final String outputFile)
            throws IOException {
        if (passPhrase == null
                || passPhrase.isEmpty()) {
            throw new IllegalArgumentException("The passphrase was null or empty.");
        }

        if (privateKeyFile == null
                || privateKeyFile.isEmpty()) {
            throw new IllegalArgumentException("The private key file was null or empty.");
        }

        if (outputFile == null
                || outputFile.isEmpty()) {
            throw new IllegalArgumentException("The private key file was null or empty.");
        }

        final Path tempFile = this.writePassphraseFile(passPhrase);

        final CommandLine commandLine = CommandLine.parse(OPENSSL_BIN);

        commandLine.addArgument("rsa");
        commandLine.addArgument("-in");
        commandLine.addArgument(privateKeyFile);
        commandLine.addArgument("-passin");
        commandLine.addArgument(String.format(
                "file:%s",
                tempFile.toAbsolutePath().toString()));
        commandLine.addArgument("-pubout");
        commandLine.addArgument("-out");
        commandLine.addArgument(outputFile);

        return commandLine;
    }

    /**
     * Generates a command line that, when invoked, will create an RSA private key.
     *
     * @param passPhrase The passphrase used to encrypt the private key.
     * @param outputFile The output file in which the encrypted private key will be placed.
     * @return The command line that, when invoked, will create an RSA private key.
     * @throws IOException Thrown when a problem with file IO occurs.
     */
    public CommandLine createRSAPrivateKey(
            final String passPhrase,
            final String outputFile)
            throws IOException {

        if (passPhrase == null
                || passPhrase.isEmpty()) {
            throw new IllegalArgumentException("The passphrase was null or empty.");
        }

        if (outputFile == null
                || outputFile.isEmpty()) {
            throw new IllegalArgumentException("The private key file was null or empty.");
        }

        final Path passphraseFile = this.writePassphraseFile(passPhrase);

        final CommandLine commandLine = CommandLine.parse(OPENSSL_BIN);

        commandLine.addArgument("genrsa");
        commandLine.addArgument("-aes256");
        commandLine.addArgument("-passout");
        commandLine.addArgument(String.format(
                "file:%s",
                passphraseFile.toAbsolutePath().toString()));
        commandLine.addArgument("-out");
        commandLine.addArgument(outputFile);
        commandLine.addArgument(Integer.toString(KEY_LENGTH));

        return commandLine;
    }

    /**
     * Generates a command line that, when invoked, will create a certificate authority signing certificate.
     * @param passPhrase The passphrase used to encrypt the private key.
     * @param privateKeyFile The private key file.
     * @param outputFile The file in which the certificate will be stored.
     * @param daysValid The number of days for which the certificate should be valid.
     * @return The command line that, when invoked, will create a certificate authority signing certificate.
     * @throws IOException Thrown when an issue arises with file IO.
     */
    public CommandLine createCertificateAuthority(
            final String passPhrase,
            final String privateKeyFile,
            final String outputFile,
            final int daysValid)
            throws IOException {

        if (passPhrase == null
                || passPhrase.isEmpty()) {
            throw new IllegalArgumentException("The passphrase was null or empty.");
        }

        if (privateKeyFile == null
                || privateKeyFile.isEmpty()) {
            throw new IllegalArgumentException("The private key file was null or empty.");
        }

        if (outputFile == null
                || outputFile.isEmpty()) {
            throw new IllegalArgumentException("The private key file was null or empty.");
        }

        if (daysValid < 1) {
            throw new IllegalArgumentException("The validity period was less than 1 day.");
        }

        final Path configFilePath = this.getConfigFilePath();
        final Path passphraseFile = this.writePassphraseFile(passPhrase);

        final Path privateKeyFilePath = Paths.get(privateKeyFile);
        final Path outputFilePath = Paths.get(outputFile);

        final CommandLine commandLine = CommandLine.parse(OPENSSL_BIN);

        commandLine.addArguments(new String[]{
                "req",
                "-config", configFilePath.toAbsolutePath().toString(),
                "-key", privateKeyFilePath.toAbsolutePath().toString(),
                "-passin", "file:".concat(passphraseFile.toAbsolutePath().toString()),
                "-new",
                "-x509",
                "-days", Integer.toString(daysValid),
                "-sha256",
                "-extensions", "certificate_authority",
                "-out", outputFilePath.toAbsolutePath().toString()
        });

        commandLine.addArgument("-subj");
        commandLine.addArgument(
                "/C=CA/ST=New Brunswick/L=Moncton/O=Home/OU=Certificates/CN=HomeCA",
                false);

        return commandLine;
    }

    /**
     * Generates a command line that, when invoked, will view a certificate.
     * @param certificateFile The certificate file.
     * @return The command line to view a certificate.
     */
    public CommandLine viewCertificate(final String certificateFile) {
        if (certificateFile == null
                || certificateFile.isEmpty()) {
            throw new IllegalArgumentException("The certificate file was null or empty.");
        }

        final Path certificateFilePath = Paths.get(certificateFile);

        final CommandLine commandLine = CommandLine.parse(OPENSSL_BIN);

        commandLine.addArguments(new String[]{
                "x509",
                "-noout",
                "-text",
                "-in", certificateFilePath.toAbsolutePath().toString()
        });

        return commandLine;
    }


    /**
     * Copies the OpenSSL configuration file, a resource, to a temporary location on the file system.  Returns
     * the path on the file system.
     *
     * @return The path to the configuration file on the file system.
     * @throws IOException Thrown when a problem with file IO occurs.
     */
    private Path getConfigFilePath()
            throws IOException {
        final URL configFileResourceUri = ClassLoader
                .getSystemClassLoader()
                .getResource("conf/openssl.cnf");

        if (configFileResourceUri == null) {
            throw new IllegalStateException("Unable to locate the OpenSSL configuration file.");
        }

        final Path configFilePath;
        try {
            configFilePath = Paths.get(configFileResourceUri.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(
                    "Unable to parse the URI for the configuration file.",
                    e);
        }

        final Path tempConfigFilePath = Files.createTempFile(
                "openssl-",
                "-config");

        Files.copy(
                configFilePath,
                tempConfigFilePath,
                StandardCopyOption.REPLACE_EXISTING);

        tempConfigFilePath
                .toFile()
                .deleteOnExit();

        return tempConfigFilePath;
    }

    /**
     * Writes a passphrase to a file.  This temporary file will be read by OpenSSL.
     *
     * @param passPhrase The passphrase file.
     * @return The path to the temporary passphrase file.
     * @throws IOException Thrown when a problem with file IO occurs.
     */
    private Path writePassphraseFile(String passPhrase)
            throws IOException {

        final Set<PosixFilePermission> ownerOnlyReadWrite = PosixFilePermissions.fromString("rw-------");
        final FileAttribute<?> permissions = PosixFilePermissions.asFileAttribute(ownerOnlyReadWrite);
        final Path tempFile = Files.createTempFile(
                "openssl-",
                "-passphrase",
                permissions);

        tempFile.toFile()
                .deleteOnExit();

        try (final FileOutputStream fileOutputStream = new FileOutputStream(tempFile.toFile())) {
            fileOutputStream.write(passPhrase.getBytes(ENCODING));
        }

        return tempFile;
    }
}
