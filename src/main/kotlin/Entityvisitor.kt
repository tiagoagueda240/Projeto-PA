import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * Represents an attribute with a name and a value.
 *
 * @property name The name of the attribute.
 * @property value The value of the attribute.
 */
data class Attribute(var name: String, var value: String)

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
annotation class XmlString(val transformer: KClass<out StringTransformer>)

/**
 * An annotation to specify the XML element name for properties.
 *
 * @property name The XML element name.
 */
annotation class XmlElementName(val name: String)

/**
 * A transformer interface for string attribute transformations.
 */
interface StringTransformer {
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
object AddPercentage : StringTransformer {
    override fun transform(value: Any?): String {
        return value.toString() + "%"
    }
}

/**
 * An interface defining entity adapters.
 */
interface XmlEntityAdapter {
    fun adapt(entity: Entity)
}
@Target(AnnotationTarget.PROPERTY)
annotation class XmlContent(){

}
@Target(AnnotationTarget.PROPERTY)
annotation class XmlAttribute(){

}
@Target(AnnotationTarget.PROPERTY)
annotation class XmlBuild(val adapter: KClass<out XmlBuilder>){

}

// Classe FUC
@XmlAdapter(FUCOrdered::class)
class FUC(
    @XmlElementName("codigo") @XmlBuild(AddAsAttribute::class) val codigo: String,
    @XmlElementName("nome") @XmlBuild(AddAsContent::class) val nome: String,
    @XmlElementName("ects") @XmlBuild(AddAsContent::class) val ects: Double,
    val observacoes: String,
    val avaliacao: List<ComponenteAvaliacao>
)

interface XmlBuilder {
    fun adapt(entity: Entity,prop: KProperty1<out Any, *>, value: String)

}

object  AddAsAttribute : XmlBuilder{
    override fun adapt(entity: Entity,prop: KProperty1<out Any, *>, value: String){
        entity.addAttribute(prop.name, value)
    }
}
object  AddAsContent : XmlBuilder{
    override fun adapt(entity: Entity,prop: KProperty1<out Any, *>, value: String){
        val childEnt = Entity(prop.name)
        childEnt.addContent(value)
        entity.addChild(childEnt)
    }
}

// Classe ComponenteAvaliacao
@XmlAdapter(ComponenteAvaliacaoAdapter::class)
class ComponenteAvaliacao(
    @XmlElementName("nome") @XmlBuild(AddAsAttribute::class) val nome: String,
    @XmlElementName("peso") @XmlString(AddPercentage::class) @XmlBuild(AddAsAttribute::class) val peso: Int
)


class FUCAdapter : XmlEntityAdapter {
    override fun adapt(entity: Entity) {
        val children = entity.getChildren().toMutableList()
        children.clear()

        val codigoEntity = entity.getChildren().find { it.name == "codigo" }
        val nomeEntity = entity.getChildren().find { it.name == "nome" }
        val ectsEntity = entity.getChildren().find { it.name == "ects" }
        val avaliacaoEntity = entity.getChildren().find { it.name == "avaliacao" }

        if (codigoEntity != null) entity.addChild(codigoEntity)
        if (ectsEntity != null) entity.addChild(ectsEntity)
        if (nomeEntity != null) entity.addChild(nomeEntity)
        if (avaliacaoEntity != null) entity.addChild(avaliacaoEntity)
    }
}

object FUCOrdered : XmlEntityAdapter {
    override fun adapt(entity: Entity) {
        val sortedChildren = entity.getChildren().sortedByDescending { it.name }
        entity.clearChildren()
        sortedChildren.forEach { entity.addChild(it) }
    }
}

class ComponenteAvaliacaoAdapter : XmlEntityAdapter {
    override fun adapt(entity: Entity) {

        entity.addAttribute("novoAtributo", "valorNovoAtributo")
        entity.removeAttribute("nome")
    }
}

class TextoAdapter : XmlEntityAdapter {
    override fun adapt(entity: Entity) {

        entity.addAttribute("novoAtributo", "valorNovoAtributo")
        entity.removeAttribute("nome")
    }
}

/**
 * Represents an entity with a name, content, and attributes.
 *
 * @property name The name of the entity.
 */
class Entity(var name: String) {
    private val children = mutableListOf<Entity>()
    private val attributes = mutableListOf<Attribute>()
    var content: String? = null
    var parentEntity: Entity? = null

    fun addChild(child: Entity) {
        child.parentEntity = this
        children.add(child)
    }

