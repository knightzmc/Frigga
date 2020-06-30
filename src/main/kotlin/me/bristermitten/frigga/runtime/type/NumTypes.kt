package me.bristermitten.frigga.runtime.type

import me.bristermitten.frigga.runtime.command.*
import me.bristermitten.frigga.runtime.data.Value
import me.bristermitten.frigga.runtime.data.decValue
import me.bristermitten.frigga.runtime.data.function.body
import me.bristermitten.frigga.runtime.data.function.signature
import me.bristermitten.frigga.runtime.data.intValue
import kotlin.math.pow

object NumType : Type(
    "Num"
)

object IntType : Type("Int") {
//    override fun union(other: Type): Type {
//        return when (other) {
//            is DecType -> DecType
//            is IntType -> IntType
//            else -> super.union(other)
//        }
//    }
//
//    override fun coerceValueTo(value: Value, other: Type): Value {
//        return when (other) {
//            is DecType -> Value(
//                other,
//                (value.value as Long).toDouble()
//            )
//            else -> super.coerceValueTo(value, other)
//        }
//    }

    init {
        fun defineIntAndDecMathFunctions(
            name: String,
            intOperator: (Long, Long) -> Long,
            decOperator: (Long, Double) -> Double
        ) {
            defineFunction {
                this.name = name
                signature {
                    input = mapOf("value" to IntType)
                    output = IntType
                }
                body { stack, context ->
                    val thisValue = stack.pull()
                    val addTo = context.findProperty("value")!!.value
                    stack.push(intValue(intOperator(thisValue.value as Long, addTo.value as Long)))
                }
            }
            defineFunction {
                this.name = name
                signature {
                    input = mapOf("value" to DecType)
                    output = DecType
                }
                body { stack, context ->
                    val thisValue = stack.pull()
                    val addTo = context.findProperty("value")!!.value
                    stack.push(decValue(decOperator(thisValue.value as Long, addTo.value as Double)))
                }
            }
        }

        defineIntAndDecMathFunctions(OPERATOR_ADD_NAME, Long::plus, Long::plus)
        defineIntAndDecMathFunctions(OPERATOR_TAKE_NAME, Long::minus, Long::minus)
        defineIntAndDecMathFunctions(OPERATOR_TIMES_NAME, Long::times, Long::times)
        defineIntAndDecMathFunctions(OPERATOR_DIVIDE_NAME, Long::div, Long::div)
        defineIntAndDecMathFunctions(OPERATOR_EXPONENT_NAME,
            { a, b -> a.toDouble().pow(b.toDouble()).toLong() }
        ) { a, b -> a.toDouble().pow(b) }
    }

    override fun coerceValueTo(value: Value, other: Type): Value {
        return when (other) {
            is DecType -> Value(other, (value.value as Long).toDouble())
            else -> super.coerceValueTo(value, other)
        }
    }

}

object DecType : Type(
    "Dec",
    NumType
) {

    init {
        fun defineMathFunction(
            name: String,
            decOperator: (Double, Double) -> Double
        ) {
            defineFunction {
                this.name = name
                signature {
                    input = mapOf("value" to DecType)
                    output = DecType
                }
                body { stack, context ->
                    val thisValue = stack.pull()
                    val addTo = context.findProperty("value")!!.value
                    val addToAsDec = addTo.type.coerceTo(addTo, DecType)
                    stack.push(decValue(decOperator(thisValue.value as Double, addToAsDec.value as Double)))
                }
            }
        }
        defineMathFunction(OPERATOR_ADD_NAME, Double::plus)
        defineMathFunction(OPERATOR_TAKE_NAME, Double::minus)
        defineMathFunction(OPERATOR_TIMES_NAME, Double::times)
        defineMathFunction(OPERATOR_DIVIDE_NAME, Double::div)
        defineMathFunction(OPERATOR_EXPONENT_NAME, Double::pow)
    }

    override fun accepts(other: Type): Boolean {
        return other == IntType || super.accepts(other)
    }
}
