package org.coral.test;

class Dog {

    public static void bark() {

        System.out.println("Dog.woof ");

    }

}

class Basenji extends Dog {

    public static void bark() {
        System.out.println("Dog.woof ");
    }

}

public class Bark {

    public static void main(String args[]) {

        Dog woofer = new Dog();

        Dog nipper = new Basenji();

        woofer.bark();

        nipper.bark();

    }

}