
all: clean build

.PHONY: build
build:
	mvn package
	mv target/bundle-tmp dist/docker/yamcs

.PHONY: clean
clean:
	mvn clean
	rm -rf target dist/docker/yamcs