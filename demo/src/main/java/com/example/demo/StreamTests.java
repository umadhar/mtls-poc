package com.example.demo;

import java.util.*;
import java.util.stream.Stream;

@FunctionalInterface
interface A{
    void show();
    String toString();
}
@FunctionalInterface
interface B{
    void printit();
    String toString();

}

public class StreamTests {
    public static void main(String[] args) {
        // Functional interface examples & Anonymous functions
        A obj = () -> System.out.println("Hello World");
        obj.show();

        B obj2 = () -> System.out.println("Hello Worldb");
        obj2.printit();

        //Stream functions method reference

        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);

        Integer s4 = list.stream()
                       .filter(x -> x % 2 == 0)
                       .map(x -> x * 2)
                       .reduce(0,(x, y) -> x + y);

        System.out.println(s4);







    }
}
