package org.rudtyz.gen;

import java.lang.reflect.Method;

public interface ContextToCallObject {
    Method getCallObjectMethod(Class<?> contextClass);
}
