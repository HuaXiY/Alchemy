package index.alchemy.core;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

import index.alchemy.api.IDLCInfo;
import index.alchemy.api.annotation.*;
import index.alchemy.core.asm.transformer.AlchemyTransformerManager;
import index.alchemy.core.debug.AlchemyDebug;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.util.Tool;
import index.alchemy.util.cache.ICache;
import index.alchemy.util.cache.StdCache;
import index.project.version.annotation.Omega;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;
import net.minecraftforge.fml.common.event.FMLEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static index.alchemy.core.AlchemyConstants.MOD_NAME;

/*
 * -----------------------------------------------
 *    __    __    ___  _   _  ____  __  __  _  _
 *   /__\  (  )  / __)( )_( )( ___)(  \/  )( \/ )
 *  /(__)\  )(__( (__  ) _ (  )__)  )    (  \  /
 * (__)(__)(____)\___)(_) (_)(____)(_/\/\_) (__)
 *
 * -----------------------------------------------
 */

@Omega
@Loading
@Alchemy
public enum AlchemyModLoader {
    
    INSTANCE;
    
    public static AlchemyModLoader instance() { return INSTANCE; }
    
    public static final String REQUIRED_BEFORE = "required-before:", REQUIRED_AFTER = "required-after:";
    
    public static final Random random = new Random();
    
    public static final Logger logger = LogManager.getLogger(MOD_NAME);
    
    public static final Stack<String> log_stack = new Stack<String>();
    
    public static void updateStack(String prefix) {
        int index = log_stack.indexOf(prefix);
        if (log_stack.size() == 0 || index == -1)
            log_stack.push(prefix);
        else if (index != log_stack.size() - 1)
            for (int i = 0, len = log_stack.size() - 1 - index; i < len; i++)
                log_stack.pop();
    }
    
    public static void info(String prefix, String info) {
        updateStack(prefix);
        logger.info(Tool.makeString(' ', log_stack.size() * 4) + prefix + ": " + info);
    }
    
    public static void info(Class<?> clazz, Object obj) {
        info("Init", "<" + clazz.getName() + "> " + obj);
    }
    
    public static final String mc_dir, config_dir;
    public static final boolean is_modding, enable_test, enable_dmain;
    public static final File mod_path;
    private static final ICache<ModState, LinkedList<Class<?>>> init_map = new StdCache<ModState, LinkedList<Class<?>>>().setOnMissGet(Lists::newLinkedList);
    private static final ICache<String, LinkedList<Class<?>>> instance_map = new StdCache<String, LinkedList<Class<?>>>().setOnMissGet(Lists::newLinkedList);
    private static final List<MethodHandle> loading_list = Lists.newLinkedList();
    private static final List<String> class_list = Lists.newLinkedList();
    
    private static final ICache<Class<? extends FMLEvent>, List<Consumer<FMLEvent>>>
            fml_event_callback_mapping = new StdCache<Class<? extends FMLEvent>, List<Consumer<FMLEvent>>>().setOnMissGet(Lists::newLinkedList);
    
    @SuppressWarnings("unchecked")
    public static <T extends FMLEvent> void addFMLEventCallback(Class<T> clazz, Consumer<T> consumer) {
        fml_event_callback_mapping.get(clazz).add((Consumer<FMLEvent>) consumer);
    }
    
    public static <T extends FMLEvent> void addFMLEventCallback(Class<T> clazz, Runnable runnable) {
        fml_event_callback_mapping.get(clazz).add(e -> runnable.run());
    }
    
    public static <T extends FMLEvent> void onFMLEvent(T event) {
        fml_event_callback_mapping.get(event.getClass()).forEach(c -> c.accept(event));
    }
    
    public static List<Class<?>> getInstance(String key) {
        return instance_map.get(key);
    }
    
    public static List<String> getClassList() { return ImmutableList.copyOf(class_list); }
    
