package com.github.cao.awa.apricot.identifier;

import com.github.cao.awa.apricot.annotations.Stable;
import com.github.cao.awa.sinuatum.manipulate.Manipulate;

import java.util.Random;

@Stable
public class FileRandomIdentifier {
    private static final char[] CHARS = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', '_', '-'};
    private static final Random RANDOM = new Random();

    public static String create() {
        return create(1024);
    }

    public static String create(int size) {
        return new String(Manipulate.operation(
                new char[size],
                result -> {
                    for (int i = 0; i < size; i++) {
                        result[i] = CHARS[RANDOM.nextInt(CHARS.length)];
                    }
                }
        ));
    }
}
