# Brain Up! 
This project is a web-application being developed to support people with a deficit in their central auditory skills with or without hearing loss, i.e. users of cochlear implant or hearing aids. Intended to be used by children older than 7 years, adults, elderly subjects;  anyone who wants to improve their auditory skills, training their brain to improve the understanding of what is heard. 
It is projected to contain several series of media exercises, which should be added incrementally. The tool structure and usage could be organized in the same way as other tools like www.uchi.ru .

## SCIENTIFIC SOURCES
 - Additional description with links to analogous applications: https://docs.google.com/document/d/1UKPE9ykj16JG6FZKoxqCoXI6ehWRhyXZnDNxPcaFLWQ/edit   
 - Scientific basis of the project: https://www.ncbi.nlm.nih.gov/pmc/articles/PMC6417420/ 
 
## WHY JOIN OUR PROJECT AS A DEVELOPER?
We use latest technologies and best practices, so developers will get to know new tools and their usage, obtaining a useful experience. We will review your code, give you advice to improve it and listen to your suggestions. 
Most importantly being an open source project you can show your work in it to any person interested, proving your development expertise with actual examples of your work flow and code samples in a live application. 

## WE ARE LOOKING FOR
1. Front-end developers: Ember, Angular. 
2. Server-side developers: Kotlin and Java. Components, REST and algorithms.
3. DevOps: Continuous integration and delivery.
4. UX: Creative designers for UX research and prototype testing. Your ideas how to improve user retention are welcome.

Join us! Learn something new, try your skills, prove yourself, get experience and get ahead!

## TOOLS WE USE
- Back-end: Java/Kotlin + Spring boot. Rest api as integration layer.
- Front-end : TBD - Ember, Angular.
- DB: Postgres in production, H2 for testing.

## HOW TO JOIN?
1. for epam developers: Just click on “JOIN TEAM” button in https://contribute.epam.com/products/143 and we will get in touch with you shortly. 
2. for all other developers: you can send an email to [elena_moshnikova@epam.com](mailto:elena_moshnikova@epam.com) or directly to [brainupproject@yandex.ru](mailto:brainupproject@yandex.ru) describing your interests or any questions you may have.
our project skype chat: https://join.skype.com/jxSiWkgwT2x1

# GETTING STARTED!
## RESOURCES
### DOCUMENTATION
https://kb.epam.com/display/EPMLABSBRN/Brn+project+documentation
### JIRA
https://jira.epam.com/jira/secure/RapidBoard.jspa?rapidView=103360&view=planning
### JENKINS
https://kb.epam.com/pages/viewpage.action?pageId=885110636
### CODING STANDARDS
https://kb.epam.com/display/EPMLABSBRN/Coding+standards

## FRONT END DEVELOPMENT
### REQUISITES
1. node v10 or above (https://nodejs.org/en/download/)
2. yarn 1.19 or above (https://yarnpkg.com/lang/en/docs/install/#mac-stable)

### START YOUR FRONT END DEVELOPMENT SERVER
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

### GET DATABASE RUNNING
The project uses postgres 11.5. [Documentation](https://www.postgresql.org/docs/11/index.html)
Currently for local development we use [postgres docker image](https://hub.docker.com/_/postgres)
To install docker:
* [on windows](https://docs.docker.com/docker-for-windows/install/)
* [on mac](https://docs.docker.com/docker-for-mac/install/)
* [oubuntu](https://docs.docker.com/install/linux/docker-ce/ubuntu/)
* [debian](https://docs.docker.com/install/linux/docker-ce/debian/)
* [centos](https://docs.docker.com/install/linux/docker-ce/centos/)

To run docker use the following command:
*  on linux:
`docker run -p 5432:5432 -e POSTGRES_DB=brn -e POSTGRES_PASSWORD=$PG_PASSWORD -e POSTGRES_USER=$PG_USER postgres:11`
* on windows: 
`docker run --name postgres_dev -d -p 5432:5432 -e POSTGRES_DB=brn -e POSTGRES_PASSWORD=$PG_PASSWORD -e POSTGRES_USER=$PG_USER postgres:11`

_$PG_PASSWORD_ and _$PG_USER_ are environment variables and  could be replaced directly or added to your operation system 
[how to add in win10](https://www.architectryan.com/2018/03/17/add-to-the-path-on-windows-10). 
Alternatively, you can just replace the variables by "admin", the default user and password for development:
_docker run --name postgres_dev5 -d -p 5432:5432 -e POSTGRES_DB=brn -e POSTGRES_PASSWORD=admin -e POSTGRES_USER=admin postgres:11_

### GET THE BACKEND PROJECT RUNNING
1. Run command 'gradle build' from main project folder to build project with tests.
2. Use --spring.profiles.active=dev when running spring project, in command line or change it in application.properties.
3. Application.kt is the main class to run application.

Note that if you are using IntelliJ, you may want to use version 2019.2 and later to avoid issues with new kotlin plugin.

## BACK END DEVELOPMENT 
### REQUISITES
1. Docker - https://hub.docker.com/search/?type=edition&offering=community 

### START BACKEND from IDEA
just run Application.kt after launching database in docker.

### Code style
- Please refer for details to kb resources: https://kb.epam.com/display/EPMCOSRINT/Code+style
- Always use Ctrl+Alt+L in IDEA to update code formatting before committing!
- Use `gradlew ktlint` command to check code style. If this task fails, the related report with error details can be found in the 'build\reports\ktlint' folder. 
- It is also possible to use `gradlew ktlintFormat` command to fix code style errors automatically.
- Please note that if `gradlew ktlint` task fails, project build will fail also.

## DEPLOY USING DOCKER COMPOSE
1. Open file docker-compose.yml and change SPRING_PROFILE to "dev".
2. From console, from project's folder, execute:
```bash
docker-compose up --build
```
Alternatively, use daemon mode (no console output):
```bash
docker-compose -d up --build
```
REST API will be accessible at http://localhost:8081/swagger-ui.html 

## DEVELOPMENT PROCESS
1. Get a task assigned in JIRA.
2. Create branch from dev with the codename of your task, i.e. EPAMLABSBRN-1.
3. Implement your task, do not forget to write tests. Remember to follow project's coding standards: https://kb.epam.com/display/EPMCOSRINT/Coding+standarts .
4. Create pull request with task name and description about what was done. 
5. Notify the team in our skype chat and wait for reviews. At least one reviewer is necessary, but more can be added in a case by case basis.
6. The task gets merged by a project mantainer. 
# 7. check that build job on jenkins passes successfully.

### Thank you very much for your support!

