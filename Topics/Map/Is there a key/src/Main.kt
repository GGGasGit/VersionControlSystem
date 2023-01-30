fun containsKeyAndValue(map: Map<String, String>, value: String): Boolean {
    return map.containsKey(value) && map.containsValue(value)
}