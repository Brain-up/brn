# Admin panel

## Quick start

| Steps | Setup                               | Update             | Comment                          |
|-------|-------------------------------------|--------------------|----------------------------------|
| 1     | install NodeJS **14+** & npm **6+** | `npm ci`           |                                  |
| 2     | `npm ci`                            | `npm run generate` |                                  |
| 3     | `npm i -g @angular/cli` **11.x.x**  |                    |                                  |
| 4     | start frontend app locally          |                    |                                  |
| 4.1   | `npm run start`                     |                    | for work with production backend |
| 4.2   | `npm run start:dev`                 |                    | for work with localhost backend  |

## Lint

`npm run lint`

## Test

`npm run test-start`

## i18n

Adding new keys for localization files (**src/assets/i18n**): `npm run i18n-extract`
