package index.alchemy.support.agent;

import index.alchemy.util.ModuleHelper;

import java.lang.instrument.Instrumentation;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.time.Instant;

public class Patcher {
    public static void premain(String agentArgs, Instrumentation inst) throws PrivilegedActionException {
        AccessController.doPrivileged((PrivilegedExceptionAction<Void>) () -> {
            ModuleHelper.openAllModule();
            return null;
        });
        System.err.println("[" + Instant.now() + "] [Alchemy-Agent] All modules opened!");

        inst.addTransformer(new Transformer());
        System.err.println("[" + Instant.now() + "] [Alchemy-Agent] Transformer added!");
    }
}
