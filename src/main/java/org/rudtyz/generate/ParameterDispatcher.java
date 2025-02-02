package org.rudtyz.generate;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public interface ParameterDispatcher {
    Method parameterDispatch(Class<?> contextClass, Method callMethod, Parameter parameter, int parameterIndex);
}