package me.bristermitten.frigga.runtime.type

import getJVMType
import me.bristermitten.frigga.runtime.UPON_NAME
import me.bristermitten.frigga.runtime.data.function.body
import me.bristermitten.frigga.runtime.data.function.signature
import me.bristermitten.frigga.runtime.error.BreakException
import java.lang.reflect.Modifier

object AnyType : Type(
    "Any"
) {
    override fun accepts(other: Type): Boolean {
        return true
    }
}

data class JVMType(val jvmClass: Class<*>) : Type(jvmClass.simpleName) {
    fun init() {
        jvmClass.methods.filterNot { it.isSynthetic }
            .filter { Modifier.isPublic(it.modifiers) }
            .forEach {
                defineFunction {
                    name = it.name
                    signature {
                        input = it.parameters.map { param ->
                            param.name to getJVMType(param.type)
                        }.toMap()
                        output = getJVMType(it.returnType)
                    }
                    body { _, context ->
                        val upon = context.findProperty(UPON_NAME)!!.value
                        it.invoke(upon.value, *it.parameters.map { param ->
                            val findProperty = context.findProperty(param.name)
                            findProperty?.value
                        }.toTypedArray())
                    }
                }
            }
    }

//    override fun accepts(other: Type): Boolean {
//        return true //JVM type will accept any value, actual validation is done by the JVM via reflection
//    }
}

object CallerType : Type("__Caller") {
    init {
        defineFunction {
            name = "ensureInsideFunction"
            body { _, friggaContext ->
                if (!friggaContext.deepestScope.isFunctionScope) {
                    throw IllegalStateException("Must be called from inside a Function.")
                }
            }
        }

        defineFunction {
            name = "break"
            body { _, _ ->
                throw BreakException
            }
        }
    }
}


object NothingType : Type("Nothing", null) {
    override fun isSubtypeOf(other: Type): Boolean {
        return true
    }
}

object StackType : Type("Stack") {
    init {
        defineFunction {
            name = "push"
            signature {
                input = mapOf("value" to AnyType)
            }
            body { stack, friggaContext ->
                stack.push(friggaContext.findProperty("value")!!.value)
            }
        }
    }
}
