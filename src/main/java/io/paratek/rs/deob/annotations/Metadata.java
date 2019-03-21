package io.paratek.rs.deob.annotations;

public @interface Metadata {

    String originalName();

    String originalDescription() default "[unassigned]";

}
