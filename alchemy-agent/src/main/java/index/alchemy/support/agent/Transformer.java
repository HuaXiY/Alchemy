package index.alchemy.support.agent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.time.Instant;
import java.util.Base64;
import java.util.zip.GZIPInputStream;

import javassist.*;
import javassist.bytecode.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class Transformer implements ClassFileTransformer {
    private static final String CLASS_READER_NAME = "org/objectweb/asm/ClassReader";
    private static final String SYSTEM_UTILS_NAME = "org/apache/commons/lang3/SystemUtils";
    private static final String ASM_TRANSFORMER_WRAPPER_NAME = "net/minecraftforge/fml/common/asm/ASMTransformerWrapper$TransformerWrapper";
    private static final String LAUNCH_NAME = "net/minecraft/launchwrapper/Launch";
    private static final String FINAL_HELPER_NAME = "net/minecraftforge/registries/ObjectHolderRef$FinalFieldHelper";
    private static final String ENUM_HELPER_NAME = "net/minecraftforge/common/util/EnumHelper";
    private static final String ITEM_STACK_HOLDER_REF_NAME = "net/minecraftforge/fml/common/registry/ItemStackHolderRef";
    private static final String ASM_EVENT_HANDLER_NAME = "net/minecraftforge/fml/common/eventhandler/ASMEventHandler";
    
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
                return patchLaunch(loader, classfileBuffer);
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
            case ITEM_STACK_HOLDER_REF_NAME:
                System.err.println("[" + Instant.now() + "] [Alchemy-Agent] Patching " + ITEM_STACK_HOLDER_REF_NAME + ".");
                return patchItemStackHolderRef(loader, classfileBuffer);
            case ASM_EVENT_HANDLER_NAME:
                System.err.println("[" + Instant.now() + "] [Alchemy-Agent] Patching " + ASM_EVENT_HANDLER_NAME + ".");
                return patchASMEventHandler(loader, classfileBuffer);
            default:
                return null;
        }
    }

    private byte[] patchLaunch(ClassLoader loader, byte[] classfileBuffer) {
        return patchUsingCtClass(loader, classfileBuffer, ctClass ->
                ctClass.getConstructor("()V").setBody(
                    "{this.classLoader = new net.minecraft.launchwrapper.LaunchClassLoader(((jdk.internal.loader.URLClassPath)(index.alchemy.util.$.$(new Object[]{this.getClass().getClassLoader(), \"ucp\"}))).getURLs());\n" +
                    "this.blackboard = new java.util.HashMap();\n" +
                    "Thread.currentThread().setContextClassLoader(this.classLoader);}"));
    }
    
    private byte[] patchClassReader(ClassLoader loader, byte[] classfileBuffer) {
        return patchUsingCtClass(loader, classfileBuffer, ctClass -> {
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
        });
    }
    
    private byte[] patchASMTransformerWrapper(ClassLoader loader, byte[] classfileBuffer) {
        return patchUsingCtClass(loader, classfileBuffer, ctClass -> {
            CtMethod ctMethod = ctClass.getMethod("transform", "(Ljava/lang/String;Ljava/lang/String;[B)[B");
            ctMethod.insertBefore("if($1.startsWith(\"net.minecraftforge.fml.asm.ASM\") || $1.startsWith(\"org.apache.commons.\") || $1.startsWith(\"sun.\")) return $3;");
        });
    }
    
    private byte[] patchSystemUtils(ClassLoader loader, byte[] classfileBuffer) {
        return patchUsingCtClass(loader, classfileBuffer, ctClass -> {
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
        });
    }
    
    private byte[] patchFinalHelper(ClassLoader loader, byte[] classfileBuffer) {
        return patchUsingCtClass(loader, classfileBuffer, ctClass -> {
            CtMethod ctMethod = ctClass.getMethod("makeWritable", "(Ljava/lang/reflect/Field;)Ljava/lang/reflect/Field;");
            ctMethod.setBody("return $1;");
            
            ctMethod = ctClass.getMethod("setField", "(Ljava/lang/reflect/Field;Ljava/lang/Object;Ljava/lang/Object;)V");
            ctMethod.setBody("try { index.alchemy.util.FinalFieldHelper.set($2, $1, $3); } catch (Exception e) { throw new ReflectiveOperationException(e); }");
        });
    }
    
    private byte[] patchEnumHelper(ClassLoader loader, byte[] classfileBuffer) {
        return patchUsingCtClass(loader, classfileBuffer, ctClass -> {
            CtMethod ctMethod = ctClass.getMethod("setFailsafeFieldValue", "(Ljava/lang/reflect/Field;Ljava/lang/Object;Ljava/lang/Object;)V");
            ctMethod.setBody("try { index.alchemy.util.FinalFieldHelper.set($2, $1, $3); } catch (Exception e) { throw new ReflectiveOperationException(e); }");
            
            ctMethod = ctClass.getMethod("setup", "()V");
            ctMethod.setBody("{if(isSetup)return;" +
                    "try{reflectionFactory=Class.forName(\"jdk.internal.reflect.ReflectionFactory\").getDeclaredMethod(\"getReflectionFactory\",new Class[0]).invoke(null,new Class[0]);\n" +
                    "newConstructorAccessor=Class.forName(\"jdk.internal.reflect.ReflectionFactory\").getDeclaredMethod(\"newConstructorAccessor\",new Class[]{java.lang.reflect.Constructor.class});\n" +
                    "newInstance=Class.forName(\"jdk.internal.reflect.ConstructorAccessor\").getDeclaredMethod(\"newInstance\",new Class[]{Object[].class});\n" +
                    "newFieldAccessor=Class.forName(\"jdk.internal.reflect.ReflectionFactory\").getDeclaredMethod(\"newFieldAccessor\",new Class[]{java.lang.reflect.Field.class,boolean.class});\n" +
                    "fieldAccessorSet=Class.forName(\"jdk.internal.reflect.FieldAccessor\").getDeclaredMethod(\"set\",new Class[]{Object.class,Object.class});}" +
                    "catch(Exception e) { org.apache.logging.log4j.Logger logger = net.minecraftforge.fml.common.FMLLog.log;" +
                    "logger.error(\"Error setting up EnumHelper.\", e); } isSetup=true;}");
        });
    }
    
    private byte[] patchItemStackHolderRef(ClassLoader loader, byte[] classfileBuffer) {
        return patchUsingCtClass(loader, classfileBuffer, ctClass -> {
            CtMethod ctMethod = ctClass.getMethod("makeWritable", "(Ljava/lang/reflect/Field;)V");
            ctMethod.setBody("{}");
            
            ctMethod = ctClass.getMethod("apply", "()V");
            ctMethod.setBody("{Object is;\n" +
                    "try\n" +
                    "{\n" +
                    "    is = index.alchemy.util.$.$(new Object[]{\"Lnet.minecraftforge.fml.common.registry.GameRegistry\", \"makeItemStack\", itemName, new Integer(meta), new Integer(1), serializednbt});\n" +
                    "} catch (RuntimeException e)\n" +
                    "{\n" +
                    "    org.apache.logging.log4j.Logger logger = net.minecraftforge.fml.common.FMLLog.log;" +
                    "    logger.error(\"Caught exception processing itemstack {},{},{} in annotation at {}.{}\", itemName, Integer.toString(meta), serializednbt,field.getClass().getName(),field.getName());\n" +
                    "    throw e;\n" +
                    "}\n" +
                    "try\n" +
                    "{\n" +
                    "\tindex.alchemy.util.FinalFieldHelper.setStatic(field, is);\n" +
                    "}\n" +
                    "catch (Exception e)\n" +
                    "{\n" +
                    "    org.apache.logging.log4j.Logger logger = net.minecraftforge.fml.common.FMLLog.log;" +
                    "    logger.warn(\"Unable to set {} with value {},{},{}\", this.field, this.itemName, Integer.toString(this.meta), this.serializednbt);\n" +
                    "}}");
        });
    }
    
    private byte[] patchASMEventHandler(ClassLoader loader, byte[] classfileBuffer) {
        return patchUsingCtClass(loader, classfileBuffer, ctClass -> {
            ExprEditor editor = new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    super.edit(m);
                    if (m.getMethodName().equals("visitMethodInsn") && m.getClassName().equals("org.objectweb.asm.MethodVisitor")) {
                        m.replace("{if($1 == 184)" +
                                "{$5 = Class.forName(instType.replace('/','.')).isInterface();}" +
                                "$_ = $proceed($$);}");
                    }
                    else if (m.getMethodName().equals("visit") && m.getClassName().equals("org.objectweb.asm.ClassWriter")) {
                        m.replace("{$1=52;$_ = $proceed($$);}");
                    }
                }
            };
            ctClass.getMethod("createWrapper", "(Ljava/lang/reflect/Method;)Ljava/lang/Class;").instrument(editor);
        });
    }
    
    private byte[] patchUsingCtClass(ClassLoader loader, byte[] classfileBuffer, ExceptionConsumer action) {
        try {
            ClassPool classPool = new ClassPool();
            classPool.appendClassPath(new LoaderClassPath(loader));
            CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
            
            action.apply(ctClass);
            
            return ctClass.toBytecode();
        } catch (Exception e) {
            // this should never happen
            e.printStackTrace();
            return null;
        }
    }
    
    private interface ExceptionConsumer {
        
        void apply(CtClass ctClass) throws Exception;
        
    }
    
}
