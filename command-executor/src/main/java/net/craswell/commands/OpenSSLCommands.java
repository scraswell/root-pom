package net.craswell.commands;

import org.apache.commons.exec.CommandLine;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

// JUSTIFICATION: Later use.
@SuppressWarnings("WeakerAccess")
public class OpenSSLCommands {
    private static final String OPENSSL_BIN = "/usr/bin/openssl";
    private static final String ENCODING = "UTF-8";
    private static final int KEY_LENGTH = 3072;

    public static CommandLine createRSAPrivateKey(
            final String passPhrase,
            final String outputFile)
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

        final CommandLine commandLine = CommandLine.parse(OPENSSL_BIN);

        commandLine.addArgument("genrsa");
        commandLine.addArgument("-aes256");
        commandLine.addArgument("-passout");
        commandLine.addArgument(String.format(
                "file:%s",
                tempFile.toAbsolutePath().toString()));
        commandLine.addArgument("-out");
        commandLine.addArgument(outputFile);
        commandLine.addArgument(Integer.toString(KEY_LENGTH));

        return commandLine;
    }

    public static CommandLine createRSAPublicKey(
            final String passPhrase,
            final String privateKeyFile,
            final String outputFile)
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

    private OpenSSLCommands() {
    }
}
