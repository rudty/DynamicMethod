package org.rudtyz.generate;

import org.junit.jupiter.api.Test;
import org.rudtyz.generate.obj1.IContext0;
import org.rudtyz.generate.obj1.MyContext;
import org.rudtyz.generate.obj1.SampleWithContext;
import org.rudtyz.generate.obj2.MyContextSample;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.function.Consumer;

public class Generate1ArgTest {
    public interface GetStringInt {
        String get(int a);
    }

    @Test
    public void context()  {
        final Consumer<MyContext> o = MethodReflections.generateInstance(
                new SampleWithContext(),
                "context",
                Consumer.class,
                MyContext.class,
                null);
        o.accept(new MyContext());
    }

    @Test
    public void icontext()  {
        final Consumer<IContext0> o = MethodReflections.generateInstance(
                new SampleWithContext(),
                "icontext",
                Consumer.class,
                IContext0.class,
                null);

        o.accept(new MyContext());
    }

    @Test
    public void icontexti()  {
        final Consumer<IContext0> o = MethodReflections.generateInstance(
                new SampleWithContext(),
                "icontextIntArg",
                Consumer.class,
                IContext0.class,
                new ParameterDispatcher() {
                    @Override
                    public Method parameterDispatch(final Class<?> contextClass, final Method callMethod, final Parameter parameter, final int parameterIndex) {
                        return MethodReflections.find(contextClass, "getIntContext0");
                    }
                });

        o.accept(new MyContext());
    }

    @Test
    public void contexti()  {
        final Consumer<MyContext> o = MethodReflections.generateInstance(
                new SampleWithContext(),
                "contextIntArg",
                Consumer.class,
                MyContext.class,
                new ParameterDispatcher() {
                    @Override
                    public Method parameterDispatch(final Class<?> contextClass, final Method callMethod, final Parameter parameter, final int parameterIndex) {
                        return MethodReflections.find(contextClass, "getIntContext0");
                    }
                });

        o.accept(new MyContext());
    }



    @Test
    public void inner_context() throws Exception{
        final Method callMethod = MethodReflections.find(MyContextSample.InnerSample.class, "context");
        final Class<?> generateClass = DynamicMethodFactory.generateClass(MyContextSample.InnerSample.class,
                callMethod,
                Consumer.class,
                MyContextSample.class,
                new ContextToCallObject() {
                    @Override
                    public Method getCallObjectMethod(final Class<?> contextClass) {
                        return MethodReflections.find(contextClass, "newInnerSample");
                    }
                },
                null);
        final Consumer<MyContextSample> o = MethodReflections.newInstance(generateClass);
        o.accept(new MyContextSample());
    }

    @Test
    public void inner_contexti() throws Exception{
        final Method callMethod = MethodReflections.find(MyContextSample.InnerSample.class, "contexti");
        final Class<?> generateClass = DynamicMethodFactory.generateClass(
                MyContextSample.InnerSample.class,
                callMethod,
                Consumer.class,
                MyContextSample.class,
                new ContextToCallObject() {
                    @Override
                    public Method getCallObjectMethod(final Class<?> contextClass) {
                        return MethodReflections.find(contextClass, "newInnerSample");
                    }
                },
                new ParameterDispatcher() {
                    @Override
                    public Method parameterDispatch(final Class<?> contextClass, final Method callMethod, final Parameter parameter, final int parameterIndex) {
                        return MethodReflections.find(contextClass, "getIntValue");
                    }
                });

        final Consumer<MyContextSample> o = MethodReflections.newInstance(generateClass);
        o.accept(new MyContextSample());
    }
}
