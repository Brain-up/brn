# Admin panel

## Quick start

| Steps | Setup                                                                                                    | Update             |
| ----- | -------------------------------------------------------------------------------------------------------- | ------------------ |
| 1     | install NodeJS **14+** & npm **6+**                                                                      | `npm ci`           |
| 2     | `npm ci`                                                                                                 | `npm run generate` |
| 3     | `npm i -g @angular/cli` **11.x.x**                                                                       |
| 4     | to use production api change `http://localhost:8081` -> `https://brainup.site` in **src/proxy.conf.json** |
| 5     | `npm run start`                                                                                          |

## Lint

`npm run lint`

## Test

`npm run test-start`

## i18n

Adding new keys for localization files (**src/assets/i18n**): `npm run i18n-extract`
