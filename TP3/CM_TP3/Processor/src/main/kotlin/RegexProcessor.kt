package processor

import annotations.Extract
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@SupportedAnnotationTypes("annotations.Extract")
class RegexProcessor : AbstractProcessor() {

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        val classMethodMap = mutableMapOf<TypeElement, MutableList<ExecutableElement>>()

        // Encontra todos os métodos com a anotação @Extract
        for (element in roundEnv.getElementsAnnotatedWith(Extract::class.java)) {
            if (element is ExecutableElement) {
                val enclosingClass = element.enclosingElement as TypeElement
                classMethodMap.computeIfAbsent(enclosingClass) {
                    mutableListOf()
                }.add(element)
            }
        }

        // Gera as classes "wrapper" para cada classe original
        for ((classElement, methods) in classMethodMap) {
            generateKotlinWrapperClass(classElement, methods)
        }

        return true
    }

    private fun generateKotlinWrapperClass(
        classElement: TypeElement,
        methods: List<ExecutableElement>
    ) {
        val packageName = processingEnv.elementUtils.getPackageOf(classElement).toString()
        val originalClassName = classElement.simpleName.toString()
        val wrapperClassName = "${originalClassName}Wrapper"

        // Cria a classe wrapper usando composição
        val classBuilder = TypeSpec.classBuilder(wrapperClassName)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("original", ClassName(packageName, originalClassName))
                    .build()
            )
            .addProperty(
                PropertySpec.builder("original", ClassName(packageName, originalClassName))
                    .initializer("original")
                    .build()
            )
            .addModifiers(KModifier.PUBLIC, KModifier.FINAL)

        // Gera os métodos wrapper
        for (method in methods) {
            val methodName = method.simpleName.toString()

            val parameters = method.parameters.map { param ->
                ParameterSpec.builder(
                    param.simpleName.toString(),
                    param.asType().asTypeName()
                ).build()
            }

            val arguments = method.parameters.joinToString(", ") {
                it.simpleName.toString()
            }

            val regexMessage = method.getAnnotation(Extract::class.java)?.regex ?: "Hello!"

            val methodBuilder = FunSpec.builder(methodName)
                .addModifiers(KModifier.PUBLIC, KModifier.FINAL)
                .addParameters(parameters)
                .addStatement("println(%S)", regexMessage) // Imprime a mensagem
                .addStatement("original.$methodName($arguments)") // Chama o método original

            classBuilder.addFunction(methodBuilder.build())
        }

        // Constrói o ficheiro Kotlin
        val file = FileSpec.builder(packageName, wrapperClassName)
            .addType(classBuilder.build())
            .build()

        // Escreve o ficheiro gerado
        try {
            val kaptKotlinGeneratedDir = processingEnv.options["kapt.kotlin.generated"]
            if (kaptKotlinGeneratedDir != null) {
                file.writeTo(File(kaptKotlinGeneratedDir))
            } else {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "kapt.kotlin.generated not found"
                )
            }
        } catch (e: Exception) {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.ERROR,
                "Error generating Kotlin file: ${e.message}"
            )
        }
    }
}