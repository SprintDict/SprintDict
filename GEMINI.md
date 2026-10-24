# AI Coding Instructions for SprintDict

## Coding Style & Guidelines
- **Refrain from using ternary expressions** because they make it difficult to see what code execution paths are covered by unit tests. Prefer standard `if-else` blocks so branch coverage can be accurately measured and verified.
- **Prefer using the "return early" pattern** when possible (e.g. `if (check) { return; }`) to minimize unnecessary nesting and keep code readable.
- The **`net.bancer.sparkdict.domain`** package must contain only classes that do **not** import Android (`android.*`) classes.
- **Always generate docblocks** (Javadoc / KDoc) for new constants, fields, methods, and classes.
- **Add unit tests** to cover new or changed code if it has not already been covered or if it represents a new scenario.
- **Add integration tests** to cover new scenarios and user flows.
