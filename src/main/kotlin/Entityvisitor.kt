
/**
 * Represents a transformer interface for string attribute transformations.
 */
interface StringTransformer {
    /**
     * Transforms the given value into a string representation.
     * @param value The value to be transformed.
     * @return The transformed string representation.
     */
    fun transform(value: Any?): String
}

/**
 * A default string transformer that simply converts the value to a string.
 */
object DefaultStringTransformer : StringTransformer {
    override fun transform(value: Any?): String {
        return value?.toString() ?: ""
    }
}

/**
 * An interface defining entity adapters.
 */
interface XmlEntityAdapter {
    /**
     * Adapts the given entity.
     * @param entity The entity to be adapted.
     */
    fun adapt(entity: Entity)
}

/**
 * An interface defining XML builders.
 */
interface XmlBuilder {
    /**
     * Adapts the entity by adding the specified property and value.
     * @param entity The entity to be adapted.
     * @param propName The name of the property.
     * @param value The value of the property.
     */
    fun adapt(entity: Entity, propName: String, value: String)
}

/**
 * A builder object for adding properties as attributes to entities.
 */
object AddAsAttribute : XmlBuilder {
    override fun adapt(entity: Entity, propName: String, value: String) {
        entity.addAttribute(propName, value)
    }
}

/**
 * A builder object for adding properties as content to entities.
 */
object AddAsContent : XmlBuilder {
    override fun adapt(entity: Entity, propName: String, value: String) {
        val childEnt = Entity(propName)
        childEnt.addContent(value)
        entity.addChild(childEnt)
    }
}

/**
 * A visitor interface for entities.
 */
interface Visitor {
    /**
     * Visits the given entity.
     * @param entity The entity to be visited.
     */
    fun visit(entity: Entity)
}

/**
 * A visitor class for printing XML representation based on XPath Query.
 */
class XPathPrintVisitor : Visitor {
    override fun visit(entity: Entity) {
        val attributes = entity.getAttributes().joinToString(" ") { "${it.name}=\"${it.value}\"" }
        println("<${entity.name} $attributes/>")
    }
}

/**
 * A visitor class for removing entities.
 * @property entityName The name of the entity to remove.
 */
class EntityRemoverVisitor(private val entityName: String) : Visitor {
    override fun visit(entity: Entity) {
        if (entity.name == entityName) {
            entity.parentEntity?.removeChild(entity)
        }
    }
}

/**
 * A visitor class for renaming entities.
 * @property oldName The current name of the entity.
 * @property newName The new name for the entity.
 */
class EntityRenamerVisitor(private val oldName: String, private val newName: String) : Visitor {
    override fun visit(entity: Entity) {
        if (entity.name == oldName) {
            entity.rename(newName)
        }
    }
}

/**
 * A visitor class for adding an attribute to entities.
 * @property entityName The name of the entity to visit.
 * @property attributeName The name of the attribute to add.
 * @property attributeValue The value of the attribute to add.
 */
class AttributeAdderVisitor(
    private val entityName: String,
    private val attributeName: String,
    private val attributeValue: String
) : Visitor {
    override fun visit(entity: Entity) {
        if (entity.name == entityName) {
            entity.addAttribute(attributeName, attributeValue)
        }
    }
}

/**
 * A visitor class for removing attributes from entities.
 * @property entityName The name of the entity to visit.
 * @property attributeName The name of the attribute to remove.
 */
class AttributeRemoverVisitor(
    private val entityName: String,
    private val attributeName: String
) : Visitor {
    override fun visit(entity: Entity) {
        if (entity.name == entityName) {
            entity.removeAttribute(attributeName)
        }
    }
}

/**
 * A visitor class for renaming attributes of entities.
 * @property entityName The name of the entity to visit.
 * @property oldAttributeName The current name of the attribute.
 * @property newAttributeName The new name for the attribute.
 */
class AttributeRenamerVisitor(
    private val entityName: String,
    private val oldAttributeName: String,
    private val newAttributeName: String
) : Visitor {
    override fun visit(entity: Entity) {
        if (entity.name == entityName) {
            val attributes = entity.getAttributes()
            attributes.forEach { attribute ->
                if (attribute.name == oldAttributeName) {
                    entity.removeAttribute(oldAttributeName)
                    entity.addAttribute(newAttributeName, attribute.value)
                }
            }
        }
    }
}

/**
 * Represents a document builder for constructing XML documents.
 * @param name The name of the document.
 * @param build The build lambda function to construct the document.
 */
fun Document(name: String, build: Document.() -> Unit) =
    Document(name).apply{
        build(this)
    }

/**
 * Adds an object to the document.
 * @param obj The object to be added.
 */
fun Document.tag(obj: Any) =
    addObject(obj)


fun main() {


    /*val tab = Document("Plano") {

        tag(FUC("123","123",123.0,"123",  listOf(
            ComponenteAvaliacao("Quizzes", 20),
            ComponenteAvaliacao("Projeto", 80))))

        tag(FUC("123","123",123.0,"123",  listOf(
            ComponenteAvaliacao("Quizzes", 20),
            ComponenteAvaliacao("Projeto", 80))))

        tag(FUC("123","123",123.0,"123",  listOf(
            ComponenteAvaliacao("Quizzes", 20),
            ComponenteAvaliacao("Projeto", 80))))

    }


     documento.writeToFile("output.xml")

    println(tab.prettyPrint())*/
}