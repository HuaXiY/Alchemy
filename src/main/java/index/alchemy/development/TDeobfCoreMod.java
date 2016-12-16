package index.alchemy.development;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import index.alchemy.util.Tool;
import index.project.version.annotation.Gamma;
import net.minecraft.launchwrapper.IClassTransformer;

import static org.objectweb.asm.Opcodes.*;

@Gamma
public class TDeobfCoreMod {
	
	public static final Map<String, IClassTransformer> ct_mapping = new HashMap<String, IClassTransformer>();
	
	public static class SrgMap {
		
		public final Map<String, String>
				func = new HashMap<String, String>(),
				field = new HashMap<String, String>(),
				clazz = new HashMap<String, String>();
		
		public static SrgMap get(File srg_mcp) throws IOException { 
			SrgMap srg = new SrgMap();
			String[] maps = Tool.read(srg_mcp).split("\n");
			for (int index = 0, len = maps.length; index < len; index++) {
				String str = maps[index];
				if (str.length() < 3) 
					continue;
				String sa[] = str.split(" "), src, to;
				if (str.startsWith("CL:")) {
					src = sa[1];
					to = sa[2];
					if (!src.equals(to)) 
						srg.clazz.put(src, to);
				} else if (str.startsWith("MD:")) {
					src = Tool.get(sa[1], ".*/(.*)");
					to = Tool.get(sa[sa.length - 2], ".*/(.*)");
					if (!src.equals(to)) 
						srg.func.put(src, to);
				} else if (str.startsWith("FD:")) {
					src = Tool.get(sa[1], ".*/(.*)");
					to = Tool.get(sa[2], ".*/(.*)");
					if (!src.equals(to)) 
						srg.field.put(src, to);
				}
			}
			return srg;
		}
		
	}
	
	public static void deobf(File input, File output, File srg_mcp) throws IOException {
		SrgMap srgmap = SrgMap.get(srg_mcp);
		ZipInputStream zis = new ZipInputStream(new FileInputStream(input));
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(output));
		for (ZipEntry entry; (entry = zis.getNextEntry()) != null;) {
			if (entry.isDirectory())
				continue;
			System.out.println("Copying: " + entry);
			IClassTransformer ct = ct_mapping.get(entry.getName());
			byte[] buffer = IOUtils.toByteArray(zis);
			if (ct != null) {
				System.out.println("ClassTransformer: " + entry.getName());
				buffer = ct.transform("", "", buffer);
			}
			if (entry.getName().endsWith(".class")) {
				buffer = deobf(buffer, srgmap);
				zos.putNextEntry(new ZipEntry(entry.getName()));
			} else {
				zos.putNextEntry(new ZipEntry(entry.getName()));
			}
			IOUtils.write(buffer, zos);
		}
		zis.close();
		zos.finish();
	}
	
	public static byte[] deobf(byte[] bytecode, final SrgMap map) throws IOException {
		ClassReader cr = new ClassReader(bytecode);
		ClassWriter cw = new ClassWriter(0);
		ClassVisitor cv = new ClassVisitor(ASM5, cw) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature,
					String[] exceptions) {
				return new MethodVisitor(ASM5, super.visitMethod(access, name, desc, signature, exceptions)) {
					@Override
					public void visitLdcInsn(Object cst) {
						if (cst.getClass() == String.class) {
							cst = map((String) cst, map.field);
							cst = map((String) cst, map.func);
						}
						super.visitLdcInsn(cst);
					}
				};
			}
		};
		cr.accept(cv, 0);
		return cw.toByteArray();
	}
	
	public static String map(String ldc, Map<String, String> map) {
		for (Entry<String, String> entry : map.entrySet())
			if (entry.getKey().equals(ldc)) {
				System.out.println("Replacing: " + entry.getKey() + " -> " + entry.getValue());
				return entry.getValue();
			}
		return ldc;
	}
	
	public static void main(String[] args) throws IOException {
		String inputDir = "D:/Forge-Alchemy-1.10/mods/";
		String inputName = "TTFR-1.10.2-1.5.2.jar";
		String outputName = inputName.replace(".jar", "-dev.jar");
		String srg_mcp = "C:/Users/93192/.gradle/caches/minecraft/de/oceanlabs/mcp/mcp_snapshot/20161111/1.10.2/srgs/srg-mcp.srg";
		//String notch_mcp = srg_mcp.replace("srg-mcp", "notch-mcp");
		deobf(new File(inputDir, inputName), new File(inputDir, outputName), new File(srg_mcp));
	}

}
