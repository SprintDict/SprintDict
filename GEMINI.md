# AI Coding Instructions for SprintDict

## Coding Style & Guidelines
- **Refrain from using ternary expressions** because they make it difficult to see what code execution paths are covered by unit tests. Prefer standard `if-else` blocks so branch coverage can be accurately measured and verified.
- **Always generate docblocks** (Javadoc / KDoc) for new constants, fields, methods, and classes.
- **Add unit tests** to cover new or changed code if it has not already been covered or if it represents a new scenario.
