package index.alchemy.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import index.alchemy.core.AlchemyEngine;

import static index.alchemy.util.$.$;

public interface ModuleHelper {
	
	MethodHandle MH_IMPL_ADD_OPENS_TO_ALL_UNNAMED = getImplAddOpensToAllUnnamedMethodHandle();
	
	static void openModuleJavaBase() {
		Module base = Object.class.getModule();
		base.getPackages().forEach(packageName -> $(base, "implAddOpensToAllUnnamed", packageName));
	}
	
	static MethodHandle getImplAddOpensToAllUnnamedMethodHandle() {
		openModuleJavaBase();
		try {
			return AlchemyEngine.lookup().findVirtual(Module.class, "implAddOpensToAllUnnamed", MethodType.methodType(void.class, String.class));
		} catch (Exception e) { throw new RuntimeException(e); }
	}
	
	static void addOpensToAllUnnamed(Module module) {
		module.getPackages().forEach(packageName -> {
			try {
				MH_IMPL_ADD_OPENS_TO_ALL_UNNAMED.invoke(module, packageName);
			} catch (Throwable e) { throw new RuntimeException(e); }
		});
	}
	
	static void openAllModule() {
		ModuleLayer.boot().modules().forEach(ModuleHelper::addOpensToAllUnnamed);
	}

}
