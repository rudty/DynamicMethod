package org.rudtyz.generate;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generate dynamic class
 *
 * <pre>
 * {@code
 * public final class [GeneratedClass] implements [interfaceClass] {
 *      private final T instance; // callObjectClass callObject
 *      public void <init>(T instance) {
 *          this.instance = instance;
 *      }
 *
 *      public void [interfaceClass.methodName](String s) {
 *          this.instance.invokeMethod(s); // callMethod
 *      }
 * }
 * }
 * </pre>
 */
class DefaultDynamicMethodFactory {
    private static final String FIELD_NAME = "instance";
    private static final AtomicInteger classNameCounter = new AtomicInteger(0);
    private final ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    protected final Type callObjectType;
    protected final Class<?> callObjectClass;
    protected final Method callMethod;

    /**
     * generate class implements this interface
     */
    protected final Class<?> interfaceClass;

    protected final Method interfaceImplementMethod;

    /**
     * org.hello.MyGenClass
     */
    private String generateClassName;

    /**
     * create field or not
     */
    private final boolean generateField;

    protected DefaultDynamicMethodFactory(
            final Class<?> callObjectClass,
            final Method callMethod,
            final Class<?> interfaceClass) {
        this(callObjectClass, callMethod, interfaceClass, true);
    }

    protected DefaultDynamicMethodFactory(
            final Class<?> callObjectClass,
            final Method callMethod,
            final Class<?> interfaceClass,
            final boolean generateField) {
        this.callObjectClass = callObjectClass;
        this.callObjectType = Type.getType(callObjectClass);
        this.callMethod = callMethod;
        this.interfaceClass = interfaceClass;
        this.generateField = generateField && !Modifier.isStatic(callMethod.getModifiers());
        this.interfaceImplementMethod = findImplementMethod(interfaceClass);
    }

    public final byte[] generate() {
        declareClass();
        declareConstructor();
        declareMethod(interfaceImplementMethod);

        return classWriter.toByteArray();
    }

    protected static Method findImplementMethod(Class<?> interfaceClass) {
        final Method[] methodCandidate = interfaceClass.getMethods();
        Method implementMethod = null;
        if (methodCandidate.length == 0) {
            throw new IllegalArgumentException("interface must have one method");
        } else if (methodCandidate.length == 1) {
            implementMethod = methodCandidate[0];
        } else {
            for (final Method m : methodCandidate) {
                if (m.isDefault()) {
                    continue;
                }

                implementMethod = m;
            }

            if (implementMethod == null) {
                throw new IllegalArgumentException("interface must have abstract method");
            }
        }
        return implementMethod;
    }

    /**
     * <pre>
     * {@code
     *  // non static
     *  public void [methodName]() {
     *    this.instance.invokeMethod();
     *  }
     * }
     * {@code
     *  // static
     *  public void [methodName]() {
     *     InstanceClass.invokeMethod();
     *  }
     * }
     * </pre>
     */
    private void declareMethod(final Method implementMethod) {
        final MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC,
                implementMethod.getName(),
                Type.getMethodDescriptor(implementMethod),
                null,
                null);

        final int opCode;
        if (Modifier.isStatic(callMethod.getModifiers())) {
            opCode = Opcodes.INVOKESTATIC;
        } else {
            if (generateField) {
                loadInstance(mv);
            }
            opCode = Opcodes.INVOKEVIRTUAL;
        }

        // load method parameter
        loadAllParameters(mv, callMethod, implementMethod);

        mv.visitMethodInsn(opCode,
                callObjectType.getInternalName(),
                callMethod.getName(),
                Type.getMethodDescriptor(callMethod),
                false);

        tryCast(mv,
                implementMethod.getReturnType(),
                callMethod.getReturnType());

