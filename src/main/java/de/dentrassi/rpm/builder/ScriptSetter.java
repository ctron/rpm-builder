package de.dentrassi.rpm.builder;

import java.util.function.BiConsumer;

@FunctionalInterface interface ScriptSetter extends BiConsumer<String, String>
{
}