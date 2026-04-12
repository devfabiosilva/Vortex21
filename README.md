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
| **PHP** | Native Extension / FFI | Web portals and legacy enterprise integration. |

## The Unified Logic Architecture

Vortex21 is architected to minimize overhead and maximize consistency:

    1- The Native Core (The "Vortex"): Handles the heavy lifting—XML schema validation, BSON serialization, and the specialized AutoDetect mode.

    2- Zero-Copy Design: Where supported (like Java's DirectByteBuffer or Node.js Buffers), the engine interacts directly with memory to avoid costly data duplication.

    3- Standardized Exception Handling: Whether you are in a try-catch block in Java or checking a Result in Rust, the underlying error handling provides consistent error codes and diagnostic messages across all languages.

## Project Vision

To provide the energy industry with a single, reliable, and lightning-fast tool that bridges the gap between low-level sensor data and high-level application logic, regardless of the programming language chosen.


