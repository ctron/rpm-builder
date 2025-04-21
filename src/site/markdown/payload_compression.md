# Payload compression

RPM packages can be compressed using one of several methods. All of these methods allow setting a custom
compression level as well as additional options.

## Configuration

Payload compression is configured using the `<payloadFlags>` element. For example:

```xml
    <configuration>
      <payloadFlags>
          <coding>zstd</coding>
          <level>19</level>
      </payloadFlags>
    </configuration>
```

### Payload coding

Payload `coding` must be one of the following compressors:

* none (uncompressed)
* gzip (default compressor, with a compression level of 9)
* bzip2
* lzma
* xz
* zstd

### Payload flags

| coding | level (number) | threads (number) | strategy (number)                              | windowLog (number) | smallMode (boolean) |
|--------|----------------|------------------|------------------------------------------------|--------------------|---------------------|
| none   | &cross;        | &cross;          | &cross;                                        | &cross;            | &cross;             |
| gzip   | &check; 0-9    | &cross;          | &check; 0 (default), 1 (filtered), 2 (huffman) | &cross;            | &cross;             |
| bzip2  | &check; 0-9    | &cross;          | &cross;                                        | &cross;            | &check; false, true |
| lzma   | &check; 0-9    | &check;          | &cross;                                        | &cross;            | &cross;             |
| xz     | &check; 0-9    | &check;          | &cross;                                        | &cross;            | &cross;             |
| zstd   | &check; 0-22   | &check;          | &cross;                                        | &check;            | &cross;             |

#### level

The compressors `gzip`, `lzma`, `bzip2`,and `xz` support a range of compression `level`s between 0 (no compression) and
9\. Additionally, `gzip` supports the value -1 for the default compression level. The compressor `zstd` supports a range
of compression levels between 0 (use default compression level) and 22.

#### threads

The compressors `lzma`, `xz`, and `zstd` support a number of `threads`. A value of 0 means that the compressor will use
`Runtime.getRuntime().availableProcessors()` threads. The compressors `gzip` and `bzip2` do not support this option.
The Java implementations of `lzma` and `xz` currently do not support multithreaded operation, so this value has no
effect.

#### strategy

The compressor `gzip` supports a `strategy` value. The default value is 0, which means that the compressor will
use the default strategy. A value of 1 means that the compressor will use a filtered strategy. A value of 2 means that
the compressor will use a huffman-only strategy.

#### windowLog

The compressor `zstd` supports a numeric `windowLog` value. The special value 0 will cause zstd to use the default
`windowLog` value.

#### smallMode

The compressor `bzip2` supports a `smallMode` boolean value. The Java implementation of `bzip2` does not support this,
so this value has no effect.

### Payload flags strings

 The payload flags are converted to a string representation in the RPM header.

| payload flags | description                                                        | valid for codings           |
|---------------|--------------------------------------------------------------------|-----------------------------|
| "9"           | level 9                                                            | gzip, bzip2, lzma, xz, zstd |
| "9h"          | level 9 huffman-only strategy                                      | gzip                        |
| "9f"          | level 9 filtered strategy                                          | gzip                        |
| "9s"          | level 9 small mode                                                 | bzip2                       |
| "7T16"        | level 7 using 16 threads                                           | lzma, xz, zstd              |
| "7T0"         | level 7 using `Runtime.getRuntime().availableProcessors()` threads | lzma, xz, zstd              |
| "7T"          | level 7 using `Runtime.getRuntime().availableProcessors()` threads | lzma, xz, zstd              |
| "7L"          | level 7 using the default window log value                         | zstd                        |
| "7L0"         | level 7 using the default window log value                         | zstd                        |
