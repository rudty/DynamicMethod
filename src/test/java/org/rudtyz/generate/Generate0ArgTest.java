package org.rudtyz.gen;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rudtyz.gen.obj0.SampleI;
import org.rudtyz.gen.obj0.SampleV;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.IntToLongFunction;
import java.util.function.Supplier;

public class Generate0ArgTest {
    public interface GetString {
        String get();
    }

    @BeforeEach
    public void before() {
        DynamicFunctionFactory.setGenerateClassJavaVersion(21);
    }

    @Test
    public void v_jdk8() throws Exception {
        DynamicFunctionFactory.setGenerateClassJavaVersion(8);
        final Runnable o = MethodReflections.generateInstance(
                new SampleV(),
                "my",
                Runnable.class);
    }

    @Test
    public void v() throws Exception {
        final Runnable o = MethodReflections.generateInstance(
                new SampleV(),
                "my",
                Runnable.class);
        o.run();
    }

    @Test
    public void i() throws Exception {
        final IntSupplier o = MethodReflections.generateInstance(
                new SampleV(),
                "myInt",
                IntSupplier.class);
        final int asInt = o.getAsInt();

        Assertions.assertEquals(42, asInt);
    }

    @Test
    public void static_i() throws Exception {
        final IntSupplier o = MethodReflections.generateInstance(
                new SampleV(),
                "staticMyInt",
                IntSupplier.class);
        final int asInt = o.getAsInt();

        Assertions.assertEquals(45, asInt);
    }

    @Test
    public void s() throws Exception {
        final Supplier<String> o = MethodReflections.generateInstance(
                new SampleV(),
                "myString",
                Supplier.class);
        final var str = o.get();

        Assertions.assertEquals("42", str);
    }

    @Test
    public void os() throws Exception {
        final GetString o = MethodReflections.generateInstance(
                new SampleV(),
                "myObjectString",
                GetString.class);
        final var str = o.get();

        Assertions.assertEquals("43", str);
    }

    @Test
    public void vi() throws Exception {
        final IntConsumer o = MethodReflections.generateInstance(
                new SampleI(),
                "my",
                IntConsumer.class);
        o.accept(3);
    }

    @Test
    public void static_vi() throws Exception {
        final IntConsumer o = MethodReflections.generateInstance(
                new SampleI(),
                "staticMy",
                IntConsumer.class);
        o.accept(3);
    }

    @Test
    public void li() throws Exception {
        final IntToLongFunction o = MethodReflections.generateInstance(
                new SampleI(),
                "myIntToLong",
                IntToLongFunction.class);
        final long v = o.applyAsLong(42);
        Assertions.assertEquals(42L, v);
    }

    @Test
    public void static_li() throws Exception {
        final IntToLongFunction o = MethodReflections.generateInstance(
                new SampleI(),
                "staticMyIntToLong",
                IntToLongFunction.class);
        final long v = o.applyAsLong(42);
        Assertions.assertEquals(42L, v);
    }
}
