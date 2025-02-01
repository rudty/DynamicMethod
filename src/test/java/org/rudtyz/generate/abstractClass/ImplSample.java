package org.rudtyz.gen.abstractClass;

public class ImplSample extends AbstractSample {
    @Override
    public int myInt() {
        System.out.println("ImplSample::myInt");
        return 42;
    }
}
