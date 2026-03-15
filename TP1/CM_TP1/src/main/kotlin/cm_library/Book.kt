package cm_library

abstract class Book(
    val title: String,
    val author: String,
    private val year: Int,
    initialCopies: Int
) {
    val publicationYear: String
        get() = when {
            year < 1980 -> "Classico"
            year in 1980..2010 -> "Moderno"
            else -> "Contemporário"
        }

    var availableCopies: Int = initialCopies
        set(value) {
            if (value < 0) {
                println("Erro: O número de cópias não pode ser negativo.")
            } else {
                field = value
                if (field == 0) {
                    println("Aviso: O livro está esgotado.")
                }
            }
        }

    init {
        println("Livro criado: '$title' por $author")
    }

    abstract fun getStorageInfo(): String

    override fun toString(): String {
        return "Livro (Título: '$title', Autor: '$author', Era: '$publicationYear', Cópias: $availableCopies)"
    }
}