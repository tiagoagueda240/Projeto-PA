import org.junit.*
import org.junit.Assert.*



class TestFileCompose {

    object AddPercentage : StringTransformer {
        override fun transform(value: Any?): String {
            return value.toString() + "%"
        }
    }

    // Classe ComponenteAvaliacao
    @XmlAdapter(OrderedAttributes::class)
    class ComponenteAvaliacao(
        /*@XmlId("")*/@XmlBuild(AddAsAttribute::class) val nome: String,
        @XmlId("Tiago") @XmlString(AddPercentage::class) @XmlBuild(AddAsAttribute::class) val peso: Int
    )

    // Classe FUC
    @XmlAdapter(FUCRename::class)
    class FUC(
        @XmlBuild(AddAsAttribute::class) val codigo: String,
        @XmlBuild(AddAsContent::class) val nome: String,
        @XmlBuild(AddAsContent::class) val ects: Double,
        val observacoes: String,
        val avaliacao: List<ComponenteAvaliacao>
    )

    object FUCRename : XmlEntityAdapter {
        override fun adapt(entity: Entity) {
            entity.rename("FUC2")
        }
    }

    object FUCOrdered : XmlEntityAdapter {
        override fun adapt(entity: Entity) {
            val sortedChildren = entity.getChildren().sortedByDescending { it.name }
            entity.clearChildren()
            sortedChildren.forEach { entity.addChild(it) }
        }
    }

    object OrderedAttributes : XmlEntityAdapter {
        override fun adapt(entity: Entity) {
            val sortedAttributes = entity.getAttributes().sortedByDescending { it.name }
            entity.clearAttributes()
            sortedAttributes.forEach { entity.addAttributeAsObject(it) }
        }
    }


    private fun createDocumentWithEntities(): Document {
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

        return tab
    }

    @Test
    fun testAddEntity() {
        val document = createDocumentWithEntities()
        assertEquals(3, document.rootEntity.getChildren().size)
        assertEquals("FUC2", document.rootEntity.getChildren()[0].name)
    }

    @Test
    fun testRemoveEntity() {
        val document = createDocumentWithEntities()
        document.removeEntity("FUC")
        assertEquals(3, document.rootEntity.getChildren().size)
    }

    @Test
    fun testAddAttribute() {
        val document = createDocumentWithEntities()
        val fucEntity = Entity("FUC")
        fucEntity.addAttribute("codigo", "M4310")
        document.rootEntity.addChild(fucEntity)
        assertEquals(1, fucEntity.getAttributes().size)
        assertEquals("codigo", fucEntity.getAttributes()[0].name)
        assertEquals("M4310", fucEntity.getAttributes()[0].value)
    }

    @Test
    fun testRemoveAttribute() {
        val document = createDocumentWithEntities()
        val fucEntity = Entity("FUC")
        fucEntity.addAttribute("codigo", "M4310")
        document.rootEntity.addChild(fucEntity)
        document.removeAttribute("FUC", "codigo")
        assertTrue(fucEntity.getAttributes().isEmpty())
    }

    @Test
    fun testPrettyPrint() {
        val document = createDocumentWithEntities()
        println(document.prettyPrint())
    }

    @Test
    fun testXPathQueryForComponenteAvaliacao() {
        val document = createDocumentWithEntities()
        val visitor = XPathPrintVisitor()
        document.xpath("Plano/FUC2/avaliacao/ComponenteAvaliacao")
    }




}
