package org.rudtyz.gen;

import java.lang.reflect.Method;

public class MethodReflections {
    public static Method find(Class<?> clazz, String methodName) {
        final Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        throw new IllegalArgumentException("method not found: " + methodName);
    }

    @SuppressWarnings("unchecked")
    public static <T> T newInstance(Class<?> clazz, Object... args) {
        Class<?>[] parameterTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = args[i].getClass();
        }
        try {
            return (T)clazz.getConstructor(parameterTypes).newInstance(args);
        } catch (Exception e) {
            throw new IllegalArgumentException("instance create fail");
        }
    }

    public static <T> T generateInstance(
            Object callInstance,
            String callMethodName,
            Class<?> interfaceClass) {
        return generateInstance(callInstance, callMethodName, interfaceClass, null, null, null);
    }

    public static <T> T generateInstance(
            Object callInstance,
            String callMethodName,
            Class<?> interfaceClass,
            Class<?> contextClass,
            ParameterDispatcher parameterDispatcher) {
        return generateInstance(
                callInstance,
                callMethodName,
                interfaceClass,
                contextClass,
                parameterDispatcher,
                null);
    }

    public static <T> T generateInstance(
            Object callInstance,
            String callMethodName,
            Class<?> interfaceClass,
            Class<?> contextClass,
            ParameterDispatcher parameterDispatcher,
            ContextToCallObject contextToCallObject) {
        final Class<?> callObjectClass = callInstance.getClass();
        final Method callMethod = find(callObjectClass, callMethodName);
        final Class<?> generateClass = DynamicFunctionFactory.generateClass(
                callObjectClass,
                callMethod,
                interfaceClass,
                contextClass,
                contextToCallObject,
                parameterDispatcher);
        return newInstance(generateClass, callInstance);
    }
}
