package dev.zorsh

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player

class ZorshizenParser {
    private val variables = HashMap<String, ZVariable>()

    // =================================================================================================================

    fun ZVariable.player(): Player {
        if (type == "Pointer")
            return get().player()
        if (value !is Player)
            throw  IllegalArgumentException("Не удалось привести переменную к типу Player")
        return (value as Player)
    }

    fun ZVariable.vector(): ZVector {
        if (type == "Pointer")
            return get().vector()
        if (value !is ZVector)
            throw  IllegalArgumentException("Не удалось привести переменную к типу Vector")
        return (value as ZVector)
    }

    fun ZVariable.number(): Double {
        if (type == "Pointer")
            return get().number()
        if (value !is Double)
            throw  IllegalArgumentException("Не удалось привести переменную к типу Number")
        return (value as Double)
    }

    fun ZVariable.location(): Location {
        if (type == "Pointer")
            return get().location()
        if (value !is Location)
            throw  IllegalArgumentException("Не удалось привести переменную к типу Location")
        return (value as Location)
    }

    fun ZVariable.world(): World {
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
        return when (operatorValue) {
            "+" -> v1 + v2
            "-" -> v1 - v2
            "*" -> v1 * v2.number()
            "/" -> v1 / v2.number()
            "=" -> v1.set(v2)
            else -> throw IllegalArgumentException("Невозможно применить оператор '$operatorValue' к '$v1' и '$v2'")
        }
    }

    // =================================================================================================================

    fun ZVariable.get(): ZVariable {
        if (type != "Pointer")
            throw  IllegalArgumentException("Взять переменную можно только у Pointer")
        return variables[(value as ZVariablePointer).target] ?: throw IllegalArgumentException("Переменной с именем ${(value as ZVariablePointer).target} не существует")
    }

//    fun ZVariable.pointerType(): String {
//        return variables[(value as ZVariablePointer).target]?.type ?: throw IllegalArgumentException("Переменной с именем ${(value as ZVariablePointer).target} не существует")
//    }

    fun ZVariable.set(new: ZVariable): ZVariable {
        if (type != "Pointer")
            throw  IllegalArgumentException("Изменить переменную можно только у Pointer")
        variables[(value as ZVariablePointer).target] = new
        pointerString = new.toString()
        return this
    }

    operator fun ZVariable.plus(v: ZVariable): ZVariable {
        when (type) {
            "Player" -> throw IllegalArgumentException("Нельзя использовать + с переменной типа Player")
            "Vector" -> return ZVariable(vector()+v.vector())
            "Number" -> return ZVariable(number()+v.number())
            "Location" -> return ZVariable(location()+v.location())
            "World" -> throw IllegalArgumentException("Нельзя использовать + с переменной типа World")
            "String" -> return ZVariable(string()+v.string())
            "Pointer" -> return get() + v
        }
        throw IllegalArgumentException("Неопознанный тип переменной (+): [$type]")
    }

    operator fun ZVariable.minus(v: ZVariable): ZVariable {
        when (type) {
            "Player" -> throw IllegalArgumentException("Нельзя использовать - с переменной типа Player")
            "Vector" -> return ZVariable(vector()-v.vector())
            "Number" -> return ZVariable(number()-v.number())
            "Location" -> return ZVariable(location()-v.location())
            "World" -> throw IllegalArgumentException("Нельзя использовать - с переменной типа World")
            "String" -> throw IllegalArgumentException("Нельзя использовать - с переменной типа String")
            "Pointer" -> return get() - v
        }
        throw IllegalArgumentException("Неопознанный тип переменной (-): [$type]")
    }

    operator fun ZVariable.times(m: Double): ZVariable {
        when (type) {
            "Player" -> throw IllegalArgumentException("Нельзя использовать * с переменной типа Player")
            "Vector" -> return ZVariable(vector()*m)
            "Number" -> return ZVariable(number()*m)
            "Location" -> return ZVariable(location()*m)
            "World" -> throw IllegalArgumentException("Нельзя использовать * с переменной типа World")
            "String" -> return ZVariable(string()*m.toInt())
            "Pointer" -> return get() * m
        }
        throw IllegalArgumentException("Неопознанный тип переменной (*): [$type]")
    }

    operator fun ZVariable.div(m: Double): ZVariable {
        when (type) {
            "Player" -> throw IllegalArgumentException("Нельзя использовать / с переменной типа Player")
            "Vector" -> return ZVariable(vector()/m)
            "Number" -> return ZVariable(number()/m)
            "Location" -> return ZVariable(location()/m)
            "World" -> throw IllegalArgumentException("Нельзя использовать / с переменной типа World")
            "String" -> throw IllegalArgumentException("Нельзя использовать / с переменной типа String")
            "Pointer" -> return get() / m
        }
        throw IllegalArgumentException("Неопознанный тип переменной (/): [$type]")
    }

    // =================================================================================================================

    private fun ZorshizenFunction.invoke(player: Player, depth: Int = 0): ZVariable {
        when (name) {
            "print" -> {
                if (args.isEmpty()) {
                    throw IllegalArgumentException("Для функции print нужен 1 аргумент")
                }

                player.logZorshizen(args[0].toString())

                return ZVariable(args[0].toString().length.toDouble())
            }
            else -> throw IllegalArgumentException("Функция $name не найдена!")
        }
    }

    private fun String.findFunctionEnd(): Int? {
        var skobko = 0
        var ind = 0
        for (c in this) {
            if (c == '(') {
                skobko++
            }
            if (c == ')') {
                skobko--
            }
            if (c == ')' && skobko <= 0) {
                return ind
            }
            ind++
        }
        return null
    }

