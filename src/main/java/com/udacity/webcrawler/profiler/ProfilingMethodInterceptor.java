package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.util.Objects;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with the {@link Profiled}
 * annotation. If they are, the method interceptor records how long the method invocation took.
 */
final class ProfilingMethodInterceptor<T> implements InvocationHandler {

    private final Clock clock;
    private final T wrappedObjekt;
    private final ProfilingState state;

    ProfilingMethodInterceptor(Clock clock, T toBeWrappedObjekt, ProfilingState state) {
        this.clock = Objects.requireNonNull(clock);
        this.wrappedObjekt = toBeWrappedObjekt;
        this.state = state;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.isAnnotationPresent(Profiled.class)) {
            long startTime = clock.millis();
            try {
                proxy = method.invoke(wrappedObjekt, args);
            } catch (InvocationTargetException e) {
                throw new InvocationTargetException(e, e.getTargetException().getMessage());
            } catch (IllegalAccessException e) {
                throw new IllegalAccessException(e.getMessage());
            } finally {
                Duration duration = Duration.ofMillis(clock.millis() - startTime);
                state.record(wrappedObjekt.getClass(), method, duration);
            }
        } else {
            try {
                proxy = method.invoke(wrappedObjekt, args);
            } catch (InvocationTargetException e) {
                throw new InvocationTargetException(e, e.getTargetException().getMessage());
            } catch (IllegalAccessException e) {
                throw new IllegalAccessException(e.getMessage());
            }
        }
        return proxy;
    }
}
