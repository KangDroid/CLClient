//
// Created by KangDroid on 2021/02/09.
//

#ifndef CLCLIENT_MAINEXECUTER_H
#define CLCLIENT_MAINEXECUTER_H

#include <boost/program_options.hpp>
#include <cpprest/http_client.h>
#include <cpprest/json.h>

#include <iostream>

#include "Return.h"

using namespace std;
using namespace boost::program_options;
using namespace web;
using namespace web::http;
using namespace web::http::client;

#define RESET   "\033[0m"
#define RED     "\033[31m"

class DataTransferObject {
public:
    long id;
    string userName;
    string userPassword;
    string dockerId;
    string computeRegion;
};

class MainExecutor {
protected:
    int argc;
    char **argv;
    DataTransferObject dto;

protected:
    string master_url;

protected:
    void get_data_stdin();

    void parse_main();

    void print_error(string &message);
    void print_error(const string &message);

    Return<bool> show_regions();

    Return<bool> request_container();

    http_response get_response(http_client &client, http_request &request_type);

public:
    MainExecutor(int argc, char **argv);

    MainExecutor() {}
};


#endif //CLCLIENT_MAINEXECUTER_H