    // =================================================================================================================

    private fun ZorshizenInstruction.convertToExpression(player: Player): MutableList<ZorshizenToken> {
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

            if (c == '<') {
                val str = text.substring(charIndex)
                if (str.length < 2) {
                    throw IllegalArgumentException("Имя переменной должно закрываться символом '>'")
                }
                if (!str.substringAfter('<').contains('>')) {
                    throw IllegalArgumentException("Имя переменной должно закрываться символом '>'")
                }
                val txt = str.substringAfter('<').substringBefore('>')
                if (txt.contains(' ')) {
                    throw IllegalArgumentException("Имя переменной не должно содержать пробелов")
                }
                if (txt.contains('.') || txt.contains(',') || txt.contains(';') || txt.contains(':') || txt.contains('%') || txt.contains('^') || txt.contains('+') || txt.contains('-') || txt.contains('*') || txt.contains('/')) {
                    throw IllegalArgumentException("Имя переменной не должно содержать специальных символов")
                }
                if (variables.containsKey(txt)) {
                    result.add(ZorshizenToken(ZVariable(ZVariablePointer(txt), variables[txt].toString())))
                } else {
                    result.add(ZorshizenToken(ZVariable(ZVariablePointer(txt))))
                }
                skip = txt.length+1

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

            if (!c.isDigit() && !OPERATOR_PRIORITIES.containsKey("$c") && (prev == 'O' || prev == 's') && c != ' ' && c != '.' && c != '(' && c != ')' && c != '>') {
                val str = text.substring(charIndex)
                if (str.contains('(') && !str.substringBefore('(').contains(' ')) {
                    val func = str.substringBefore('(')

                    val after = str.substringAfter(func)
                    val end = after.findFunctionEnd() ?: throw IllegalArgumentException("Вызов функции должен оканчиваться символом ')'")

                    var argsText = after.substringAfter('(').substring(0, end-1)
                    //player.logZorshizen("ABOBA: $argsText")
                    skip = -1 + func.length + 2 + argsText.length
                    argsText = argsText.trim()

                    val args = mutableListOf<ZVariable>()
                    if (argsText.isNotEmpty()) {
                        val toParse = argsText.split(',')
                        toParse.forEach { arg ->
                            if (arg.trim().isEmpty()) {
                                throw IllegalArgumentException("Аргумент функции не может быть пустым выражением ($func)")
                            }

                            args.add(evaluate(ZorshizenInstruction(arg.trim()).convertToExpression(player), player))
                        }
                    }

                    result.add(ZorshizenToken(ZorshizenFunction(func, args).invoke(player, depth=0)))
                    prev = 'F'

                    charIndex++
                    continue
                }
            }

            if (!c.isDigit() && c != '.' && prev == 'N' && num != null) {
                result.add(ZorshizenToken(ZVariable(num / divider)))
                divide = false
                divider = 1
                num = null
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
            } else if (c != ' ') {
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

    private fun evaluate(tokens: MutableList<ZorshizenToken>, player: Player): ZVariable {
        val stack = mutableListOf<ZVariable>()
        for (t in tokens) {
            if (t.type == TokenType.VARIABLE) {
                stack.add(t.variableValue!!)
            } else if (t.type == TokenType.OPERATOR) {
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
        if (stack.isEmpty()) {
            throw IllegalArgumentException("Невозможно вычислить пустое выражение")
        }
        return stack.last()
    }

    // =================================================================================================================

    private fun parseInstruction(instructionText: String) {
        val player: Player = variables["__player"]!!.player()
        val instruction = ZorshizenInstruction(instructionText)
//    player.sendMessage("§7Parsing: §b${instruction}")

        val expression = instruction.convertToExpression(player)
        evaluate(expression, player)
        //player.logZorshizen("Evaluated: ${evaluate(expression, player)}")
    }

    private fun parseInstructions(instructions: List<String>): ZorshizenError? {
        var i = 0
        instructions.forEach { instructionRaw ->
            i++
            variables["__instruction"] = ZVariable(i.toDouble())
            val instruction = instructionRaw.substringBefore('#').trim()
            if (instruction.isNotEmpty()) {
                try {
                    try {
                        parseInstruction(instruction)
                    } catch (e: IllegalArgumentException) {
                        return ZorshizenError(i, e.message, instruction)
                    }
                } catch (e: Exception) {
                    return ZorshizenError(i, "Неопознанная ошибка: ${e.message}", instruction)
                }
            }
        }
        return null
    }

    private fun parseSpellErrors(spellText: String, player: Player): SpellParsingResult? {
        if (!spellText.contains("Code:") || !spellText.substringAfter("Code:").contains("\n")) {
            return SpellParsingResult(true, "Код должен обозначаться при помощи §7'§eCode:§7'")
        }
        val instructions = spellText.substringAfter("Code:").substringAfter("\n").split("\n")
        variables["__player"] = ZVariable(player)
        val error: ZorshizenError? = parseInstructions(instructions)
        if (error != null) {
            return SpellParsingResult(
                true,
                "§fИнструкция ${error.instructionNumber}: §c${error.message}\n§bКод инструкции> §7${error.instruction}"
            )
        }

//    return SpellParsingResult(false, "§aВсе работает!")
        return null
    }

    fun parseSpell(spellText: String, player: Player) {
        val parsingResult = parseSpellErrors(spellText.substringAfter("Name:").substringAfter("\n"), player)
        if (parsingResult != null) {
            if (parsingResult.isError) {
                player.sendMessage("§c[Zorshizen Error] §7Произошла ошибка: §c${parsingResult.message}")
            } else {
                player.sendMessage("§a${parsingResult.message}")
            }
        }
    }
}