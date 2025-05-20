package dev.zorsh.engine

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.math.sqrt

operator fun Location.plus(l: Location): Location {
    return Location(this.world, this.x + l.x, this.y + l.y, this.z + l.z)
}

operator fun Location.minus(l: Location): Location {
    return Location(this.world, this.x - l.x, this.y - l.y, this.z - l.z)
}

operator fun Location.times(m: Double): Location {
    return Location(this.world, this.x * m, this.y * m, this.z * m)
}

operator fun Location.div(m: Double): Location {
    return Location(this.world, this.x / m, this.y / m, this.z / m)
}

class ZVector(var x: Double, var y: Double, var z: Double) {
    fun length(): Double {
        return sqrt(x*x + y*y + z*z)
    }

    constructor(location: Location): this(location.x, location.y, location.z) {}
    constructor(vector: ZVector): this(vector.x, vector.y, vector.z) {}
    constructor(vector: Vector): this(vector.x, vector.y, vector.z) {}

    operator fun times(m: Double): ZVector {
        return ZVector(x*m, y*m, z*m)
    }

    operator fun div(m: Double): ZVector {
        return ZVector(x/m, y/m, z/m)
    }

    operator fun plus(v: ZVector): ZVector {
        return ZVector(x+v.x, y+v.y, z+v.z)
    }

    operator fun minus(v: ZVector): ZVector {
        return ZVector(x-v.x, y-v.y, z-v.z)
    }
}

operator fun String.times(m: Int): String {
    if (m <= 0) {
        return ""
    }

    var res = this
    repeat(m-1) {
        res += this
    }
    return res
}

operator fun Location.plus(v: ZVector): Location {
    return Location(this.world, this.x + v.x, this.y + v.y, this.z + v.z, this.yaw, this.pitch)
}

operator fun Location.minus(v: ZVector): Location {
    return Location(this.world, this.x - v.x, this.y - v.y, this.z - v.z, this.yaw, this.pitch)
}

class ZVariablePointer(val target: String) {

}

class ZVariable(var type: String, var value: Any, var pointerString: String? = null) {
    companion object {
        @JvmStatic
        fun create(a: Any): ZVariable {
            when {
                a is Player -> return ZVariable(a)
                a is ZVector -> return ZVariable(a)
                a is Double -> return ZVariable(a)
                a is Location -> return ZVariable(a)
                a is World -> return ZVariable(a)
                a is String -> return ZVariable(a)
                a is Boolean -> return ZVariable(a)
                else -> throw IllegalArgumentException("Невозможно создать переменную из неопознанного класса")
            }
        }
    }

    constructor(p: Player) : this("Player", p) {}
    constructor(v: ZVector) : this("Vector", v) {}
    constructor(n: Double) : this("Number", n) {}
    constructor(l: Location) : this("Location", l) {}
    constructor(w: World) : this("World", w) {}
    constructor(s: String) : this("String", s) {}
    constructor(b: Boolean) : this("Boolean", b) {}
    constructor(z: ZVariablePointer) : this("Pointer", z) {}
    constructor(z: ZVariablePointer, s: String) : this("Pointer", z, s) {}

    override fun toString(): String {
        if (type == "Pointer") {
            if (pointerString == null) {
                throw IllegalArgumentException("Переменной с именем ${(value as ZVariablePointer).target} не существует")
            }
            return pointerString!!
        }
        return value.toString()
    }

    fun string(): String {
        return this.toString()
    }
}

fun Player.logZorshizen(text: String) {
    this.sendMessage("§e[Zorshizen]: §7$text")
}

enum class TokenType {
    VARIABLE,
    OPERATOR
//    OPERATOR,
//    FUNCTION
}

val OPERATOR_PRIORITIES = hashMapOf(
    Pair("=", -1),
    Pair("||", 1),
    Pair("&&", 2),
    Pair(">", 3),
    Pair("<", 3),
    Pair(">=", 3),
    Pair("<=", 3),
    Pair("==", 3),
    Pair("!=", 3),
    Pair("+", 4),
    Pair("-", 4),
    Pair("*", 5),
    Pair("/", 5),
    Pair("%", 6),
    Pair("$", 7),
    Pair("!", 7),
    Pair("(", -20),
    Pair(")", -19)
)

class ZorshizenToken() {
    var operatorValue: String? = null
    var variableValue: ZVariable? = null
    var type: TokenType? = null
    var priority: Int? = null

    constructor(text: String) : this() {
        operatorValue = text
        type = TokenType.OPERATOR

//        when (text.trim()) {
//            "=" -> type = TokenType.OPERATOR
//            "+" -> type = TokenType.OPERATOR
//            "-" -> type = TokenType.OPERATOR
//            "*" -> type = TokenType.OPERATOR
//            "/" -> type = TokenType.OPERATOR
//            else -> type = TokenType.VARIABLE
//        }

        priority = OPERATOR_PRIORITIES[text.trim()]
    }

    constructor(zvar: ZVariable) : this() {
        type = TokenType.VARIABLE
        variableValue = zvar
    }

    override fun toString(): String {
        return when (type) {
            TokenType.OPERATOR -> operatorValue ?: throw IllegalArgumentException("Невозможно преобразовать токен в строку")
            else -> variableValue.toString()
        }
    }
}

class ZorshizenInstruction(initialText: String) {
    var text = initialText.trim()

    override fun toString(): String {
        return this.text
    }
}

class ZorshizenError(val instructionNumber: Int, val message: String?, val instruction: String) {}

class SpellParsingResult(val isError: Boolean, val message: String?) {}

class ZorshizenFunction(initialText: String, val args: MutableList<ZVariable>) {
    val name = initialText.trim()

    override fun toString(): String {
        var res = name
        res += '('
        var first = true
        args.forEach { arg ->
            if (!first) {
                res += ", "
            }

            res += arg.toString()
            first = false
        }
        res += ')'
        return res
    }
}

class ActionParameters(val data: List<Any>) {
    operator fun get(i: Int): ZVariable {
        return ZVariable.create(data[i])
    }

    companion object {
        @JvmStatic
        fun build(vararg newData: Any): ActionParameters {
            val list = mutableListOf<Any>()
            newData.forEach { list.add(it) }
            return ActionParameters(list.toList())
        }
    }
}