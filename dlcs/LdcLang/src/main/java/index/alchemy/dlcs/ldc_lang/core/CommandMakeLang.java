package index.alchemy.dlcs.ldc_lang.core;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;

import index.alchemy.command.AlchemyCommandClient;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.util.Tool;
import index.project.version.annotation.Omega;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import com.google.common.base.Joiner;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import static org.objectweb.asm.Opcodes.*;

@Omega
public class CommandMakeLang extends AlchemyCommandClient {
    
    @Override
    public String getName() {
        return "mlang";
    }
    
    @Override
    public String getUsage(ICommandSender sender) {
        return "/mlang <mod file name>";
    }
    
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length > 0)
            try {
                String name = Joiner.on(' ').join(args);
                File file = new File(AlchemyModLoader.mc_dir, "mods/" + name);
                if (file.exists() && !file.isDirectory()) {
                    ClassLoader loader = new URLClassLoader(new URL[]{file.toURI().toURL()}, null);
                    ClassPath path = ClassPath.from(loader);
                    StringBuilder builder = new StringBuilder();
                    for (ClassInfo info : path.getAllClasses()) {
                        ClassReader reader = new ClassReader(info.getName());
                        ClassNode node = new ClassNode(ASM5);
                        reader.accept(node, 0);
                        for (MethodNode method : node.methods) {
                            int index = 0;
                            for (Iterator<AbstractInsnNode> iterator = method.instructions.iterator(); iterator.hasNext(); ) {
                                AbstractInsnNode insn = iterator.next();
                                if (insn instanceof LdcInsnNode) {
                                    LdcInsnNode ldc = (LdcInsnNode) insn;
                                    if (ldc.cst instanceof String)
                                        builder.append('-').append(info.getName()).append('#').append(method.name).append('@')
                                                .append(index++).append('=').append(((String) ldc.cst).replace("\n", "\\n")).append('\n');
                                }
                            }
                        }
                    }
                    File dir = new File(AlchemyModLoader.mc_dir, "lang"),
                            lang = new File(AlchemyModLoader.mc_dir, "lang/" + file.getName() + ".lang");
                    if (!dir.exists())
                        dir.mkdirs();
                    Tool.save(lang, builder.toString());
                }
                else
                    sender.sendMessage(new TextComponentString(file.getCanonicalPath() + "not found."));
            } catch (Exception e) { e.printStackTrace(); }
    }
    
}
