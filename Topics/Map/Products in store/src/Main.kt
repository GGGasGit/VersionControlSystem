fun bill(priceList: Map<String, Int>, shoppingList: MutableList<String>): Int {
    var totalPrice = 0
    for (product in shoppingList) {
        if (priceList.containsKey(product)) totalPrice += priceList[product]!!
    }
    return totalPrice
}