package com.example.accessingdataneo4j

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.config.EnableReactiveNeo4jRepositories
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@SpringBootApplication
@EnableReactiveNeo4jRepositories
class AccessingDataNeo4jApplication

fun main(args: Array<String>) {
    runApplication<AccessingDataNeo4jApplication>(*args)
}

@org.springframework.stereotype.Service
class Service(
    parentRepository: ParentRepository
) {

    init {

        parentRepository.save(Parent().apply {
            name = "Foo"
            children.add(Child().apply { name = "Bar" })
            children.add(Child().apply { name = "Pop" })
        })

        val parents = parentRepository.getParent("Foo")

        println(parents.joinToString("\n") { parent ->
            "${parent.name}, children: ${parent.children.joinToString(", ") { child -> child.name!! }}"
        })
    }
}

@Node
class Parent {

    @Id
    @GeneratedValue
    var id: Long? = null

    var name: String? = null

    @Relationship(type = "HAS_CHILD", direction = Relationship.Direction.OUTGOING)
    var children: MutableList<Child> = mutableListOf()

}

@Node
class Child {

    @Id
    @GeneratedValue
    var id: Long? = null

    var name: String? = null
}

@Repository
interface ParentRepository : Neo4jRepository<Parent, Long> {

    @Query(
        """
            match (p:Parent { name: ${'$'}name })-[has]->(c:Child)
            return p, has, c
        """
    )
    fun getParent(@Param("name") name: String): List<Parent>
}
