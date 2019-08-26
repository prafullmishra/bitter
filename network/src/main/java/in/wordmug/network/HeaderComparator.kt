package `in`.wordmug.network

class HeaderComparator : Comparator<Header>{

    override fun compare(o1: Header, o2: Header): Int {
        return "${o1.key}${o1.value}".compareTo("${o2.key}${o2.value}")
    }

}