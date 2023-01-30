fun iterator(map: Map<String, Int>) {
    for (elem in map) {
        println(
            if (elem.value % 3 == 0) {
                "Fizz"
            } else if (elem.value % 5 == 0) {
                "Buzz"
            } else {
                "FizzBuzz"
            }
        )
    }
}