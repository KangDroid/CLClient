//
// Created by KangDroid on 2021/02/14.
//

#include <gtest/gtest.h>
#include <cpprest/http_client.h>
#include <cpprest/json.h>

#include "ServerManagement.h"
#include "Return.h"

using namespace web;
using namespace web::http;
using namespace web::http::client;

#ifndef CLCLIENT_SERVERMANAGEMENTTEST_H
#define CLCLIENT_SERVERMANAGEMENTTEST_H

class ServerManagementTest: public testing::Test {
public:
    ServerManagement server_management;
};

// Use Github URL to checkout whether getting response is working.
TEST_F(ServerManagementTest, isRequestingWorking) {
    http_client client_test("https://github.com/"); // should work
    http_request client_request(methods::GET);

    Return<http_response> response = server_management.get_response(
            client_test, client_request
    );

    // There should be no error message at all.
    EXPECT_EQ(response.get_message().size(), 0);
}

// While Testing, we do not know whether server is alive or not.
// Therefore, Use False-Test only for now.
TEST_F(ServerManagementTest, isServerAliveWorking) {
    Return<bool> response = server_management.is_server_alive();
    EXPECT_NE(response.get_message().size(), 0);
    EXPECT_EQ(response.inner_values, false);
}

#endif //CLCLIENT_SERVERMANAGEMENTTEST_H
