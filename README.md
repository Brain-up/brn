# Brain Up! 
This project is a web-application being developed to support people with a deficit in their central auditory skills with or without hearing loss, i.e. users of cochlear implant or hearing aids. Intended to be used by children older than 7 years, adults, elderly subjects;  anyone who wants to improve their auditory skills, training their brain to improve the understanding of what is heard. 
It is projected to contain several series of media exercises, which should be added incrementally. The tool structure and usage could be organized in the same way as other tools like www.uchi.ru.

Our current version is here : http://brainup.site && http://31.184.253.199/ && http://audibly.ru/ (test user default@default.ru password)
Now you as a user can register and start doing exercises.

## SCIENTIFIC SOURCES
 - Additional description with links to analogous applications: https://docs.google.com/document/d/1UKPE9ykj16JG6FZKoxqCoXI6ehWRhyXZnDNxPcaFLWQ/edit   
 - Scientific basis of the project: https://www.ncbi.nlm.nih.gov/pmc/articles/PMC6417420/ 
 - https://youtu.be/BMNrFENZ4Rw 
 - https://youtu.be/QUoBMSKq7zg
 
## Published ARTICLES about our project
- https://habr.com/ru/company/epam_systems/blog/530824 01.12.2020
- https://anywhere.epam.com/volunteer/pages-1/brainApp.html (will be fixed soon) 05.05.2020
- https://info.epam.com/content/infoepam/topics/locations/russia/articles/2020/may/pro-bono-volunteers_ru.html (only for epamers)
- https://info.epam.com/topics/global/industries/articles/2020/jul/brain-up_en.html (only for epamers)
 
## WHY JOIN OUR PROJECT AS A DEVELOPER?
We use latest technologies and best practices, so developers will get to know new tools and their usage, obtaining a useful experience. We will review your code, give you advice to improve it and listen to your suggestions. 
Most importantly being an open source project you can show your work in it to any person interested, proving your development expertise with actual examples of your work flow and code samples in a live application. 

## WE ARE LOOKING FOR
1. Front-end developers: Ember, Angular. 
2. Server-side developers: Kotlin and Java. Components, REST and algorithms.
3. DevOps: Continuous integration and delivery.
4. UX: Creative designers for UX research and prototype testing. Your ideas how to improve user retention are welcome.
5. Paintest: for creation pictures for exercises. 

Join us! Learn something new, try your skills, prove yourself, get experience and get ahead!

## TOOLS WE USE
- Back-end: KOTLIN + Spring boot. Rest api as integration layer.
- Front-end : TBD - Ember, Angular.
- DB: Postgres13.
- TestContainers for running integration tests.

## HOW TO JOIN?
1. for epam developers: Just click on “JOIN TEAM” button in https://contribute.epam.com/products/143 and we will get in touch with you shortly. 
2. for all other developers: you can send an email to [elena_moshnikova@epam.com](mailto:elena_moshnikova@epam.com) or directly to [brainupproject@yandex.ru](mailto:brainupproject@yandex.ru) describing your interests or any questions you may have.
our project skype chat: https://join.skype.com/jxSiWkgwT2x1

# GETTING STARTED!
## Resources:
### Documentation
https://github.com/Brain-up/brn/wiki ||
https://kb.epam.com/display/EPMLABSBRN/Brn+project+documentation
### Jira
https://github.com/Brain-up/brn/issues ||
https://jira.epam.com/jira/secure/RapidBoard.jspa?rapidView=103360&view=planning
### Jenkins (only for epamers now)
https://kb.epam.com/pages/viewpage.action?pageId=885110636
### Coding standarts
https://github.com/Brain-up/brn/wiki/Coding-Standards ||
https://kb.epam.com/display/EPMLABSBRN/Coding+standards

## Development:

### Development prerequisites
1. FrontEnd: Install node v10 or above https://nodejs.org/en/download/
2. FrontEnd: Install yarn 1.19 or above https://yarnpkg.com/lang/en/docs/install
3. FrontEnd: In order to make any commit you need Husky dependency be installed (you can use frontend build to get it)
4. BackEnd: Install Docker https://hub.docker.com/search/?type=edition&offering=community 
5. BackEnd: Idea

### Start Front Angular Part - admin application
go to location where the project is download for example C:\brn\brn\frontend-angular
1. run first time
`npm install`
2. update proxy: open file proxy.conf.json and change target for local development. Do not commit this changes
"http://localhost:8081" -> "http://audibly.ru"
3. run to start angular part
`npm run start`

