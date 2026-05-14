package processor

import annotations.Greeting
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

// avisa o compilador que isto e um processador
@AutoService(Processor::class)
// versao do java
@SupportedSourceVersion(SourceVersion.RELEASE_21)
// diz qual etiqueta vamos procurar no codigo
@SupportedAnnotationTypes("annotations.Greeting")
class GreetingProcessor : AbstractProcessor() {

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        // cria uma lista para guardar as funcoes que encontrarmos
        val classMethodMap = mutableMapOf<TypeElement, MutableList<ExecutableElement>>()

        // procura tudo o que tem a etiqueta greeting
        for (element in roundEnv.getElementsAnnotatedWith(Greeting::class.java)) {
            // garante que e uma funcao
            if (element is ExecutableElement) {
                // descobre em que classe a funcao ta
                val enclosingClass = element.enclosingElement as TypeElement
                // guarda a funcao na nossa lista
                classMethodMap.computeIfAbsent(enclosingClass) {
                    mutableListOf()
                }.add(element)
            }
        }

        // vai classe a classe e manda gerar o ficheiro novo
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
        // o nome da classe nova vai ter wrapper no fim
        val wrapperClassName = "${originalClassName}Wrapper"

        // usa o kotlinpoet para comecar a montar a classe nova
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

        // cria as funcoes novas
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

            // vai ler o texto que escrevemos dentro da etiqueta
            val greetingMessage = method.getAnnotation(Greeting::class.java)?.message ?: "Hello!"

            // monta a funcao em si
            val methodBuilder = FunSpec.builder(methodName)
                .addModifiers(KModifier.PUBLIC, KModifier.FINAL)
                .addParameters(parameters)
                // escreve a linha de codigo que vai dar o print do texto
                .addStatement("println(%S)", greetingMessage)
                // escreve a linha de codigo que chama a funcao verdadeira
                .addStatement("original.$methodName($arguments)")

            classBuilder.addFunction(methodBuilder.build())
        }

        // prepara o ficheiro
        val file = FileSpec.builder(packageName, wrapperClassName)
            .addType(classBuilder.build())
            .build()

        // tenta gravar o ficheiro gerado
        try {
            // descobre a pasta certa pra guardar as coisas geradas
            val kaptKotlinGeneratedDir = processingEnv.options["kapt.kotlin.generated"]
            if (kaptKotlinGeneratedDir != null) {
                // guarda mesmo o ficheiro no pc
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