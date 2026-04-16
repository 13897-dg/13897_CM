class Pipeline {
    private val stages = mutableListOf<Pair<String, (List<String>) -> List<String>>>()
    fun addStage(name: String, transform: (List<String>) -> List<String>) {
        stages.add(Pair(name, transform))
    }
    fun describe() {
        println("Pipeline stages:")
        stages.forEachIndexed { index, stage -> println("${index+1}. ${stage.first}") }
    }

    fun execute(input: List<String>): List<String> {
        var currentData = input
        for (stage in stages) {
            currentData = stage.second(currentData)
        }
        return currentData
    }
}
fun buildPipeline(builder: Pipeline.() -> Unit): Pipeline {
    val pipeline = Pipeline()
    pipeline.builder()
    return pipeline
}

fun main() {
    val logs = listOf(
        " INFO : server started ",
        " ERROR : disk full ",
        " DEBUG : checking config ",
        " ERROR : out of memory ",
        " INFO : request received ",
        " ERROR : connection timeout "
    )

    val pipeline = buildPipeline {
        addStage("Trim") { input ->
            input.map { it.trim() }
        }

        addStage("Filter errors") { input ->
            input.filter { it.contains("ERROR") }
        }

        addStage("Uppercase") { input ->
            input.map { it.uppercase() }

        }

        addStage("Add index") { input ->
            input.mapIndexed { index, linha -> "${index+1}. ${linha}" }

        }
    }

    pipeline.describe()

    println("Result:")
    val finalResult = pipeline.execute(logs)

    finalResult.forEach { println(it) }
}