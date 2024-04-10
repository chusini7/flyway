package com.efreight.flyway.holder;

import org.flywaydb.core.Flyway;
import org.springframework.core.NamedThreadLocal;

/**
 * @author Libiao
 * @date 2022/10/9
 */
public class FlywayHolder {

    private static final ThreadLocal<Flyway> flywayThreadLocal = new NamedThreadLocal<>("flywayThreadLocal");

    public static Flyway getFlyway() {
        return flywayThreadLocal.get();
    }

    public static void setFlyway(Flyway flyway) {
        flywayThreadLocal.set(flyway);
    }

    public static void remove() {
        flywayThreadLocal.remove();
    }
}
