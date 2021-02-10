//
// Created by KangDroid on 2021/02/10.
//

#ifndef CLCLIENT_RETURN_H
#define CLCLIENT_RETURN_H

#include <iostream>

using namespace std;

template<typename TARGET>
class Return {
private:
    string error_message;

public:
    TARGET inner_values;

public:

    /**
     * get_message: Return Error Message.
     * @return error_message
     */
    string &get_message() {
        return this->error_message;
    }

    /**
     * set_err_message: Set param string to error_message.
     * Warning: "SET" error message means it will overwrite current content of
     * error_message. Use append_err_message to append it.
     * @param message to set
     */
    void set_err_message(string message) {
        this->error_message = message;
    }

    /**
     * append_err_message: Append param string to error_message
     * This won't reset current content of error_message, unlike set_err_message.
     * @param message to append.
     */
    void append_err_message(string message) {
        this->error_message += message;
    }

    Return() {
        this->error_message = "";
    }

    Return(const string &message) {
        this->error_message = message;
    }

    Return(TARGET inner_values) {
        this->inner_values = inner_values;
        this->error_message = "";
    }
};

#endif //CLCLIENT_RETURN_H
