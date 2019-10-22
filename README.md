# BRN.EPAM.COM - Brain Up! 
This project is a web-application being developed to support people with a deficit in their central auditory skills with or without hearing loss, i.e. users of cochlear implant or hearing aids. Intended to be used by children older than 7 years, adults, elderly subjects;  anyone who wants to improve their auditory skills, training their brain to improve the understanding of what is heard. 
It is projected to contain several series of media exercises, which should be added incrementally. The tool structure and usage could be organized in the same way as other tools like www.uchi.ru .

## SCIENCE SOURCES
 - Scientific basis of the project: https://www.ncbi.nlm.nih.gov/pmc/articles/PMC6417420/ 
 - Additional description with links to analogous applications: https://docs.google.com/document/d/1UKPE9ykj16JG6FZKoxqCoXI6ehWRhyXZnDNxPcaFLWQ/edit   
 
## WHAT CHALLENGES YOU CAN TAKE AS A DEVELOPER ON IN THIS PROJECT
We use latest technologies and best practices, so developers will get to know new tools and their usage, obtaining a useful experience. Being an open source project you can show your work in it to any person interested, proving your development expertise.

## WHAT WE ARE LOOKING FOR:
1. Front-end developers: Ember, React or Angular. 
2. Server-side developers: Kotlin and Java. Components, REST and algorithms.
3. DevOps: Continuous integration and delivery.
4. UX: Creative designers for UX research and prototype testing. Your ideas how to improve user retention are welcome.

Join us! Learn something new, try your skills, prove yourself, get experience and get ahead!

## TOOLS WE USE:
- Back-end: Java/Kotlin + Spring boot. Rest api as integration layer.
- Front-end : TBD - Ember, React or Angular.
- DB: Postgres in production, H2 for testing.

## HOW TO JOIN?
Just click on “JOIN TEAM” button and we will get in touch with you shortly. 
You can also send an email to [elena_moshnikova@epam.com](mailto:elena_moshnikova@epam.com) describing your interests or any questions you may have.

# GETTING STARTED!

## RESOURCES
### JIRA
https://jira.epam.com/jira/secure/RapidBoard.jspa?rapidView=103360&view=planning
### DOCUMENTATION
https://kb.epam.com/display/EPMCOSRINT/PROJECT+Recovery+brain+auditory+abilities

## GET THE PROJECT RUNNING
0. Clone dev branch with idea or using command line "git clone https://github.com/Brain-up/brn.git"
1. Run command 'gradle assemble' to build project successfully
'gradle build' with tests.
2. Use --spring.profiles.active=dev when running spring project, in command line or change it in application.properties.
3. Application.kt is the main class to run application.

Note that if you are using IntelliJ, you may want to use version 2019.2 and later to avoid issues with new kotlin plugin.

## GET DATABASE RUNNING
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

## DEVELOPMENT PROCESS
1. Get a task assigned in JIRA.
2. Create branch from dev with the codename of your task, i.e. EPAMLABSBRN-1.
3. Implement your task, do not forget to test. Remember to follow project's coding standards: https://kb.epam.com/display/EPMCOSRINT/Coding+standarts .
4. Create pull request with task name and description about what was done. 
5. Notify the team in our our skype chat and wait for reviews. At least one reviewer is necessary, but more can be added in a case by case basis.
6. The task gets merged by a project mantainer. Thank you very much for your support!


## FRONT END DEVELOPMENT REQUISITES
1. node v10 or above (https://nodejs.org/en/download/)
2. yarn 1.19 or above (https://yarnpkg.com/lang/en/docs/install/#mac-stable)

## START YOUR FRONT END DEVELOPMENT SERVER
Run following commands:
```bash
cd ./frontend/ && yarn && node ./node_modules/.bin/ember serve
```
FE dev server now accesable at http://localhost:4200/

## START BACKEND APPLICATION
Run following commands build and run as example:
```bash
 C:\Brain\brn>gradlew build
 C:\Brain\brn>gradlew assemble (build without tests)
 C:\Brain\brn>java -jar C:\brain\brn\build\libs\epam-brn.jar
```
BE server accesible as http://localhost:8080/swagger-ui.html

## REST API 
http://localhost:8080/swagger-ui.html
