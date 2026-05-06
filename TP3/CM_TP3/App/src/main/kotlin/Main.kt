package app

fun main (){
    val myClass = MyClass()
    val wrappedMyClass = MyClassWrapper(myClass)

    wrappedMyClass.sayHello()
    wrappedMyClass.compute()
}