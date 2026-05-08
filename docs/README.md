# Avaje Inject — Docs

This directory contains the reference documentation and step-by-step guides for [avaje-inject](https://avaje.io/inject/).

## Key files

| File | Contents |
|------|----------|
| [LIBRARY.md](LIBRARY.md) | Full library reference — identity, API, annotations, use cases, performance, and AI agent instructions. Start here for capability questions. |
| [guides/README.md](guides/README.md) | Index of all step-by-step guides. |

## Guides

| Guide | What it covers |
|-------|---------------|
| [guides/creating-beans.md](guides/creating-beans.md) | Annotate classes as beans with `@Singleton`, `@Prototype`, and `@Factory` |
| [guides/dependency-injection.md](guides/dependency-injection.md) | Inject dependencies via constructor, field, and method injection |
| [guides/factory-methods.md](guides/factory-methods.md) | Use `@Factory` + `@Bean` methods to create and configure beans |
| [guides/lifecycle-hooks.md](guides/lifecycle-hooks.md) | Initialize and clean up beans with `@PostConstruct` and `@PreDestroy` |
| [guides/qualifiers.md](guides/qualifiers.md) | Disambiguate multiple implementations with `@Named` and `@Primary` |
| [guides/testing.md](guides/testing.md) | Write integration tests with `avaje-inject-test` and `@InjectTest` |
| [guides/native-image.md](guides/native-image.md) | Build GraalVM native images — zero extra configuration needed |

## Agent navigation tip

- **Capability / API questions** → open [`LIBRARY.md`](LIBRARY.md)
- **How-to / task questions** → open the relevant guide above
