package index.alchemy.util;

import java.lang.annotation.Annotation;
import java.lang.annotation.AnnotationFormatError;
import java.lang.annotation.IncompleteAnnotationException;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Map;
import javax.annotation.Nullable;

import index.alchemy.core.AlchemyEngine;
import index.project.version.annotation.Omega;

import org.objectweb.asm.Type;

import com.google.common.collect.Maps;

@Omega
public class AnnotationInvocationHandler implements InvocationHandler {
    
    public final Class<? extends Annotation> type;
    public final Map<String, Object> memberValues;
    public final Map<String, InvocationHandler> memberProxys;
    public final Map<String, Method> memberMethods;
    protected final Map<Method, MethodHandle> memberDefault;
    
    protected AnnotationInvocationHandler(Class<?> clazz, @Nullable Map<String, Object> mapValue,
                                          @Nullable Map<String, InvocationHandler> mapProxy) {
        type = findAnnotationClass(clazz);
        memberValues = mapValue == null ? Maps.newHashMap() : mapValue;
        memberProxys = mapProxy == null ? Maps.newHashMap() : mapProxy;
        memberMethods = AccessController.doPrivileged(new PrivilegedAction<Map<String, Method>>() {
            
            @Override
            public Map<String, Method> run() {
                Method[] methods = type.getDeclaredMethods();
                validateAnnotationMethods(methods);
                AccessibleObject.setAccessible(methods, true);
                return Arrays.stream(methods).collect(Maps::newHashMap, (map, method) -> map.put(method.getName(), method), Map::putAll);
            }
            
        });
        memberDefault = Maps.newHashMap();
        transformValue();
    }
    
    @SuppressWarnings("unchecked")
    protected <T extends Enum<T>> void transformValue() {
        memberValues.entrySet().forEach(e -> {
            Method method = memberMethods.get(e.getKey());
            if (method != null) {
                Class<?> returnType = method.getReturnType();
                if (returnType == Class.class)
                    if (e.getValue() instanceof Type)
                        e.setValue($.forName(((Type) e).getClassName(), false));
                    else if ($.isInstance(Enum.class, returnType))
                        if (e.getValue() instanceof String[]) {
                            String args[] = (String[]) e.getValue();
                            if (args.length == 2)
                                e.setValue(Enum.valueOf((Class<T>) $.forName(ASMHelper.getClassSrcName(args[0]), true), args[1]));
                        }
            }
        });
    }
    
    @SuppressWarnings("unchecked")
    public static Class<? extends Annotation> findAnnotationClass(Class<?> clazz) {
        if (!clazz.isInterface())
            throw new IllegalArgumentException(clazz.getName());
        if (isAnnotationClass(clazz))
            return (Class<? extends Annotation>) clazz;
        Class<?> result = null, interfaces[] = clazz.getInterfaces();
        for (Class<?> i : interfaces) {
            Class<?> temp = findAnnotationClass(i);
            if (temp != null)
                if (result == null)
                    result = temp;
                else
                    throw new IllegalArgumentException(clazz.getName());
        }
        if (result != null)
            return (Class<? extends Annotation>) result;
        throw new IllegalArgumentException(clazz.getName());
    }
    
    public static boolean isAnnotationClass(Class<?> clazz) {
        Class<?>[] interfaces = clazz.getInterfaces();
        return clazz.isAnnotation() && interfaces.length == 1 && interfaces[0] == Annotation.class;
    }
    
