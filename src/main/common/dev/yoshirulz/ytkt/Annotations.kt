package dev.yoshirulz.ytkt

/** for suppressing unused warnings in IDEA */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
internal annotation class EntryPoint

/** waiting on [Kotlin/kotlinx.serialization#188](https://github.com/Kotlin/kotlinx.serialization/issues/188), one of the contributors has made a separate library but it doesn't target Kotlin/Native */
@Experimental(level = Experimental.Level.WARNING)
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
internal annotation class RequiresXMLParser
