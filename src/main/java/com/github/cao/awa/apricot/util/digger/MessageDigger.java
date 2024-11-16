package com.github.cao.awa.apricot.util.digger;

import com.github.cao.awa.apricot.annotations.Stable;
import com.github.cao.awa.lilium.mathematic.Mathematics;
import com.github.cao.awa.sinuatum.manipulate.ManipulateBuilder;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Stable
public class MessageDigger {
    private static final int BUF_SIZE = 16384;

    public static String digest(byte[] message, DigestAlgorithm sha) {
        MessageDigest digest = sha.instance();
        if (digest == null) {
            return null;
        }
        digest.update(message);
        StringBuilder result = new StringBuilder();
        digest(digest,
               result
        );
        return result.toString();
    }

    public static String digest(String message, DigestAlgorithm sha) {
        return digest(message.getBytes(StandardCharsets.UTF_8),
                      sha
        );
    }

    public static byte[] digestBytes(byte[] message, DigestAlgorithm algorithm) {
        MessageDigest digest = ManipulateBuilder.supply(() -> MessageDigest.getInstance(algorithm.instanceName())).get();
        if (digest == null) {
            return null;
        }
        digest.update(message);
        return digest.digest();
    }

    public static byte[] digestToBytes(byte[] message, DigestAlgorithm sha) {
        return digestBytes(message,
                                          sha
                                   )
        ;
    }

    public static byte[] digestFileToBytes(File file, DigestAlgorithm sha) throws Exception {
        return Mathematics.toBytes(digestFile(file,
                                              sha
                                   ),
                                   16
        );
    }
    public static String digestFile(File file, DigestAlgorithm sha) throws Exception {
        if (! file.isFile()) {
            return "0";
        }

        RandomAccessFile accessFile = new RandomAccessFile(
                file,
                "r"
        );

        MessageDigest digest = MessageDigest.getInstance(sha.instanceName());

        byte[] buffer = new byte[BUF_SIZE];

        long read = 0;

        long offset = accessFile.length();
        int length;
        while (read < offset) {
            length = (int) (offset - read < BUF_SIZE ? offset - read : BUF_SIZE);
            accessFile.read(
                    buffer,
                    0,
                    length
            );

            digest.update(
                    buffer,
                    0,
                    length
            );

            read += length;
        }

        accessFile.close();

        StringBuilder result = new StringBuilder();

        digest(digest,
               result
        );

        return result.toString();
    }

    private static void digest(MessageDigest digest, StringBuilder result) {
        for (byte b : digest.digest()) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() < 2) {
                result.append(0);
            }
            result.append(hex);
        }
    }

    @Deprecated
    public enum Sha1 implements Sha {
        SHA("SHA-1");

        private final String instance;
        private final MessageDigest digestInstance;

        Sha1(String instance) {
            this.instance = instance;
            this.digestInstance = ManipulateBuilder.supply(() -> MessageDigest.getInstance(instance)).get();
        }

        @Override
        public String instanceName() {
            return instance;
        }

        @Override
        public MessageDigest instance() {
            return this.digestInstance;
        }
    }

    public enum Sha3 implements Sha {
        SHA_224("SHA-224"), SHA_256("SHA-256"), SHA_512("SHA-512");

        private final String instance;
        private final MessageDigest digestInstance;

        Sha3(String instance) {
            this.instance = instance;
            this.digestInstance = ManipulateBuilder.supply(() -> MessageDigest.getInstance(instance)).get();
        }

        @Override
        public String instanceName() {
            return instance;
        }

        @Override
        public MessageDigest instance() {
            return this.digestInstance;
        }
    }

    public interface DigestAlgorithm {
        String instanceName();
        MessageDigest instance();
    }

    public interface Sha extends DigestAlgorithm {
    }

    public interface MD extends DigestAlgorithm {
    }
}

