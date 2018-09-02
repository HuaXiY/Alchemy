package index.alchemy.support.agent;

import java.lang.instrument.Instrumentation;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.time.Instant;

import index.alchemy.util.ModuleHelper;

public class Patcher {
    
    private static Instrumentation INSTRUMENTATION = null;
    
    public static void premain(String agentArgs, Instrumentation inst) throws PrivilegedActionException {
        INSTRUMENTATION = inst;
        
        AccessController.doPrivileged((PrivilegedExceptionAction<Void>) () -> {
            ModuleHelper.openAllModule();
            return null;
        });
        System.err.println("[" + Instant.now() + "] [Alchemy-Agent] All modules opened!");
        
        inst.addTransformer(new Transformer());
        System.err.println("[" + Instant.now() + "] [Alchemy-Agent] Transformer added!");
    }
    
    public static Instrumentation getInstrumentation() {
        return INSTRUMENTATION;
    }
    
}
