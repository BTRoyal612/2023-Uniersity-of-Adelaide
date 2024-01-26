# A Makefile for a Java project.

# Output directory
OUTPUT_DIR := .

# Source directory
SOURCE_DIR := .

# Java compiler
JAVAC := javac

# Find utility
FIND := find

# Java compiler flags
JFLAGS :=

# Temp file for holding source file list
all_javas := $(OUTPUT_DIR)/all.javas

# Compile the source
.PHONY: compile
compile: $(all_javas)
	$(JAVAC) $(JFLAGS) @$<

# Gather source file list
.INTERMEDIATE: $(all_javas)
$(all_javas):
	$(FIND) $(SOURCE_DIR) -name '*.java' > $@