    public static void addClass(Collection<String> classes) {
        checkInvokePermissions();
        checkState();
        for (String clazz : classes)
            if (class_list.contains(clazz))
                AlchemyRuntimeException.onException(new RuntimeException(clazz));
            else
                class_list.add(clazz);
    }
    
    public static ModContainer getModContainer() {
        return AlchemyModContainer.instance;
    }
    
    private static ModState state = ModState.UNLOADED;
    
    public static ModState getState() {
        return state;
    }
    
    public static boolean isAvailable() {
        return getState().ordinal() >= ModState.AVAILABLE.ordinal();
    }
    
    public static void checkState() {
        if (isAvailable())
            AlchemyRuntimeException.onException(new RuntimeException("Abnormal state: " + getState().name()));
    }
    
    public static void checkState(ModState state) {
        if (getState() != state)
            AlchemyRuntimeException.onException(new RuntimeException("Abnormal state: " + getState().name()));
    }
    
    public static void checkInvokePermissions() {
        Tool.checkInvokePermissions(3, AlchemyModLoader.class);
    }
    
    public static boolean isModLoaded(String modid) {
        for (ModContainer modContainer : Loader.instance().getModList())
            if (modContainer.getModId().equals(modid))
                return true;
        return false;
    }
    
    public static void init(Class<?> clazz) {
        DLC dlc = clazz.getAnnotation(DLC.class);
        if (dlc != null) {
            IDLCInfo info = AlchemyDLCLoader.findDLC(dlc.id());
            if (info != null)
                FMLCommonHandler.instance().addModToResourcePack(info.getDLCContainer());
            else
                throw new RuntimeException("DLC(" + clazz.getSimpleName() + ") no have IDLCInfo");
        }
    }
    
    public static void restart() {
        checkInvokePermissions();
        RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
        String cp = bean.getClassPath();
        List<String> args = bean.getInputArguments();
        String main = System.getProperty("sun.java.command");
        try {
            Process process = Runtime.getRuntime().exec("java " + Joiner.on(' ').join(args) + " -cp " + cp + " " + main);
            process.getInputStream().close();
            process.getOutputStream().close();
            process.getErrorStream().close();
            FMLCommonHandler.instance().exitJava(0x0, false);
        } catch (IOException e) { AlchemyRuntimeException.onException(e); }
    }
    
    static {
        is_modding = !AlchemyEngine.isRuntimeDeobfuscationEnabled();
        mc_dir = AlchemyEngine.getMinecraftDir().getPath();
        config_dir = mc_dir + "/config";
        System.out.println(AlchemyEngine.getAlchemyCoreLocation());
        if (AlchemyEngine.getAlchemyCoreLocation() != null)
            mod_path = AlchemyEngine.getAlchemyCoreLocation();
        else try {
            String offset = AlchemyModLoader.class.getName().replace('.', '/') + ".class";
            URL src = AlchemyModLoader.class.getProtectionDomain().getCodeSource().getLocation();
            switch (src.getProtocol()) {
                case "jar":
                    mod_path = new File(((JarURLConnection) src.openConnection()).getJarFileURL().getFile());
                    break;
                case "file":
                    mod_path = new File(src.getFile().replace(offset, ""));
                    break;
                default:
                    mod_path = null;
                    throw new NullPointerException("mod_path");
            }
        } catch (Exception e) {
            AlchemyRuntimeException.onException(e);
            throw new RuntimeException(e);
        }
        logger.info("Mod path: " + mod_path);
        
        enable_test = Boolean.getBoolean("index.alchemy.enable_test");
        logger.info("Test mode state: " + enable_test);
        
        enable_dmain = is_modding && Boolean.getBoolean("index.alchemy.enable_dmain");
        logger.info("Development mode state: " + enable_dmain);
    }
    
    private static final String BOOTSTRAP = "bootstrap";
    
