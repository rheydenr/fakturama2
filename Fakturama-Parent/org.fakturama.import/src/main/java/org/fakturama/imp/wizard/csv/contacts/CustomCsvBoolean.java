package org.fakturama.imp.wizard.csv.contacts;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is only a marker interface for a sort of Boolean values which can be in translated form (German only). 
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CustomCsvBoolean {

}
