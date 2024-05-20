import org.junit.*
import org.junit.Assert.*

class TestFileCompose {
    @Test
    fun testAddAndRemoveEntity() {
        val document = Document()
        val entity = Entity("testEntity")

        document.rootEntity.addChild(entity)
        assertEquals(1, document.rootEntity.getChildren().size)

        val entityRemoverVisitor = EntityRemoverVisitor("testEntity")
        document.rootEntity.acceptVisitor(entityRemoverVisitor)
        assertEquals(0, document.rootEntity.getChildren().size)
    }

    @Test
    fun testAddRemoveAndModifyAttributes() {
        val document = Document()
        val entity = Entity("testEntity")
        document.rootEntity.addChild(entity)

        val attributeAdderVisitor = AttributeAdderVisitor("testEntity", "testAttribute", "value")
        document.rootEntity.acceptVisitor(attributeAdderVisitor)
        assertEquals("value", entity.getAttributeByName("testAttribute")?.value)

        val attributeRemoverVisitor = AttributeRemoverVisitor("testEntity", "testAttribute")
        document.rootEntity.acceptVisitor(attributeRemoverVisitor)
        assertNull(entity.getAttributeByName("testAttribute"))

        val attributeAdderVisitor2 = AttributeAdderVisitor("testEntity", "testAttribute", "value")
        document.rootEntity.acceptVisitor(attributeAdderVisitor2)
        val attribute = entity.getAttributeByName("testAttribute")
        attribute?.value = "modifiedValue"
        assertEquals("modifiedValue", entity.getAttributeByName("testAttribute")?.value)
    }

    @Test
    fun testAccessParentAndChildEntities() {
        val document = Document()
        val parentEntity = Entity("parentEntity")
        val childEntity = Entity("childEntity")

        parentEntity.addChild(childEntity)
        document.rootEntity.addChild(parentEntity)

        assertEquals("parentEntity", childEntity.parentEntity?.name)
        assertEquals("childEntity", parentEntity.getChildren()[0].name)
    }

    @Test
    fun testAttributeAddingVisitor() {
        val document = Document()
        val entity = Entity("curso")
        val visitor = AttributeAdderVisitor("curso", "novo_atributo", "valor_do_atributo")

        document.rootEntity.addChild(entity)
        entity.acceptVisitor(visitor)

        val addedAttribute = entity.getAttributes().find { it.name == "novo_atributo" }
        assertEquals("valor_do_atributo", addedAttribute?.value)
    }

    @Test
    fun testRenamingEntitiesVisitor() {
        val documento = Document()
        val curso = Entity("curso").apply { addContent("Mestrado em Engenharia Informática") }
        documento.rootEntity.addChild(curso)

        val entityRenamerVisitor = EntityRenamerVisitor("curso", "curso_novo")

        documento.rootEntity.acceptVisitor(entityRenamerVisitor)

        val renamedEntity = documento.rootEntity.getChildren().firstOrNull()

        assertEquals("curso_novo", renamedEntity?.name)
    }

    @Test
    fun testRenamingAttributesVisitor() {
        val documento = Document()
        val entity = Entity("test_entity")
        entity.addAttribute("old_attribute", "old_value")
        documento.rootEntity.addChild(entity)

        val visitor = AttributeRenamerVisitor("test_entity", "old_attribute", "new_attribute")
        documento.rootEntity.acceptVisitor(visitor)

        val renamedAttribute = documento.rootEntity.getChildren()[0].getAttributeByName("new_attribute")

        assertEquals("old_value", renamedAttribute?.value)
    }

}