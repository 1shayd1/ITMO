JC = javac
JVM = java
SRC_DIR = .
BIN_DIR = bin
MAIN_CLASS = Story

SRCS = $(shell find $(SRC_DIR) -name "*.java")

JFLAGS = -d $(BIN_DIR) -sourcepath $(SRC_DIR)

all: compile

compile: $(SRC)
	mkdir -p $(BIN_DIR)
	$(JC) $(JFLAGS) $(SRCS)


run: compile
	$(JVM) -cp $(BIN_DIR) $(MAIN_CLASS)

clean:
	rm -rf $(BIN_DIR)


.PHONY: all compile run clean
