package com.application.requiemproject

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class ArchitectureTest {
    private val root = sequenceOf(File("src/main/java/com/application/requiemproject"), File("app/src/main/java/com/application/requiemproject"))
        .first { it.isDirectory }

    @Test fun domainIsIndependentOfAndroidAndOuterLayers() {
        val violations = root.resolve("domain").walkTopDown().filter { it.extension == "kt" }.flatMap { file ->
            file.readLines().filter {
                it.startsWith("import android.") || it.startsWith("import androidx.") ||
                    it.startsWith("import com.application.requiemproject.data.") ||
                    it.startsWith("import com.application.requiemproject.presentation.")
            }.map { "${file.name}: $it" }
        }.toList()
        assertTrue(violations.joinToString("\n"), violations.isEmpty())
    }

    @Test fun viewModelsNeverAccessDataOrAndroidContext() {
        val violations = root.resolve("presentation").walkTopDown().filter { it.name.endsWith("ViewModel.kt") }.flatMap { file ->
            file.readLines().filter {
                it.startsWith("import com.application.requiemproject.data.") ||
                    it.startsWith("import android.")
            }.map { "${file.name}: $it" }
        }.toList()
        assertTrue(violations.joinToString("\n"), violations.isEmpty())
    }

    @Test fun dataDoesNotDependOnPresentation() {
        val violations = root.resolve("data").walkTopDown().filter { it.extension == "kt" }.filter { file ->
            file.readText().contains("import com.application.requiemproject.presentation.")
        }.toList()
        assertTrue(violations.toString(), violations.isEmpty())
    }
}