    fun addContent(content: String) {
        this.content = content
    }

    fun rename(newName: String) {
        this.name = newName
    }

    fun removeChild(child: Entity) {
        children.remove(child)
        child.parentEntity = null
    }

    fun addAttribute(name: String, value: String) {
        attributes.add(Attribute(name, value))
    }

    fun removeAttribute(name: String) {
        attributes.removeIf { it.name == name }
    }

    fun getAttributeByName(name: String): Attribute? {
        return attributes.find { it.name == name }
    }

    fun getAttributes(): List<Attribute> {
        return attributes.toList()
    }

    fun getChildren(): List<Entity> {
        return children.toList()
    }

    fun clearChildren() {
        children.clear()
    }

    fun acceptVisitor(visitor: Visitor) {
        visitor.visit(this)
        val childrenCopy = children.toList()
        childrenCopy.forEach { it.acceptVisitor(visitor) }
    }

    fun queryXPath(expression: String, visitor: Visitor) {
        val parts = expression.split("/")
        var currentEntities = listOf(this)
        for (part in parts) {
            currentEntities = currentEntities.flatMap { it.getChildren() }.filter { it.name == part }
        }
        currentEntities.forEach { it.acceptVisitor(visitor) }
    }
}

/**
 * A visitor interface for entities.
 */
interface Visitor {
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
 *
 * @param entityName The name of the entity to remove.
 */
class EntityRemoverVisitor(
    private val entityName: String
) : Visitor {
    override fun visit(entity: Entity) {
        if (entity.name == entityName) {
            entity.parentEntity?.removeChild(entity)
        }
    }
}

/**
 * A visitor class for renaming entities.
 *
 * @param oldName The current name of the entity.
 * @param newName The new name for the entity.
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
 *
 * @param entityName The name of the entity to visit.
 * @param attributeName The name of the attribute to add.
 * @param attributeValue The value of the attribute to add.
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
 *
 * @param entityName The name of the entity to visit.
 * @param attributeName The name of the attribute to remove.
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
 *
 * @param entityName The name of the entity to visit.
 * @param oldAttributeName The current name of the attribute.
 * @param newAttributeName The new name for the attribute.
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
 * A document class representing an XML document.
 */
class Document(rootName : String) {
    val rootEntity = Entity(rootName)

    fun prettyPrint(): String {
        val result = StringBuilder()
        result.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        buildXmlString(rootEntity, result, 0)
        return result.toString()
    }

    private fun buildXmlString(entity: Entity, result: StringBuilder, level: Int) {
        result.append("\t".repeat(level))
        result.append("<${entity.name}")

        entity.getAttributes().forEach { attribute ->
            result.append(" ${attribute.name}=\"${attribute.value}\"")
        }

        if (entity.getChildren().isEmpty() && entity.content == null) {
            result.append("/>\n")
        } else {
            if (entity.content != null) {
                result.append(">")
                result.append(entity.content)
            } else {
                result.append(">\n")
                entity.getChildren().forEach { child ->
                    buildXmlString(child, result, level + 1)
                }
                result.append("\t".repeat(level))
            }

            result.append("</${entity.name}>\n")
        }
    }

    fun writeToFile(filename: String) {
        val prettyXml = prettyPrint()
        File(filename).writeText(prettyXml)
    }

    fun addAttribute(entityName: String, attributeName: String, attributeValue: String) {
        val attributeAdderVisitor = AttributeAdderVisitor(entityName, attributeName, attributeValue)
        this.rootEntity.acceptVisitor(attributeAdderVisitor)
    }

    fun renameEntity(oldName: String, newName: String) {
        val entityRenamerVisitor = EntityRenamerVisitor(oldName, newName)
        this.rootEntity.acceptVisitor(entityRenamerVisitor)
    }

    fun renameAttribute(entityName: String, oldAttributeName: String, newAttributeName: String) {
        val attributeRenamerVisitor = AttributeRenamerVisitor(entityName, oldAttributeName, newAttributeName)
        this.rootEntity.acceptVisitor(attributeRenamerVisitor)
    }

    fun removeEntity(entityName: String) {
        val entityRemoverVisitor = EntityRemoverVisitor(entityName)
        this.rootEntity.acceptVisitor(entityRemoverVisitor)
    }

    fun removeAttribute(entityName: String, attributeName: String) {
        val attributeRemoverVisitor = AttributeRemoverVisitor(entityName, attributeName)
        this.rootEntity.acceptVisitor(attributeRemoverVisitor)
    }

