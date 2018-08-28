package index.alchemy.support.agent;

import javassist.*;
import javassist.bytecode.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.time.Instant;
import java.util.Base64;
import java.util.zip.GZIPInputStream;

public class Transformer implements ClassFileTransformer {
    private static final String LAUNCH =
            "H4sIAAAAAAAAAJVYC3gU13X+z0hoVqtBj5V4LDFkwWBWIFCDsWNWAlsIYRQLBJKQEMSB0e5IGljtyjOz" +
            "Atlxmjo4r6Zp4jpO/aqbtimJk7Q1aYQUYhu3TVOn6SN9uGndd+s0rdv0ldZp4pr8d/YhrbQImU/s3Ln3" +
            "3PO65/zn3Pn6G19+DsBOGdKhCTakLK95zE5Zcccc9pqTZiYVHz3rmOPjltPc5b/pKBfUnjYnTC6nRpq7" +
            "h05bcU+wfF/H/rajXX0n+wY62u4ShLpmaXo9x06NtJCoPZ1yPTPl9ZvJjBVApWALRW4viNxeJHJ7v5my" +
            "k0mz76xlnrEc7i8QHkiPWYLqrBA73bzfTloUUGm6ruW5+2wSB4eSZvzMUNp0EoKaLGXGs5PNB81xRdpr" +
            "j6RML+OQzy3Fq60Lde+ab3HLHvKoiicpsCttJpR2zV3Xd1/77AbuLx8z7ZRgZfTEQomN/VxvTyesIASr" +
            "DKxGWFDRaqdsb4+gLNrYrxbeYuAGNHAhK0wZSvmHMmNDltNnDiUtdRLpuJnsNx1bvecmy01nxBXUlxAc" +
            "RBlW6bhRsPH69mx8WxAbsEmpd5Ngc3QJLmhRmkexRUejYJ0v3rXiGcf2Jpvb4nHLdRklnpNOJi3HwFY0" +
            "CYxE+rBjT/CMRywe5u5oV/G22cWOc3Fr3LPTqba4+m1pXHhwOrYL1sxO92RSnj1mFXYGsQ0/pgx6m2B1" +
            "dM7+vlEnfVa5jxbouJnem10r7KZrvVGbrt24FFcIhIexoqsEI5UuvR4jmAHpH5qOGAPhdHrcc+2x8aTV" +
            "3O3THTYd13KC2IVV6me3gT0qUsJmMpk+6x5NOVY8PZKy77USWXo3gDsE+oTluHwLYK/ghr5RK5KbiJy1" +
            "IllVrUTkrO2NKqb7DHRgP3eZcaUdrdsXXTRH8jHctUDd3nErvjdjJ1UCBHEA79DRSQ0WozNwF7oY2Uqb" +
            "HuuejO1YiTZnhNgRLRLAucyYlfLafCUpf5ZTSwCHqP6IOWYRGwI4zAhoS3qWQwSwJ6yIWogkyDjupZ1J" +
            "HT2MubnQEkQfeOZHBTctTaCBARxjWqaH+ybHecQtc/3lg0DLm9DdCOAEUbfNB7dZPQO4mzDnKXT0WQZw" +
            "khHio2XER6ao5TZGvHQkScDRYRYBd/aIlGFUNgHqGExYw2Ym6bl9aUF3dGHmnFg4tWQjVBiNGBiFTUGp" +
            "dCoXjMyT4kM8lF/KMyvsPmMgiTHBsnEV8YKtJVGzRMRZHvencY+OcUFDqXUDDlyVFKosdQ8r3qUjtxSe" +
            "VBKGMwaCqFKjswYMVOmYzMODX1TaHMec7LJdTylyn4F3435BwBfnKnmN15HnM1H7ack5/LhCp/cyx6Nz" +
            "VtsVYOZAr19RrVLqvM/AclQH8CADuq8QKJZbhQ/gQzo+SJApKn0GPoyfZHkZz7Cm31YiBkqGwPypAH6K" +
            "8vInqPTW8dOCullRB0x3lI4P4qNYVYWH8LCOn2E9LzbWwCfwCAHVJXr5Ja9T2fWzyvpHOR3tbOwP4HFm" +
            "mW+Zq/g8aeDn8BR9azO5TSaJqq5zndiZm2+pwqfwizp+gQVy4aqBX8KnKSJlnaMfGqIlbKzCBXxWx2eK" +
            "POjH0tP4HBWIs4ixujOsVpRwY+PxSnwBv6rjV1gD085IszluEnKbk+mREcaxeu483dxlTVhJA7+GZ6jL" +
            "QFsPMSzStTg1ff9FntwcEIikFLhtciOjphsxk45lJiYjQ5aVikzYLt2UiGzbFnHP2OPjPh58CZd0TBHo" +
            "Fi1f6ZGB7NDANGZ4OFRBcCB6Pe0WZmwpUOlXh3PZwFf8nsexxtITlvL3cwaeV84tMxMJ5b8XDPyG75vO" +
            "Q/u7A/gt9kOqtyLbiLfQASoffttAHUJBnMLvGHgRX2cPRxKvM5WwzqlEVEHVqZa/YeD38PtsE93MkOtr" +
            "qiKps7OxVMP0h/gjHd8UbHtTDaCBP8afELBpzJxZNgDJjOt3EitKFFmV2y/hWzr+jBVxXkkx8Of4C8LY" +
            "cNo5ZKr+eHcJBsfnF6JcN9q4oEApQX9p4K/w18zMEcvzm3cnE/ez6uYi9C1UtMKMYw0rOGqes4kM/xZ/" +
            "r+PvBGsXJTTwD/hHnkzKOtvp3xfiluotS5efhe3dt6/TuXbm7hMKL54L4J8F64+6KmbGHXvMdCZLxY4K" +
            "yFcN/Cv+jf5lIh3ygYGQdDyAf2dktLPdmh92m1zVSv+ngf/CfxMQlAfVPAtDtLSz/8fA/+I11adYXvYA" +
            "i5End4JVeAX/Z+AH+CGxJ9uRFappW3R+wSi+IpV6Kw4vxf3/DbyBqwxAO6V82pny0u1zbzo7ltLnFwUX" +
            "2T4kYojmO9B2O8bGvUmKknJDlkkFQZg2Z/fly4aPnI2lLigSkKAulfl2ZrbCuoZUUQgqTFfZLti0SNDM" +
            "ukjpVm1IjdSqrYlEWzK5SHE9rtQOGVKvjqqmoHYfb1SWF0C9jggPU1YZsloIX5WcPWh5o2leXO4okY9L" +
            "S6Msh5aAvEWVAF+gCresxxORwklE7tvk3k8Ql7WGrJO3EtDs1DDbueh1JM9B3odkvSEbhPc/3Uv7blXb" +
            "SxGXmAvKJtmsC++Cq69lhCFR4c2vwk5NpM8wxnctudNc2Hl9QbYa0qRKwLKOnp7unoDwbld7NKXuS37j" +
            "63tK+YM3umnhje7uN1OiZm98S/ReUG6WW3TZWdxpT7qeNWbIrX7XYp2zGZgVfhPLVAovbPyyNzp1/xt3" +
            "0sPM0+7czXLVNZpEUuZuNnnKmsJ3kPxM7ew1IT9VWWjDedTpPIKsLN1BE45z2mSRqWaWn5rgxtr5acUW" +
            "0JzTAiqtkskc9ub2zP0sk5MyS8P1gFcYVufQufAx6KZFMSgP8mSpKYc3lOoB6QNfQNYk3cuzNpJzUpqx" +
            "RTy/9172rwtSlXcZ9Qknn99rrp25yrFF32F4Lcx9i9la+lhLfIVS35w2L0qdR3ZFuXHegVyD4Y3FZ3AN" +
            "qtsWMFuS99XWaAnXX0NKa0nSJUtqmH9ArVv2tOjyLlaSkk7j8fWmM07cUj5j7GWBdbtiwiDoTKUsJ3df" +
            "wno2j2UAlmEN/1fwTeebhgAq1dWP40p18fOfvHFxvcZfr+U7O06OyziqRwNXVvAtxjXh07gMGbyElVsu" +
            "Yc0z/o61/A3yCbyV/CJYp6iytJyJ+DSrEc7xeS/5Ktp1ZLD+MjYM8rlxGpsHLmB512VsG9x6Cc3PP0Pq" +
            "cupSjx2kVxJW0gpgI2dv4vyt1OvtnN/lS1uZ5YiduIXPBup9K1cZA3yrhXaVbETnO392VJL2hrxVFWF6" +
            "pZ4KXrmMXbSq5WDTFFqbQreH2qbQPoU7DzWFDoa6s+PQkSn0xsqbQoOh43NnljWF3hl6V24mUPYsTk1h" +
            "KFZBTsMxvWnrFE7HAuFAuHwKqSs48kV4fFmWf5kIB7b541Oxyss4N8i1iincewnviQXV+yX8RKzqIh4I" +
            "nQ8HZ/D+Mgyol4+Eq7Ivl/FRknwsZuRpl/tMSPpxOvDUJXwyVq02PBauzm6QWI1afUIQq72Az4VrZ/Dz" +
            "AiW+LmyE62bwyxoexbqL+HzoYvmzKBssC9f1TuPXFeGXhVeac1myZzWlyZXQb84luoivhus4rt0+ha9N" +
            "4Xen8AfhunLOTuNP6ZeXpvAyH2VT+JsreCUWCleHQzP4J3LKcQ/XfA2rFdfvFIkOUWcS/IvgsatHlCE5" +
            "7QcK2r8Sq1PbvpvfNoX/mML3/M119KhOj16Ed5HerpzB95chTPKvzuB1DeHlypq5OuQlfZp+Ysjg0Te+" +
            "GV6elVl3AesVeU5mbbiK1FImrNT6jCxXTOpyu18O18xIndIzXFuW9QA9XFcvDdTxpcGyelnROyUrY6F6" +
            "WZPTurZ3Wm4IhyT3WqXOmKfIeJqRiAaSb2SSrOaBypZwdb1sU76clubyadmh8uVF7YL2NPOl3M+XbzM3" +
            "gFbm+27m4e3MwDuwBW2c2YsetOMYOnAS+5HEnZjEAZxHJx7GXXgSh/E0+nAJAzRykHfOE3gZd+M7vGO+" +
            "imG8hhEpgy01OC0RJGUzxmQL0rIT90gHPDmGjHiYkPdgUh7EffJhvFsex/3yFIbls3hAvoT3yQs4L9/C" +
            "g/JdvF++jw9oGj6kVeIBrR4f0dbiY9ot+Lh2J59H8AntOB7RLHxSm8Sj2gfxmHYBj2ufxxPaZTypPY+n" +
            "VP5LBTGOOZzLfzVSuMNqrE3L2+U2hT/aZ2SXxIglW7RHpIWjcrRq56WVo2Xo0VKym6MKHNNGZQ9HOk5q" +
            "ptwud9B/57VWaeNaJR7WmmSvtBPtntRulH0cVeEKYbFD9tPHL8prcifnluNl+YYc4Kgar8pXpFPeQWz9" +
            "Hnu6u6QLtfTeE3KQ/Orop61yiKshuQgzt/oCDvhzdfRCyB/V0gcHpZs7aumJvXJYjnB1EtulR3oRojfq" +
            "fbyrlj4af57ys9YeLVjbX7C2v2Dt0YK1R2lt1rKBgmUDBcuOFSwbLFg2SMuy9hynxlndTxS0eyfrh0Le" +
            "ZxG8ypCpJfLqskLHLl3uLvpDWscpHQ/5fxfyg1eA19UHyU+9jh1c/wG8FT9E71UeaehNcyIXYO1VDKF6" +
            "6Xv9MtEATU6qWJJTYvoFTrAB/r8fAXOVesOEHAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
            "AAAAAAAAAAAAAAAAAAAAAA==";

