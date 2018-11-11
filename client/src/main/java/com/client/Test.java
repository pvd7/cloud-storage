package com.client;

public class Test {

    final static int MAX = 1000000000;

    static void simpleNumber1() {
        int c = 2;
        boolean b;
        for (int i = 3; i < MAX; i++) {
            b = false;
            for (int j = 2; j < i; j++) {
                if (i % j == 0) {
                    b = true;
                    break;
                }
            }
            if (!b) c++;
        }
        System.out.println(c);
    }

    static void simpleNumber2() {
        boolean[] arr = new boolean[MAX];
        int count = 0;
        for (int i = 2; i < MAX; i++) {
           if (!arr[i]) {
               count++;
               for (int j = i * 2; j < MAX; j += i) {
                   if (!arr[j]) arr[j] = true;
               }
           }
        }
        System.out.println(count);
    }

    public static void main(String[] args) {
        simpleNumber2();

    }

}
