# Brain Up

[![Gitpod ready-to-code](https://img.shields.io/badge/Gitpod-ready--to--code-blue?logo=gitpod)](https://gitpod.io/#https://github.com/Brain-up/brn)
[![GitHub Stars](https://img.shields.io/github/stars/Brain-up/brn?style=flat-square)](https://github.com/Brain-up/brn)
[![License: CC0-1.0](https://img.shields.io/badge/License-CC0_1.0-lightgrey.svg?style=flat-square)](https://creativecommons.org/publicdomain/zero/1.0/)
[![Website](https://img.shields.io/website?url=https%3A%2F%2Fbrainup.site&style=flat-square&label=brainup.site)](https://brainup.site)

**[Brain Up](https://brainup.site)** is a web application for auditory training, designed to help people with central auditory processing deficits (with or without hearing loss) improve their listening skills. It targets children (7+), adults, and elderly users — including cochlear implant and hearing aid wearers — through progressive series of audio exercises.

> **Try it now:** https://brainup.site
> Test user: `default@default.ru` / `password` | Test specialist: `default2@default.ru` / `password`

---

## Project Structure

```
brn/
├── frontend/             # User-facing SPA (Ember.js / TypeScript)
├── frontend-angular/     # Admin panel (Angular)
├── src/                  # Backend API (Kotlin / Spring Boot)
├── api-contract/         # REST API contract (RAML)
├── docker-compose.yml    # Full-stack local setup
└── .github/workflows/    # CI/CD pipelines
```

## Tech Stack

| Layer | Technology |
|-------|------------|
| **Backend** | Kotlin + Spring Boot, REST API |
| **Frontend (user)** | Ember.js 4.12 (Octane), TypeScript, Tailwind CSS |
| **Frontend (admin)** | Angular |
| **Database** | PostgreSQL 13 |
| **Auth** | Firebase |
| **CI/CD** | GitHub Actions, Docker |
| **Testing** | JUnit + TestContainers (BE), QUnit + Mirage (FE) |

## Quick Start

### Prerequisites

- [Docker](https://docs.docker.com/get-docker/)
- [Node.js](https://nodejs.org/) >= 22 (via [Volta](https://volta.sh/)) — for frontend
- [JDK 17+](https://adoptium.net/) — for backend
- [Yarn](https://yarnpkg.com/) 1.x — for frontend

### Run Everything with Docker Compose

```bash
git clone https://github.com/Brain-up/brn.git
cd brn
docker compose up --build
```

- **API + Swagger:** http://localhost:8081/api/swagger-ui.html
- **Public Swagger:** https://brainup.site/admin/swagger

### Run Frontend Only

```bash
cd frontend
yarn install
yarn local     # → localhost:4200, proxies API to localhost:8081
yarn mirage    # → localhost:4200, mocked API (no backend needed)
```

See [frontend/README.md](./frontend/README.md) for full details.

### Run Backend Only

```bash
# Start PostgreSQL
docker run --name postgres_dev -d -p 5432:5432 \
  -e POSTGRES_DB=brn -e POSTGRES_PASSWORD=admin -e POSTGRES_USER=admin postgres:13

# Build and run
./gradlew build
# Run Application.kt from your IDE
```

### Run Admin Panel

See [frontend-angular/README.md](./frontend-angular/README.md).

---

## Resources

| Resource | Link |
|----------|------|
| Documentation (Wiki) | https://github.com/Brain-up/brn/wiki |
| Product Vision | [Product Vision](https://github.com/Brain-up/brn/wiki/BRN:-Product-Vision) |
| Platform Structure | [Platform Schema](https://github.com/Brain-up/brn/wiki/Some-platform-organization-description) |
| Roadmap | [roadmap.md](./roadmap.md) |
| Issues | https://github.com/Brain-up/brn/issues |
| Coding Standards | [Coding Standards](https://github.com/Brain-up/brn/wiki/Coding-Standards) |
| Sonar | https://sonarcloud.io/code?id=Brain-up_brn |

### Scientific Background

- [Scientific basis (PubMed)](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC6417420/)
- [Analogous applications overview](https://docs.google.com/document/d/1UKPE9ykj16JG6FZKoxqCoXI6ehWRhyXZnDNxPcaFLWQ/edit)

### Articles About the Project

- [WeAreCommunity (2020)](https://wearecommunity.io/communities/community-platform/articles/753)
- [Habr (2020)](https://habr.com/ru/company/epam_systems/blog/530824)

---

## Contributing

We welcome contributors of all skill levels!

### How to Join

- **Telegram:** Contact Elena Moshnikova (project founder) — [https://t.me/ElenaBrainUp](https://t.me/ElenaBrainUp)
- **Email:** [brainupspbproject@gmail.com](mailto:brainupspbproject@gmail.com)
- **Dev chat:** https://t.me/+R-6ThlxgP5QyZmMy

### We Are Looking For

- **Frontend developers** (Ember.js, Angular)
- **Backend developers** (Kotlin, Java)
- **Mobile developers** (iOS, Android)
- **DevOps engineers** (CI/CD, Docker)
- **UX designers** (research, prototyping, user retention)
- **QA engineers** (manual and automation)
- **Content specialists** (Russian, English, sign language)
- **Artists** for exercise illustrations

### Development Workflow

1. Pick an [issue](https://github.com/Brain-up/brn/issues) and discuss with [@ElenaSpb](https://github.com/ElenaSpb)
2. Create a branch: `#issue_number-short-description`
3. Implement with tests, follow [coding standards](https://github.com/Brain-up/brn/wiki/Coding-Standards)
4. Open a pull request — at least one review required
5. Maintainer merges after approval

### Code Style

- **Backend:** Run `./gradlew ktlint` before committing. Auto-fix with `./gradlew ktlintFormat`
- **Frontend:** Pre-commit hooks handle formatting automatically (Husky + lint-staged)
- **Flyway migrations:** Name as `V2yearmonthday_taskNumber` (e.g. `V220210804_899`)

---

## Thanks

- **EPAM** — infrastructure support and volunteer program
- **JetBrains** — IDE licenses
- **Selectel** — hosting
- **GitHub** — code hosting and CI/CD
- **Yandex** — Speech Kit service

And thanks to all [volunteers](https://brainup.site/contributors) for their contributions!

## License

[CC0 1.0 Universal](https://creativecommons.org/publicdomain/zero/1.0/) — public domain dedication.
