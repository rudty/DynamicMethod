package org.rudtyz.generate;

import org.junit.jupiter.api.Test;

public class Example {
    public void instanceMethod() {
        System.out.println("call instanceMethod");
    }

    public static void staticMethod() {
        System.out.println("call staticMethod");
    }

    @Test
    public void call_instance() throws Exception {
        /*
         * // Generated code
         * public final class GenerateClass implements Runnable {
         *     private final Example example;
         *     public GenerateClass(Example example) {
         *         this.example = example;
         *     }
         *     public void run() {
         *         example.runExample();
         *     }
         * }
         */
        Class<? extends Runnable> runExample = DynamicMethodFactory.generateClass(
                Example.class,
                Example.class.getMethod("instanceMethod"),
                Runnable.class);

        /*
         * // Generated code
         *  GenerateClass g = new GenerateClass(new Example());
         *  g.run();
         */
        Runnable generateRunnable = runExample
                .getConstructor(Example.class)
                .newInstance(this);
        generateRunnable.run();
    }

    @Test
    public void call_static() throws Exception {
        /*
         * // Generated code
         * public final class GenerateClass implements Runnable {
         *     public GenerateClass(Example example) {
         *     }
         *     public GenerateClass() {
         *     }
         *     public void run() {
         *         Example.staticRunExample();
         *     }
         * }
         */
        Class<? extends Runnable> runExample = DynamicMethodFactory.generateClass(
                Example.class,
                Example.class.getMethod("staticMethod"),
                Runnable.class);

        /*
         *  GenerateClass g = new GenerateClass();
         *  g.run();
         */
        Runnable generateRunnable = runExample
                .getConstructor()
                .newInstance();
        generateRunnable.run();
    }
}
