# Warning

Do not, **ever**, throw exceptions, get stacktrace or use caller sensitive methods inside a coroutine, that **will crash the jvm**. This happens because the JVM was not designed to work with coroutines.

#Usage

First, you need a `CoroutineContext` object. To get one use the static method `getContext()`, which returns the appropriate implementation for the current machine.

```java
CoroutineContext ctx = CoroutineContext.getContext();
```

Then, you need to use the `create(CoroutineFunc)` method to create a coroutine.

```java
Coroutine c = ctx.create((context, arg)->{
    context.yield("Hello world!");
});
```

To run it, use the `resume(Coroutine, Object)` method. It returns the value given to `yield(Object)`

```java
System.out.println(ctx.resume(c, null));
```

After using the context, remember to destroy it

```java
ctx.destroy();
```

Additionally, you can destroy individual coroutines to free their resources

```java
ctx.destroy(c);
```

All `CoroutineContext` objects are meant to be used only in the thread that called `getContext()`, and *will* error if used on another thread.

## Example: Fibonacci generator

This example prints the 10 first number on the fibonacci sequence

```java
CoroutineContext ctx = CoroutineContext.getContext();
Coroutine fibonacci = ctx.create((ctx1, arg)->{
    int a = 1, b = 1;
    ctx1.yield(a);
    ctx1.yield(b);
    for(int i = 0; i < 100; i++) {
        int c = a + b;
        a = b;
        b = c;
        ctx1.yield(c);
    }
    ctx1.error("No more data");
});

for(int i = 0; i < 10; i++) {
    System.out.print(ctx.resume(fibonacci, null) + " ");
}
```
