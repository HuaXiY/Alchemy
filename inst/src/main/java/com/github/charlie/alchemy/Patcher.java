package com.github.charlie.alchemy;

import java.lang.instrument.Instrumentation;

public class Patcher {
    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new Transformer());
    }
}
