# ISSUES.md — Bug Tracker & Solutions Log
# FinTrack: Personal Finance REST API

## Resolved ✅

### ISSUE-001: @NonNull annotation redefinition breaks CI compilation
**Date:** 2026-03-18
**Status:** ✅ Resolved
**Category:** Bug
**Files:** src/main/java/com/fintrack/common/security/JwtAuthFilter.java

**What happened:** CI failed immediately during compilation. Local build passed.
**Error:** Illegal redefinition of parameter — @NonNull on HttpServletRequest, HttpServletResponse, FilterChain parameters
**Root cause:** maven-compiler-plugin in strict mode treats @NonNull on already-typed parameters as illegal redefinition. IDE ignored it, CI did not.
**Solution:** Removed @NonNull annotations and unused import from JwtAuthFilter.java
**Prevention:** Never add @NonNull to method parameters that already have explicit types. Only use Lombok's @NonNull on fields, not on doFilterInternal parameters.

---
### ISSUE-002: Maven build failure due to missing Lombok version in annotationProcessorPaths
**Date:** 2026-03-18
**Status:** ✅ Resolved
**Category:** Bug
**Files:** `pom.xml`

**What happened:** The Docker build failed during `mvn clean package`.
**Error:** `Resolution of annotationProcessorPath dependencies failed: version can neither be null, empty nor blank`
**Root cause:** Maven's `maven-compiler-plugin` requires explicitly specified `<version>` tags for dependencies inside `<annotationProcessorPaths>`, even if a parent POM like Spring Boot provides dependency management. The `lombok` path was missing its version.
**Solution:** Added `<version>${lombok.version}</version>` to the `lombok` entry within `annotationProcessorPaths`.
**Prevention:** Always explicitly declare `${lombok.version}` when defining `annotationProcessorPaths` to use MapStruct alongside Lombok.
