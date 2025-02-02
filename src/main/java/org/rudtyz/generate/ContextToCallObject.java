package org.rudtyz.generate;

import java.lang.reflect.Method;

public interface ContextToCallObject {
    Method getCallObjectMethod(Class<?> contextClass);
}
