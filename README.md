# DynamicMethod
generate dynamic method

# Example
```java
public class Example {
    public void instanceMethod() {
        System.out.println("call instanceMethod");
    }

    public static void staticMethod() {
        System.out.println("call staticMethod");
    }
}
```

```java
public class Main {
    public static void main(String[] args) {
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
                .newInstance(new Example());
        generateRunnable.run();
    }
}
```