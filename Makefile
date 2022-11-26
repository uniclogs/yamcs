
all: clean build

.PHONY: build
build:
	mvn package
	mv target/bundle-tmp docker/yamcs

.PHONY: clean
clean:
	mvn clean
	rm -rf target docker/yamcs