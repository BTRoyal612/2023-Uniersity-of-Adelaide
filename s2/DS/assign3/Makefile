# Variables
JAVA = java
JAVAC = javac
LIB = lib
SRC = src
OUT = out
CPTEST = -cp $(LIB)/*:$(OUT)/

MAIN_SOURCES = $(wildcard $(SRC)/main/**/*.java)
TEST_SOURCES = $(wildcard $(SRC)/test/**/*.java)

TEST_MAIN_CLASS = org.junit.platform.console.ConsoleLauncher

# Targets and their actions
all: compile-main

compile-main:
	@mkdir -p $(OUT)
	@$(JAVAC) -d $(OUT) $(MAIN_SOURCES)

compile-test: compile-main
	@$(JAVAC) $(CPTEST) -d $(OUT) $(TEST_SOURCES)

test: compile-test
	@$(JAVA) $(CPTEST) $(TEST_MAIN_CLASS) --scan-classpath

clean:
	@find . -name "*.class" -exec rm {} +
	@rm -rf $(OUT)

.PHONY: all clean test run compile-main compile-test
