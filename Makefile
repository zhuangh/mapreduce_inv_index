JAVA_HOME=/usr/lib/jvm/java-6-sun

PATH:=${JAVA_HOME}/bin:${PATH}
HADOOP_PATH=/classes/cse223b/sp13/labs/lab1/hadoop
NEW_CLASSPATH=${HADOOP_PATH}/hadoop-core-1.0.4.jar:${HADOOP_PATH}/lib/*:${HADOOP_PATH}/lib/jetty-ext/*:${CLASSPATH}

INPUT_PATH=/classes/cse223b/sp13/labs/lab1/input/classics
# INPUT_PATH=/classes/cse223b/sp13/labs/lab1/input/wc_input

USER=hazhuang

SRC = $(wildcard *.java)

all: build

build: ${SRC}
	${JAVA_HOME}/bin/javac -Xlint:deprecation -classpath ${NEW_CLASSPATH} ${SRC}
	${JAVA_HOME}/bin/jar cvf build.jar *.class

run:   
	rm -r ~/classics_output_local
	${HADOOP_PATH}/bin/hadoop dfs -copyFromLocal ${INPUT_PATH} /user/${USER}/classics_input
	${HADOOP_PATH}/bin/hadoop jar build.jar Trivial  /user/${USER}/classics_input  /user/${USER}/classics_output
	${HADOOP_PATH}/bin/hadoop dfs -copyToLocal   /user/${USER}/classics_output ~/classics_output_local
	${HADOOP_PATH}/bin/hadoop dfs -rmr  /user/${USER}/classics_output
	${HADOOP_PATH}/bin/hadoop dfs -rmr  /user/${USER}/classics_input

clean:
	rm -f *.class *.jar

turnin:
	tar cvfz `whoami`-turnin.tgz *
	chmod 600 `whoami`-turnin.tgz
	cp `whoami`-turnin.tgz /classes/cse223b/sp13/turnin/lab1/
