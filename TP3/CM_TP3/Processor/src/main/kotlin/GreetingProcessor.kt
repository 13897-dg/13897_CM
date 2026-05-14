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

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_21)
// so vai procurar esta etiqueta no codigo
@SupportedAnnotationTypes("annotations.Greeting")
class GreetingProcessor : AbstractProcessor() {

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        // cria um mapa pra guardar onde estao as etiquetas q encontrou
        val classMethodMap = mutableMapOf<TypeElement, MutableList<ExecutableElement>>()

        // procura todas as coisas q tenham a etiqueta greeting
        for (element in roundEnv.getElementsAnnotatedWith(Greeting::class.java)) {
            if (element is ExecutableElement) {
                // descobre a qual classe esta funcao pertence
                val enclosingClass = element.enclosingElement as TypeElement
                // guarda a funcao e a classe dela no mapa pra usar a seguir
                classMethodMap.computeIfAbsent(enclosingClass) {
                    mutableListOf()
                }.add(element)
            }
        }

        // classe a classe
        for ((classElement, methods) in classMethodMap) {
            // manda criar um ficheiro novo para esta classe
            generateKotlinWrapperClass(classElement, methods)
        }

        return true
    }

    // funcao q desenha a classe nova
    private fun generateKotlinWrapperClass(
        classElement: TypeElement,
        methods: List<ExecutableElement>
    ) {
        // descobre a pasta onde a classe ta guardada
        val packageName = processingEnv.elementUtils.getPackageOf(classElement).toString()
        // guarda o nome da classe original
        val originalClassName = classElement.simpleName.toString()
        val wrapperClassName = "${originalClassName}Wrapper"

        // começa a construir a classe nova com a biblioteca kotlinpoet
        val classBuilder = TypeSpec.classBuilder(wrapperClassName)
            // cria o construtor pra receber dados
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    // pede a classe original como argumento
                    .addParameter("original", ClassName(packageName, originalClassName))
                    .build()
            )
            // guarda a classe original numa variavel pra poder chamar as funcoes dela
            .addProperty(
                PropertySpec.builder("original", ClassName(packageName, originalClassName))
                    .initializer("original")
                    .build()
            )
            // diz q a classe e publica
            .addModifiers(KModifier.PUBLIC, KModifier.FINAL)

        // vai fazer isto pra cada funcao q tinha etiqueta
        for (method in methods) {
            // copia o nome da funcao antiga
            val methodName = method.simpleName.toString()

            // ve se a funcao antiga pedia variaveis tipo x ou y e copia isso tmb
            val parameters = method.parameters.map { param ->
                ParameterSpec.builder(
                    param.simpleName.toString(),
                    param.asType().asTypeName()
                ).build()
            }

            // prepara o texto com os argumentos para quando formos chamar a funcao
            val arguments = method.parameters.joinToString(", ") {
                it.simpleName.toString()
            }

            // vai a etiqueta ver q texto escrevemos se nao tiver nada escreve hello
            val greetingMessage = method.getAnnotation(Greeting::class.java)?.message ?: "Hello!"

            // comeca a construir a funcao nova
            val methodBuilder = FunSpec.builder(methodName)
                .addModifiers(KModifier.PUBLIC, KModifier.FINAL)
                // mete as variaveis q copiou
                .addParameters(parameters)
                .addStatement("println(%S)", greetingMessage)
                .addStatement("original.$methodName($arguments)")

            // mete a funcao nova dentro da classe nova
            classBuilder.addFunction(methodBuilder.build())
        }

        // empacota a classe toda e prepara o ficheiro
        val file = FileSpec.builder(packageName, wrapperClassName)
            .addType(classBuilder.build())
            .build()

        // parte q grava o ficheiro novo no pc
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