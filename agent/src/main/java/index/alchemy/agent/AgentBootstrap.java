package index.alchemy.agent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.Objects;

import net.minecraft.launchwrapper.Launch;

public class AgentBootstrap {
	
	private static final String ALCHEMY_ENGINE = "index.alchemy.core.AlchemyEngine", INSTRUMENTATION = "INSTRUMENTATION";
	
	public static void premain(String agentOps, Instrumentation inst) {
		instrumentBootstrap(agentOps, inst);
	}
	
	public static void agentmain(String agentOps, Instrumentation inst) {
		instrumentBootstrap(agentOps, inst);
	}
	
	private static void instrumentBootstrap(String agentOps, Instrumentation inst) {
		try {
			Objects.requireNonNull(inst);
			sun.misc.Unsafe unsafe = AccessController.doPrivileged(new PrivilegedExceptionAction<sun.misc.Unsafe>() {
				
				@Override
				public sun.misc.Unsafe run() throws Exception {
					Field theUnsafe = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
					theUnsafe.setAccessible(true);
					return (sun.misc.Unsafe) theUnsafe.get(null);
				}
				
		   });
			Field target = Launch.classLoader
					.findClass(ALCHEMY_ENGINE)
					.getDeclaredField(INSTRUMENTATION);
			Objects.requireNonNull(target);
			unsafe.putObject(unsafe.staticFieldBase(target), unsafe.staticFieldOffset(target), inst);
		} catch (Throwable t) {
			try {
				RuntimeException exception = new RuntimeException("Set up instrument failed !", t);
				new File("logs").mkdirs();
				try (FileOutputStream stream = new FileOutputStream(new File("logs/agent-error.log"))) {
					stream.write(getStringFormThrowable(exception).getBytes("UTF-8"));
				}
				throw exception;
			} catch (Throwable result) {
				if (result instanceof RuntimeException)
					throw (RuntimeException) result;
				else
					throw new RuntimeException(result);
			}
		}
	}
	
	private static String getStringFormThrowable(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return sw.toString();
	}

}
