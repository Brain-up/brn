## WHAT IS BRN.EPAM.COM? 
It is a web-application which is now developed to support people with central auditory skills deficit, with or without hearing loss, users of cochlear implant or hearing aids. It may be taken by children older than 7 years, adults/ elderly subjects, or anyone who wants to improve auditory skills and train the brain to listen better. 
It would contain several series of media exercises (maybe organized like like www.uchi.ru for example).
 
## WHAT CHALLENGES YOU CAN GET IN THIS PROJECT
We use latest technologies, so there you can acquaint with them, try them and get a useful experience. 
- Server side: Java/Kotlin + Spring boot (rest api for front)
- DB: Postgres (h2 we plan to use for tests)
- Front-end : up to front-end developers - Ember, React or Angular

## SCIENCE SOURCE
https://www.ncbi.nlm.nih.gov/pmc/articles/PMC6417420/ science source for this topic 
https://docs.google.com/document/d/1UKPE9ykj16JG6FZKoxqCoXI6ehWRhyXZnDNxPcaFLWQ/edit  additional description with links to analogs 

## WE ARE LOOKING FOR:
1. front-end developers (ember, react or angular..)
2. server-side developers Kotlin/Java   to develop new components, end-points and personalization algorithms
3. DevOps  to make ci cd process
4. Creative UX designers for UX research and prototypes testing. Your ideas how to improve user retention are welcome.

Join us, learn something new you want, try your skills, prove yourself, get experience and go ahead!

## HOW TO JOIN?
Just click on “JOIN TEAM” button and we will get in touch with you shortly. Or please email to
[elena_moshnikova@epam.com](mailto:elena_moshnikova@epam.com) and describe your interests or any questions.

### JIRA
https://jira.epam.com/jira/secure/RapidBoard.jspa?rapidView=103360&view=planning

### Some documentation
https://kb.epam.com/display/EPMCOSRINT/PROJECT+Recovery+brain+auditory+abilities

---
## For developer's start
0. clone dev branch (git clone https://github.com/Brain-up/brn.git)
1. run command 'gradle assemble' to build project successfully
'gradle build' with tests
2. Application.kt - main class to run application from idea directly

## Data base Postgrees (run from docker image)
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
or simple
`docker run --name postgres_dev -d -p 5432:5432 -e POSTGRES_DB=brn -e POSTGRES_PASSWORD=admin -e POSTGRES_USER=admin postgres:11`

_$PG_PASSWORD_ and _$PG_USER_ are environment variables and  could be replaced directly or added to your operation system 
[how to add in win10](https://www.architectryan.com/2018/03/17/add-to-the-path-on-windows-10)

### Code style
Please refer for details to kb resources: https://kb.epam.com/display/EPMCOSRINT/Code+style

Always use Ctrl+Alt+L in IDEA to update code formatting before committing!

Use `gradlew ktlint` command to check code style. If this task fails, the related report with error details can
be found in the 'build\reports\ktlint' folder. 
It is also possible to use `gradlew ktlintFormat` command to fix code style errors automatically.
Please note that if `gradlew ktlint` task fails, project build will fail also.

## FE/BE dev process
1. create branch from dev with name EPAMLABSBRN-1 for example
2. development + tests
3. make MR (with task name and description about what was done), put it to our skype chat and wait several reviews (1 minimum)
4. merge it in dev branch

### REST API
http://localhost:8080/swagger-ui.html

## FE dev pre-requisites
1. node v10 or above (https://nodejs.org/en/download/)
2. yarn 1.19 or above (https://yarnpkg.com/lang/en/docs/install/#mac-stable)

## How to start FE dev server
Run following commands:
```bash
cd ./frontend/ && yarn && node ./node_modules/.bin/ember serve
```
FE dev server now accesable at http://localhost:4200/

## How to start BE dev server
Run following commands build and run as example:
* 1. C:\Brain\brn>gradlew build
* or
* C:\Brain\brn>gradlew assemble (build without tests)

* 2. C:\Brain\brn>java -jar C:\brain\brn\build\libs\epam-brn.jar

BE server accesable as http://localhost:8080/swagger-ui.html