    fun xpath(path: String) {
        val xpathVisitor = XPathPrintVisitor()
        this.rootEntity.queryXPath(path, xpathVisitor)
    }

    fun addObject(obj: Any) {
        val entity = mapObjectToEntity(obj)
        applyAnnotation(obj ,entity)

        rootEntity.addChild(entity)
    }

    private fun applyAnnotation(obj: Any, entity: Entity) {
        val clazz = obj::class

        // Check if the class has an XmlAdapter annotation
        val xmlAdapterAnnotation = clazz.findAnnotation<XmlAdapter>()
        if (xmlAdapterAnnotation != null) {
            val adapterClass = xmlAdapterAnnotation.adapter.objectInstance?.adapt(entity)
        }
    }

    private fun mapObjectToEntity(obj: Any): Entity {
        val clazz = obj::class
        val entityName = getEntityName(clazz)
        val entity = Entity(entityName)

        clazz.memberProperties.forEach { prop ->
            val propValue = prop.getter.call(obj)

            if (propValue != null) {

                if (propValue is List<*>) {

                    val entityChild = Entity(prop.name)
                    propValue.filterNotNull().forEach { listItem ->
                        val newEntity = mapObjectToEntity(listItem)
                        entityChild.addChild(newEntity)
                    }
                    entity.addChild(entityChild)

                } else {
                    val xmlBuildAnnotation = prop.findAnnotation<XmlBuild>()
                    if (xmlBuildAnnotation != null) {
                        handleXmlBuildAnnotation(entity, prop, propValue, xmlBuildAnnotation)
                    } else {
                        // Default behavior: Add property as a child entity
                        val propEntity = Entity(prop.name)
                        val valueTransformed = transformToString(prop, propValue)
                        propEntity.addContent(valueTransformed)
                        entity.addChild(propEntity)
                    }
                }
            }
        }

        return entity
    }
    private fun handleXmlBuildAnnotation(
        entity: Entity,
        prop: KProperty1<out Any, *>,
        propValue: Any,
        xmlBuildAnnotation: XmlBuild
    ) {
        val builderClass = xmlBuildAnnotation.adapter
        val builderInstance = builderClass.objectInstance?:return
        val valueTransformed = transformToString(prop, propValue)
        builderInstance.adapt(entity, prop, valueTransformed)
    }

    private fun transformToString(prop: KProperty1<out Any, *>, value: Any): String {
        val transformer = getTransformer(prop)
        return transformer.transform(value)
    }

    private fun getTransformer(prop: KProperty1<out Any, *>): StringTransformer {
        val stringTransformerAnnotation = prop.annotations.filterIsInstance<XmlString>().firstOrNull()
        return stringTransformerAnnotation?.transformer?.objectInstance?: DefaultStringTransformer
    }

    private fun getEntityName(clazz: KClass<*>): String {
        val entityNameAnnotation = clazz.annotations.filterIsInstance<XmlElementName>().firstOrNull()
        return entityNameAnnotation?.name ?: clazz.simpleName ?: "entity"
    }

    private fun getPropertyName(prop: KProperty1<out Any, *>): String {
        val propNameAnnotation = prop.annotations.filterIsInstance<XmlElementName>().firstOrNull()
        return propNameAnnotation?.name ?: prop.name
    }
}

fun Document(name: String, build: Document.() -> Unit) =
    Document(name).apply{
        build(this)
    }

fun Document.tag(obj: Any) =
    addObject(obj)


fun main() {


    val tab = Document("Plano") {

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


    //val documento = Document("Plano")

    // Exemplo de objeto ComponenteAvaliacao
    /*val componente = ComponenteAvaliacao("Quizzes", 20)
    documento.addObject(componente)*/

    //documento.addObject(Attribute("curso", "texto"))


    /*val cursoEntity = Entity("curso")
    cursoEntity.addContent("Mestrado em Engenharia Informática")
    //documento.addObject(cursoEntity)*/


    /* // Exemplo de objeto FUC
     val fuc = FUC("M4310", "Programação Avançada", 6.0, "la la...",
         listOf(
             ComponenteAvaliacao("Quizzes", 20),
             ComponenteAvaliacao("Projeto", 80)
         )
     )



     documento.addObject(fuc)
     documento.addObject(fuc)

     documento.writeToFile("output.xml") */

    println(tab.prettyPrint())
}