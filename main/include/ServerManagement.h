//
// Created by KangDroid on 2021/02/14.
//

#ifndef CLCLIENT_SERVERMANAGEMENT_H
#define CLCLIENT_SERVERMANAGEMENT_H

#include <iostream>
#include <cpprest/http_client.h>
#include <cpprest/json.h>

#include "KDRPrinter.h"
#include "Return.h"

using namespace std;
using namespace web;
using namespace web::http;
using namespace web::http::client;

class ServerManagement {
private:
    string* user_token;
    Return<http_client*> create_client(string& url, int timeout = 5);
    void input_password(string* id, string* password);

public:
    string server_base_url; // must include ports as well, i.e http://localhost:8080

public:
    Return<bool> needs_login();

public:
    Return<http_response> get_response(http_client &client, http_request &request_type);
    Return<bool> is_server_alive();
    Return<bool> login(const bool& is_register);
    Return<bool> show_regions();
    Return<bool> create_image();
    ServerManagement();
    ~ServerManagement();
};


#endif //CLCLIENT_SERVERMANAGEMENT_H
