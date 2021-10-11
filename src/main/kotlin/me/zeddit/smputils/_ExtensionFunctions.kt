package me.zeddit.smputils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player

fun String.toError() : Component = Component.text(this, NamedTextColor.RED)
fun String.toSuccess() : Component = Component.text(this, NamedTextColor.GREEN)
fun String.toComponent(color: NamedTextColor = NamedTextColor.WHITE) : Component = Component.text(this, color)