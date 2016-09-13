package index.alchemy.development;

import static org.objectweb.asm.Opcodes.*;

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
import net.minecraft.launchwrapper.IClassTransformer;

public class TDeobfCoreMod {
	
	public static final Map<String, IClassTransformer> ct_mapping = new HashMap<String, IClassTransformer>();
	static {
		ct_mapping.put("shadersmod/transform/SMCConfig.class", new IClassTransformer() {
			@Override
			public byte[] transform(String name, String transformedName, byte[] basicClass) {
				final ClassReader cr = new ClassReader(basicClass);
				final ClassWriter cw = new ClassWriter(0);
				final ClassVisitor cv = new ClassVisitor(ASM5, cw) {
					@Override
					public MethodVisitor visitMethod(int access, String name, String desc, String signature,
							String[] exceptions) {
						if (name.equals("getNamer")) {
							System.out.println(name + desc);
							String cl = "shadersmod.transform.NamerSrg".replace('.', '/');
							MethodVisitor mv = cw.visitMethod(access, name, desc, signature, exceptions);
							mv.visitCode();
							mv.visitTypeInsn(NEW, cl);  
							mv.visitInsn(DUP);  
							mv.visitMethodInsn(INVOKESPECIAL, cl, "<init>", "()V", false);
							mv.visitInsn(ARETURN);
							mv.visitMaxs(2, 1);
				            mv.visitEnd();
							return null;
						}
						return super.visitMethod(access, name, desc, signature, exceptions);
					}
				};
				cr.accept(cv, 0);
				return cw.toByteArray();
			}
		});
	}
	
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
	
	public static void deobf(File input, File output, File srg_mcp, File notch_mcp) throws IOException {
		SrgMap srgmap = SrgMap.get(srg_mcp);
		SrgMap notchmap = SrgMap.get(notch_mcp);
		ZipInputStream zis = new ZipInputStream(new FileInputStream(input));
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(output));
		for (ZipEntry entry; (entry = zis.getNextEntry()) != null;) {
			System.out.println("Copying: " + entry);
			IClassTransformer ct = ct_mapping.get(entry.getName());
			byte[] buffer = new byte[(int) entry.getSize()];
			IOUtils.read(zis, buffer);
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
		String inputName = "ShadersMod-1-10-2.jar";
		String outputName = inputName.replace(".jar", "-dev.jar");
		String srg_mcp = "C:/Users/93192/.gradle/caches/minecraft/de/oceanlabs/mcp/mcp_snapshot/20160822/1.10.2/srgs/srg-mcp.srg";
		String notch_mcp = srg_mcp.replace("srg-mcp", "notch-mcp");
		deobf(new File(inputDir, inputName), new File(inputDir, outputName), new File(srg_mcp), new File(notch_mcp));
	}

}
