package de.swa.clv.demo;

import java.util.Arrays;

public class Util {
    
    public static Enum<?>[] appendNull(Enum<?>[] array) {
        Enum<?>[] arrayWithNull = Arrays.copyOf(array, array.length + 1);
        arrayWithNull[array.length] = null;
        return arrayWithNull;
    }
}
