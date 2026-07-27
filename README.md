# Vortex21 🌪️

The Unified High-Performance Engine for WITSML 2.1

*_Vortex21_* is a cross-platform, multi-language processing engine designed to handle the complexities of the WITSML 2.1 standard. At its core is a robust, highly optimized C/C++ engine (powered by gSOAP) that ensures identical validation, parsing, and serialization logic across every supported environment.

## One Core, Universal Bridges

Vortex21 follows a "Write Once, Run Everywhere" philosophy at the native level. By maintaining a single logic base in C, we eliminate the risk of divergent parsing results between different tech stacks.

| Language | Integration Method | Best For... |
| :--- | :--- | :--- |
| **C / C++** | Native Linkage | Embedded systems and core engine development. |
| **Java** | JNI (Java Native Interface) | Enterprise middleware and Big Data pipelines (WITSML 2.1). |
| **Python 3** | Ctypes / C-API | Data Science, automation, and rapid prototyping. |
| **Node.js** | N-API Addons | High-concurrency cloud services and real-time dashboards. |
| **Rust** | FFI (Foreign Function Interface) | Memory-safe systems programming with native speed. |
| **Go** | CGO | Scalable microservices and high-performance backend tools. |
| **.NET** | Native extension | Wrapper for .NET/C# |
| **PHP** | Native Extension / FFI | Web portals and legacy enterprise integration. |

## The Unified Logic Architecture

Vortex21 is architected to minimize overhead and maximize consistency:

