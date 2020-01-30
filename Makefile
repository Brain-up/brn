# Backend
init-githooks:
	@echo "[init-githooks]"
	git config core.hooksPath .githooks

clean:
	rm -rf build out
	rm -rf frontend/node_modules
	rm -rf frontend/tmp

docker_clean_app_images: stop
	docker rmi brn_test db_brn brn

docker_clean_test_containers:
	docker-compose -f docker-compose-unit-test.yml down

docker_unit_test: clean
	docker-compose -f docker-compose-unit-test.yml up --build --force-recreate --exit-code-from brn-test

start:
	docker-compose up --build --force-recreate

stop:
	docker-compose down

restart: clean stop start
