package dev.zorsh

import dev.mryd.Main
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.time.LocalTime
import kotlin.math.*

class ZorshizenParser(private val player: Player) {
    private val variables = HashMap<String, ZVariable>()
    private var delayPrecise: Long = 0
    private var delay: Int = 0
    private val startTime = LocalTime.now()
    private var calculations = 0
    private var prints = 0

    // =================================================================================================================

    private fun ZVariable.boolean(): Boolean {
        if (type == "Pointer")
            return get().boolean()
        if (value !is Boolean)
            throw  IllegalArgumentException("Не удалось привести переменную к типу Boolean")
        return (value as Boolean)
    }

    private fun ZVariable.player(): Player {
        if (type == "Pointer")
            return get().player()
        if (value !is Player)
            throw  IllegalArgumentException("Не удалось привести переменную к типу Player")
        return (value as Player)
    }

    private fun ZVariable.vector(): ZVector {
        if (type == "Pointer")
            return get().vector()
        if (type == "Location") {
            val loc = (value as Location)
            return ZVector(loc.x, loc.y, loc.z)
        }
        if (value !is ZVector)
            throw  IllegalArgumentException("Не удалось привести переменную к типу Vector")
        return (value as ZVector)
    }

    private fun ZVariable.number(): Double {
        if (type == "Pointer")
            return get().number()
        if (value !is Double)
            throw  IllegalArgumentException("Не удалось привести переменную к типу Number")
        return (value as Double)
    }

    private fun ZVariable.location(): Location {
        if (type == "Pointer")
            return get().location()
        if (value !is Location)
            throw  IllegalArgumentException("Не удалось привести переменную к типу Location")
        return (value as Location)
    }

    private fun ZVariable.world(): World {
        if (type == "Pointer")
            return get().world()
        if (value !is World)
            throw  IllegalArgumentException("Не удалось привести переменную к типу World")
        return (value as World)
    }

    // =================================================================================================================

    fun ZorshizenToken.Apply(v1: ZVariable, v2: ZVariable): ZVariable {
        if (type != TokenType.OPERATOR) {
            throw IllegalArgumentException("Невозможно применить оператор! Токен не является оператором")
        }
        calculations++
        return when (operatorValue) {
            "+" -> v1 + v2
            "-" -> v1 - v2
            "*" -> v1 * v2.number()
            "/" -> v1 / v2.number()
            "%" -> v1 % v2.number()
            ">" -> ZVariable(v1.number() > v2.number())
            "<" -> ZVariable(v1.number() < v2.number())
            ">=" -> ZVariable(v1.number() >= v2.number())
            "<=" -> ZVariable(v1.number() <= v2.number())
            "==" -> ZVariable(v1.string() == v2.string())
            "!=" -> ZVariable(v1.string() != v2.string())
            "&&" -> ZVariable(v1.boolean() && v2.boolean())
            "||" -> ZVariable(v1.boolean() || v2.boolean())
            "=" -> v1.set(v2)
            else -> throw IllegalArgumentException("Невозможно применить оператор '$operatorValue' к '$v1' и '$v2'")
        }
    }

    // =================================================================================================================

    fun ZVariable.get(): ZVariable {
        if (type != "Pointer")
            throw  IllegalArgumentException("Взять значение можно только у переменной")
        return variables[(value as ZVariablePointer).target] ?: throw IllegalArgumentException("Переменной с именем ${(value as ZVariablePointer).target} не существует")
    }

    fun ZVariable.set(new: ZVariable): ZVariable {
        if (type != "Pointer")
            throw  IllegalArgumentException("Присвоить значение можно только переменной")
        if (new.type == "Pointer") {
            variables[(value as ZVariablePointer).target] = new.get()
            pointerString = new.get().toString()
        } else {
            variables[(value as ZVariablePointer).target] = new
            pointerString = new.toString()
        }
        return this
    }

//    fun ZVariable.pointerType(): String {
//        return variables[(value as ZVariablePointer).target]?.type ?: throw IllegalArgumentException("Переменной с именем ${(value as ZVariablePointer).target} не существует")
//    }

    operator fun ZVariable.plus(v: ZVariable): ZVariable {
        when (type) {
            "Vector" -> return ZVariable(vector()+v.vector())
            "Number" -> return ZVariable(number()+v.number())
            "Location" -> return ZVariable(location()+v.vector())
            "String" -> return ZVariable(string()+v.string())
            "Pointer" -> return get() + v
        }
        throw IllegalArgumentException("Нельзя использовать + с переменной типа $type")
    }

    operator fun ZVariable.minus(v: ZVariable): ZVariable {
        when (type) {
            "Vector" -> return ZVariable(vector()-v.vector())
            "Number" -> return ZVariable(number()-v.number())
            "Location" -> return ZVariable(location()-v.vector())
            "Pointer" -> return get() - v
        }
        throw IllegalArgumentException("Нельзя использовать - с переменной типа $type")
    }

