package com.github.charlie.alchemy;

import java.lang.instrument.Instrumentation;
import java.time.Instant;

public class Patcher {
    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new Transformer());
        System.err.println("[" + Instant.now() + "] [Alchemy-Instrumentation] Transformer added!");
    }
}
