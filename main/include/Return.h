//
// Created by KangDroid on 2021/02/10.
//

#ifndef CLCLIENT_RETURN_H
#define CLCLIENT_RETURN_H

#include <iostream>
#include <vector>

using namespace std;

template<typename TARGET>
class Return {
private:
    vector<string> error_message;

public:
    TARGET inner_values;

public:

    /**
     * get_message: Return Error Message.
     * @return error_message
     */
    vector<string> &get_message() {
        return this->error_message;
    }

    /**
     * append_err_message: Append param string to error_message[vector]
     * @param message to append.
     */
    void append_err_message(string message) {
        error_message.push_back(message);
    }

    /**
     * append_err_message: Append param vector string to error_message[vector]
     * @param message to append
     */
    void append_err_message(vector<string> &message) {
        for (const string &tmp : message) {
            error_message.push_back(tmp);
        }
    }

    Return(vector<string> &message) {
        error_message = message;
    }

    Return(const vector<string> &message) {
        error_message = message;
    }

    Return(const string &message) {
        error_message.push_back(message);
    }

    Return(TARGET inner_values) {
        this->inner_values = inner_values;
    }

    Return(TARGET inner_values, const string &message) {
        this->inner_values = inner_values;
        error_message.push_back(message);
    }

    Return(TARGET inner_values, string &message) {
        this->inner_values = inner_values;
        error_message.push_back(message);
    }

    Return(TARGET inner_values, vector<string> &message) {
        this->inner_values = inner_values;
        this->error_message = message;
    }

    Return(TARGET inner_values, const vector<string> &message) {
        this->inner_values = inner_values;
        this->error_message = message;
    }
};

#endif //CLCLIENT_RETURN_H
