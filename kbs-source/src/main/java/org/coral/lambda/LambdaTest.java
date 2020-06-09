package org.coral.lambda;

import java.io.IOException;
import java.util.stream.Stream;

public class LambdaTest {
    public static void main(String[] args) {
        Stream.of("a", "b", "c").forEach(str -> {
            try {
                System.out.println("111111111");
                System.out.println("111111111");
                System.out.println("111111111");
                System.out.println("111111111");
                throw new IOException();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
