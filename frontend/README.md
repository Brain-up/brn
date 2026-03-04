# brn user UI

This part of BRN project responsible for end-user UI.

## Tech Stack

* **Framework:** Ember.js 4.12 (Octane)
* **Data Layer:** [WarpDrive](https://github.com/emberjs/data) 5.8.1 (successor to ember-data)
* **Language:** TypeScript 5.9
* **Styling:** Tailwind CSS
* **Auth:** Firebase + ember-simple-auth
* **Testing:** QUnit + ember-cli-mirage

### Data Architecture

The app uses WarpDrive's schema-driven architecture:

* **Schemas** (`app/schemas/`) — JSON schema definitions for all model types, replacing class-based `@attr`/`@belongsTo`/`@hasMany` models
* **Handlers** (`app/handlers/`) — `AuthHandler` injects auth tokens; `BrnApiHandler` normalizes the backend's REST responses to JSON:API format for the WarpDrive cache
* **Transformations** (`app/transformations/`) — Custom field transformations (`full-date` for Luxon DateTime, `array` for array fields)
* **Store** (`app/services/store.ts`) — Configured with a `RequestManager` pipeline: Auth → API normalization → Fetch → Cache

## Prerequisites

You will need the following things properly installed on your computer.

* [Git](https://git-scm.com/)
* [Node.js](https://nodejs.org/) (>= 22)
* [Yarn](https://yarnpkg.com/) (1.x)
* [Ember CLI](https://ember-cli.com/)
* [Google Chrome](https://google.com/chrome/)

## Installation

* install [volta.sh](https://volta.sh/)
* install [watchman](https://facebook.github.io/watchman/docs/install.html)
* `git clone <repository-url>` this repository
* `cd brn/frontend`
* `yarn install`

## Running / Development

* `yarn local` starts DEV incremental build pointed to localhost:8081 as API
* `yarn remote` starts DEV build pointing to production BE

* Visit your app at [http://localhost:4200](http://localhost:4200).
* Visit your tests at [http://localhost:4200/tests](http://localhost:4200/tests).

### Running Tests

* `ember test`
* `ember test --server`

#### Test coverage report
* `yarn test:coverage`
* open `./coverage/index.html` to see detailed report

### Linting

* `yarn lint:hbs`
* `yarn lint:js`
* `yarn lint:js --fix`

### Building

* `ember build` (development)
* `ember build --environment production` (production)
