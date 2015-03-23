LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE:= nativelib
LOCAL_CFLAGS:= -Werror
COCAL_SRC_FILES:= nativeInterface.c
LOCAL_LDLIBS:= -llog

inclue ($BUILD_SHARED_LIBRARY)