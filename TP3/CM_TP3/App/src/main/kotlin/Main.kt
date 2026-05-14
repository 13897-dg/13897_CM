package app

fun main (){
    val myClass = MyClass()
    val wrappedMyClass = MyClassWrapper(myClass)
    val input = "Name: John Address: 123 street"
    val extractor = DataProcessorExtractor(input)

    wrappedMyClass.sayHello()
    wrappedMyClass.compute()
    println("Name: ${extractor.getName()}")
    println("Address: ${extractor.getAddress()}")
}