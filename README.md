Librarian
=====

### Opus #6

An innovative system for managing and accessing services or resources without requiring them as dependencies, or from external Java applications.

Librarian is designed for easily distributing and accessing implementations of standard library interfaces (similar to Java's Service Provider Interface) but with the added bonus of also allowing easy "blind" access to resources of which the type is not known at compile-time.

This could, for example, allow extensible systems (such as game-servers) to utilise alternative implementations of particular features provided by third-party modules, plugins or other modifications without needing to know anything about them.

Librarian differs from other simple interface providers in that it offers a new option: to use a resource about which *nothing* is known.

This is helpful in cases where resources are somewhat standardised in form but do not use any sort of widely-recognised template or interface. An example scenario is detailed below.

> **Example:**
> A 'vehicles' mod for a game would like to add functionality for users to buy and sell cars, but has no access to any 'economy' module.
>
> The game may have an 'economy' module installed as well, but as it is a third-party module, we have no way of knowing anything about it. Perhaps there are multiple different third-party 'economy' modules available, and the user could have installed any of them.
> 
> While we don't know the exact structure, we can assume 'economy' will provide certain things:
> a `getBalance(Player)` method and a `setBalance(Player)` method.
> 

In the above example, we can use Librarian to query those resources.
```java 
interface Economy {
    double getBalance(Player player);
    void setBalance(Player player, double balance);
}

final Economy economy = library.lookFor(Economy.class);
economy.setBalance(player, 66);
assert economy.getBalance(player) == 66;
```

**Note:** we are using an interface that *we* have provided, and Librarian is populating it for us.

Librarian binds our interface methods to the methods from the resources using a relatively efficient proxy system.
`Method call -> Proxy -> Original method`

This allows Librarian to provide a very simple and easy-to-use way to call the methods, rather than having the user do reflection, casting or some sort of stringified method lookup.

As the methods in the proxy are resolved, prepared and cached ahead of time, using the proxy is relatively efficient.

### Maven Information

```xml

<repository>
    <id>pan-repo</id>
    <name>Pandaemonium Repository</name>
    <url>https://gitlab.com/api/v4/projects/18568066/packages/maven</url>
</repository>
``` 

```xml

<dependency>
    <groupId>mx.kenzie</groupId>
    <artifactId>librarian</artifactId>
    <version>1.0.0</version>
    <scope>compile</scope>
</dependency>
```

### Remote Libraries

Librarian has basic support for acquiring libraries from and distributing to other JVMs and Java applications on external machines. This makes use of [Cobweb](https://github.com/Moderocky/Cobweb) to do so (and is required as a dependency to use this feature.)

Remote libraries are significantly more limited (by marshalling constraints, etc.) but can still be used to great effect in many situations.

The Resource object itself must be a Remote object able to be marshalled, so that it can be distributed/accessed via RMI.

The dependency information for Cobweb is below.
```xml
<dependency>
    <groupId>mx.kenzie</groupId>
    <artifactId>cobweb</artifactId>
    <version>1.0.1</version>
    <scope>compile</scope>
</dependency>
```

### Examples

Registering and requesting a simple resource by known class.

```java 
final Library<Object> library = new SimpleLibrary();
class Alice { // An example class
    public int a() { return 1; }
    public int b() { return 2; }
}
assert library.register(new Alice());
final Alice alice = library.getResource(Alice.class); // Any supertype of Alice.class would be acceptable here.
assert alice != null;
assert alice.a() == 1;
assert alice.b() == 2;
```

Registering a resource, and requesting it using `lookFor` query.

```java 
final Library<Object> library = new SimpleLibrary();
class Alice { // An example class
    public int a() { return 1; }
    public int b() { return 2; }
}
assert library.register(new Alice());
interface Bob { // This is the template we try to match
    int a();
    int b();
}
final Bob bob = library.lookFor(Bob.class); // Populate our template with methods from the resources
assert bob != null;
assert bob.a() == 1; // These are called on our "alice" object we registered
assert bob.b() == 2;
```

Registering two resources, and accessing them both using a single `lookFor` query.

```java 
final Library<Object> library = new SimpleLibrary();
class Alice {
    public int a() { return 1; }
    public int b() { return 2; }
}
class Jeremy {
    public int c() { return 5; }
    public int d() { return 6; }
}
assert library.register(new Alice());
assert library.register(new Jeremy());
interface Bob {
    int a();
    int b();
    int c();
    int d();
}
final Bob bob = library.lookFor(Bob.class);
assert bob != null;
assert bob.a() == 1; // Populated from Alice
assert bob.b() == 2; // Populated from Alice
assert bob.c() == 5; // Populated from Jeremy
assert bob.d() == 6; // Populated from Jeremy
```


(Remote Library)
Exporting a remote object, then accessing it using a `lookFor` query.

```java 
final Library<Remote> library = new RemoteLibrary();
interface Jeremy extends Remote { // We need an interface for the RMI to stub
    int a() throws RemoteException;
    int b() throws RemoteException;
}
class Bob implements Jeremy {
    public int a() throws RemoteException { return 1; };
    public int b() throws RemoteException { return 2; };
}
assert library.register(new Bob()); // This actually exports the Jeremy interface
interface Alice {
    int a();
    int b();
}
final Alice alice = library.lookFor(Alice.class); // We lookup the Jeremy interface
assert alice != null;
assert alice.a() == 1;
assert alice.b() == 2;
```
