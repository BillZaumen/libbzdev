/**
 * ESP scripting language module.
 * <P>
 * This module provides a service provider for the EP scripting
 * language.
 */
module org.bzdev.esp {
   requires java.base;
   requires java.scripting;
   requires org.bzdev.base;
   opens org.bzdev.providers.esp.lpack;

   provides javax.script.ScriptEngineFactory
       with org.bzdev.providers.esp.ESPFactory;
}

//  LocalWords:  ESP
