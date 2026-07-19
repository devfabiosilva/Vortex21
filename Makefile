ENDIAN?=LITTLE
STAT?=WITH_STATISTICS
AR=ar rcs
LD=ld -r -b binary
CC=gcc
STRIP=strip
CURDIR=$(PWD)
INCLUDEDIR=$(CURDIR)/core/include

MONGO_C_GIT=https://github.com/mongodb/mongo-c-driver.git
MONGO_C_BRANCH=2.3.1
MONGO_C_DIR=$(CURDIR)/third-party/mongo-c-driver

FLAG=-lpthread -Wno-stringop-truncation -O3 -fPIC -march=native -fno-plt -Wl,--exclude-libs,libbson-shared-${MONGO_C_BRANCH}.a -D$(STAT) -DCWS_$(ENDIAN)_ENDIAN
#DEBUG_FLAG=-g -fsanitize=address,leak -DSOAP_DEBUG $(FLAG)

#TODO Eliminate unused symbols in .so
JAVA_FLAG=-fvisibility=hidden $(FLAG)

GO_FLAG=$(FLAG)

CS_FLAG=$(FLAG)

LIBDIR=$(CURDIR)/core/lib

EXAMPLES_PATH=$(CURDIR)/examples

#JNI_LIB_PATH=wrappers/java
JNI_LIB_PATH=wrappers/java/src/main/resources
JNI_LIB=libw21java11.so
JAVA_EXAMPLES=$(EXAMPLES_PATH)/java
JAVA_SIMPLE_EXAMPLE=$(JAVA_EXAMPLES)/app/SimpleExample
JAVA_SIMPLE_EXAMPLE_EXECUTE_JAR=java -jar SimpleExample-1.0-SNAPSHOT.jar

GO_BASE_PATH=$(CURDIR)/wrappers/go
GO_SRC_PATH=$(GO_BASE_PATH)/c_src
GO_LIB_PATH=$(GO_SRC_PATH)/lib
GO_INCLUDE_PATH=$(GO_SRC_PATH)/include
GO_LIB=libw21go.so

CS_BASE_PATH=$(CURDIR)/wrappers/dotNET/W21CSparser
CS_SRC_PATH=$(CS_BASE_PATH)/src
CS_LIB_PATH=$(CS_BASE_PATH)/libs
CS_INCLUDE_PATH=$(CS_BASE_PATH)/include
CS_LIB=libw21cs.so

# all: jni go cs
all: mvn_install

witsml21C_o3_native_shared: install_bson
ifneq ("$(wildcard $(CURDIR)/witsml21C_o3_native_shared.o)","")
	@echo "Nothing to do. Skipping witsml21C_o3_native_shared.o"
else
	@echo "Compiling witsml21C_o3_native_shared.o. It can take a little longer ..."
	@$(CC) -c -o witsml21C_o3_native_shared.o $(FLAG) -I. -Icore/include witsml21C.c -DNOHTTP -Wall
	@echo "witsml21C_o3_native_shared.o finished"
endif

jni: witsml21C_o3_native_shared
ifneq ("$(wildcard $(CURDIR)/$(JNI_LIB_PATH)/$(JNI_LIB))","")
	@echo "Nothing to do. $(JNI_LIB)"
else
	@echo "Compiling Java 11 wrapper"
	@$(CC) -o $(JNI_LIB_PATH)/$(JNI_LIB) -shared $(JAVA_FLAG) -I/usr/lib/jvm/java-11-openjdk-amd64/include -I/usr/lib/jvm/java-11-openjdk-amd64/include/linux w21_validator.c w21_deserializer.c core/cws_bson_utils.c core/cws_utils.c w21_config.c w21_events.c w21_input.c w21_messages.c w21_errors.c stdsoap2.c  witsml21C_o3_native_shared.o wrappers/java/parser.c -I. -Icore/include -Iwrappers/java -lbson-shared-${MONGO_C_BRANCH} -Lcore/lib -DNOHTTP -DVERGEN -D$(STAT) -Wall
	strip --strip-unneeded $(JNI_LIB_PATH)/$(JNI_LIB)
	@echo "Finished"