    operator fun ZVariable.times(m: Double): ZVariable {
        when (type) {
            "Vector" -> return ZVariable(vector()*m)
            "Number" -> return ZVariable(number()*m)
            "Location" -> return ZVariable(location()*m)
            "String" -> return ZVariable(string()*m.toInt())
            "Pointer" -> return get() * m
        }
        throw IllegalArgumentException("Нельзя использовать * с переменной типа $type")
    }

    operator fun ZVariable.div(m: Double): ZVariable {
        when (type) {
            "Vector" -> return ZVariable(vector()/m)
            "Number" -> return ZVariable(number()/m)
            "Location" -> return ZVariable(location()/m)
            "Pointer" -> return get() / m
        }
        throw IllegalArgumentException("Нельзя использовать / с переменной типа $type")
    }

    operator fun ZVariable.rem(m: Double): ZVariable {
        when (type) {
            "Vector" -> return ZVariable(ZVector(vector().x % m, vector().y % m, vector().z % m))
            "Number" -> return ZVariable(number() % m)
            "Location" -> return ZVariable(Location(location().world, vector().x % m, vector().y % m, vector().z % m))
            "Pointer" -> return get() % m
        }
        throw IllegalArgumentException("Нельзя использовать % с переменной типа $type")
    }

    // =================================================================================================================

    private fun delayZorshizen(duration: Long) {
//        working += duration
//        if (working > 100000) {
//            throw IllegalArgumentException("Превышено максимальное время работы программы (100 секунд)")
//        }
        delayPrecise += duration
        delayPrecise = max(0L, delayPrecise)
//        delay(duration)
    }

    // =================================================================================================================

    private fun drainMana(amount: Int) {
        if (!Main.mana.containsKey(player.name)) {
            Main.mana[player.name] = maxMana
        }
        if (Main.mana[player.name]!! < amount) {
            Main.mana[player.name] = 0
            throw IllegalArgumentException("Мана закончилась")
        }
        Main.mana[player.name] = Main.mana[player.name]!! - amount
    }

    private val functionManaCost = hashMapOf(
        Pair("Particle", 50),
        Pair("Location", 10),
        Pair("Vector", 10),
        Pair("Player", 100),
        Pair("print", 1),
        Pair("printf", 1)
    )

    private fun MutableList<ZVariable>.arg(n: Int): ZVariable {
        if (n < 0) {
            throw IllegalArgumentException("Hyi")
        }
        if (this.size < n+1) {
            throw IllegalArgumentException("Недостаточно аргументов для вызова функции")
        }
        return this[n]
    }

    private fun particle(particle: Particle, location: Location, velocity: ZVector) {
        location.world.players.forEach { pl ->
            if (pl.location.distance(location) < 100) {
                pl.spawnParticle(particle, location.x, location.y, location.z, 0, velocity.x, velocity.y, velocity.z, 1.0, null, true)
            }
        }
    }

    private fun Location.findPlayers(rad: Double = 0.5): List<Player> {
        return this.getNearbyPlayers(rad).toList()
    }

