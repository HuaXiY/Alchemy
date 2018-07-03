package index.alchemy.agent.support;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.text.WordUtils;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import com.sun.tools.attach.spi.AttachProvider;

import index.alchemy.util.Tool;
import sun.tools.attach.LinuxVirtualMachine;
import sun.tools.attach.WindowsVirtualMachine;

public class AgentSupport {
	
	public static class FakeAttachProvider extends AttachProvider {
		
		protected static final FakeAttachProvider INSTANCE = new FakeAttachProvider();
		
		public static final FakeAttachProvider instance() { return INSTANCE; }
		
		protected FakeAttachProvider() { }

		@Override
		public String name() { return null; }

		@Override
		public String type() { return null; }

		@Override
		public VirtualMachine attachVirtualMachine(String id) throws AttachNotSupportedException, IOException { return null; }

		@Override
		public List<VirtualMachineDescriptor> listVirtualMachines() { return null; }
		
	}
	
	private static boolean libState;
	
	public static void checkLibState() throws AttachNotSupportedException {
		if (!libState)
			try {
				loadLib();
			} catch (Exception e) { throw new AttachNotSupportedException(e); }
	}
	
	public static enum OS {
		
		WINDOWS() {
			
			@Override
			public VirtualMachine getVMFromPid(String pid) throws AttachNotSupportedException, IOException {
				return new WindowsVirtualMachine(FakeAttachProvider.instance(), pid);
			}
			
		}, LINUX() {
			
			@Override
			public VirtualMachine getVMFromPid(String pid) throws AttachNotSupportedException, IOException {
				return new LinuxVirtualMachine(FakeAttachProvider.instance(), pid);
			}
			
		};
		
		public abstract VirtualMachine getVMFromPid(String pid) throws AttachNotSupportedException, IOException;
		
		public static final OS current() {
			String name = System.getProperty("os.name");
			int p = name.indexOf(' ');
			if (p != -1)
				name =  name.substring(0, p);
			return OS.valueOf(name.toUpperCase(Locale.US));
		}
		
	}
	
	public static void loadAgent(String path) throws AttachNotSupportedException, IOException, AgentLoadException, AgentInitializationException {
		loadAgent(path, null);
	}
	
	public static void loadAgent(String path, String args) throws AttachNotSupportedException, IOException, AgentLoadException, AgentInitializationException {
		try (VirtualMachine vm = getVMFromPid(discoverPid())) {
			vm.loadAgent(path, args);
		}
	}
	
	public static final String discoverPid() {
		String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
		int p = nameOfRunningVM.indexOf('@');
		return nameOfRunningVM.substring(0, p);
	 }
	
	public static VirtualMachine getVMFromPid(String pid) throws AttachNotSupportedException, IOException {
		return OS.current().getVMFromPid(pid);
	}
	
	public static void loadLib() throws Exception {
		System.load(Tool.createTempFile(AgentSupport.class.getResourceAsStream("/lib/x" + System.getProperty("sun.arch.data.model")
				+ "/" + System.mapLibraryName("attach")), WordUtils.initials("Index-Alchemy-Agent-Native-Attach.", '-'), ".tmp").getPath());
		libState = true;
	}

}
