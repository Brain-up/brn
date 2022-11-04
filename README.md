[![Gitpod ready-to-code](https://img.shields.io/badge/Gitpod-ready--to--code-blue?logo=gitpod)](https://gitpod.io/#https://github.com/Brain-up/brn)

# Brain Up!
This project is a web-application being developed to support people with a deficit in their central auditory skills with or without hearing loss, i.e. users of cochlear implant or hearing aids. Intended to be used by children older than 7 years, adults, elderly subjects;  anyone who wants to improve their auditory skills, training their brain to improve the understanding of what is heard. 
It is projected to contain several series of media exercises, which should be added incrementally. 

Our current version is here : http://brainup.site && http://31.184.253.199/ (test user default@default.ru password).
Now you as a user can register and start doing exercises.

Our road map https://github.com/Brain-up/brn/blob/master/roadmap.md.
Product Vision https://github.com/Brain-up/brn/wiki/Product-Vision.

## SCIENTIFIC SOURCES
- Additional description with links to analogous applications: https://docs.google.com/document/d/1UKPE9ykj16JG6FZKoxqCoXI6ehWRhyXZnDNxPcaFLWQ/edit
- Scientific basis of the project: https://www.ncbi.nlm.nih.gov/pmc/articles/PMC6417420/

## Published ARTICLES about our project
- https://aw.club/global/en/blog/volunteer/brain-up?utm_source=telegram&utm_medium=social&utm_campaign=ongoing 11.08.2022 (russian, english)
- https://wearecommunity.io/communities/community-platform/articles/753 01.12.2020 russian 
- https://habr.com/ru/company/epam_systems/blog/530824 01.12.2020
- https://anywhere.epam.com/volunteer/pages-1/brainApp.html (will be fixed soon) 05.05.2020
- https://info.epam.com/content/infoepam/topics/locations/russia/articles/2020/may/pro-bono-volunteers_ru.html (only for epamers)
- https://info.epam.com/topics/global/industries/articles/2020/jul/brain-up_en.html (only for epamers)
 
## WHY JOIN OUR PROJECT AS A DEVELOPER?
We use the latest technologies and best practices, so developers will get to know new tools and their usage, obtaining a useful experience. We will review your code, give you advice to improve it and listen to your suggestions. 
Most importantly being an open source project you can show your work in it to any person interested, proving your development expertise with actual examples of your work flow and code samples in a live application. 

## WE ARE LOOKING FOR
1. Front-end developers: Ember, Angular. 
2. Server-side developers: Kotlin and Java. Components, REST and algorithms.
3. IoS developers.
4. Android developers.
5. DevOps: Continuous integration and delivery.
6. UX: Creative designers for UX research and prototype testing. Your ideas how to improve user retention are welcome.
7. Paintest: for creation pictures for exercises. 
8. Sign language specialist to create/improve content.
9. QA engeniers. 

Join us! Learn something new, try your skills, prove yourself, get experience and get ahead!

## TOOLS WE USE
- Back-end: KOTLIN + Spring boot. Rest api as integration layer.
- Front-end : TBD - Ember, Angular.
- DB: Postgres13.
- TestContainers for running integration tests.

## HOW TO JOIN?
You can write directly in Telegram to Elena Moshnikova (project founder and tech lead) https://t.me/ElenaLovesSpb
or to project emaeil [brainupproject@yandex.ru](mailto:brainupproject@yandex.ru) describing your interests or any questions you may have.  
Also we have project team Telegram chat: https://t.me/+R-6ThlxgP5QyZmMy  
Project on epam plus (for Epamers only): https://plus.epam.com/projects/115

# GETTING STARTED!
## Resources:
### Documentation
https://awclub.atlassian.net/wiki/spaces/EPMLABSBRN/overview
(https://github.com/Brain-up/brn/wiki)
### RoadMap: https://awclub.atlassian.net/wiki/spaces/EPMLABSBRN/pages/2130452/Roadmap
### Product Vision: https://awclub.atlassian.net/wiki/spaces/EPMLABSBRN/pages/2130224/BRN+Product+Vision

### Jira
https://awclub.atlassian.net/jira/software/c/projects/EPMLABSBRN/issues
(https://github.com/Brain-up/brn/issues)

### Coding standards
https://github.com/Brain-up/brn/wiki/Coding-Standards  

## Development:

### Development prerequisites
1. FrontEnd: Install node v12 or above https://nodejs.org/en/download/
2. FrontEnd: Install yarn 1.19 or above https://yarnpkg.com/lang/en/docs/install
3. FrontEnd: In order to make any commit you need Husky dependency be installed (you can use frontend build to get it)
4. BackEnd: Install Docker https://hub.docker.com/search/?type=edition&offering=community 
5. BackEnd: Idea

### Start Front Angular Part - admin application
Go to location where the project is download for example C:\brn\brn\frontend-angular

📄 [See here](./frontend-angular/README.md)

### Start Front Ember Part - user application
📄 [See here](./frontend/README.md)

### Database running:
1. Install docker to your machine.
The project uses postgres 11.5. [Documentation](https://www.postgresql.org/docs/11/index.html)
Currently for local development we use [postgres docker image](https://hub.docker.com/_/postgres)
To install docker use:
* [on windows](https://docs.docker.com/docker-for-windows/install/)
* [on mac](https://docs.docker.com/docker-for-mac/install/)
* [on ubuntu](https://docs.docker.com/install/linux/docker-ce/ubuntu/)
* [debian](https://docs.docker.com/install/linux/docker-ce/debian/)
* [centos](https://docs.docker.com/install/linux/docker-ce/centos/)

2.1 To run docker db image use the following command:
* on linux\windows:
```
docker run --name postgres_dev -d -p 5432:5432 -e POSTGRES_DB=brn -e POSTGRES_PASSWORD=$PG_PASSWORD -e POSTGRES_USER=$PG_USER postgres:13
```
`$PG_PASSWORD` and `$PG_USER` are environment variables and  could be replaced directly or added to your operating system. 
[how to add in win10](https://www.architectryan.com/2018/03/17/add-to-the-path-on-windows-10). 

2.2 Alternatively, you can just replace the variables by "admin", the default user and password for development:
```bash
docker run --name postgres_dev -d -p 5432:5432 -e POSTGRES_DB=brn -e POSTGRES_PASSWORD=admin -e POSTGRES_USER=admin postgres:13
```

### Back end Kotlin Part:
1. Run command 'gradle build' from main project folder to build project with tests.
2. Application.kt is the main class to run application from Idea for example.
3. Get Postman Collection from https://www.postman.com/koylubaevNT/workspace/brainup/collection/2686902-d91ba307-e131-4d4f-a308-0a272e063c95 and environment from https://www.postman.com/koylubaevNT/workspace/brainup/environment/2686902-95b2c495-82a1-4244-83c7-dad7f8efebd7.
4. Make post request from "Firebase" folder "Get Authorization Token" using credentials. 
5. Then you can call all other end-points from this Postman collection
end-point specification is here: https://github.com/Brain-up/brn/blob/master/api-contract/api.raml
6. use https://brainup.site/admin/swagger

Note that if you are using IntelliJ, you may want to use version 2019.2 and later to avoid issues with new kotlin plugin.

#### Useful Postman scripts:
- [Script](./postman_scripts/generate_month_history.js) to generate month tasks statistic (you can use it with `brnlogin` request in `Test` tab)

### Deploy Application USING DOCKER COMPOSE:
(back-end part and front-end parts, but it is rather slow. it is better to use GitPod)
From console, from project's folder, execute:
```bash
docker-compose up --build
```
Alternatively, use daemon mode (no console output):
```bash
docker-compose up --build -d
```
Local REST API will be accessible at http://localhost:8081/api/swagger-ui.html 
Public is always here https://brainup.site/admin/swagger (login with ADMIN role user)

docker useful command:
```shell
docker ps -a # for show all containers
docker stop idContainer # for stop running container
docker rm $(docker ps -a -q) # Remove all stopped containers
```
## Development tips:
1. Get a task assigned in JIRA (for epamers) or issue (https://github.com/Brain-up/brn/issues) you choosed and discused it with @ElenaSpb, send your githubnick to her.
2. Create branch from dev with the codename of your task, i.e. #GitHub_TaskNumber#.
3. Implement your task, do not forget to write tests. Remember to follow project's coding standards: https://github.com/Brain-up/brn/wiki/Coding-Standards.
4. Create pull request with task name and description about what was done. 
5. Notify the team in our skype chat and wait for reviews. At least one reviewer is necessary, but more can be added in a case by case basis.
6. The task gets merged by a project maintainer. 
7. check that build job on jenkins passes successfully.
8. Before load config files check them with https://orfogrammka.ru/ service.
9. Integration tests does not run in build process, it can/should be run locally with run job verification/integrationTests.

### Code style:
1. Please refer for details to kb resources: https://github.com/Brain-up/brn/wiki/Coding-Standards
2. Always use Ctrl+Alt+L in IDEA to update code formatting before committing!
3. Use `gradlew ktlint` command to check code style. If this task fails, the related report with error details can be found in the 'build\reports\ktlint' folder. 
4. It is also possible to use `gradlew ktlintFormat` command to fix code style errors automatically.
5. Please note that if `gradlew ktlint` task fails, project build will fail also.
6. For `api.raml` validation, you can use [Api Designer] (https://rawgit.com/mulesoft/api-designer/v0.4.2/dist/index.html).

### Kotlin input dto validation:
https://github.com/Brain-up/brn/wiki/Kotlin-request-dto-validation-with-annotations

### Flyway scripts naming
use `V2yearmonthday_taskNumber`
for example `V220210804_899`.

### Branches:
Use format '#GitHub_TaskNumber-# issue description' or 'Merge description'. Issue number must be in range [0-1999]

### Sonar:
1. https://sonarcloud.io/code?id=Brain-up_brn our project sonar cloud.
2. To view test coveradge locally  
 2.1 use jacoco gradle task locally `jacocoTestReport` 
 2.2 command line: `gradle jacocoTestReport`
3. Pay attention that main local metric would be a little bit different from the one in Sonar cloud.

### Thanks companies for support
- EPAM for Jira/Confluence, test instance and Jenkins, for contribution support program.
- JetBrains for IDEA licenses
- Selectel for public instance
- GitHub for code place and actions where we run CICD
- Yandex for Yandex Speech Kit service and free account to use it

### Thanks all volunteerы for contribution!

### License
CC0 1.0 Universal https://joinup.ec.europa.eu/licence/cc0-10-universal-cc0-10-public-domain-dedication  
