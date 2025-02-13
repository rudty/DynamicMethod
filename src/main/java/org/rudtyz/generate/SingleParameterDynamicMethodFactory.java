package org.rudtyz.generate;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

class SingleParameterDynamicMethodFactory extends DefaultDynamicMethodFactory {

    private final Class<?> contextClass;
    private final Class<?> implementMethodParameterClass;

    /**
     * context -> parameter
     * <pre>
     * {@code
     *   context.getParameter();
     * }
     * </pre>
     */
    private final ParameterDispatcher parameterDispatcher;
    private final ContextToCallObject contextToCallObject;
    public SingleParameterDynamicMethodFactory(
            final Class<?> callObjectClass,
            final Method callMethod,
            final Class<?> interfaceClass,
            final Class<?> contextClass,
            final ContextToCallObject contextToCallObject,
            final ParameterDispatcher parameterDispatcher) {
        super(callObjectClass, callMethod, interfaceClass, contextToCallObject == null);
        this.contextToCallObject = contextToCallObject;
        this.parameterDispatcher = parameterDispatcher;

        if (interfaceImplementMethod.getParameterCount() > 1) {
            throw new IllegalArgumentException(interfaceImplementMethod + " method must have one parameter");
        }

        final Class<?>[] parameterTypes = interfaceImplementMethod.getParameterTypes();
        if (parameterTypes.length == 1) {
            this.implementMethodParameterClass = parameterTypes[0];
        } else {
            this.implementMethodParameterClass = null;
        }

        if (contextClass == null) {
            this.contextClass = implementMethodParameterClass;
        } else {
            this.contextClass = contextClass;
        }
    }

    @Override
    protected void loadAllParameters(final MethodVisitor mv, final Method callMethod, final Method implementMethod) {
        if (contextToCallObject != null) {
            final Method method;
            try {
                method = contextToCallObject.getCallObjectMethod(contextClass);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("getCallObjectMethod", e);
            }

            if (method == null) {
                throw new IllegalArgumentException(contextClass + "get context method is null");
            }

            if (method.getReturnType() == void.class) {
                throw new IllegalArgumentException(contextClass + "get context method return type is void");
            }

            invokeV(mv, method);
            tryCast(mv,
                    callObjectClass,
                    method.getReturnType());
        }

        super.loadAllParameters(mv, callMethod, implementMethod);
    }

    @Override
    protected boolean loadParameter(
            MethodVisitor mv,
            Method callMethod,
            Parameter parameter,
            int parameterIndex,
            Method implementMethod) {

        if (contextClass == null) {
            return false;
        }

        final Class<?> parameterType = parameter.getType();
        if (parameterType == contextClass) {
            loadArgument(mv, 1);
            tryCast(mv, contextClass, implementMethodParameterClass);
            return true;
        }

        final Method parameterDispatchMethod = getParameterDispatchMethod(callMethod, parameter, parameterIndex);
        invokeV(mv, parameterDispatchMethod);

        tryCast(mv, parameterType, parameterDispatchMethod.getReturnType());
        return true;
    }

    /**
     * invoke ? method(void)
     * <pre>
     * {@code
     *   // non static
     *   void [FunctionName]([InternalClassName] internalClassName) {
     *       internalClassName.invokeMethod();
     * }
     * }
     * {@code
     *   // static
     *   void [FunctionName]() {
     *       [InternalClassName].invokeMethod();
     * }
     * }
     * </pre>
     * @param mv method visitor
     * @param invokeMethod method to invoke
     */
    private void invokeV(MethodVisitor mv, Method invokeMethod) {
        final Class<?> invokeMethodDeclaringClass = invokeMethod.getDeclaringClass();
        final String internalClassName = Type.getInternalName(invokeMethodDeclaringClass);
        final int callOpCode;
        if (Modifier.isStatic(invokeMethod.getModifiers())) {
            callOpCode = Opcodes.INVOKESTATIC;
        } else {
            if (invokeMethodDeclaringClass.isInterface()) {
                callOpCode = Opcodes.INVOKEINTERFACE;
            } else {
                callOpCode = Opcodes.INVOKEVIRTUAL;
            }

            loadArgument(mv, 1);
            mv.visitTypeInsn(Opcodes.CHECKCAST, internalClassName);
        }

        mv.visitMethodInsn(callOpCode,
                internalClassName,
                invokeMethod.getName(),
                Type.getMethodDescriptor(invokeMethod),
                callOpCode == Opcodes.INVOKEINTERFACE);
    }

    private Method getParameterDispatchMethod(Method callMethod, Parameter parameter, int parameterIndex) {
        if (parameterDispatcher == null) {
            throw new IllegalArgumentException("ParameterDispatcher is not set");
        }
        final Method method;
        try {
            method = parameterDispatcher.parameterDispatch(contextClass, callMethod, parameter, parameterIndex);
        } catch (Exception e) {
            throw new IllegalArgumentException("parameterDispatch", e);
        }

        if (method.getParameterCount() != 0) {
            throw new IllegalArgumentException("ParameterDispatcher method support only no parameter method");
        }

        return method;
    }
}
