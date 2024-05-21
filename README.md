# Biblioteca de Manipulação de XML em Kotlin

## Descrição do Projeto

Esta biblioteca foi desenvolvida como parte do Projeto de Programação Avançada 2023/2024 e tem como objetivo fornecer uma API para a geração e manipulação de documentos XML em Kotlin. A biblioteca permite criar, manipular e exportar documentos XML, abrangendo os elementos essenciais como documentos, entidades (tags), entidades e atributos.

## Funcionalidades

### Modelo

As classes da biblioteca permitem as seguintes operações:

1. Adicionar e remover entidades
2. Adicionar, remover e alterar atributos em entidades
3. Acessar a entidade mãe e entidades filhas de uma entidade
4. Pretty print em formato de String e escrita para ficheiro
5. Varrimento do documento com objetos visitantes (Visitor)
6. Adicionar atributos globalmente ao documento
7. Renomeação de entidades globalmente ao documento
8. Renomeação de atributos globalmente ao documento
9. Remoção de entidades globalmente ao documento
10. Remoção de atributos globalmente ao documento

### Micro-XPath

A biblioteca permite pesquisar no documento com expressões XPath simples, compostas por uma sequência de nomes de entidades.

### Mapeamento Classes-XML

A biblioteca oferece uma forma de obtenção automática de entidades XML a partir de objetos Kotlin, com base na estrutura das classes e personalização por meio de anotações.

### DSL Interna (opcional)

Disponibiliza uma API que facilita a instanciação de modelos XML por meio de uma DSL interna em Kotlin.

## Exemplo de Uso

```kotlin
import java.io.File

// Exemplo de uso da biblioteca

val documento = Document("Plano") {
    tag(FUC("123", "Programação Avançada", 6.0, "Observações", listOf(
        ComponenteAvaliacao("Quizzes", 20),
        ComponenteAvaliacao("Projeto", 80)
    )))
}

println(documento.prettyPrint())
documento.writeToFile("output.xml")
```

### Anotações Disponíveis
@XmlAdapter
Especifica um adaptador para a entidade.

@XmlString
Especifica um transformador de string para atributos.

@XmlElementName
Especifica o nome do elemento XML para propriedades.

@XmlAttribute
Marca uma propriedade como atributo XML.

@XmlContent
Marca uma propriedade como conteúdo XML.

@XmlBuild
Especifica um adaptador para a construção de uma propriedade XML.

Transformadores de String
DefaultStringTransformer
Transforma o valor para string por omissão.

AddPercentage
Adiciona um símbolo de porcentagem ao valor.

Adaptadores de Entidades
XmlEntityAdapter
Interface para definir adaptadores de entidades.


### Visitors
Interface para visitantes de entidades.

Implementações de Visitors
XPathPrintVisitor
EntityRemoverVisitor
EntityRenamerVisitor
AttributeAdderVisitor
AttributeRemoverVisitor
AttributeRenamerVisitor


Testes
Todas as funcionalidades da biblioteca foram testadas utilizando JUnit. Os testes garantem que todas as operações são realizadas conforme esperado.