- **The Native Core (The "Vortex"):** Handles the heavy lifting—XML schema validation, BSON serialization, SIMD/AVX instructions (`-O3 -march=native`), and the specialized AutoDetect mode.
- **Zero-Copy Design:** Where supported (like Java's DirectByteBuffer or Node.js Buffers), the engine interacts directly with memory via `jniBeginRead`/`jniEndRead` to avoid costly data duplication and JVM Garbage Collection overhead.
- **Standardized Exception Handling:** Whether you are in a try-catch block in Java or checking a Result in Rust, the underlying error handling provides consistent error codes and diagnostic messages across all languages.

---

## 🛠️ Build Management & Commands

The native layer compilation is driven by a highly parallelized `Makefile`. 

### Native Compilation
To trigger the full compilation utilizing parallel jobs (all available processor cores minus one):
```sh
make -j$(($(nproc) - 1))
```

⚠️ **Note on First Compilation:** The initial build clones and compiles the libmongoc dependency alongside the WITSML 2.1 heavy business logic objects. This process can take anywhere between 10 to 20 minutes on the first run. Subsequent compilations will be incremental and fast.

### Running examples

```sh
make -j$(($(nproc) - 1)) java_examples
```

### Maintenance and Cleanup Tasks

```sh
# Clean up Java artifacts and local sample builds
make clean

# Remove the cloned libmongoc repository and its compiled binaries
make remove_bson

# Wipe the pre-compiled WITSML 2.1 business logic rules objects
make remove_pre
```

## 🚀 BETA Tester Quick Start & Execution Output

### Prerequisites
* **Operating System:** Linux x86_64
* **Java Runtime:** Java 17 or higher installed (`java --version`)

### Running the Pre-compiled Beta Artifacts
You don't need to build from scratch to test the core pipeline. Pre-compiled binaries are provided directly inside the repository. Navigate to the example folder and execute the runtime package passing a valid WITSML 2.1 XML file:

```sh
cd SimpleExample/compiled
java -jar SimpleExample-1.0-SNAPSHOT.jar Log.xml
```

To verify the engine's compilation, raw performance metrics, and serialization workflows, you can load a sample WITSML document using the standalone beta tester application.

Running the [Example](https://github.com/devfabiosilva/Vortex21/tree/master/examples/java/app/SimpleExample/compiled)

Expected Benchmark Execution Log

Upon parsing, the native core triggers the internal statistics profiler, measuring execution down to the exact CPU cycle and hardware constraint:

```sh
Welcome to Vortex21 0.1.0 beta version tester
Opening file Log.xml in AUTODETECT MODE ...

Detected object: Log
=== WITSML 2.1 STATISTICS SUMMARY FOR Log ===
│
└──[INPUT] - WITSML 2.1 XML Stream or File
   │
   ├─Stream or File Size : 0,154 MB (161610 bytes)
   │
   ├─[PHASE 1] - WITSML 2.1 to C pointer structs
   │  ├─CPB.............: 36,38
   │  ├─CPU Cycles......: 5879530
   │  ├─Throughput......: 62,86 MB/s
   │  ├─Total time......: 2,452 ms (2451669 ns)
   │  └─Used memory.....: 0,242 MB (253968 bytes)
   │
   ├─[PHASE 2] - C pointer structs to BSON Object | JSON String
   │  │
   │  ├─[BSON OBJECT]
   │  │  ├─CPB.............: 5,58
   │  │  ├─CPU Cycles......: 901798
   │  │  ├─Throughput......: 54,440 MB/s (Phase1 + Phase2)
   │  │  ├─Total time......: 0,379 ms (379374 ns)
   │  │  └─Used memory.....: 0,128 MB (133792 bytes)
   │  ├─[JSON String]
   │  │  ├─CPB.............: 11,80
   │  │  ├─CPU Cycles......: 1906896
   │  │  ├─Throughput......: 47,469 MB/s (Phase1 + Phase2)
   │  │  ├─Total time......: 0,795 ms (795174 ns)
   │  │  └─Used memory.....: 0,063 MB (65584 bytes)
   │  │
   │  └─[BSON/JSON DOCUMENT STATISTICS]
   │     │
   │     ├─[INVENTORY: COMPLEX TYPES]
   │     │  ├─Measures........: 98
   │     │  ├─Date times......: 107
   │     │  ├─Arrays..........: 113
   │     │  ├─Costs...........: 0
   │     │  └─Event types.....: 0
   │     │
   │     ├─[INVENTORY: PRIMITIVES]
   │     │  ├─Strings.........: 850
   │     │  ├─Enums...........: 114
   │     │  ├─Booleans........: 7
   │     │  ├─Long64's........: 89
   │     │  ├─Doubles.........: 26
   │     │  ├─Ints............: 0
   │     │  └─Shorts..........: 0
   │     │
   │     └─TOTAL OBJECTS.......: 1404 into Log object
   │
   └─[SUMMARY TOTAL (Phase 1 + Phase 2)]
      ├─CPB.............: 53,76
      ├─CPU Cycles......: 8688224
      ├─Throughput......: 42,50 MB/s (Phase1 + Phase2)
      ├─Total time......: 3,626 ms (3626217 ns)
      └─Used memory.....: 0,43 MB (453344 bytes)
Saving file Log.xml.bson ...

Saved: Log.xml.bson
Saving file Log.xml.json ...

Saved: Log.xml.json
Vortex21 instance closed successfully with status code 0
```

🎯 Outputs Generated: The execution successfully flushes the structural parsing trees, producing Log.xml.bson (high-performance binary database ready) and Log.xml.json (human-readable debug string) directly in the execution context path.

## 🔄 Data Transformation Blueprint (XML to BSON/JSON Example)

The engine transparently maps complex, heavily nested WITSML 2.1 XML models into structural BSON, providing a native translation layer for modern storage and query architectures.

```xml
<rdw211:Log uuid="523e4568-e89b-12d3-a456-426614174000" schemaVersion="any version" objectVersion="any object version">
  <rdw212:Aliases xsi:type="rdw212:ObjectAlias" authority="log authority">
    <rdw212:Identifier xsi:type="rdw212:String64">Log identifier</rdw212:Identifier>
    <rdw212:IdentifierKind xsi:type="rdw212:AliasIdentifierKindExt">Log indentifier kind</rdw212:IdentifierKind>
    <rdw212:Description xsi:type="rdw212:String2000">
     Log description (max 2000 char allowed)
    </rdw212:Description>
    <rdw212:EffectiveDateTime xsi:type="rdw212:TimeStamp">2026-01-05T18:39:47Z</rdw212:EffectiveDateTime>
    <rdw212:TerminationDateTime xsi:type="rdw212:TimeStamp">2026-02-05T19:39:48Z</rdw212:TerminationDateTime>
  </rdw212:Aliases>
  <rdw212:Citation xsi:type="rdw212:Citation">
    <rdw212:Title xsi:type="rdw212:String256">
     Citation title string (max 256 chars)
    </rdw212:Title>
    <rdw212:Originator xsi:type="rdw212:String64">b</rdw212:Originator>
    <rdw212:Creation xsi:type="rdw212:TimeStamp">2026-02-05T19:39:47Z</rdw212:Creation>
    <rdw212:Format xsi:type="rdw212:String2000">c</rdw212:Format>
    <rdw212:Editor xsi:type="rdw212:String64">d</rdw212:Editor>
    ...
  </rdw212:Citation>
  ...
</rdw211:Log>

```

```json
{
    "Log": {
        "#attributes": {
            "uuid": "523e4568-e89b-12d3-a456-426614174000",
            "schemaVersion": "any version",
            "objectVersion": "any object version"
        },
        "Aliases": [
            {
                "#attributes": {
                    "authority": "log authority"
                },
                "Identifier": "Log identifier",
                "IdentifierKind": "Log indentifier kind",
                "Description": "\n     Log description (max 2000 char allowed)\n    ",
                "EffectiveDateTime": {
                    "$date": "2026-01-05T18:39:47Z"
                },
                "TerminationDateTime": {
                    "$date": "2026-02-05T19:39:48Z"
                }
            }
        ],
        "Citation": {
            "Title": "\n     Citation title string (max 256 chars)\n    ",
            "Originator": "b",
            "Creation": {
                "$date": "2026-02-05T19:39:47Z"
            },
            "Format": "c",
            "Editor": "d",
            ...
        }
        ...
    }
    ...
}
```

## ⚠️ Known Issues & Environment Behaviors

### Extended JSON String Serialization (Textual Dumps)
When using optional helper utilities to export binary BSON into human-readable Extended JSON text, the string formatter (`libbson 2.2.3` core) may inherit the host system's local decimal separator (`LC_NUMERIC`) during float-to-string slow-path conversions. See details [here](https://jira.mongodb.org/browse/CDRIVER-6377)

*   **Impact:** Complex floating-point expressions may render a comma `,` instead of a dot `.` inside textual JSON representations (e.g., in `relaxed` or `canonical` modes), violating strict RFC 8259 syntax specifications for text engines. **The use of JSON strings is discouraged. For fast performance, use BSON instead.**

*   **Status & Mitigation:** Production-level BSON binary data remains 100% accurate, safe, and completely unaffected. This issue is isolated strictly to the third-party visual text export module. A compliance ticket is scheduled for the upstream repository, and thread-level locale isolation (`uselocale`) is targeted for the next sprint.

## Project Vision

To provide the energy industry with a single, reliable, and lightning-fast tool that bridges the gap between low-level sensor data and high-level application logic, regardless of the programming language chosen.

_The core parsing logic is based on the optimization techniques described [here](https://www.linkedin.com/posts/devfabiosilva_witsml-etp-osdu-activity-7444730290482315264-WCb5/?utm_source=share&utm_medium=member_desktop&rcm=ACoAAC4WCXQBKw6A3l9SOV1M9CWPXZOWL20H8b8)_