        returnMethod(mv, implementMethod);
    }

    protected void loadAllParameters(MethodVisitor mv, Method callMethod, Method implementMethod) {
        final Parameter[] parameters = callMethod.getParameters();
        for (int i = 0; i < parameters.length; ++i) {
            final Parameter parameter = parameters[i];
            final Class<?> parameterType = parameter.getType();
            if (implementMethod.getParameterCount() == 1 &&
                    parameterType == implementMethod.getParameterTypes()[0]) {
                loadArgument(mv, 1, parameterType);
            } else {
                if (!loadParameter(mv, callMethod, parameter, i, implementMethod)) {
                    throw new IllegalArgumentException("method: " + callMethod.getName() + "parameter not found: " + parameter);
                }
            }
        }
    }

    protected boolean loadParameter(
            MethodVisitor mv,
            Method callMethod,
            Parameter parameter,
            int parameterIndex,
            Method implementMethod) {
        return false;
    }

    /**
     * <pre>
     * {@code
     *    (castClass) stackClass
     * }
     * </pre>
     * @param mv method visitor
     * @param castClass cast class
     * @param stackClass current stack class
     */
    @SuppressWarnings("StatementWithEmptyBody")
    protected final void tryCast(MethodVisitor mv, Class<?> castClass, Class<?> stackClass) {
        if (castClass != stackClass) {
            if (castClass.isPrimitive() || stackClass.isPrimitive()) {
                throw new IllegalArgumentException(
                        "return type not match " + castClass + " " + stackClass + " not support primitive cast");
            }

            if (castClass.isAssignableFrom(stackClass)) {
                // do nothing
            } else if (stackClass.isAssignableFrom(castClass)) {
                mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(castClass));
            } else {
                throw new IllegalArgumentException("return type not match" + castClass + " " + stackClass);
            }
        }
    }

    /**
     * convert org.hello.MyGenClass to org/hello/MyGenClass
     * @return internal class name
     */
    private String getInternalClassName() {
        return getClassName().replace('.', '/');
    }

    /**
     * <pre>
     * {@code
     * public class GeneratedClass extends Object implements interfaceClass {
     *
     * }
     * </pre>
     */
    private void declareClass() {
        classWriter.visit(DynamicMethodFactory.javaVersion,
                Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
                getInternalClassName(),
                null,
                "java/lang/Object",
                new String[] {Type.getInternalName(interfaceClass)});
    }

    /**
     * <pre>
     * {@code
     *  // non static
     *  private final T instance;
     *  public void <init>(T instance) {
     *    super();
     *    this.instance = instance;
     *  }
     * }
     * {@code
     *  // static
     *  public void <init>(T instance) {
     *     super();
     *  }
     *  public void <init>() {
     *       super();
     *  }
     * }
     * </pre>
     */
    private void declareConstructor() {
        final MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC,
                "<init>",
                Type.getMethodDescriptor(Type.VOID_TYPE, callObjectType),
                null,
                null);

        // super();
        loadThis(mv);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
                Type.getInternalName(Object.class),
                "<init>",
                Type.getMethodDescriptor(Type.VOID_TYPE),
                false);

        boolean generateEmptyConstructor = false;
        if (generateField) {
            //this.instance = instance;
            final String callObjectDescriptor = callObjectType.getDescriptor();
            classWriter.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL,
                    FIELD_NAME,
                    callObjectDescriptor,
                    null,
                    null);

            loadThis(mv);
            loadArgument(mv, 1);
            mv.visitFieldInsn(
                    Opcodes.PUTFIELD,
                    getInternalClassName(),
                    FIELD_NAME,
                    callObjectDescriptor);
        } // end !static
        else {
            generateEmptyConstructor = true;
        }

        returnMethod(mv, null);


        if (generateEmptyConstructor) {
            declareEmptyConstructor();
        }
    }
    /**
     * <pre>
     * {@code
     *  public void <init>() {
     *      super();
     *  }
     * }
     * </pre>
     */
    private void declareEmptyConstructor() {
        final MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC,
                "<init>",
                Type.getMethodDescriptor(Type.VOID_TYPE),
                null,
                null);

        // super();
        loadThis(mv);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
                Type.getInternalName(Object.class),
                "<init>",
                Type.getMethodDescriptor(Type.VOID_TYPE),
                false);

        returnMethod(mv, null);
    }

    private String autoClassName() {
        final String className = callObjectType.getClassName();
        final String name = callMethod.getName();
        return className + "_" + name + "_" + classNameCounter.getAndIncrement();
    }

    private void loadInstance(MethodVisitor mv) {
        loadThis(mv);
        final String className = getInternalClassName();
        mv.visitFieldInsn(
                Opcodes.GETFIELD,
                className,
                FIELD_NAME,
                callObjectType.getDescriptor());
    }

    protected void loadThis(MethodVisitor mv) {
        loadArgument(mv, 0);
    }

    protected void loadArgument(MethodVisitor mv, final int index) {
        loadArgument(mv, index, null);
    }

    protected void loadArgument(
            final MethodVisitor mv,
            final int index,
            final Class<?> parameterClass) {

        final int opCode;
        if (parameterClass == int.class ||
                parameterClass == byte.class ||
                parameterClass == char.class ||
                parameterClass == short.class ||
                parameterClass == boolean.class) {
            opCode = Opcodes.ILOAD;
        } else if (parameterClass == long.class) {
            opCode = Opcodes.LLOAD;
        } else if (parameterClass == float.class) {
            opCode = Opcodes.FLOAD;
        } else if (parameterClass == double.class) {
            opCode = Opcodes.DLOAD;
        } else {
            opCode = Opcodes.ALOAD;
        }

        mv.visitVarInsn(opCode, index);
    }

    private void returnMethod(MethodVisitor mv, Method m) {
        if (m == null) {
            mv.visitInsn(Opcodes.RETURN);
        } else {
            final Class<?> returnType = m.getReturnType();
            if (returnType == int.class ||
                    returnType == byte.class ||
                    returnType == char.class ||
                    returnType == short.class ||
                    returnType == boolean.class) {
                mv.visitInsn(Opcodes.IRETURN);
            } else if (returnType == long.class) {
                mv.visitInsn(Opcodes.LRETURN);
            } else if (returnType == float.class) {
                mv.visitInsn(Opcodes.FRETURN);
            } else if (returnType == double.class) {
                mv.visitInsn(Opcodes.DRETURN);
            } else if (returnType == void.class) {
                mv.visitInsn(Opcodes.RETURN);
            } else {
                // object return
                mv.visitInsn(Opcodes.ARETURN);
            }
        }

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    public String getClassName() {
        if (generateClassName == null) {
            generateClassName = autoClassName();
        }

        return generateClassName;
    }

    public void setGenerateClassName(final String generateClassName) {
        this.generateClassName = generateClassName;
    }
}
