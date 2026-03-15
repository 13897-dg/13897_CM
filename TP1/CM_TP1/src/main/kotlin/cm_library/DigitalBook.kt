package cm_library

class DigitalBook(
    title: String,
    author: String,
    year: Int,
    initialCopies: Int,
    val fileSize: Double,
    val format: String
) : Book(title, author, year, initialCopies) {

    override fun getStorageInfo(): String {
        return "Armazenado digitalmente: $fileSize MB, Formato: $format"
    }

    override fun toString(): String {
        return super.toString() + " -> [${getStorageInfo()}]"
    }
}