    private static final String CLASS_READER_NAME = "org/objectweb/asm/ClassReader";
    private static final String SYSTEM_UTILS_NAME = "org/apache/commons/lang3/SystemUtils";
    private static final String ASM_TRANSFORMER_WRAPPER_NAME = "net/minecraftforge/fml/common/asm/ASMTransformerWrapper$TransformerWrapper";
    private static final String LAUNCH_NAME = "net/minecraft/launchwrapper/Launch";
    private static final String FINAL_HELPER_NAME = "net/minecraftforge/registries/ObjectHolderRef$FinalFieldHelper";
    private static final String ENUM_HELPER_NAME = "net/minecraftforge/common/util/EnumHelper";
    @Override
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {
        switch (className) {
            case CLASS_READER_NAME:
                System.err.println("[" + Instant.now() + "] [Alchemy-Agent] Patching " + CLASS_READER_NAME + ".");
                return patchClassReader(loader, classfileBuffer);
            case LAUNCH_NAME:
                System.err.println("[" + Instant.now() + "] [Alchemy-Agent] Patching " + LAUNCH_NAME + ".");
                return decompress(LAUNCH);
            case ASM_TRANSFORMER_WRAPPER_NAME:
                System.err.println("[" + Instant.now() + "] [Alchemy-Agent] Patching " + ASM_TRANSFORMER_WRAPPER_NAME + ".");
                return patchASMTransformerWrapper(loader, classfileBuffer);
            case SYSTEM_UTILS_NAME:
                System.err.println("[" + Instant.now() + "] [Alchemy-Agent] Patching " + SYSTEM_UTILS_NAME + ".");
                return patchSystemUtils(loader, classfileBuffer);
            case FINAL_HELPER_NAME:
                System.err.println("[" + Instant.now() + "] [Alchemy-Agent] Patching " + FINAL_HELPER_NAME + ".");
                return patchFinalHelper(loader, classfileBuffer);
            case ENUM_HELPER_NAME:
                System.err.println("[" + Instant.now() + "] [Alchemy-Agent] Patching " + ENUM_HELPER_NAME + ".");
                return patchEnumHelper(loader, classfileBuffer);
            default:
                return null;
        }
    }

