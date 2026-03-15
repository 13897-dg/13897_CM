package cm_library

data class LibraryMember(
    val name: String,
    val membershipId: String,
    val borrowedBooks: MutableList<String> = mutableListOf()
)