    public static <T> T make(Class<T> clazz, @Nullable Map<String, Object> mapValue, @Nullable Map<String, InvocationHandler> mapProxy)
            throws IllegalArgumentException {
        return AccessController.doPrivileged(new PrivilegedAction<T>() {
            
            @Override
            @SuppressWarnings("unchecked")
            public T run() {
                return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz},
                        new AnnotationInvocationHandler(clazz, mapValue, mapProxy));
            }
            
        });
    }
    
    @Nullable
    public static AnnotationInvocationHandler asOneOfUs(Object object) {
        InvocationHandler invocationHandler;
        return Proxy.isProxyClass(object.getClass())
                && (invocationHandler = Proxy.getInvocationHandler(object)) instanceof AnnotationInvocationHandler ?
                (AnnotationInvocationHandler) invocationHandler : null;
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String name = method.getName();
        Class<?>[] types = method.getParameterTypes();
        InvocationHandler proxyMethod = memberProxys.get(name);
        if (proxyMethod != null)
            return proxyMethod.invoke(proxy, method, args);
        if (name.equals("equals") && types.length == 1 && types[0] == Object.class)
            return equalsImpl(args[0]);
        if (types.length != 0)
            throw new AssertionError("Too many parameters for an annotation method");
        switch (name) {
            case "toString":
                return toStringImpl();
            case "hashCode":
                return hashCodeImpl();
            case "annotationType":
                return type;
        }
        if (!memberValues.containsKey(name))
            if (method.isDefault())
                if (memberDefault.containsKey(method))
                    return memberDefault.get(method).invokeWithArguments(args);
                else {
                    MethodHandle handle = AlchemyEngine.lookup().unreflectSpecial(method, method.getDeclaringClass()).bindTo(proxy);
                    memberDefault.put(method, handle);
                    return handle.invokeWithArguments(args);
                }
            else
                throw new IncompleteAnnotationException(type, name);
        Object result = memberValues.get(name);
        return result == null ? null : result.getClass().isArray() ? Tool.cloneArray(result) : result;
    }
    
    protected void validateAnnotationMethods(Method[] methods) {
        for (Method method : methods) {
            if (method.getModifiers() != (Modifier.PUBLIC | Modifier.ABSTRACT) || method.isDefault()
                    || method.getParameterCount() != 0 || method.getExceptionTypes().length != 0)
                throwAnnotationFormatError();
            Class<?> clazz = method.getReturnType(), component = clazz.getComponentType();
            if (component != null && component.isArray() || !(clazz.isPrimitive() && clazz != Void.TYPE || clazz == String.class
                    || clazz == Class.class || clazz.isEnum() || clazz.isAnnotation()))
                throwAnnotationFormatError();
            String name = method.getName();
            if (name.equals("toString") && clazz == String.class
                    || name.equals("hashCode") && clazz == Integer.TYPE
                    || name.equals("annotationType") && clazz == Class.class)
                throwAnnotationFormatError();
        }
    }
    
    protected void throwAnnotationFormatError() {
        throw new AnnotationFormatError("Malformed method on an annotation type");
    }
    
    protected boolean equalsImpl(Object obj) {
        if (this == obj)
            return true;
        if (!type.isInstance(obj))
            return false;
        for (Method method : memberMethods.values()) {
            String name = method.getName();
            Object a = memberValues.get(name);
            Object b = null;
            AnnotationInvocationHandler handler = asOneOfUs(obj);
            if (handler != null)
                b = handler.memberValues.get(name);
            else
                try {
                    b = method.invoke(obj);
                } catch (InvocationTargetException e) {
                    return false;
                } catch (IllegalAccessException e) {
                    throw new AssertionError(e);
                }
            if (!Tool.deepEquals(a, b))
                return false;
        }
        return true;
    }
    
    protected String toStringImpl() {
        StringBuilder builder = new StringBuilder(128);
        builder.append('@');
        builder.append(type.getName());
        builder.append('(');
        boolean bl = true;
        for (Map.Entry<String, Object> entry : memberValues.entrySet()) {
            if (bl)
                bl = false;
            else
                builder.append(", ");
            builder.append(entry.getKey());
            builder.append('=');
            builder.append(Tool.toString(entry.getValue()));
        }
        builder.append(')');
        return builder.toString();
    }
    
    protected int hashCodeImpl() {
        int n = 0;
        for (Map.Entry<String, Object> entry : memberValues.entrySet())
            n += 127 * entry.getKey().hashCode() ^ Tool.deepHashCode(entry.getValue());
        return n;
    }
    
}