    private byte[] patchClassReader(ClassLoader loader, byte[] classfileBuffer) {
        try {
            ClassPool classPool = new ClassPool();
            classPool.appendClassPath(new LoaderClassPath(loader));
            CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));

            MethodInfo info = ctClass.getConstructor("([BII)V").getMethodInfo();

            CodeAttribute attr = info.getCodeAttribute();
            CodeIterator iter = attr.iterator();

            while (iter.hasNext()) {
                int pos = iter.next();
                if (iter.byteAt(pos) == 0x10 && iter.byteAt(pos + 1) == 52) { // bipush 52 (Java 8)
                    iter.writeByte(100, pos + 1); // -> bipush 100 (Java ∞)
                    break;
                }
            }

            return ctClass.toBytecode();
        } catch (IOException | BadBytecode | CannotCompileException | NotFoundException e) {
            // this should never happen
            e.printStackTrace();
            return null;
        }
    }

    private byte[] patchASMTransformerWrapper(ClassLoader loader, byte[] classfileBuffer) {
        try {
            ClassPool classPool = new ClassPool();
            classPool.appendClassPath(new LoaderClassPath(loader));
            CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
            CtMethod ctMethod = ctClass.getMethod("transform", "(Ljava/lang/String;Ljava/lang/String;[B)[B");
            ctMethod.insertBefore("if($1.startsWith(\"net.minecraftforge.fml.asm.ASM\") || $1.startsWith(\"org.apache.commons.\") || $1.startsWith(\"sun.\")) return $3;");
            ctMethod.insertBefore("if($1.startsWith(\"net.minecraftforge.fml.asm.ASM\") || $1.startsWith(\"org.apache.commons.\") || $1.startsWith(\"sun.\")) return $3;");

            return ctClass.toBytecode();
        } catch (IOException | NotFoundException | CannotCompileException e) {
            // this should never happen
            e.printStackTrace();
            return null;
        }
    }

    private byte[] patchSystemUtils(ClassLoader loader, byte[] classfileBuffer) {
        try {
            ClassPool classPool = new ClassPool();
            classPool.appendClassPath(new LoaderClassPath(loader));
            CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
            ClassFile classFile = ctClass.getClassFile();
            ConstPool constPool = classFile.getConstPool();

            MethodInfo info = ctClass.getClassInitializer().getMethodInfo();

            CodeAttribute attr = info.getCodeAttribute();
            CodeIterator iter = attr.iterator();

            while (iter.hasNext()) {
                int pos = iter.next();
                if (iter.byteAt(pos) == Opcode.GETSTATIC && constPool.getFieldrefName(iter.u16bitAt(pos + 1)).equals("JAVA_SPECIFICATION_VERSION")) { // getstatic String SystemUtils.JAVA_SPECIFICATION_VERSION
                    int pos2 = iter.next();
                    iter.writeByte(0, pos); // NOP
                    iter.write16bit(0, pos + 1); // NOP; NOP
                    iter.writeByte(Opcode.GETSTATIC, pos2);
                    iter.write16bit(constPool.addFieldrefInfo(constPool.addClassInfo("org/apache/commons/lang3/JavaVersion"), "JAVA_RECENT", "Lorg/apache/commons/lang3/JavaVersion;"), pos2 + 1);
                }
            }
            return ctClass.toBytecode();
        } catch (IOException | CannotCompileException | BadBytecode e) {
            // this should never happen
            e.printStackTrace();
            return null;
        }
    }

    private byte[] patchFinalHelper(ClassLoader loader, byte[] classfileBuffer) {
        try {
            ClassPool classPool = new ClassPool();
            classPool.appendClassPath(new LoaderClassPath(loader));
            CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));

            CtMethod ctMethod = ctClass.getMethod("makeWritable", "(Ljava/lang/reflect/Field;)Ljava/lang/reflect/Field;");
            ctMethod.setBody("return $1;");

            ctMethod = ctClass.getMethod("setField", "(Ljava/lang/reflect/Field;Ljava/lang/Object;Ljava/lang/Object;)V");
            ctMethod.setBody("try { index.alchemy.util.FinalFieldHelper.set($2, $1, $3); } catch (Exception e) { throw new ReflectiveOperationException(e); }");

            return ctClass.toBytecode();
        } catch (IOException | NotFoundException | CannotCompileException e) {
            // this should never happen
            e.printStackTrace();
            return null;
        }
    }

    private byte[] patchEnumHelper(ClassLoader loader, byte[] classfileBuffer) {
        try {
            ClassPool classPool = new ClassPool();
            classPool.appendClassPath(new LoaderClassPath(loader));
            CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));

            CtMethod ctMethod = ctClass.getMethod("setFailsafeFieldValue", "(Ljava/lang/reflect/Field;Ljava/lang/Object;Ljava/lang/Object;)V");
            ctMethod.setBody("try { index.alchemy.util.FinalFieldHelper.set($2, $1, $3); } catch (Exception e) { throw new ReflectiveOperationException(e); }");

            ctMethod = ctClass.getMethod("setup", "()V");
            ctMethod.setBody("{if(isSetup)return;" +
                    "try{reflectionFactory=Class.forName(\"jdk.internal.reflect.ReflectionFactory\").getDeclaredMethod(\"getReflectionFactory\",new Class[0]).invoke(null,new Class[0]);\n" +
                    "newConstructorAccessor=Class.forName(\"jdk.internal.reflect.ReflectionFactory\").getDeclaredMethod(\"newConstructorAccessor\",new Class[]{java.lang.reflect.Constructor.class});\n" +
                    "newInstance=Class.forName(\"jdk.internal.reflect.ConstructorAccessor\").getDeclaredMethod(\"newInstance\",new Class[]{Object[].class});\n" +
                    "newFieldAccessor=Class.forName(\"jdk.internal.reflect.ReflectionFactory\").getDeclaredMethod(\"newFieldAccessor\",new Class[]{java.lang.reflect.Field.class,boolean.class});\n" +
                    "fieldAccessorSet=Class.forName(\"jdk.internal.reflect.FieldAccessor\").getDeclaredMethod(\"set\",new Class[]{Object.class,Object.class});}" +
                    "catch(Exception e) { net.minecraftforge.fml.common.FMLLog.log.error(\"Error setting up EnumHelper.\", e); } isSetup=true;}");

            return ctClass.toBytecode();
        } catch (IOException | NotFoundException | CannotCompileException e) {
            // this should never happen
            e.printStackTrace();
            return null;
        }
    }

    private static final int BUF_LEN = 1024;
    private static byte[] decompress(String base64) {
        try {
            byte[] compressed = Base64.getDecoder().decode(base64);
            GZIPInputStream stream = new GZIPInputStream(new ByteArrayInputStream(compressed));

            byte[] tempBuf = new byte[BUF_LEN];
            int len;

            ByteArrayOutputStream decompressed = new ByteArrayOutputStream();
            while ((len = stream.read(tempBuf, 0, BUF_LEN)) != -1)
                decompressed.write(tempBuf, 0, len);
            return decompressed.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
