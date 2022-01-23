# brn user UI

This part of BRN project responsible for end-user UI.

## Prerequisites

You will need the following things properly installed on your computer.

* [Git](https://git-scm.com/)
* [Node.js](https://nodejs.org/)
* [Yarn](https://yarnpkg.com/)
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
* `yarn remote:prod` starts DEV build pointing to production BE

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
