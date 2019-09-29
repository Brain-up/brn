## WHAT IS BRN.EPAM.COM? 
It is a web-application which is now developed to support people with central auditory skills deficit, with or without hearing loss, users of cochlear implant or hearing aids. It may be taken by children older than 7 years, adults/ elderly subjects, or anyone who wants to improve auditory skills and train the brain to listen better. 
It would contain several series of media exercises (maybe organized like like www.uchi.ru for example).
 
## WHAT CHALLENGES YOU CAN GET IN THIS PROJECT

We use latest technologies, so there you can acquaint with them, try them and get a useful experience. 
- Server side: Java/Kotlin + Spring boot (rest api for front)
- DB: Postgres (h2 now is used for development)
- Front-end : up to front-end developers - React or Angular (now Vaadin is used in one branch)

## SCIENCE SOURCE

https://www.ncbi.nlm.nih.gov/pmc/articles/PMC6417420/ science source for this topic 
https://docs.google.com/document/d/1UKPE9ykj16JG6FZKoxqCoXI6ehWRhyXZnDNxPcaFLWQ/edit  additional description with links to analogs 

## WE ARE LOOKING FOR:

1. front-end developers (react or angular - now there are no one, only vaadin in one branch)
2. server-side developers Kotlin/Java   to develop new components and personalization algorithms
3.  DevOps  to make ci cd process
4. Creative UX designers for UX research and prototypes testing. Your ideas how to improve user retention are welcome.

Join us, learn something new you want, try your skills, prove yourself, get experience and go ahead!

## HOW TO JOIN?
Just click on “JOIN TEAM” button and we will get in touch with you shortly. Or please email to
[elena_moshnikova@epam.com](mailto:elena_moshnikova@epam.com) and describe your interests or any questions.

### JIRA
https://jira.epam.com/jira/secure/RapidBoard.jspa?rapidView=103360&view=planning

---
## Developer

1. The Vaadin 14 team expects you to have Node.js and npm tools installed on your computer. 
(https://nodejs.org/en/download/) after install - restart Idea.

2. run command 'gradle assemble' to build project successfully

3. Application.kt - main class to run application from idea directly

## Developer notes for Vaadin UI

Open [http://localhost:8080/ui-vaadin](http://localhost:8080/ui-vaadin) in your browser.

If you want to run your app locally in production mode, run `mvn spring-boot:run -Pproduction`

For documentation on using Vaadin Flow and Spring, visit [vaadin.com/docs](https://vaadin.com/docs/flow/spring/tutorial-spring-basic.html).

For more information on Vaadin Flow, visit [vaadin.com/flow](https://vaadin.com/flow).

## Data base
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
* on windows: `?`

_$PG_PASSWORD_ and _$PG_USER_ are environment variables and  could be replaced directly or added to your operation system 
[how to add in win10](https://www.architectryan.com/2018/03/17/add-to-the-path-on-windows-10)