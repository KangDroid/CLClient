//
// Created by KangDroid on 2021/02/14.
//

#include "KDRPrinter.h"

void KDRPrinter::print_error(string &error_message) {
    cerr << RED << "[Error]: " << error_message << RESET << endl;
}

void KDRPrinter::print_error(const string &error_message) {
    cerr << RED << "[Error]: " << error_message << RESET << endl;
}
