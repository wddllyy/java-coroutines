# Warning

Do not, **ever**, throw exceptions, get stacktrace or use caller sensitive methods inside a coroutine, that **will crash the jvm**. This happens because the JVM was not designed to work with coroutines.

