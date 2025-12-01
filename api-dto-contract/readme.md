# Alexandra API DTO Contract

This module defines the shared DTOs used between Alexandra services:
compiler, oracle, registry, executor, trainer and UI.

Design goals:
- immutable Java records
- framework-agnostic
- JSON-serialization friendly
- safe to expose as a public contract
