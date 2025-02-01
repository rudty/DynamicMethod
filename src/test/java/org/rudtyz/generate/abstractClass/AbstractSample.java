package org.rudtyz.gen.abstractClass;

public abstract class AbstractSample {
    public String myString() {
        System.out.println("AbstractSample.myString String ");
        return "42";
    }

    public abstract int myInt();
}
