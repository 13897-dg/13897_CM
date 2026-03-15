package cm_library

class PhysicalBook(
    title: String,
    author: String,
    year: Int,
    initialCopies: Int,
    val weight: Int,
    val hasHardcover: Boolean = true
) : Book(title, author, year, initialCopies) {

    override fun getStorageInfo(): String {
        val cover = if (hasHardcover) "Sim" else "Não"
        return "Livro físico: ${weight}g, Capa dura: $cover"
    }

    override fun toString(): String {
        return super.toString() + " -> [${getStorageInfo()}]"
    }
}