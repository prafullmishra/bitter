package `in`.wordmug.network

class Header (val key: String, var value: String)
{
    override fun equals(other: Any?): Boolean {
        return if(other is Header) (other.key == this.key && other.value == this.value)
        else false
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }

    override fun toString(): String {
        return "$key = $value"
    }
}