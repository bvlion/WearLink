package info.bvlion.wearlink.data

object Constant {
  const val MAX_SYNC_COUNT = 4

  enum class BodyType {
    FORM_PARAMS,
    JSON,
    QUERY
  }

  enum class HttpMethod {
    GET, POST, PUT, PATCH, DELETE;
  }
}