    private fun ZorshizenFunction.invoke(depth: Int = 0): ZVariable {
        val args = mutableListOf<ZVariable>()
        this.args.forEach { arg ->
            if (arg.type == "Pointer") {
                args.add(arg.get())
            } else {
                args.add(arg)
            }
        }
        calculations++
        if (functionManaCost.containsKey(name)) {
            drainMana(functionManaCost[name]!!)
        } else {
            drainMana(10)
        }
        when (name) {
            "Particle" -> {
                if (args.size < 3) {
                    throw IllegalArgumentException("Для функции Particle нужны 3 аргумента")
                }
                val pos = args[1].location()
                val vel = args[2].vector()
                when (args[0].toString().trim().lowercase()) {
                    "flame" -> {
                        particle(Particle.FLAME, pos, vel)
                    }
                    else -> throw IllegalArgumentException("Партикл ${args[0].toString().trim()} не найден")
                }
                return ZVariable(args[0].toString().trim())
            }
            "printf" -> {
                if (args.isEmpty()) {
                    throw IllegalArgumentException("Для функции printf нужен хотя бы 1 аргумент")
                }

                val delimeter = args[0].toString()
                var toPrint = ""
                var first = true
                args.drop(1).forEach { arg ->
                    if (!first) {
                        toPrint += delimeter
                    }

                    toPrint += arg.toString()
                    first = false
                }

                player.logZorshizen(toPrint)
                prints++
                if (prints >= 5) {
                    prints = 0
                    delayZorshizen(1)
                }

                return ZVariable(toPrint.length.toDouble())
            }
            "print" -> {
                var toPrint = ""
                var first = true
                args.forEach { arg ->
                    if (!first) {
                        toPrint += ", "
                    }

                    toPrint += arg.toString()
                    first = false
                }

                player.logZorshizen(toPrint)
                prints++
                if (prints >= 5) {
                    prints = 0
                    delayZorshizen(1)
                }

                return ZVariable(toPrint.length.toDouble())
            }
            "Player" -> {
                if (args.size != 1) {
                    throw IllegalArgumentException("Для функции Player нужен 1 аргумент")
                }
                val pl: Player = Bukkit.getPlayer(args[0].toString()) ?: throw IllegalArgumentException("Игрок с именем ${args[0]} не найден")
                return ZVariable(pl)
            }
            "Cast" -> {
                val name = args.arg(0).toString().lowercase()
                when (name) {
                    "barrier" -> {
                        drainMana(100)
                        val loc = args.arg(1).location()
                        repeat(2) {
                            particle(Particle.END_ROD, loc, ZVector(0.0, 0.0, 0.0))
                        }
                        loc.findPlayers().forEach { trg ->
                            if (trg != player) {
                                var dir = ZVector(trg.location + ZVector(0.0, 1.0, 0.0) - loc)
                                dir = dir / dir.length() * 0.3
                                trg.velocity = Vector(dir.x, dir.y, dir.z)
                            }
                        }
                    }
                }
                return ZVariable(name)
            }
            "Vector" -> {
                if (args.size != 3) {
                    throw IllegalArgumentException("Для функции Vector нужны 3 аргумента")
                }
                return ZVariable(ZVector(args[0].number(), args[1].number(), args[2].number()))
            }
            "World" -> {
                if (args.size != 1) {
                    throw IllegalArgumentException("Для функции World нужен 1 аргумент")
                }
                val w: World = Bukkit.getWorld(args[0].toString()) ?: throw IllegalArgumentException("Мир с именем ${args[0]} не найден")
                return ZVariable(w)
            }
            "wait" -> {
                if (args.size != 1) {
                    throw IllegalArgumentException("Для функции wait нужен 1 аргумент")
                }
                val duration = args[0].number().toLong()
                delayZorshizen(duration)
                return ZVariable(duration.toDouble())
            }
            "name" -> {
                if (args.size != 1) {
                    throw IllegalArgumentException("Для функции name нужен 1 аргумент")
                }
                return ZVariable(args[0].player().name)
            }
            "cot" -> {
                if (args.size != 1) {
                    throw IllegalArgumentException("Для функции cos нужен 1 аргумент")
                }
                return ZVariable(1.0 / tan(args[0].number()))
            }
            "tan" -> {
                if (args.size != 1) {
                    throw IllegalArgumentException("Для функции cos нужен 1 аргумент")
                }
                return ZVariable(tan(args[0].number()))
            }
            "cos" -> {
                if (args.size != 1) {
                    throw IllegalArgumentException("Для функции cos нужен 1 аргумент")
                }
                return ZVariable(cos(args[0].number()))
            }
            "sin" -> {
                if (args.size != 1) {
                    throw IllegalArgumentException("Для функции sin нужен 1 аргумент")
                }
                return ZVariable(sin(args[0].number()))
            }
            "round" -> {
                if (args.size != 1) {
                    throw IllegalArgumentException("Для функции round нужен 1 аргумент")
                }
                return ZVariable(round(args[0].number()))
            }
            "length" -> {
                if (args.size != 1) {
                    throw IllegalArgumentException("Для функции length нужен 1 аргумент")
                }
                return ZVariable(args[0].vector().length())
            }
            "pitch" -> {
                if (args.size != 1) {
                    throw IllegalArgumentException("Для функции pitch нужен 1 аргумент")
                }
                return ZVariable(args[0].location().pitch.toDouble())
            }
            "yaw" -> {
                if (args.size != 1) {
                    throw IllegalArgumentException("Для функции yaw нужен 1 аргумент")
                }
                return ZVariable(args[0].location().yaw.toDouble())
            }
            "x" -> {
                if (args.size != 1) {
                    throw IllegalArgumentException("Для функции X нужен 1 аргумент")
                }
                return ZVariable(args[0].vector().x)
            }
            "y" -> {
                if (args.size != 1) {
                    throw IllegalArgumentException("Для функции X нужен 1 аргумент")
                }
                return ZVariable(args[0].vector().y)
            }
            "z" -> {
                if (args.size != 1) {
                    throw IllegalArgumentException("Для функции X нужен 1 аргумент")
                }
                return ZVariable(args[0].vector().z)
            }
            "locationX" -> {
                return ZVariable(ZorshizenFunction("Location", args).invoke(depth).location().x)
            }
            "locationY" -> {
                return ZVariable(ZorshizenFunction("Location", args).invoke(depth).location().y)
            }
            "locationZ" -> {
                return ZVariable(ZorshizenFunction("Location", args).invoke(depth).location().z)
            }
            "direction" -> {
                if (args.isEmpty()) {
                    throw IllegalArgumentException("Для функции direction необходим 1 аргумент")
                }
                val dir = args[0].location().direction
                return ZVariable(ZVector(dir.x, dir.y, dir.z))
            }
            "Location" -> {
                if (args.size == 1 && args[0].type == "Player") {
                    return ZVariable(args[0].player().location)
                }

                if (args.size == 2 && args[0].type == "Vector" && args[1].type == "World") {
                    return ZVariable(Location(args[1].world(), args[0].vector().x, args[0].vector().y, args[0].vector().z))
                }

                if (args.size == 4 && args[0].type == "Number" && args[1].type == "Number" && args[2].type == "Number" && args[3].type == "World") {
                    return ZVariable(Location(args[3].world(), args[0].number(), args[1].number(), args[2].number()))
                }

                if (args.size == 6 && args[0].type == "Number" && args[1].type == "Number" && args[2].type == "Number" && args[3].type == "Number" && args[4].type == "Number" && args[5].type == "World") {
                    return ZVariable(Location(args[5].world(), args[0].number(), args[1].number(), args[2].number(), args[3].number().toFloat(), args[4].number().toFloat()))
                }

                throw IllegalArgumentException("Использование: Location(Player) или Location(Vector, World), Location(Number(x), Number(y), Number(z), World) или Location(Number(x), Number(y), Number(z), Number(yaw), Number(pitch), World)")
            }
            else -> throw IllegalArgumentException("Функция $name не найдена!")
        }
    }