endif

# $(($(nproc) - 1))

mvn_install: jni
	@echo "Running maven ..."
ifneq ("$(wildcard $(CURDIR)/wrappers/java/target)","")
	@echo "Library already compiled. Skipping ..."
else
	pwd && cd $(CURDIR)/wrappers/java && pwd && mvn -U clean install
	@echo "Finished ..."
endif

java_examples: mvn_install
	@echo "Running example $(JAVA_SIMPLE_EXAMPLE) ..."
ifneq ("$(wildcard $(JAVA_SIMPLE_EXAMPLE)/target)","")
	@echo "Already compiled. Skip"
	pwd && cd $(JAVA_SIMPLE_EXAMPLE) && cd target && $(JAVA_SIMPLE_EXAMPLE_EXECUTE_JAR) ../LogInvalid.xml && $(JAVA_SIMPLE_EXAMPLE_EXECUTE_JAR) ../Log.xml && $(JAVA_SIMPLE_EXAMPLE_EXECUTE_JAR) ../OpsReport.xml
else
	pwd && cd $(JAVA_SIMPLE_EXAMPLE) && pwd && mvn clean install && cd target && $(JAVA_SIMPLE_EXAMPLE_EXECUTE_JAR) ../LogInvalid.xml && $(JAVA_SIMPLE_EXAMPLE_EXECUTE_JAR) ../Log.xml && $(JAVA_SIMPLE_EXAMPLE_EXECUTE_JAR) ../OpsReport.xml
endif

	@echo "Java Examples finished"

.PHONY:
install_bson:
	@echo "Check if mongo-c-driver directory exists ..."
ifneq ("$(wildcard $(MONGO_C_DIR))","")
	@echo "Already cloned. Skip"
