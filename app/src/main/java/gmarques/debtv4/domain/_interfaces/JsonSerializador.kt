package gmarques.debtv4.domain._interfaces

interface JsonSerializador {

    fun <T> toJSon(objeto: T): String

    fun <T> fromJson(json: String, clazz: Class<T>): T
}