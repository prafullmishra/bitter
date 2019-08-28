package `in`.wordmug.network

import org.junit.Test
import org.junit.Assert.*
import java.util.HashMap

class WrapperTestUnit
{
    private val wrapper = TwitterWrapper.getInstance("token","verifier")

    @Test
    fun testPercentEncode()
    {
        assertEquals("Hello%20world%21", wrapper.percentEncode("Hello world!"))
        assertEquals("%24%25%40%24%5E%26-%3D%2F%28%29",wrapper.percentEncode("\$%@\$^&-=/()"))
        assertEquals("~%60%5B%5D%28%29", wrapper.percentEncode("~`[]()"))
    }
}