    private static void bootstrap() throws Throwable {
        checkInvokePermissions();
        AlchemyTransformerManager.loadAllTransformClass();
        
        try {
            for (String line : Tool.read(AlchemyModLoader.class.getResourceAsStream("/ascii_art.txt")).split("\n"))
                logger.info(line);
        } catch (Exception e) { }
        
        AlchemyDebug.start(BOOTSTRAP);
        class_list.addAll(0, AlchemyEngine.findClassFromURL(mod_path.toURI().toURL()));
        
        AlchemyDLCLoader.stream().filter(IDLCInfo::shouldLoad).map(IDLCInfo::getDLCAllClass).forEach(AlchemyModLoader::addClass);
        
        Side side = AlchemyEngine.runtimeSide();
        ClassLoader loader = AlchemyEngine.getLaunchClassLoader();
        
        for (String name : class_list)
            try {
                Class<?> clazz = Class.forName(name, false, loader);
                SideOnly only = clazz.getAnnotation(SideOnly.class);
                if (only != null && only.value() != side)
                    continue;
                Loading loading = clazz.getAnnotation(Loading.class);
                if (loading != null) {
                    logger.info(AlchemyModLoader.class.getName() + " Add -> " + clazz);
                    loading_list.add(AlchemyEngine.lookup().findStatic(clazz, "init", MethodType.methodType(void.class, Class.class)));
                }
            } catch (ClassNotFoundException | NoClassDefFoundError e) { }
        
        for (String name : class_list)
            try {
                Class<?> clazz = Class.forName(name, false, loader);
                logger.info(AlchemyModLoader.class.getName() + " Loading -> " + clazz);
                for (MethodHandle handle : loading_list)
                    handle.invoke(clazz);
                Init init = clazz.getAnnotation(Init.class);
                if (init != null && init.enable())
                    if (init.index() < 0)
                        init_map.get(init.state()).addFirst(clazz);
                    else
                        init_map.get(init.state()).add(clazz);
                InitInstance instance = clazz.getAnnotation(InitInstance.class);
                if (instance != null)
                    if (instance.value() != null)
                        instance_map.get(instance.value()).add(clazz);
                    else
                        AlchemyRuntimeException.onException(new NullPointerException(clazz + " -> @InitInstance.value()"));
            } catch (ClassNotFoundException | NoClassDefFoundError e) { continue; }
        
        AlchemyDebug.end(BOOTSTRAP);
        log_stack.clear();
        
        init(ModState.LOADED);
    }
    
    public static void tryBootstrap() throws IOException {
        try {
            bootstrap();
        } catch (Throwable e) {
            AlchemyRuntimeException.onException(new RuntimeException("Can't bootstrap !!!", e));
        }
    }
    
    public static String format(String src, String max) {
        double fix = (max.length() - src.length()) / 2D;
        return Tool.getString(' ', (int) Math.floor(fix)) + src + Tool.getString(' ', (int) Math.ceil(fix));
    }
    
    public static void init(ModState state) {
        checkInvokePermissions();
        if (AlchemyModLoader.state.ordinal() >= state.ordinal())
            return;
        log_stack.clear();
        AlchemyModLoader.state = state;
        String state_str = format(state.toString(), ModState.POSTINITIALIZED.toString());
        logger.info("************************************   " + state_str + " START   ************************************");
        ProgressBar bar = ProgressManager.push("AlchemyModLoader", init_map.get(state).size());
        for (Class<?> clazz : init_map.get(state)) {
            bar.step(clazz.getSimpleName());
            if (clazz.getAnnotation(Test.class) == null || enable_test)
                init0(clazz);
        }
        ProgressManager.pop(bar);
        logger.info("************************************   " + state_str + "  END    ************************************");
    }
    
    public static void init0(Class<?> clazz) {
        try {
            logger.info("Starting init class: " + clazz.getName());
            try {
                AlchemyEngine.lookup().findStatic(clazz, "init", MethodType.methodType(void.class)).invoke();
            } catch (NoSuchMethodException e) {
                Tool.init(clazz);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
            logger.info("Successful !");
        } catch (Exception e) {
            logger.error("Failed !");
            AlchemyRuntimeException.onException(e);
        }
    }
    
}
