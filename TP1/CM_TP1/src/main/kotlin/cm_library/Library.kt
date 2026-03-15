package cm_library

class Library (val name: String){
    private val books = mutableListOf<Book>()

    companion object {
        private var totalBooksAdded = 0

        fun getTotalBooksCreated(): Int {
            return totalBooksAdded
        }
    }

    fun addBook(book: Book) {
        books.add(book)
        totalBooksAdded++ // Aumenta a contagem global sempre que se adiciona um livro
        println("Livro adicionado à biblioteca: ${book.title}")
    }

    fun borrowBook(title: String) {
        val book = books.find { it.title.equals(title, ignoreCase = true) }
        if (book != null) {
            if (book.availableCopies > 0) {
                println("Livro '$title' emprestado com sucesso. Cópias disponíveis: ${book.availableCopies}")
                book.availableCopies -= 1
            } else {
                println("Erro: Não há cópias disponíveis do livro '$title'.")
            }
        } else {
            println("Erro: Livro '$title' não encontrado na biblioteca.")
        }
    }

    fun returnBook(title: String) {
        val book = books.find { it.title.equals(title, ignoreCase = true) }
        if (book != null) {
            book.availableCopies += 1
            println("Livro '$title' devolvido com sucesso. Cópias disponíveis: ${book.availableCopies}")
        } else {
            println("Erro: Livro '$title' não encontrado.")
        }
    }

    fun showBooks() {
        println("\n--- Catálogo da Biblioteca ---")
        if (books.isEmpty()) {
            println("A biblioteca está vazia.")
            return
        }
        for (book in books) {
            println(book)
        }
        println("------------------------------\n")
    }

    fun searchByAuthor(author: String) {
        val authorBooks = books.filter { it.author.equals(author, ignoreCase = true) }
        println("\n--- Resultados para o autor: $author ---")
        if (authorBooks.isEmpty()) {
            println("Nenhum livro encontrado.")
        } else {
            for (book in authorBooks) {
                println("- ${book.title} (Era: ${book.publicationYear}) Cópias disponíveis: ${book.availableCopies}")
            }
        }
        println("---------------------------------------\n")
    }
}