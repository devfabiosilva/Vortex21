ENDIAN?=LITTLE
STAT?=WITH_STATISTICS
AR=ar rcs
LD=ld -r -b binary
CC=gcc
STRIP=strip
CURDIR=$(PWD)
INCLUDEDIR=$(CURDIR)/core/include

FLAG=-lpthread -Wno-stringop-truncation -O3 -fPIC -march=native -fno-plt -D$(STAT) -DCWS_$(ENDIAN)_ENDIAN
#DEBUG_FLAG=-g -fsanitize=address,leak -DSOAP_DEBUG $(FLAG)

MONGO_C_GIT=https://github.com/mongodb/mongo-c-driver.git
MONGO_C_BRANCH=2.2.1
MONGO_C_DIR=$(CURDIR)/third-party/mongo-c-driver

LIBDIR=$(CURDIR)/core/lib

JNI_LIB_PATH=wrappers/java
JNI_LIB=libw21parser.so

all: main

witsml21C_o3_native_shared: install_bson
ifneq ("$(wildcard $(CURDIR)/witsml21C_o3_native_shared.o)","")
	@echo "Nothing to do. Skipping witsml21C_o3_native_shared.o"
else
	@echo "Compiling witsml21C_o3_native_shared.o. It can take a little longer ..."
	@$(CC) -c -o witsml21C_o3_native_shared.o $(FLAG) -I. -Icore/include witsml21C.c -DNOHTTP -Wall
	@echo "witsml21C_o3_native_shared.o finished"
endif

main: witsml21C_o3_native_shared
ifneq ("$(wildcard $(CURDIR)/$(JNI_LIB_PATH)/$(JNI_LIB))","")
	@echo "Nothing to do. $(JNI_LIB)"
else
	@echo "Compiling ..."
	@$(CC) -o $(JNI_LIB_PATH)/$(JNI_LIB) -shared $(FLAG) -I/usr/lib/jvm/java-11-openjdk-amd64/include -I/usr/lib/jvm/java-11-openjdk-amd64/include/linux w21_validator.c w21_deserializer.c core/cws_bson_utils.c core/cws_utils.c w21_config.c w21_events.c w21_input.c w21_messages.c stdsoap2.c  witsml21C_o3_native_shared.o wrappers/java/parser.c -I. -Icore/include -Iwrappers/java -lbson-shared-2.2.1 -Lcore/lib -DNOHTTP -DVERGEN -D$(STAT) -Wall
	@echo "Finished"
endif

.PHONY:
install_bson:
	@echo "Check if mongo-c-driver directory exists ..."
ifneq ("$(wildcard $(MONGO_C_DIR))","")
	@echo "Already cloned. Skip"
else
	@echo "Cloning branch $(MONGO_C_BRANCH) from $(MONGO_C_GIT)"
	pwd && cd $(CURDIR)/third-party && pwd && git clone -b $(MONGO_C_BRANCH) $(MONGO_C_GIT) && cd mongo-c-driver && mkdir compiled && cd compiled && cmake .. -DCMAKE_BUILD_TYPE=Release -DENABLE_MONGOC=OFF -DCMAKE_C_FLAGS="-O3 -march=native -fno-plt" -DCMAKE_INSTALL_PREFIX=$(MONGO_C_DIR)/compiled/out && make -j12 && make install && pwd && cp out/lib/libbson2.a $(LIBDIR)/libbson-static-$(MONGO_C_BRANCH).a -v && cp -frv out/include/bson-$(MONGO_C_BRANCH)/bson $(INCLUDEDIR) && cd src/libbson/CMakeFiles/bson_shared.dir && pwd && ar rcs $(LIBDIR)/libbson-shared-$(MONGO_C_BRANCH).a src/bson/*.o src/jsonsl/*.o __/common/src/*.o
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
	@echo "Finished"
else
	@echo "Nothing to do on removing $(JNI_LIB)"
endif

