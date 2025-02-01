package org.rudtyz.generate;

import org.objectweb.asm.Opcodes;

import java.lang.reflect.Method;

public final class DynamicFunctionFactory {
    static int javaVersion = Opcodes.V21;

    public record GenerateResult(String className, byte[] classData) {
    }

    private DynamicFunctionFactory() {
        throw new AssertionError("static class");
    }


    /**
     * <pre>
     * {@code
     *  public class [GenerateClass] {
     *    private final [CallObjectClass] instance;
     *    public void [callMethod]() {
     *      instance.callMethod();
     *    }
     *  }
     * }
     * </pre>
     * @param callObjectClass call class
     * @param callMethod call method
     * @param interfaceClass generated class implements interface
     * @return generate class
     * @param <T> interface type
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<? extends T> generateClass(
            final Class<?> callObjectClass,
            final Method callMethod,
            final Class<T> interfaceClass) {
        final GenerateResult result = generate(callObjectClass, callMethod, interfaceClass);
        final Class<?> generateClass = DynamicClassLoader.INSTANCE.defineClass(
                result.className(),
                result.classData());
        return (Class<? extends T>)generateClass;
    }

    public static <T> GenerateResult generate(
            final Class<?> callObjectClass,
            final Method callMethod,
            final Class<T> interfaceClass) {
        final DynamicFunctionFactory1 g = new DynamicFunctionFactory1(callObjectClass, callMethod, interfaceClass, null, null, null);
        final byte[] generateClassData = g.generate();
        final String generateClassName = g.getClassName();
        return new GenerateResult(generateClassName, generateClassData);
    }

    /**
     * <pre>
     * {@code
     *  public class [GenerateClass] {
     *    private final [CallObjectClass] instance;
     *    public void [callMethod]() {
     *      instance.callMethod();
     *    }
     *  }
     * }
     * </pre>
     * @param callObjectClass call class
     * @param callMethod call method
     * @param interfaceClass generated class implements interface
     * @param contextClass nullable, parameter class
     * @param contextToCallObject nullable, context to call object
     * @param parameterDispatcher nullable, parameter to method
     * @return generate class
     * @param <T> interface type
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<? extends T> generateClass(
            final Class<?> callObjectClass,
            final Method callMethod,
            final Class<T> interfaceClass,
            final Class<?> contextClass,
            final ContextToCallObject contextToCallObject,
            final ParameterDispatcher parameterDispatcher) {

        final GenerateResult result = generate(
                callObjectClass,
                callMethod,
                interfaceClass,
                contextClass,
                contextToCallObject,
                parameterDispatcher);
        final Class<?> generateClass = DynamicClassLoader.INSTANCE.defineClass(
                result.className(),
                result.classData());
        return (Class<? extends T>) generateClass;
    }

    public static GenerateResult generate(
            final Class<?> callObjectClass,
            final Method callMethod,
            final Class<?> interfaceClass,
            final Class<?> contextClass,
            final ContextToCallObject contextToCallObject,
            final ParameterDispatcher parameterDispatcher) {
        final DynamicFunctionFactory1 g = new DynamicFunctionFactory1(
                callObjectClass,
                callMethod,
                interfaceClass,
                contextClass,
                contextToCallObject,
                parameterDispatcher);
        final byte[] generateClassData = g.generate();
        final String generateClassName = g.getClassName();
        return new GenerateResult(generateClassName, generateClassData);
    }


    /**
     * <ul>
     * <li>21 = JAVA 21</li>
     * <li>20 = JAVA 20</li>
     * <li>19 = JAVA 19</li>
     * <li>18 = JAVA 18</li>
     * <li>17 = JAVA 17</li>
     * <li>16 = JAVA 16</li>
     * <li>15 = JAVA 15</li>
     * <li>14 = JAVA 14</li>
     * <li>13 = JAVA 13</li>
     * <li>12 = JAVA 12</li>
     * <li>11 = JAVA 11</li>
     * <li>10 = JAVA 10</li>
     * <li>9 = JAVA 9</li>
     * <li>8 = JAVA 8</li>
     * </ul>
     * @param version generate class java version
     */
    public static void setGenerateClassJavaVersion(final int version) {
        javaVersion = version + 44;
    }

    public static final class DynamicClassLoader extends ClassLoader {

        public static final DynamicClassLoader INSTANCE = new DynamicClassLoader();

        public Class<?> defineClass(String name, byte[] byteCodes) {
            return defineClass(name, byteCodes, 0, byteCodes.length);
        }
    }
}
