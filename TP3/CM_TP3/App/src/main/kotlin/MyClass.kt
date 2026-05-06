package app
import annotations.Greeting
open class MyClass {
    @Greeting("Hello from Myclass!")
    open fun sayHello(){
        println("Executing sayHello method")
    }
    @Greeting("welcome to the compute function!")
    open fun compute(){
        println("Computing something important...")
    }
}