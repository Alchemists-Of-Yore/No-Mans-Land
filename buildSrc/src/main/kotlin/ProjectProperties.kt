import org.gradle.api.Project

fun Project.prop(key: String): String =
    requireNotNull(findProperty(key)) { "Missing property '$key' in gradle.properties" }.toString()

@JvmInline
value class PropertyGroup(private val pair: Pair<Project, String>) {
    operator fun get(key: String): String = pair.first.prop("${pair.second}.$key")
}

fun Project.group(prefix: String) = PropertyGroup(this to prefix)

val Project.mod get() = group("mod")
val Project.mc get() = group("mc")
val Project.neo get() = group("neo")
val Project.parchment get() = group("parchment")
val Project.deps get() = group("deps")