### Start Front Ember Part - user application
Run following commands:
linux/mac:
``` 
cd ./frontend/ && yarn && node ./node_modules/.bin/ember serve
```
for windows:
```
 ./node_modules/.bin/ember serve --port=4201
```
FE dev server now accessible at http://localhost:4200/

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
*  on linux:
`docker run -p 5432:5432 -e POSTGRES_DB=brn -e POSTGRES_PASSWORD=$PG_PASSWORD -e POSTGRES_USER=$PG_USER postgres:11`
* on windows: 
`docker run --name postgres_dev -d -p 5432:5432 -e POSTGRES_DB=brn -e POSTGRES_PASSWORD=$PG_PASSWORD -e POSTGRES_USER=$PG_USER postgres:11`
_$PG_PASSWORD_ and _$PG_USER_ are environment variables and  could be replaced directly or added to your operation system 
[how to add in win10](https://www.architectryan.com/2018/03/17/add-to-the-path-on-windows-10). 

2.2 Alternatively, you can just replace the variables by "admin", the default user and password for development:
_docker run --name postgres_dev5 -d -p 5432:5432 -e POSTGRES_DB=brn -e POSTGRES_PASSWORD=admin -e POSTGRES_USER=admin postgres:11_

### Back end Kotlin Part:
1. Run command 'gradle build' from main project folder to build project with tests.
2. Application.kt is the main class to run application from Idea for example.
3. on first running in application.properties set `spring.jpa.hibernate.ddl-auto=create`
it would create db first time. and if you would not delete db image - structure will saved on second run with `spring.jpa.hibernate.ddl-auto=validate`
4. post http://localhost:8081/api/brnlogin use in body
{
  "grant_type": "password",
  "username": "default@default.ru",
  "password": "password"
}
5. then you can call all other end-points from Postman collection https://app.getpostman.com/join-team?invite_code=a0b5da8aaf3fe3f5b7f157db5023e834 like
http://localhost:8081/api/exercises/142 
end-point specification is here: https://github.com/Brain-up/brn/blob/master/api-contract/api.raml
6. for logout use http://localhost:8081/api/logout

Note that if you are using IntelliJ, you may want to use version 2019.2 and later to avoid issues with new kotlin plugin.

### Deploy Application (back-end part and front-end parts, but it is rather slow) USING DOCKER COMPOSE:
1. Open file docker-compose.yml and change SPRING_PROFILE to "dev".
2. From console, from project's folder, execute:
```bash
docker-compose up --build
```
Alternatively, use daemon mode (no console output):
```bash
docker-compose -d up --build
```
REST API will be accessible at http://localhost:8081/api/swagger-ui.html 

docker useful command:
docker ps -a -q for show all containers
docker stop idContainer for stop running container
docker rm $(docker ps -a -q) Remove all stopped containers

## Development tips:
1. Get a task assigned in JIRA (for epamers) or issue (https://github.com/Brain-up/brn/issues) you choosed and discused with @ElenaSpb.
2. Create branch from dev with the codename of your task, i.e. EPAMLABSBRN-1.
3. Implement your task, do not forget to write tests. Remember to follow project's coding standards: https://github.com/Brain-up/brn/wiki/Coding-Standards or https://kb.epam.com/display/EPMCOSRINT/Coding+standarts .
4. Create pull request with task name and description about what was done. 
5. Notify the team in our skype chat and wait for reviews. At least one reviewer is necessary, but more can be added in a case by case basis.
6. The task gets merged by a project mantainer. 
7. check that build job on jenkins passes successfully.

### Code style:
1. Please refer for details to kb resources: https://github.com/Brain-up/brn/wiki/Code-Style or https://kb.epam.com/display/EPMCOSRINT/Code+style
2. Always use Ctrl+Alt+L in IDEA to update code formatting before committing!
3. Use `gradlew ktlint` command to check code style. If this task fails, the related report with error details can be found in the 'build\reports\ktlint' folder. 
4. It is also possible to use `gradlew ktlintFormat` command to fix code style errors automatically.
5. Please note that if `gradlew ktlint` task fails, project build will fail also.

### Branches:
Use format 'EPMLABSBRN-# issue description' or 'Merge description'. Issue number must be in range [0-1999]

### Sonar:
1. https://sonarcloud.io/code?id=Brain-up_brn our project sonar cloud.
2 To view test coveradge locally  
 2.1 use jacoco gradle task locally `jacocoTestReport` 
 2.2 command line: `gradle jacocoTestReport`
3. Pay attention that main local metric would be a little bit different from the one in Sonar cloud.


### Thank you very much for your support!

our application is run on http://ecse005003f1.epam.com/ && http://31.184.253.199/

### License
This project is licensed under the MIT license. See the LICENSE [text](https://opensource.org/licenses/MIT).
