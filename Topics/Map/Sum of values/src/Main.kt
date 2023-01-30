fun summator(map: Map<Int, Int>): Int {
    var sum = 0
    for ((key, value) in map) {
        sum += if (key % 2 == 0) value else 0
    }
    return sum
}