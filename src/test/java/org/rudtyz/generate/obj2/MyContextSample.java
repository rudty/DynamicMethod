package org.rudtyz.generate.obj2;

public class MyContextSample {
    public static class InnerSample {
        public void context(MyContextSample s) {
            System.out.println("context");
        }
        public void contexti(MyContextSample s, int i) {
            System.out.println("context int " + i);
        }
    }

    public InnerSample newInnerSample() {
        return new InnerSample();
    }

    public int getIntValue() {
        return 42;
    }
}
