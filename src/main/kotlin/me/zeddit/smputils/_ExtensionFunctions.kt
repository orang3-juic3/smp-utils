package me.zeddit.smputils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import java.lang.StringBuilder
import kotlin.math.ceil
import kotlin.math.floor

fun String.toError() : Component = Component.text(this, NamedTextColor.RED)
fun String.toSuccess() : Component = Component.text(this, NamedTextColor.GREEN)
fun String.toComponent(color: NamedTextColor = NamedTextColor.WHITE) : Component = Component.text(this, color)
fun Location.fmtCoord() : String {
    val ser =this.serialize()
    return ser.keys
        .filter { ser[it] is Double }.joinToString(", ") {
            (ser[it] as Double).let { dbl ->
                if (dbl < 0) {
                    ceil(dbl).toInt().toString()
                } else {
                    floor(dbl).toInt().toString()
                }
            }
        }
}