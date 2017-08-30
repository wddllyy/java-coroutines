# Warning

**Never** throw exceptions, get stacktrace or use caller sensitive methods inside a win32 coroutine, that **will crash the jvm**. This happens because the JVM was not designed to work with coroutines. If code running inside a coroutine might throw, use the fallback context via `CoroutineContext.getFallbackContext()`.

# Usage

First, you need a `CoroutineContext` object. To get one use the static method `getContext()`, which returns the appropriate implementation for the current machine.

```java
CoroutineContext ctx = CoroutineContext.getContext();
```

Then, you need to use the `create(CoroutineFunc)` method to create a coroutine.

```java
Coroutine<Object, String> c = ctx.create((context, arg)->{
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

Since throwing errors inside a coroutine makes the JVM crash, there's an `error(Object)` method on `CoroutineContext`. When it's called it will stop execution of all coroutines in the call stack until it's safe to throw an exception. Then, it'll throw a `CoroutineExecutionError`, giving it the object given to `error()`

All `CoroutineContext` objects are meant to be used only in the thread that called `getContext()`, and *will* error if used on another thread.

## Example: Fibonacci generator

This example prints the 10 first number on the fibonacci sequence

```java
CoroutineContext ctx = CoroutineContext.getContext();
Coroutine<Object, Integer> fibonacci = ctx.create((ctx1, arg)->{
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
