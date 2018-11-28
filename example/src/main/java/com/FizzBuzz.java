package com;

public class FizzBuzz {

    private static void fizzBuzzSieve(int[] a, int count) {
        for (int i = 3; i < count; i = i + 3) {
            a[i] = 1;
        }
        for (int i = 5; i < count; i = i + 5) {
            a[i] += 2;
        }
    }

    private static void fizzBuzzMod(int[] a, int count) {
        for (int i = 0; i < count; i++) {
            if (i % 15 == 0) a[i] = 3;
            else if (i % 3 == 0) a[i] = 1;
            else if (i % 5 == 0) a[i] = 2;
        }
    }

    public static void main(String[] args) {
        long time;
        int[] a;

        int COUNT = 10000000;

        a = new int[COUNT];
        time = System.currentTimeMillis();
        fizzBuzzSieve(a, COUNT);
        time = System.currentTimeMillis() - time;
        System.out.printf("fizzBuzzSieve: %d ms\n", time);

//        a = new int[COUNT];
        for (int i = 0; i < COUNT; i++) a[i] = 0;
        time = System.currentTimeMillis();
        fizzBuzzMod(a, COUNT);
        time = System.currentTimeMillis() - time;
        System.out.printf("fizzBuzzMod: %d ms\n", time);
    }

}

