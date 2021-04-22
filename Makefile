.PHONY: all clean doc compile

CONF = "config-hot.txt"
PEERSIM_JARS=""
UNAME_S := $(shell uname -s)
ifeq ($(UNAME_S),Linux)
	LIB_JARS=`find -L lib/ -name "*.jar" | tr [:space:] :`
endif
ifeq ($(UNAME_S),Darwin)
	LIB_JARS=`find -L lib -name "*.jar" | tr '\n' ':'`
endif


compile:
	mkdir -p classes
	javac -sourcepath src -classpath $(LIB_JARS):$(PEERSIM_JARS) -d classes `find -L ./src -name "*.java"` -Xlint
doc:
	mkdir -p doc
	javadoc -sourcepath src -classpath $(LIB_JARS):$(PEERSIM_JARS) -d doc peersim.chord

run:
	java -Xss1024M -cp $(LIB_JARS):$(PEERSIM_JARS):classes peersim.Simulator $(CONF)

all: compile doc run

clean:
	rm -fr classes doc

cleandata:
	rm -rf out/data/*