package uk.gov.justice.digital.hmpps.healthandmedication.utils

class Pagination {
  data class PaginationMetadata(
    val first: Boolean,
    val last: Boolean,
    val numberOfElements: Int,
    val offset: Int,
    val pageNumber: Int,
    val size: Int,
    val totalElements: Int,
    val totalPages: Int,
  )

  data class PaginatedCollection<TCollection>(
    val content: List<TCollection>,
    val metadata: PaginationMetadata,
  )

  companion object {
    fun <TCollection> paginateCollection(
      page: Int,
      pageSize: Int,
      collection: List<TCollection>,
    ): PaginatedCollection<TCollection> {
      val startIndex = (page - 1) * pageSize
      val lastIndex = (startIndex + pageSize - 1).coerceAtMost(collection.size - 1)
      val content = collection.slice(startIndex..lastIndex)

      return PaginatedCollection(
        content,
        PaginationMetadata(
          first = startIndex == 0,
          last = (lastIndex + 1) >= collection.size,
          numberOfElements = content.size,
          offset = startIndex,
          pageNumber = page,
          size = pageSize,
          totalElements = collection.size,
          totalPages = Math.ceilDiv(collection.size, pageSize).coerceAtLeast(1),
        ),
      )
    }
  }
}
