# AI Agent Guidelines

This file dictactes how the AntiGravity AI agent should behave for this project.
The AI agent must follow a planning-first approach.

Rules:
- Always read the documentation inside `/docs` before generating code.
- Follow the architecture defined in `docs/06_architecture.md`.
- Generate Kotlin code only for Android components.
- the UI must use traditional **XML Views**. Jetpack Compose is NOT allowed.
- Generate code step-by-step following the implementation plan in `docs/08_implementation_plan.md`.
- Keep changes atomic and do not generate exceptionally large files simultaneously unless necessary.
- Respect all rules listed here.
