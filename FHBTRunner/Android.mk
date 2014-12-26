#
# This file was modified by Dolby Laboratories, Inc. The portions of the
# code that are surrounded by "DOLBY..." are copyrighted and
# licensed separately, as follows:
#
# (C) 2012-2013 Dolby Laboratories, Inc.
# All rights reserved.
#
# This program is protected under international and U.S. Copyright laws as
# an unpublished work. This program is confidential and proprietary to the
# copyright owners. Reproduction or disclosure, in whole or in part, or the
# production of derivative works therefrom without the express permission of
# the copyright owners is prohibited.
#
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_PACKAGE_NAME := FHBTRunner

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_SDK_VERSION := current

LOCAL_CERTIFICATE := platform

LOCAL_PRIVILEGED_MODULE := true

WITH_DEXPREOPT := false

include $(BUILD_PACKAGE)
