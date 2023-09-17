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
If your text has a variable length, you should consider specifying a fixed total length for the result. The length of the result will then be the same no matter how long the input is:

    Obfuscator obfuscator = Obfuscator.portion()
            .keepAtStart(2)
            .keepAtEnd(2)
            .withFixedTotalLength(6)
            .build();
    CharSequence obfuscated = obfuscator.obfuscateText("Hello World");
    // obfuscated represents "He**ld"
    obfuscated = obfuscator.obfuscateText("foo");
    // obfuscated represents "fo**oo"

Note that if `keepAtStart` and `keepAtEnd` are both specified, parts of the input may be repeated in the result if the input's length is less than the combined number of characters to keep. This makes it harder to find the original input. For example, if in the example `foo` would be obfuscated into `fo***o` instead, it would be clear that the input was `foo`. Instead, it can now be anything that starts with `fo` and ends with `oo`.

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
            .withFixedTotalLength(27)
            .build();
    try (Writer obfuscatingWriter = obfuscator.streamTo(writer)) {
        obfuscatingWriter.write("username=admin");
        obfuscatingWriter.write("&password=hello");
    }
    // "username=admin&password=***" has been written to writer
    // note that writer has not been closed at this point

## Combining obfuscators

Sometimes the obfucators in this library alone cannot perform the obfuscation you need. For instance, if you want to obfuscate credit cards, but keep the first and last 4 characters. If the credit cards are all fixed length, `Obfuscator.portion` can do just that:

    Obfuscator obfuscator = Obfuscator.portion()
            .keepAtStart(4)
            .keepAtEnd(4)
            .build();
    CharSequence obfuscated = obfuscator.obfuscateText("1234567890123456");
    // obfuscated represents "1234********3456"

However, if you attempt to use such an obfuscator on only a part of a credit card, you could end up leaking parts of the credit card that you wanted to obfuscate:

    CharSequence incorrectlyObfuscated = obfuscator.obfuscateText("12345678901234");
    // incorrectlyObfuscated represents "1234******1234" where "1234********34" would probably be preferred

To overcome this issue, it's possible to combine obfuscators. The form is as follows:

* Specify the first obfuscator, and the input length to which it should be used.
* Specify any other obfuscators, and the input lengths to which they should be used. Note that each input length should be larger than the previous input length.
* Specify the obfuscator that will be used for the remainder.

For instance, for credit card numbers of exactly 16 characters, the above can also be written like this:

    Obfuscator obfuscator = Obfuscator.none().untilLength(4)
            .then(Obfuscator.all()).untilLength(12)
            .then(Obfuscator.none());

With this chaining, it's now possible to keep the first and last 4 characters, but with at least 8 characters in between:

    Obfuscator obfuscator = Obfuscator.none().untilLength(4)
            .then(Obfuscator.portion()
                    .keepAtEnd(4)
                    .atLeastFromStart(8)
                    .build());
    CharSequence obfuscated = obfuscator.obfuscateText("12345678901234");
    // obfuscated represents "1234********34"

## Splitting text during obfuscation

To make it easier to create obfuscators for structured text like email addresses, use a <a href="apidocs/com/github/robtimus/obfuscation/Obfuscator.SplitPoint.html">SplitPoint</a>. For instance:

    // Keep the domain as-is
    Obfuscator localPartObfuscator = Obfuscator.portion()
            .keepAtStart(1)
            .keepAtEnd(1)
            .withFixedTotalLength(8)
            .build();
    Obfuscator domainObfuscator = Obfuscator.none();
    Obfuscator obfuscator = Obfuscator.SplitPoint.atFirst('@').splitTo(localPartObfuscator, domainObfuscator);
    CharSequence obfuscated = obfuscator.obfuscateText("test@example.org");
    // obfuscated represents "t******t@example.org"

To obfuscate the domain except for the TLD, use a nested `SplitPoint`:

    // Keep only the TLD of the domain
    Obfuscator localPartObfuscator = Obfuscator.portion()
            .keepAtStart(1)
            .keepAtEnd(1)
            .withFixedTotalLength(8)
            .build();
    Obfuscator domainObfuscator = Obfuscator.SplitPoint.atLast('.').splitTo(Obfuscator.all(), Obfuscator.none());
    Obfuscator obfuscator = Obfuscator.SplitPoint.atFirst('@').splitTo(localPartObfuscator, domainObfuscator);
    CharSequence obfuscated = obfuscator.obfuscateText("test@example.org");
    // obfuscated represents "t******t@*******.org"
