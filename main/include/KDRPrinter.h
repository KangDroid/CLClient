//
// Created by KangDroid on 2021/02/14.
//

#ifndef CLCLIENT_KDRPRINTER_H
#define CLCLIENT_KDRPRINTER_H

#include <iostream>

#define RESET   "\033[0m"
#define RED     "\033[31m"

using namespace std;

class KDRPrinter {
public:
    static void print_error(string& error_message);
    static void print_error(const string& error_message);
};


#endif //CLCLIENT_KDRPRINTER_H
