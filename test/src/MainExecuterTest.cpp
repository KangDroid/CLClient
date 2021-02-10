//
// Created by KangDroid on 2021/02/09.
//
#include <gtest/gtest.h>
#include <iostream>
#include "MainExecuter.h"

class MainExecutorTest : public testing::Test, protected MainExecutor {
};

TEST_F(MainExecutorTest, isRequestingWorks) {
    master_url = "http://localhost:8080";
    EXPECT_EQ(request_container(), true);
}

TEST_F(MainExecutorTest, isRegionShowWorks) {
    master_url = "http://localhost:8080";
    EXPECT_EQ(show_regions(), true);
}