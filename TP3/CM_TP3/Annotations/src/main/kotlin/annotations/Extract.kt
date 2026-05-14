package annotations

// so da pra usar em funcoes
@Target(AnnotationTarget.FUNCTION)

// etioqueta morre na compilação
@Retention(AnnotationRetention.SOURCE)

// cria a etiqueta e diz que tem de ter uma string
annotation class Extract(val regex: String) {
}