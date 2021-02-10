//
// Created by KangDroid on 2021/02/09.
//
#include <gtest/gtest.h>
#include <iostream>
#include "MainExecuter.h"
#include "Return.h"

class MainExecutorTest : public testing::Test, protected MainExecutor {
};

TEST_F(MainExecutorTest, isRequestingWorks) {
    master_url = "http://localhost:8080";
    auto response = request_container();
    EXPECT_EQ(response.get_message().empty(), true);
}

TEST_F(MainExecutorTest, isRegionShowWorks) {
    master_url = "http://localhost:8080";
    auto response = show_regions();
    EXPECT_EQ(response.get_message().empty(), true);
}