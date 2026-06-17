package org.example.storemanager.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogActivity {
    String actionType(); // e.g. "CREATE", "UPDATE", "DELETE", "UPDATE_STATUS"
    String entityName(); // e.g. "Unit"
    Class<?> entityClass(); // e.g. Unit.class
}
