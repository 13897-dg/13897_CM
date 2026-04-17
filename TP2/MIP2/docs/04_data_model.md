# Data Model

The application parses data mapping to the JSON structure provided by the Picsum Photos API.

## ImageItem
```kotlin
data class ImageItem(
    val id: String,
    val author: String,
    val width: Int,
    val height: Int,
    val url: String,
    val download_url: String 
)
```
*(Only `id`, `author`, and `download_url` will be primarily used for the RecyclerView UI)*
