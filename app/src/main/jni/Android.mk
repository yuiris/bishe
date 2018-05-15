LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE    := native-lib
LOCAL_SRC_FILES := \
	audio_ns.cpp\
        noise_suppression.c \
        ns_core.c \
        fft4g.c \
        native-lib.cpp
include $(BUILD_SHARED_LIBRARY)