    private fun String.findEnd(start: Char, end: Char): Int? {
        var skobko = 0
        var ind = 0
        var kav = false
        for (c in this) {
            if (c == '"') {
                kav = !kav
            }
            if (c == start && !kav) {
                skobko++
            }
            if (c == end && !kav) {
                skobko--
            }
            if (c == end && !kav && skobko <= 0) {
                return ind
            }
            ind++
        }
        return null
    }

    private fun String.findKav(trg: Char): Int {
        var ind = 0
        var kav = false
        for (c in this) {
            if (c == '"') {
                kav = !kav
            }
            if (c == trg && !kav) {
                return ind
            }
            ind++
        }
        return -1
    }

    // =================================================================================================================

    private fun ZorshizenInstruction.convertToExpression(): MutableList<ZorshizenToken> {
        val result = mutableListOf<ZorshizenToken>()
        val operators = mutableListOf<ZorshizenToken>()

        var prev = 'O'
        var num: Double? = null
        var divider = 1
        var divide = false
        var charIndex = 0
        var skobko = 0
        var skip = 0
        for (c in text) {
            if (skip > 0) {
                skip--
                charIndex++
                continue
            }

            if (text.substring(charIndex).startsWith("true")) {
                result.add(ZorshizenToken(ZVariable(true)))
                skip = 3
                prev = 'V'
                charIndex++
                continue
            }

            if (text.substring(charIndex).startsWith("false")) {
                result.add(ZorshizenToken(ZVariable(false)))
                skip = 4
                prev = 'V'
                charIndex++
                continue
            }

            val varStart = '['
            val varEnd = ']'

            if (c == varStart) {
                val str = text.substring(charIndex)
                if (str.length < 2) {
                    throw IllegalArgumentException("Имя переменной должно закрываться символом '$varEnd'")
                }
                if (!str.substringAfter(varStart).contains(varEnd)) {
                    throw IllegalArgumentException("Имя переменной должно закрываться символом '$varEnd'")
                }
                val ind: Int = str.findEnd('[', ']') ?: throw IllegalArgumentException("Имя переменной должно закрываться символом '$varEnd'")
                val txt = str.substring(1, ind)
                if (txt.trim().isEmpty()) {
                    throw IllegalArgumentException("Имя переменной не может быть пустым")
                }
                if (txt[0] == varStart && txt.last() == varEnd) {
                    var eval = txt.substring(1, txt.length-1)
                    if (eval.trim().isEmpty()) {
                        throw IllegalArgumentException("Имя переменной не может быть пустым")
                    }
                    eval = evaluate(ZorshizenInstruction(eval).convertToExpression()).toString()
                    if (eval.trim().isEmpty()) {
                        throw IllegalArgumentException("Имя переменной не может быть пустым")
                    }
                    if (eval.contains(' ')) {
                        throw IllegalArgumentException("Имя переменной не должно содержать пробелов")
                    }
                    if (eval.contains('.') || eval.contains(',') || eval.contains(';') || eval.contains('{') || eval.contains('}') || eval.contains(':') || eval.contains('%') || eval.contains('^') || eval.contains('+') || eval.contains('-') || eval.contains('*') || eval.contains('/')) {
                        throw IllegalArgumentException("Имя переменной не должно содержать специальных символов")
                    }
                    if (variables.containsKey(eval)) {
                        result.add(ZorshizenToken(ZVariable(ZVariablePointer(eval), variables[eval].toString())))
                    } else {
                        result.add(ZorshizenToken(ZVariable(ZVariablePointer(eval))))
                    }
                    skip = txt.length + 2
                } else {
                    if (txt.contains(' ')) {
                        throw IllegalArgumentException("Имя переменной не должно содержать пробелов")
                    }
                    if (txt.contains('.') || txt.contains(',') || txt.contains(';') || txt.contains('{') || txt.contains('}') || txt.contains(':') || txt.contains('%') || txt.contains('^') || txt.contains('+') || txt.contains('-') || txt.contains('*') || txt.contains('/')) {
                        throw IllegalArgumentException("Имя переменной не должно содержать специальных символов")
                    }
                    if (variables.containsKey(txt)) {
                        result.add(ZorshizenToken(ZVariable(ZVariablePointer(txt), variables[txt].toString())))
                    } else {
                        result.add(ZorshizenToken(ZVariable(ZVariablePointer(txt))))
                    }
                    skip = txt.length + 1
                }

                prev = 'V'
                charIndex++
                continue
            }

            if (c == '"') {
                val str = text.substring(charIndex)
                if (str.length < 2) {
                    throw IllegalArgumentException("Текст должен закрываться кавычками")
                }
                if (!str.substringAfter('"').contains('"')) {
                    throw IllegalArgumentException("Текст должен закрываться кавычками")
                }
                val txt = str.substringAfter('"').substringBefore('"')
                result.add(ZorshizenToken(ZVariable(txt)))
                skip = txt.length+1

                prev = 'T'
                charIndex++
                continue
            }

            if (!c.isDigit() && c != '.' && prev == 'N' && num != null) {
                result.add(ZorshizenToken(ZVariable(num / divider)))
                divide = false
                divider = 1
                num = null
            }

            if (text.length > charIndex+1 && OPERATOR_PRIORITIES.containsKey(text.substring(charIndex, charIndex+2)) && prev != 'O' && prev != 's' && c != ')' && c != '(') {
                val t = ZorshizenToken(text.substring(charIndex, charIndex+2))
                run {
                    while (operators.isNotEmpty() && t.priority!! <= operators.last().priority!!) {
                        result.add(operators.last())
                        operators.removeLast()
                    }
                }
                operators.add(t)
                prev = 'O'

                skip = 1
                charIndex++
                continue
            } else if (c == '-' && (prev == 'O' || prev == 's')) {
//                player.logZorshizen("Dolboeb")
                val t = ZorshizenToken("$")
                operators.add(t)

                prev = 'O'
                charIndex++
                continue
            } else if (c == '!' && (prev == 'O' || prev == 's')) {
//                player.logZorshizen("Dolboeb")
                val t = ZorshizenToken("!")
                operators.add(t)

                prev = 'O'
                charIndex++
                continue
            } else if (!c.isDigit() && !OPERATOR_PRIORITIES.containsKey("$c") && (prev == 'O' || prev == 's') && c != ' ' && c != '.' && c != '(' && c != ')' && c != '>') {
                val str = text.substring(charIndex)
                if (str.contains('(') && !str.substringBefore('(').contains(' ')) {
                    val func = str.substringBefore('(')

                    val after = str.substringAfter(func)
                    val end = after.findEnd('(',  ')') ?: throw IllegalArgumentException("Вызов функции должен оканчиваться символом ')' ($text) ($after)")

                    var argsText = after.substringAfter('(').substring(0, end-1)
                    //player.logZorshizen("ABOBA: $argsText")
                    skip = -1 + func.length + 2 + argsText.length
                    argsText = argsText.trim()

                    val args = mutableListOf<ZVariable>()
                    var arg = ""
                    var kav = false
                    skobko = 0
                    for (argC in argsText) {
                        if (argC == '"') {
                            kav = !kav
                        }
                        if (argC == '(' && !kav) {
                            skobko++
                        }
                        if (argC == ')' && !kav) {
                            skobko--
                        }
                        if (argC == ',' && !kav && skobko <= 0) {
                            if (arg.trim().isEmpty()) {
                                throw IllegalArgumentException("Аргумент функции не может быть пустым выражением ($func)")
                            }

                            args.add(evaluate(ZorshizenInstruction(arg.trim()).convertToExpression()))
                            arg = ""
                        } else {
                            arg += argC
                        }
                    }
                    if (arg.trim().isNotEmpty()) {
                        args.add(evaluate(ZorshizenInstruction(arg.trim()).convertToExpression()))
                    }
//                    if (argsText.isNotEmpty()) {
//                        val toParse = argsText.split(',')
//                        toParse.forEach { arg ->
//                            if (arg.trim().isEmpty()) {
//                                throw IllegalArgumentException("Аргумент функции не может быть пустым выражением ($func)")
//                            }
//
//                            args.add(evaluate(ZorshizenInstruction(arg.trim()).convertToExpression(player), player))
//                        }
//                    }

                    result.add(ZorshizenToken(ZorshizenFunction(func, args).invoke(depth=0)))
                    prev = 'F'

                    charIndex++
                    continue
                }
            }

            if (c == '.' && prev == 'N' && !divide) {
                divide = true
            } else if (c.isDigit() && (prev == 'O' || prev == 'N' || prev == 's')) {
                if (prev != 'N' || num == null) {
                    num = 0.0
                }
                if (divide) {
                    divider *= 10
                }
                num *= 10
                num += c.code - '0'.code
                prev = 'N'
            } else if (OPERATOR_PRIORITIES.containsKey("$c") && prev != 'O' && prev != 's' && c != ')' && c != '(') {
                val t = ZorshizenToken("$c")
                run {
                    while (operators.isNotEmpty() && t.priority!! <= operators.last().priority!!) {
                        result.add(operators.last())
                        operators.removeLast()
                    }
                }
                operators.add(t)
                prev = 'O'
            } else if (c == '(' && (prev == 'O' || prev == 's')) {
                val t = ZorshizenToken("$c")
                operators.add(t)
                prev = 's'
                skobko++
            } else if (c == ')' && prev != 'O' && skobko > 0) {
                val t = ZorshizenToken("$c")
                run {
                    while (operators.isNotEmpty() && t.priority!! <= operators.last().priority!!) {
                        result.add(operators.last())
                        operators.removeLast()
                    }
                    operators.removeLast()
                }
                prev = 'S'
                skobko--
            } else if (c != ' ' && c != '{' && c != '}') {
                val instrCol = "§e"
                val errorCol = "§c§l§n"
                if (text.length == 1) {
                    throw IllegalArgumentException("Невозможно получить токены из инструкции $errorCol$c")
                } else if (charIndex == 0) {
                    throw IllegalArgumentException(
                        "Невозможно получить токены из инструкции $errorCol$c$instrCol${
                            text.substring(
                                1
                            )
                        }"
                    )
                } else if (charIndex == text.length - 1) {
                    throw IllegalArgumentException(
                        "Невозможно получить токены из инструкции $instrCol${
                            text.substring(
                                0,
                                charIndex
                            )
                        }$errorCol$c"
                    )
                } else {
                    throw IllegalArgumentException(
                        "Невозможно получить токены из инструкции $instrCol${
                            text.substring(
                                0,
                                charIndex
                            )
                        }$errorCol$c$instrCol${text.substring(charIndex + 1)}"
                    )
                }
            }
            charIndex++
        }
        if (num != null) {
            result.add(ZorshizenToken(ZVariable(num / divider)))
        }
        run {
            while (operators.isNotEmpty()) {
                result.add(operators.last())
                operators.removeLast()
            }
        }
        return result
    }

    private fun evaluate(tokens: MutableList<ZorshizenToken>): ZVariable {
        if (calculations > 1000) {
            calculations = 0
            delayZorshizen(5)
        }
//        tokens.forEach { t ->
//            if (t.type == TokenType.VARIABLE) {
//                player.logZorshizen("Token: ${t.variableValue}")
//            } else {
//                player.logZorshizen("Token: ${t.operatorValue}")
//            }
//        }
        val stack = mutableListOf<ZVariable>()
        for (t in tokens) {
            if (t.type == TokenType.VARIABLE) {
                stack.add(t.variableValue!!)
            } else if (t.type == TokenType.OPERATOR) {
                if (t.operatorValue == "$") {
                    val v = stack.last()
                    stack.removeLast()
                    val eval = ZVariable(v.number() * -1)
                    stack.add(eval)
                } else if (t.operatorValue == "!") {
                    val v = stack.last()
                    stack.removeLast()
                    val eval = ZVariable(!v.boolean())
                    stack.add(eval)
                } else {
                    if (stack.size < 2) {
                        throw IllegalArgumentException("Невозможно вычислить выражение: недостаточно элементов для выполнения операции '${t.operatorValue}'")
                    }
                    val v2 = stack.last()
                    stack.removeLast()
                    val v1 = stack.last()
                    stack.removeLast()
                    val eval = t.Apply(v1, v2)
                    stack.add(eval)
                }
            }
        }
        if (stack.isEmpty()) {
            throw IllegalArgumentException("Невозможно вычислить пустое выражение")
        }
        return stack.last()
    }

    // =================================================================================================================

    private fun parseInstruction(instructionText: String) {
        if (instructionText.replace('{', ' ').replace('}', ' ').trim().isNotEmpty()) {
            val instruction = ZorshizenInstruction(instructionText)
            val expression = instruction.convertToExpression()
            evaluate(expression)
        }
    }

    private var skipInstructions = 0

    private var error: ZorshizenError? = null
    private var active = true

    private fun checkStatement(instruction: String, afterInstructions: List<String>): Pair<String, Pair<Int, String>>? {
        if (instruction.startsWith(':')) {
            return null
        }
        if (instruction.startsWith("if") &&
            instruction.substringAfter("if").contains('(') &&
            instruction.substringAfter("if").substringBefore('(').trim().isEmpty()) {
            val after = instruction.substringAfter("if").trim()
            val end = after.findEnd('(',  ')') ?: throw IllegalArgumentException("Выражение в if-условии должно оканчиваться символом ')'")
            val ifStatement = after.substringAfter('(').substring(0, end-1)
            val afterIf = after.substringAfter(ifStatement).substringAfter(')').trim()
            if (!afterIf.startsWith('{') && !(afterIf.isEmpty() && afterInstructions.isNotEmpty() && afterInstructions[0].trim().startsWith('{'))) {
                throw IllegalArgumentException("Блок кода после if должен начинаться символом '{'")
            }
            var skobko = 0
            if (afterIf.startsWith('{')) {
                skobko = 1
            }
            if (afterIf.substringAfter('{').trim().isNotEmpty()) {
                throw IllegalArgumentException("После '{' на той же строчке не может идти код")
            }
            var instrInd = 0
            afterInstructions.forEach { instr ->
                instrInd++
                var checking = instr
                if (checking.findKav('#') != -1) {
                    checking = checking.substring(0, checking.findKav('#'))
                }
                checking = checking.trim()
//                player.logZorshizen("CHECKING: ($checking) -> $instrInd -> $skobko")
                var kav = false
                for (c in checking) {
                    if (c == '"')
                        kav = !kav
                    if (c == '{' && !kav) {
                        skobko++
                    }
                    if (c == '}' && !kav) {
                        skobko--
                        if (skobko < 1) {
                            val result = evaluate(ZorshizenInstruction(ifStatement.trim()).convertToExpression())
                            if (!result.boolean()) {
                                skipInstructions = instrInd
                            }
                            if (afterInstructions.size > instrInd && afterInstructions[instrInd].trim().startsWith("else")) {
                                val next = afterInstructions[instrInd].trim()
//                                player.logZorshizen("Replacing")
                                return Pair("if", Pair(instrInd, next.replace("else", "if (${!result.boolean()})")))
                            }
                            return null
                        }
                    }
                }
            }
        }
        if (instruction.startsWith("repeat") &&
            instruction.substringAfter("repeat").contains('(') &&
            instruction.substringAfter("repeat").substringBefore('(').trim().isEmpty()) {
            val after = instruction.substringAfter("repeat").trim()
            val end = after.findEnd('(',  ')') ?: throw IllegalArgumentException("Выражение в repeat-условии должно оканчиваться символом ')'")
            val ifStatement = after.substringAfter('(').substring(0, end-1)
            val afterIf = after.substringAfter(ifStatement).substringAfter(')').trim()
            if (!afterIf.startsWith('{') && !(afterIf.isEmpty() && afterInstructions.isNotEmpty() && afterInstructions[0].trim().startsWith('{'))) {
                throw IllegalArgumentException("Блок кода после repeat должен начинаться символом '{'")
            }
            var skobko = 0
            if (afterIf.startsWith('{')) {
                skobko = 1
            }
            if (afterIf.substringAfter('{').trim().isNotEmpty()) {
                throw IllegalArgumentException("После '{' на той же строчке не может идти код")
            }
            var instrInd = 0
            afterInstructions.forEach { instr ->
                instrInd++
                var checking = instr
                if (checking.findKav('#') != -1) {
                    checking = checking.substring(0, checking.findKav('#'))
                }
                checking = checking.trim()
//                player.logZorshizen("CHECKING: ($checking) -> $instrInd -> $skobko")
                var kav = false
                for (c in checking) {
                    if (c == '"')
                        kav = !kav
                    if (c == '{' && !kav) {
                        skobko++
                    }
                    if (c == '}' && !kav) {
                        skobko--
                        if (skobko < 1) {
                            val result = evaluate(ZorshizenInstruction(ifStatement.trim()).convertToExpression())
                            val toRepeat = result.number().toInt()
                            if (toRepeat < 0) {
                                throw IllegalArgumentException("Нельзя использовать отрицательные числа в repeat")
                            }
                            if (toRepeat == 0) {
                                skipInstructions = instrInd
                            } else {
                                return Pair("repeat", Pair(instrInd, "$toRepeat"))
                            }
                            return null
                        }
                    }
                }
            }
        }
        if (instruction.startsWith("goto") &&
            instruction.substringAfter("goto").contains('(') &&
            instruction.substringAfter("goto").substringBefore('(').trim().isEmpty()) {
            val after = instruction.substringAfter("goto").trim()
            val end = after.findEnd('(',  ')') ?: throw IllegalArgumentException("Выражение в goto-условии должно оканчиваться символом ')'")
            val ifStatement = after.substringAfter('(').substring(0, end-1)
            val tag = evaluate(ZorshizenInstruction(ifStatement).convertToExpression()).toString().trim()
            if (tag.isEmpty()) {
                throw IllegalArgumentException("Выражение в goto-условии не может быть пустым")
            }
            var instrInd = 0
            afterInstructions.forEach { instr ->
                instrInd++
                var checking = instr
                if (checking.findKav('#') != -1) {
                    checking = checking.substring(0, checking.findKav('#'))
                }
                checking = checking.trim()
                if (checking.startsWith(":$tag")) {
                    return Pair("goto", Pair(instrInd, "Hyi"))
                }
            }
            throw IllegalArgumentException("Место для перехода goto '$tag' не найдено (Переходить можно только вперед по коду)")
        }
        parseInstruction(instruction)
        return null
    }

    private fun parseInstructions(instructionsRaw: List<String>) {
        var i = 0
        val instructions = instructionsRaw.toMutableList()
        //        Instr Index V      Amount to jump V      V Times to jump
        val jumps = hashMapOf<Int, MutableList<Pair<Int, Int>>>()

        object : BukkitRunnable() {
            override fun run() {
                if (LocalTime.now().minusSeconds(10).isAfter(startTime)) {
                    error = ZorshizenError(i, "Превышено максимальное время работы программы!", instructions[i-1])
                    active = false
                    this.cancel()
                }

                if (i >= instructions.size) {
                    active = false
                    this.cancel()
                }

                while (i < instructions.size && delay == 0) {
                    if (jumps.containsKey(i) && jumps[i] != null && jumps[i]!!.isNotEmpty()) {
                        val pair = jumps[i]!![0]
                        if (pair.second > 1) {
                            jumps[i]!![0] = Pair(pair.first, pair.second - 1)
                        } else {
                            jumps[i]!!.removeFirst()
                        }
                        i -= pair.first
                    }

                    val instructionRaw = instructions[i]
                    i++

                    if (skipInstructions > 0) {
                        skipInstructions--
                    } else {
                        variables["__instruction"] = ZVariable(i.toDouble())
                        var instruction = instructionRaw
                        if (instructionRaw.findKav('#') != -1) {
                            instruction = instructionRaw.substring(0, instructionRaw.findKav('#'))
                        }
                        instruction = instruction.trim()
                        if (instruction.isNotEmpty()) {
                            try {
                                try {
                                    val replacement = checkStatement(instruction, instructions.drop(i))
                                    if (replacement != null) {
                                        if (replacement.first == "if") {
                                            instructions[replacement.second.first + i] =
                                                replacement.second.second
                                        } else if (replacement.first == "repeat") {
                                            val size = replacement.second.first
                                            val count = replacement.second.second.toInt()
                                            if (count > 1) {
                                                if (!jumps.containsKey(i + size - 1)) {
                                                    jumps[i + size - 1] = mutableListOf()
                                                }
                                                jumps[i + size - 1]?.addFirst(Pair(size - 1, count - 1))
                                            }
                                        } else if (replacement.first == "goto") {
                                            skipInstructions = replacement.second.first
                                        }
                                    }
                                } catch (e: IllegalArgumentException) {
                                    error = ZorshizenError(i, e.message, instructions[i - 1])
                                    active = false
                                    this.cancel()
                                }
                            } catch (e: Exception) {
                                error =
                                    ZorshizenError(i, "Неопознанная ошибка: ${e.message}", instructions[i - 1])
                                active = false
                                this.cancel()
                            }
                        }
                    }

                    while (delayPrecise >= 50) {
                        delayPrecise -= 50
                        delay++
                    }
                }
                if (delay > 0) {
                    delay--
                }
            }
        }.runTaskTimer(Main.instance, 0L, 1L)
    }

    private suspend fun waitForEnd(): ZorshizenError? {
        while (active && LocalTime.now().minusSeconds(20).isBefore(startTime)) {
            delay(100L)
        }
        return error
    }

    private fun parseSpellErrors(spellText: String) {
        if (!spellText.contains("Code:") || !spellText.substringAfter("Code:").contains("\n")) {
            player.sendMessage("§c[Zorshizen error] §7Код должен обозначаться при помощи §7'§eCode:§7'")
            return
        }
        val instructions = spellText.substringAfter("Code:").substringAfter("\n").split("\n")
        variables["__player"] = ZVariable(player)
        parseInstructions(instructions)
    }

    suspend fun waitForResult() {
        val error: ZorshizenError? = waitForEnd()
        if (error != null) {
            val parsingResult = SpellParsingResult(
                true,
                "§fИнструкция ${error.instructionNumber}: §c${error.message}\n§bКод инструкции> §7${error.instruction.trim()}"
            )
            if (parsingResult.isError) {
                player.sendMessage("§c[Zorshizen Error] §7Произошла ошибка:\n§c${parsingResult.message}")
            } else {
                player.sendMessage("§a${parsingResult.message}")
            }
        }
    }

    fun parseSpell(spellText: String) {
        parseSpellErrors(spellText.substringAfter("Name:").substringAfter("\n"))
    }
}