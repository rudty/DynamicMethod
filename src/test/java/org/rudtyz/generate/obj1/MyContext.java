package org.rudtyz.gen.obj1;

public class MyContext implements IContext0 {
    public int getInt() {
        System.out.println("MyContext.getInt");
        return 42;
    }

    public String getString() {
        System.out.println("MyContext.getInt");
        return "42";
    }

    @Override
    public int getIntContext0() {
        return 53;
    }
}
