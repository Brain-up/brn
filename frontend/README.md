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

* `git clone <repository-url>` this repository
* `cd brn/frontend`
* `yarn install`

## Running / Development

* `yarn local` starts DEV incremental build pointed to localhost:8081 as API 
* `yarn mirage` starts DEV incremental build with mock server (see ./mirage folder)
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
