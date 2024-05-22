import kotlin.reflect.KClass

/**
 * An annotation to specify adapters for entities.
 *
 * @property adapter The adapter class to use.
 */
@Target(AnnotationTarget.CLASS)
annotation class XmlAdapter(val adapter: KClass<out XmlEntityAdapter>)

/**
 * An annotation to specify transformations for string attributes.
 *
 * @property transformer The transformer class to use.
 */
@Target(AnnotationTarget.PROPERTY)
@Repeatable
annotation class XmlString(val transformer: KClass<out StringTransformer>)


/**
 * An annotation to specify XML builders for entities.
 * @property builder The builder class to use.
 */
@Target(AnnotationTarget.PROPERTY)
annotation class XmlBuild(val adapter: KClass<out XmlBuilder>)


/**
 * An annotation to specify the identifier for entities.
 * @property nameChanger The name changer for the identifier.
 */
@Target(AnnotationTarget.PROPERTY)
annotation class XmlId(val nameChanger: String)


/**
 * An annotation to exclude attributes from entities.
 */
@Target(AnnotationTarget.PROPERTY)
annotation class ExcludeAttribute()