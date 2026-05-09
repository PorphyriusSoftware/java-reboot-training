package com.porphyriussoftware;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;

public class TestResultExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback, AfterAllCallback {


    private static final String START_TIME = "start-time";

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        context.getStore(ExtensionContext.Namespace.GLOBAL)
            .put(START_TIME + context.getUniqueId(), System.currentTimeMillis());
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        long start = context.getStore(ExtensionContext.Namespace.GLOBAL)
            .remove(START_TIME + context.getUniqueId(), long.class);

        long duration = System.currentTimeMillis() - start;

        boolean failed = context.getExecutionException().isPresent();
        String name = context.getDisplayName();

        if (failed) {
            System.out.printf("💀 \u001B[31mFAIL\u001B[0m: %s (%d ms)%n", name, duration);
            System.out.println("🐉 DRAGON MODE ACTIVATED — TABLE FLIPPED");
        } else {
            System.out.printf("🔥 \u001B[32mPASS\u001B[0m: %s (%d ms)%n", name, duration);
        }
    }

    @Override
    public void afterAll(ExtensionContext context) {
        System.out.println("""

                \u001B[36m
                       ███████╗██╗   ██╗██████╗ ███████╗██████╗
                       ██╔════╝██║   ██║██╔══██╗██╔════╝██╔══██╗
                       ███████╗██║   ██║██████╔╝█████╗  ██████╔╝
                       ╚════██║██║   ██║██╔══██╗██╔══╝  ██╔══██╗
                       ███████║╚██████╔╝██║  ██║███████╗██║  ██║
                       ╚══════╝ ╚═════╝ ╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝
                \u001B[0m
                """);

        System.out.println("🌟 \u001B[32mALL TESTS PASSED — SUPER SAIYAN MODE ACHIEVED\u001B[0m 🌟");
    }
}
