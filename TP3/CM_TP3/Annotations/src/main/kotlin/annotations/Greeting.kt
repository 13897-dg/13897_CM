package annotations

// so da pra usar em funcoes
@Target(AnnotationTarget.FUNCTION)

// etioqueta morre na compilação
@Retention(AnnotationRetention.SOURCE)

// cria a etiqueta e pede a mensagem
annotation class Greeting(val message: String) {
}