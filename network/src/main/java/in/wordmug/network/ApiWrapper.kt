package `in`.wordmug.network

class ApiWrapper {
    suspend fun getInitToken(): String
    {
        return WebApi.retrofitService.getInitToken().await().string()
    }
}