import java.io.File
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

/**
 * A document class representing an XML document.
 *
 * @property rootEntity The root entity of the document.
 * @constructor Creates a Document with the specified root name.
 */
class Document(rootName : String) {
    val rootEntity = Entity(rootName)

    /**
     * Generates a pretty printed XML representation of the document.
     *
     * @return The XML representation of the document.
     */
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

    /**
     * Writes the XML representation of the document to a file.
     *
     * @param filename The name of the file to write to.
     */
    fun writeToFile(filename: String) {
        val prettyXml = prettyPrint()
        File(filename).writeText(prettyXml)
    }

    /**
     * Adds an attribute to entities with the specified name.
     *
     * @param entityName The name of the entity to add the attribute to.
     * @param attributeName The name of the attribute to add.
     * @param attributeValue The value of the attribute to add.
     */
    fun addAttribute(entityName: String, attributeName: String, attributeValue: String) {
        val attributeAdderVisitor = AttributeAdderVisitor(entityName, attributeName, attributeValue)
        this.rootEntity.acceptVisitor(attributeAdderVisitor)
    }

    /**
     * Renames entities with the specified old name to the new name.
     *
     * @param oldName The current name of the entity.
     * @param newName The new name for the entity.
     */
    fun renameEntity(oldName: String, newName: String) {
        val entityRenamerVisitor = EntityRenamerVisitor(oldName, newName)
        this.rootEntity.acceptVisitor(entityRenamerVisitor)
    }

    /**
     * Renames attributes of entities with the specified entity name.
     *
     * @param entityName The name of the entity to rename the attribute in.
     * @param oldAttributeName The current name of the attribute.
     * @param newAttributeName The new name for the attribute.
     */
    fun renameAttribute(entityName: String, oldAttributeName: String, newAttributeName: String) {
        val attributeRenamerVisitor = AttributeRenamerVisitor(entityName, oldAttributeName, newAttributeName)
        this.rootEntity.acceptVisitor(attributeRenamerVisitor)
    }

    /**
     * Removes entities with the specified name from the document.
     *
     * @param entityName The name of the entity to remove.
     */
    fun removeEntity(entityName: String) {
        val entityRemoverVisitor = EntityRemoverVisitor(entityName)
        this.rootEntity.acceptVisitor(entityRemoverVisitor)
    }

    /**
     * Removes attributes with the specified name from entities with the specified name.
     *
     * @param entityName The name of the entity to remove the attribute from.
     * @param attributeName The name of the attribute to remove.
     */
    fun removeAttribute(entityName: String, attributeName: String) {
        val attributeRemoverVisitor = AttributeRemoverVisitor(entityName, attributeName)
        this.rootEntity.acceptVisitor(attributeRemoverVisitor)
    }

    /**
     * Executes an XPath query on the document.
     *
     * @param path The XPath query string.
     */
    fun xpath(path: String) {
        val xpathVisitor = XPathPrintVisitor()
        this.rootEntity.queryXPath(path, xpathVisitor)
    }

    /**
     * Adds an object to the document.
     *
     * @param obj The object to add.
     */
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
        val entityName = clazz.simpleName?: ""
        val entity = Entity(entityName)

        clazz.memberProperties.forEach { prop ->
            val propValue = prop.getter.call(obj)

            if (propValue != null && prop.findAnnotation<ExcludeAttribute>() == null) {

                if (propValue is List<*>) {

                    val entityChild = Entity(getPropNewName(prop))
                    propValue.filterNotNull().forEach { listItem ->
                        val newEntity = mapObjectToEntity(listItem)
                        applyAnnotation(listItem ,newEntity)
                        entityChild.addChild(newEntity)
                    }
                    entity.addChild(entityChild)

                } else {
                    val xmlBuildAnnotation = prop.findAnnotation<XmlBuild>()
                    if (xmlBuildAnnotation != null) {
                        handleXmlBuildAnnotation(entity, prop, propValue, xmlBuildAnnotation)
                    } else {
                        // Default behavior: Add property as a child entity
                        val propEntity = Entity(getPropNewName(prop))
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
        val propNewName = getPropNewName(prop)
        builderInstance.adapt(entity, propNewName, valueTransformed)
    }

    private fun transformToString(prop: KProperty1<out Any, *>, value: Any): String {
        val transformer = getTransformer(prop)
        return transformer.transform(value)
    }

    private fun getTransformer(prop: KProperty1<out Any, *>): StringTransformer {
        val stringTransformerAnnotation = prop.annotations.filterIsInstance<XmlString>().firstOrNull()
        return stringTransformerAnnotation?.transformer?.objectInstance?: DefaultStringTransformer
    }

    private fun getPropNewName(prop: KProperty1<out Any, *>): String {
        val propNameAnnotation = prop.annotations.filterIsInstance<XmlId>().firstOrNull()
        return propNameAnnotation?.nameChanger?: prop.name
    }


}