package index.alchemy.dlcs.non_update.core;

import java.io.File;
import java.io.IOException;
import java.security.Permission;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import index.alchemy.api.annotation.DLC;
import index.alchemy.util.ReflectionHelper;
import index.alchemy.util.Tool;
import index.project.version.annotation.Omega;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.relauncher.FMLSecurityManager.ExitTrappedException;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static index.alchemy.dlcs.non_update.core.NonUpdate.*;

@Omega
@DLC(id = DLC_ID, name = DLC_NAME, version = DLC_VERSION, mcVersion = "*")
public class NonUpdate {
    
    public static final String
            DLC_ID = "non_update",
            DLC_NAME = "NonUpdate",
            DLC_VERSION = "0.0.1-dev";
    
    public static final Logger logger = LogManager.getLogger(NonUpdate.class.getSimpleName());
    
    private static final ImmutableList<String> DEFAULT_WHITE_LIST = ImmutableList.of(
            "minecraft.net",
            "mojang.com",
            "skin.prinzeugen.net",
            "fleey.org",
            "www.skinme.cc"
    );
    
    public static String getFMLPackageName() { return "net.minecraftforge.fml."; }
    
    private static Configuration configuration = new Configuration(new File("config/" + NonUpdate.DLC_NAME + ".cfg"));
    
    static boolean onlyPreventMainThread = false;
    static String redirectAddress = "0.0.0.0";
    
    private static void loadConfiguration() {
        onlyPreventMainThread = configuration.getBoolean("onlyPreventMainThread", "Thread", onlyPreventMainThread, "Only prevent main thread url access.");
        redirectAddress = configuration.getString("redirectAddress", "Address", redirectAddress, "Redirect address.");
    }
    
    static { init(); }
    
    public static void init() {
        configuration.load();
        loadConfiguration();
        configuration.save();
        ReflectionHelper.setSecurityManager(new SecurityManager() {
            
            List<String> whitelist = getWhiteList();
            
            private boolean isIP(String host) {
                return host.matches("[0-9.]*") || host.contains(":");
            }
            
            @Override
            public void checkConnect(String host, int port) {
                checkConnect(host, port, null);
            }
            
            @Override
            public void checkConnect(String host, int port, @Nullable Object context) {
                if (isIP(host) || port != 80 && port != 443)
                    return;
                logger.info("Check: " + host + ":" + port);
                if (onlyPreventMainThread) {
                    String name = Thread.currentThread().getName();
                    if (!(name.equals("Client thread") || name.equals("Server thread"))) {
                        logger.info("Release: " + host + ":" + port);
                        return;
                    }
                }
                for (String exp : whitelist)
                    if (host.endsWith(exp)) {
                        logger.info("Release: " + host + ":" + port);
                        return;
                    }
                logger.info("Redirect: " + host + " -> " + redirectAddress);
                Tool.coverString(host, redirectAddress);
            }
            
            @Override
            public void checkPermission(Permission perm) {
                String permName = perm.getName() != null ? perm.getName() : "missing";
                if (permName.startsWith("exitVM")) {
                    Class<?>[] classContexts = getClassContext();
                    String callingClass = classContexts.length > 4 ? classContexts[4].getName() : "none";
                    String callingParent = classContexts.length > 5 ? classContexts[5].getName() : "none";
                    // FML is allowed to call system exit and the Minecraft applet (from the quit button)
                    if (!(callingClass.startsWith(getFMLPackageName())
                            || "net.minecraft.server.dedicated.ServerHangWatchdog$1".equals(callingClass)
                            || "net.minecraft.server.dedicated.ServerHangWatchdog".equals(callingClass)
                            || "net.minecraft.client.Minecraft".equals(callingClass) && "net.minecraft.client.Minecraft".equals(callingParent)
                            || "net.minecraft.server.dedicated.DedicatedServer".equals(callingClass) && "net.minecraft.server.MinecraftServer".equals(callingParent))
                    )
                        throw new ExitTrappedException();
                }
                else if ("setSecurityManager".equals(permName))
                    throw new SecurityException("Cannot replace the FML security manager");
            }
            
            @Override
            public void checkPermission(Permission perm, Object context) { checkPermission(perm); }
            
        });
    }
    
    public static ImmutableList<String> getWhiteList() {
        File file = new File("nu-whitelist.txt");
        try {
            return Files.readLines(file, Charsets.UTF_8, new LineProcessor<>() {
                
                List<String> result = Lists.newLinkedList();
                
                @Override
                public boolean processLine(@Nonnull String line) {
                    if (!line.startsWith("#") && !line.isEmpty())
                        result.add(line);
                    return true;
                }
                
                @Override
                public ImmutableList<String> getResult() {
                    return ImmutableList.copyOf(result);
                }
                
            });
        } catch (IOException e) {
            if (!file.isFile())
                try {
                    if (file.createNewFile()) {
                        String whitelist = Joiner.on('\n').join(DEFAULT_WHITE_LIST);
                        logger.info("Create file: nu-whitelist.txt\n" + whitelist);
                        Files.write(whitelist, file, Charsets.UTF_8);
                        return DEFAULT_WHITE_LIST;
                    }
                } catch (IOException ex) { ex.printStackTrace(); }
            return ImmutableList.of();
        }
    }
    
}