else
	@echo "Cloning branch $(MONGO_C_BRANCH) from $(MONGO_C_GIT)"
	pwd && cd $(CURDIR)/third-party && pwd && git clone -b $(MONGO_C_BRANCH) $(MONGO_C_GIT) && cd mongo-c-driver && mkdir compiled && cd compiled && cmake .. -DCMAKE_BUILD_TYPE=Release -DENABLE_MONGOC=OFF -DCMAKE_C_FLAGS="-O3 -march=native -fno-plt" -DCMAKE_INSTALL_PREFIX=$(MONGO_C_DIR)/compiled/out && make -j12 && make install && pwd && cp out/lib/libbson2.a $(LIBDIR)/libbson-static-$(MONGO_C_BRANCH).a -v && cp -frv out/include/bson-$(MONGO_C_BRANCH)/bson $(INCLUDEDIR) && cd src/libbson/CMakeFiles/bson_shared.dir && pwd && ar rcs $(LIBDIR)/libbson-shared-$(MONGO_C_BRANCH).a src/bson/*.o src/jsonsl/*.o __/common/src/*.o
endif

go: witsml21C_o3_native_shared
	@echo "Compiling Golang wrapper"
ifneq ("$(wildcard $(GO_LIB_PATH)/$(GO_LIB))","")
	@echo "Already compiled $(GO_LIB). Skipping ..."
else
	@$(CC) -o $(GO_LIB_PATH)/$(GO_LIB) -shared $(GO_FLAG) w21_validator.c w21_deserializer.c core/cws_bson_utils.c core/cws_utils.c w21_config.c w21_events.c w21_input.c w21_messages.c w21_errors.c stdsoap2.c witsml21C_o3_native_shared.o $(GO_SRC_PATH)/w21go.c -I. -I$(GO_INCLUDE_PATH) -Icore/include -lbson-shared-${MONGO_C_BRANCH} -Lcore/lib -DNOHTTP -DVERGEN -D$(STAT) -Wall
	strip --strip-unneeded $(GO_LIB_PATH)/$(GO_LIB)
	@echo "Finished"
endif

cs: witsml21C_o3_native_shared
	@echo "Compiling .NET/C# wrapper"
ifneq ("$(wildcard $(CS_LIB_PATH)/$(CS_LIB))","")
	@echo "Already compiled $(CS_LIB). Skipping ..."
else
	@$(CC) -o $(CS_LIB_PATH)/$(CS_LIB) -shared $(CS_FLAG) w21_validator.c w21_deserializer.c core/cws_bson_utils.c core/cws_utils.c w21_config.c w21_events.c w21_input.c w21_messages.c w21_errors.c stdsoap2.c witsml21C_o3_native_shared.o $(CS_SRC_PATH)/w21_csparser.c -I. -I$(CS_INCLUDE_PATH) -Icore/include -lbson-shared-${MONGO_C_BRANCH} -Lcore/lib -DNOHTTP -DVERGEN -D$(STAT) -Wall
	strip --strip-unneeded $(CS_LIB_PATH)/$(CS_LIB)
	@echo "Finished"
endif

remove_bson:
ifneq ("$(wildcard $(LIBDIR)/lib*.a)","")
	@echo "Removing BSON library"
	rm -v $(LIBDIR)/lib*.a
	@echo "Removed"
else
	@echo "Nothing to do to remove BSON library"
endif

ifneq ("$(wildcard $(INCLUDEDIR)/bson)","")
	@echo "Removing BSON includes $(INCLUDEDIR)/bson"
	rm -rfv $(INCLUDEDIR)/bson
	@echo "Removed BSON includes $(INCLUDEDIR)/bson"
else
	@echo "Nothing to do to remove BSON includes $(INCLUDEDIR)/bson"
endif

ifneq ("$(wildcard $(MONGO_C_DIR))","")
	@echo "Removing Mongo C branch $(MONGO_C_BRANCH)"
	rm -rfv $(MONGO_C_DIR)
	@echo "Removed Mongo C $(MONGO_C_BRANCH)"
else
	@echo "Nothing to do to remove Mongo C $(MONGO_C_BRANCH)"
endif


remove_pre:
ifneq ("$(wildcard $(CURDIR)/witsml21C_o3_native_shared.o)","")
	@echo "Removing witsml21C_o3_native_shared.o ..."
	rm -v $(CURDIR)/witsml21C_o3_native_shared.o
	@echo "Removed"
else
	@echo "Nothing to do to witsml21C_o3_native_shared.o"
endif


clean:
ifneq ("$(wildcard $(CURDIR)/$(JNI_LIB_PATH)/$(JNI_LIB))","")
	@echo "Removing  $(JNI_LIB)..."
	rm -v $(CURDIR)/$(JNI_LIB_PATH)/$(JNI_LIB)
	@echo "Cleaning maven ..."
	cd $(CURDIR)/wrappers/java && mvn -U clean
	@echo "Cleaning Java examples"
	cd $(JAVA_EXAMPLES)/app/SimpleExample && mvn clean
 ifneq ("$(wildcard $(JAVA_EXAMPLES)/app/SimpleExample/*.bson)","")
	@echo "Removing *.bson documents in Vortex21 JAVA samples"
	rm -v $(JAVA_EXAMPLES)/app/SimpleExample/*.bson
 else
	@echo "No *.bson document found in Vortex21 JAVA samples"
 endif

 ifneq ("$(wildcard $(JAVA_EXAMPLES)/app/SimpleExample/*.json)","")

	rm -v $(JAVA_EXAMPLES)/app/SimpleExample/*.json
 else
	@echo "No *.json document found in Vortex21 JAVA samples"
 endif

	@echo "Finished"
else
	@echo "Nothing to do on removing $(JNI_LIB)"
endif

ifneq ("$(wildcard $(GO_LIB_PATH)/$(GO_LIB))","")
	@echo "Removing $(GO_LIB)..."
	rm -v $(GO_LIB_PATH)/$(GO_LIB)
	@echo "Finished"
else
	@echo "Nothing to do on removing $(GO_LIB)"
endif

ifneq ("$(wildcard $(CS_LIB_PATH)/$(CS_LIB))","")
	@echo "Removing $(CS_LIB)..."
	rm -v $(CS_LIB_PATH)/$(CS_LIB)
	@echo "Finished"
else
	@echo "Nothing to do on removing $(CS_LIB)"
endif

