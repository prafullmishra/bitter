package `in`.wordmug.bitter

import `in`.wordmug.bitter.DataUtils.extractMP4
import `in`.wordmug.bitter.DataUtils.getShortenedCount
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class DatautilsTestUnit {

    @Test
    fun shortenedCountTest()
    {
        assertEquals("100k", getShortenedCount(100000L))
        assertEquals("0", getShortenedCount(0L))
        assertEquals("56", getShortenedCount(56L))
        assertEquals("2.3M", getShortenedCount(2300000L))
        assertEquals("5.6B", getShortenedCount(5600000000L))
    }

    /**
     * Similarly test cases for other Util functions can be created
     */

}