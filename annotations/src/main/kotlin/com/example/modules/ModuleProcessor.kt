package com.example.modules

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.writeTo

class ModuleProcessor(
    private val generator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    private val moduleClass = Module::class
    private val moduleName = moduleClass.simpleName!!
    private val moduleQualifiedName = moduleClass.qualifiedName!!
    private var run = false

    @OptIn(KspExperimental::class)
    private fun KSClassDeclaration.getModuleAnnotation(): Module = getAnnotationsByType(moduleClass).first()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (run) return emptyList()
        run = true

        val symbols = resolver.getSymbolsWithAnnotation(moduleQualifiedName).toList()
        val validSymbols = symbols.mapNotNull(::validateSymbol)

        logger.warn("--- $moduleName Processor ---")
        logger.warn("Found ${validSymbols.size} modules")
        logger.warn("Generating ExampleModules.kt")

        val file = FileSpec.builder("com.example.modules", "ExampleModules")
            .indent("    ")
            .addType(
                TypeSpec.objectBuilder("ExampleModules").apply {
                    this.addProperty(
                        PropertySpec.builder("isDev", Boolean::class).apply {
                            this.addModifiers(KModifier.PRIVATE)
                            this.initializer(
                                CodeBlock.builder().apply {
                                    add("at.hannibal2.skyhanni.utils.system.PlatformUtils.isDevEnvironment")
                                }.build(),
                            )
                        }.build(),
                    )

                    this.addProperty(
                        PropertySpec.builder(
                            "modules",
                            List::class.asClassName().parameterizedBy(Any::class.asClassName()),
                        ).apply {
                            this.initializer(
                                CodeBlock.builder().apply {
                                    add("buildList {\n")
                                    validSymbols.forEach { symbol ->
                                        val string = buildString {
                                            if (symbol.getModuleAnnotation().devOnly) append("if (isDev) ")
                                            append("add(${symbol.qualifiedName!!.asString()})")
                                        }
                                        add("$string\n")
                                    }
                                    add("}\n")
                                }.build(),
                            )
                        }.build(),
                    )

                }.build(),
            )

        file.build().writeTo(
            generator,
            Dependencies(true, *validSymbols.mapNotNull(KSClassDeclaration::containingFile).toTypedArray()),
        )

        logger.warn("Generated ExampleModules.kt")

        return emptyList()
    }

    private fun validateSymbol(symbol: KSAnnotated): KSClassDeclaration? {
        if (!symbol.validate()) {
            logger.warn("Symbol is not valid: $symbol")
            return null
        }

        if (symbol !is KSClassDeclaration || symbol.classKind != ClassKind.OBJECT) {
            logger.error("@$moduleName is only valid on objects", symbol)
            return null
        }
        return symbol
    }
}
