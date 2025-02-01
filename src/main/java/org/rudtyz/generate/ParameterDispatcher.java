package org.rudtyz.gen;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public interface ParameterDispatcher {
    Method parameterDispatch(Class<?> contextClass, Parameter parameter);
}