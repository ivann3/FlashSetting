LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-subdir-java-files)

LOCAL_PACKAGE_NAME := FlashSetting

LOCAL_CERTIFICATE := platform

CAL_RESOURCE_DIR += $(LOCAL_PATH)/res

#输入第三方jar包的别名
LOCAL_STATIC_JAVA_LIBRARIES := \
        android-support-v4 \
        android-support-v7-appcompat \
#		log4j \
#		android-logging-log4
        
include $(BUILD_PACKAGE)

###################################################
#include $(CLEAR_VARS)
#冒号前面为jar别名，后面为jar文件的实际路径
#LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := log4j:/lib/log4j-1.2.17.jar \
#	android-logging-log4:/lib/android-logging-log4j-1.0.3.jar
#include $(BUILD_MULTI_PREBUILT)
###################################################

include $(call all-makefiles-under,$(LOCAL_PATH))

