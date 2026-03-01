# Brain Up - Frontend

[![GitHub Stars](https://img.shields.io/github/stars/Brain-up/brn?style=flat-square)](https://github.com/Brain-up/brn)
[![License: CC0-1.0](https://img.shields.io/badge/License-CC0_1.0-lightgrey.svg?style=flat-square)](https://creativecommons.org/publicdomain/zero/1.0/)
[![Website](https://img.shields.io/website?url=https%3A%2F%2Fbrainup.site&style=flat-square&label=brainup.site)](https://brainup.site)

**[Brain Up](https://brainup.site)** is a web application for auditory training, designed to help people with central auditory processing deficits improve their listening skills. It supports children (7+), adults, and elderly users, including cochlear implant and hearing aid wearers, through progressive series of audio exercises.

This directory contains the **frontend** application — an Ember.js Octane SPA that drives the user-facing experience.

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| **Framework** | [Ember.js](https://emberjs.com/) 4.12 (Octane) |
| **Language** | TypeScript 5.9 (strict checks enabled) |
| **Data** | [WarpDrive](https://github.com/emberjs/data) 5.8.1 — schema-driven, replaces ember-data |
| **Styling** | Tailwind CSS + PostCSS + PurgeCSS |
| **Auth** | Firebase + ember-simple-auth |
| **Audio** | Tone.js |
| **Charts** | Billboard.js |
| **i18n** | ember-intl (English, Russian) |
| **Testing** | QUnit + ember-cli-mirage + ember-cli-code-coverage |
| **CI** | GitHub Actions |

## Architecture

```
app/
├── authenticators/       # Firebase & OAuth2 auth strategies
├── components/           # 30+ Glimmer components (task-player, audio-player, skeleton, ui/...)
├── controllers/          # Route-specific state
├── handlers/             # WarpDrive request pipeline handlers
│   ├── auth-handler      #   → injects Firebase auth tokens
│   └── brn-api-handler   #   → normalizes REST → JSON:API
├── helpers/              # Template helpers
├── modifiers/            # Glimmer element modifiers
├── routes/               # Route definitions & model hooks
├── schemas/              # WarpDrive model schemas (15 types)
├── services/             # Singletons (store, audio, network, stats, timers, ...)
├── styles/               # Tailwind config & app CSS
├── templates/            # Handlebars templates (incl. skeleton loading states)
├── transformations/      # Custom field transforms (full-date, array)
└── utils/                # Shared utilities
```

### Data Flow

Requests go through a WarpDrive `RequestManager` pipeline:

```
Request → AuthHandler → BrnApiHandler → Fetch → Cache
            (token)       (REST→JSON:API)
```

Schemas in `app/schemas/` define all model types declaratively — no class-based `@attr` models.

## Prerequisites

- [Node.js](https://nodejs.org/) >= 22 (managed via [Volta](https://volta.sh/))
- [Yarn](https://yarnpkg.com/) 1.x
- [Watchman](https://facebook.github.io/watchman/docs/install.html)
- [Google Chrome](https://google.com/chrome/) (for tests)

## Getting Started

```bash
# Install Volta (manages Node & Yarn versions automatically)
curl https://get.volta.sh | bash

# Clone and install
git clone https://github.com/Brain-up/brn.git
cd brn/frontend
yarn install
```

## Development

| Command | Description |
|---------|-------------|
| `yarn local` | Dev server with local API (`localhost:8081`) |
| `yarn remote` | Dev server with production API (`www.brainup.site`) |
| `yarn mirage` | Dev server with mocked API (no backend needed) |
| `yarn develop` | Dev server with Firebase dev environment |

App runs at **http://localhost:4200**.

### Test Accounts (on [brainup.site](https://brainup.site))

| Role | Email | Password |
|------|-------|----------|
| User | `default@default.ru` | `password` |
| Specialist | `default2@default.ru` | `password` |

## Testing

```bash
yarn test              # Lint + tests + coverage
yarn test:ember        # Ember tests only
yarn test:coverage     # Generate coverage report (open coverage/index.html)
ember test --server    # Watch mode
```

## Linting

```bash
yarn lint              # Run all linters
yarn lint:js           # ESLint
yarn lint:hbs          # Template lint
yarn lint:fix          # Auto-fix all
```

Pre-commit hooks (Husky + lint-staged) automatically format and lint staged files.

## Building

```bash
yarn build             # Production build (minified, fingerprinted, PurgeCSS)
```

## Key Routes

| Path | Description |
|------|-------------|
| `/groups` | Exercise category listing |
| `/groups/:group_id/series/:series_id/subgroup/:subgroup_id/exercise/:exercise_id/task/:task_id` | Full exercise flow |
| `/login` | Authentication |
| `/registration` | User sign-up |
| `/profile` | Account settings |
| `/profile/statistics` | Progress tracking & charts |
| `/contributors` | Open-source contributors |

## License

[CC0 1.0 Universal](https://creativecommons.org/publicdomain/zero/1.0/)
