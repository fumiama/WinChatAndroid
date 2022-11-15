package top.fumiama.winchatandroid.client

data class CommandType(val typ: Int) {
    constructor(typ: Byte): this(typ.toInt())
    val typB = typ.toByte()
}
