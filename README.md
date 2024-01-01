# Yamcs QuickStart

This repository holds the source code to start Yamcs: a C2 (Command-and-Control) service for managing space-fairing missions.

&nbsp;

***

# Prerequisites

* Java 21+
* Linux x64/aarch64, macOS x64, or Windows x64
* Maven >= 3.1

### **Note:** Yamcs does not currently support running on Apple M1 or M2. We hope to address this soon.

&nbsp;

***

# Quick start

Running Yamcs via Maven:

* `mvn yamcs:run`

Running Yamcs via Maven in debug mode:

* `mvn yamcs:debug`

# Packaging and Installation

Packaging binary executables for Yamcs:

* `mvn package`

&nbsp;

For docker-based development see the [Docker instructions](./docker/README.md).
