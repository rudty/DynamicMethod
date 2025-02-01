package org.rudtyz.gen.obj0;

public class SampleI {
    public void my(int a) {
        System.out.println("my: " + a);
    }

    public static void staticMy(int a) {
        System.out.println("staticMy: " + a);
    }

    public long myIntToLong(int a) {
        System.out.println("myIntToLong: " + a);
        return a;
    }

    public static long staticMyIntToLong(int a) {
        System.out.println("staticMyIntToLong: " + a);
        return a;
    }
}
