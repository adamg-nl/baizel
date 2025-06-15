## TypeRef

In Java, generic type information is lost during compilation, unless it is captured as a type parameters of a class being extended by some another class.

In a generic method, we can add an extra parameter of type TypeRefN, which caller will always supply in form of an expression `new TypeRefN<>() {}`.

At runtime, methods in form `typeRefN.tN()` give type-safe access to the parameter `Class`es:

```java
Map<String, Integer> map = Items.map(new TypeRef2<>(){}, "a", 1, "b", 2); // type safety will be enforced at runtime
```
