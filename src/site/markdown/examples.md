<head>
  <title>Examples</title>
</head>

## Obfuscating all characters

    CharSequence obfuscated = Obfuscator.all().obfuscateText("Hello World");
    // obfuscated represents "***********"

Note: using this obfuscator still leaks out information about the length of the original text. One of the following two is more secure.

## Obfuscating with a fixed length

    CharSequence obfuscated = Obfuscator.fixedLength(5).obfuscateText("Hello World");
    // obfuscated represents "*****"

## Obfuscating with a fixed value

    CharSequence obfuscated = Obfuscator.fixedValue("foo").obfuscateText("Hello World");
    // obfuscated represents "foo"

## Obfuscating portions of text

While the above examples are simple, they are not very flexible. Using `Obfuscator.portion` you can build obfuscators that obfuscate only specific portions of text. Some examples:

### Obfuscating all but the last 4 characters

Useful for obfuscating values like credit card numbers.

    CharSequence obfuscated = Obfuscator.portion()
            .keepAtEnd(4)
            .build()
            .obfuscateText("1234567890123456");
    // obfuscated represents "************3456"

It's advised to use `atLeastFromStart`, to make sure that values of fewer than 16 characters are still obfuscated properly:

    CharSequence obfuscated = Obfuscator.portion()
            .keepAtEnd(4)
            .atLeastFromStart(12)
            .build()
            .obfuscateText("1234567890");
    // obfuscated represents "**********" and not "******7890"

### Obfuscating only the last 2 characters

Useful for obfuscating values like zip codes, where the first part is not as sensitive as the full zip code:

    CharSequence obfuscated = Obfuscator.portion()
            .keepAtStart(Integer.MAX_VALUE)
            .atLeastFromEnd(2)
            .build()
            .obfuscateText("SW1A 2AA");
    // obfuscated represents "SW1A 2**"

Here, the `keepAtStart` instructs the obfuscator to keep everything; however, `atLeastFromEnd` overrides that partly to ensure that the last two characters are obfuscated regardless of the value specified by `keepAtStart`.

### Using a fixed length

Similar to using `Obfuscator.all`, by default an obfuscator built using `Obfuscator.portion` leaks out the length of the original text.
If your text has a variable length, you should consider providing a fixed length to the result. The length of the obfuscated part will then be the same no matter how long the input is:

    Obfuscator obfuscator = Obfuscator.portion()
            .keepAtStart(2)
            .keepAtEnd(2)
            .withFixedLength(3)
            .build();
    CharSequence obfuscated = obfuscator.obfuscateText("Hello World");
    // obfuscated represents "He***ld"
    obfuscated = obfuscator.obfuscateText("foo");
    // obfuscated represents "fo***o"

## Using a predefined function

Assuming the current class has a method `CharSequence obfuscate(CharSequence s)`:

    Obfuscator obfuscator = Obfuscator.fromFunction(this::obfuscate);
    CharSequence obfuscated = obfuscator.obfuscateText("Hello World");
    // obfuscated is the result of calling obfuscate("Hello World")

Note that the input argument needs to be either `CharSequence` or `Object`; the return type of the method may be a sub type of `CharSequence` like `String` or `StringBuilder`.

If the method's input argument is instead `String` you need to use a lambda that calls `toString()` on its argument before passing it to the method:

    Obfuscator obfuscator = Obfuscator.fromFunction(s -> obfuscate(s.toString()));
    CharSequence obfuscated = obfuscator.obfuscateText("Hello World");
    // obfuscated is the result of calling obfuscate("Hello World".toString())

## Obfuscating objects

    LocalDate date = LocalDate.of(2020, 1, 1);
    Obfuscated<LocalDate> obfuscated = Obfuscator.portion()
            .keepAtStart(8)
            .build()
            .obfuscateObject(date);
    // obfuscated.toString() returns "2020-01-**"
    // the date itself can be retrieved using obfuscated.value()

In this example, the obfuscation will always be the same. That means that the result of obfuscation can be cached, to prevent it from being recalculated every time:

    LocalDate date = LocalDate.of(2020, 1, 1);
    Obfuscated<LocalDate> obfuscated = Obfuscator.portion()
            .keepAtStart(8)
            .build()
            .obfuscateObject(date)
            .cached();
    // obfuscated.toString() returns the same string instance every time it's called

## Obfuscating collections

    List<String> list = new ArrayList<>();
    list.add("hello");
    list.add("world");
    list = Obfuscator.fixedLength(3).obfuscateList(list);
    // list is just like any other List, apart from toString;
    // that returns "[***, ***]"

    Set<String> set = new HashSet<>();
    set.add("hello");
    set.add("world");
    set = Obfuscator.fixedLength(3).obfuscateSet(set);
    // list is just like any other Set, apart from toString;
    // that returns "[***, ***]"

    Collection<String> collection = new ArrayList<>();
    collection.add("hello");
    collection.add("world");
    collection = Obfuscator.fixedLength(3).obfuscateCollection(collection);
    // list is just like any other Collection, apart from toString;
    // that returns "[***, ***]"

## Obfuscating maps

To obfuscate each value the same way:

    Map<String, String> map = new LinkedHashMap<>();
    map.put("username", "admin");
    map.put("password", "hello");
    map = Obfuscator.fixedLength(3).obfuscateMap(map);
    // map is just like any other Map, apart from toString;
    // that returns "{username=***, password=***}"

To provide separate obfuscation per entry:

    Map<String, String> map = new LinkedHashMap<>();
    map.put("username", "admin");
    map.put("password", "hello");
    map = MapObfuscator.<String, String>builder()
            .withKey("password", Obfuscator.fixedLength(3))
            .build()
            .obfuscateMap(map);
    // map is just like any other Map, apart from toString;
    // that returns "{username=admin, password=***}"

## Obfuscating Properties objects:

    Properties properties = new Properties();
    properties.put("username", "admin");
    properties.put("password", "hello");
    properties = PropertiesObfuscator.builder()
            .withProperty("password", Obfuscator.fixedLength(3))
            .build()
            .obfuscateProperties(properties);
    // properties is just like any other Properties, apart from:
    // - toString; that returns "{username=admin, password=***}"
    //   or "{password=***, username=admin}"
    // - list; that prints "username=admin" and "password=***"

## Streaming obfuscation

    // assume that writer is an existing Writer
    Obfuscator obfuscator = Obfuscator.portion()
            .keepAtStart(24)
            .withFixedLength(3)
            .build();
    try (Writer obfuscatingWriter = obfuscator.streamTo(writer)) {
        obfuscatingWriter.write("username=admin");
        obfuscatingWriter.write("&password=hello");
    }
    // "username=admin&password=***" has been written to writer
    // note that writer has not been closed at this point
