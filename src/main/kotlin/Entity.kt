/**
 * Represents an attribute with a name and a value.
 *
 * @property name The name of the attribute.
 * @property value The value of the attribute.
 */
data class Attribute(var name: String, var value: String)

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

    /**
     * Adds a child entity to this entity.
     *
     * @param child The child entity to add.
     */
    fun addChild(child: Entity) {
        child.parentEntity = this
        children.add(child)
    }

    /**
     * Sets the content of this entity.
     *
     * @param content The content to set.
     */
    fun addContent(content: String) {
        this.content = content
    }

    /**
     * Renames this entity.
     *
     * @param newName The new name for the entity.
     */
    fun rename(newName: String) {
        this.name = newName
    }

    /**
     * Removes a child entity from this entity.
     *
     * @param child The child entity to remove.
     */
    fun removeChild(child: Entity) {
        children.remove(child)
        child.parentEntity = null
    }

    /**
     * Adds an attribute to this entity.
     *
     * @param name The name of the attribute to add.
     * @param value The value of the attribute to add.
     */
    fun addAttribute(name: String, value: String) {
        attributes.add(Attribute(name, value))
    }

    /**
     * Adds an attribute object to this entity.
     *
     * @param attribute The attribute object to add.
     */
    fun addAttributeAsObject(attribute: Attribute) {
        attributes.add(attribute)
    }

    /**
     * Removes an attribute from this entity.
     *
     * @param name The name of the attribute to remove.
     */
    fun removeAttribute(name: String) {
        attributes.removeIf { it.name == name }
    }

    /**
     * Gets an attribute by name from this entity.
     *
     * @param name The name of the attribute to get.
     * @return The attribute object if found, otherwise null.
     */
    fun getAttributeByName(name: String): Attribute? {
        return attributes.find { it.name == name }
    }

    /**
     * Gets all attributes of this entity.
     *
     * @return The list of attributes.
     */
    fun getAttributes(): List<Attribute> {
        return attributes.toList()
    }

    /**
     * Gets all child entities of this entity.
     *
     * @return The list of child entities.
     */
    fun getChildren(): List<Entity> {
        return children.toList()
    }

    /**
     * Clears all child entities of this entity.
     */
    fun clearChildren() {
        children.clear()
    }

    /**
     * Clears all attributes of this entity.
     */
    fun clearAttributes() {
        attributes.clear()
    }

    /**
     * Accepts a visitor for traversing the entity tree.
     *
     * @param visitor The visitor to accept.
     */
    fun acceptVisitor(visitor: Visitor) {
        visitor.visit(this)
        val childrenCopy = children.toList()
        childrenCopy.forEach { it.acceptVisitor(visitor) }
    }

    /**
     * Executes an XPath query on this entity.
     *
     * @param expression The XPath query expression.
     * @param visitor The visitor to apply to matching entities.
     */
    fun queryXPath(expression: String, visitor: Visitor) {
        val parts = expression.split("/")
        var currentEntities = listOf(this)
        for (part in parts) {
            currentEntities = currentEntities.flatMap { it.getChildren() }.filter { it.name == part }
        }
        currentEntities.forEach { it.acceptVisitor(visitor) }
    }


}