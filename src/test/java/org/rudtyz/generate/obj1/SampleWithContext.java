package org.rudtyz.gen.obj1;

import java.util.Objects;

public class SampleWithContext {
    public void context(MyContext context) {
        Objects.requireNonNull(context);
        System.out.println("context");
    }

    public void icontext(IContext0 context) {
        Objects.requireNonNull(context);
        System.out.println("icontext");
    }

    public void icontextIntArg(IContext0 context, int a) {
        Objects.requireNonNull(context);
        System.out.println("icontext int arg: " + a);
    }

    public void contextIntArg(MyContext context, int a) {
        Objects.requireNonNull(context);
        System.out.println("MyContext int arg: " + a);
    }
}
