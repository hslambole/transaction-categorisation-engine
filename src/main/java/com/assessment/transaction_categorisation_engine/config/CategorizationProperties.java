package com.assessment.transaction_categorisation_engine.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "categorization")
public class CategorizationProperties {

    private Fallback fallback = new Fallback();
    private AsyncPool async = new AsyncPool();

    public Fallback getFallback() {
        return fallback;
    }

    public void setFallback(Fallback fallback) {
        this.fallback = fallback;
    }

    public AsyncPool getAsync() {
        return async;
    }

    public void setAsync(AsyncPool async) {
        this.async = async;
    }

    public static class Fallback {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class AsyncPool {
        private int coreSize = 2;
        private int maxSize = 8;

        public int getCoreSize() {
            return coreSize;
        }

        public void setCoreSize(int coreSize) {
            this.coreSize = coreSize;
        }

        public int getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(int maxSize) {
            this.maxSize = maxSize;
        }
    }
}
