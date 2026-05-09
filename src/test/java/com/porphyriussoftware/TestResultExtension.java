package com.porphyriussoftware;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;

public class TestResultExtension implements AfterTestExecutionCallback {

    @Override
    public void afterTestExecution(ExtensionContext context) {
        boolean failed = context.getExecutionException().isPresent();
        String name = context.getDisplayName();

        if (failed) {
            System.out.println("💀 FAIL: " + name);
        } else {
            System.out.println("🔥 PASS: " + name);
        }
    }
}
