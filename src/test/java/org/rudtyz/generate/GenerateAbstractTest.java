package org.rudtyz.gen;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rudtyz.gen.abstractClass.ImplSample;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class GenerateAbstractTest {
    @Test
    public void i() throws Exception {
        final IntSupplier o = MethodReflections.generateInstance(
                new ImplSample(),
                "myInt",
                IntSupplier.class);
        final int asInt = o.getAsInt();

        Assertions.assertEquals(42, asInt);
    }

    @Test
    public void s() throws Exception {
        final Supplier<String> o = MethodReflections.generateInstance(
                new ImplSample(),
                "myString",
                Supplier.class);
        final var str = o.get();

        Assertions.assertEquals("42", str);
    }
}
