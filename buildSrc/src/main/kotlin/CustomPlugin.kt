import org.gradle.plugin.use.PluginDependenciesSpec
import org.gradle.plugin.use.PluginDependencySpec

fun PluginDependenciesSpec.customPlugin(pluginName: String): PluginDependencySpec {
    return id("kr.b1ink.$pluginName")
}