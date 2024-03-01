# Working on RPM builder

A few notes for working on this project.

## Conventional commits

Using conventional commits allows for an automated changelog generation, please make use of them.

## Running tests

Tests can be run using:

```bash
mvn verify -Pits
```

## Required tooling for testing

While the goal of the project is to support platform independent RPM processing, the tests do
require some Linux/RPM based tooling. It therefore is not possible to run this on a platform
missing looks like `rpm`.

However, there's a "devcontainer" in this repository, which has all the required tools.

