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

void KDRPrinter::print_error(vector<string> &error_message) {
    for (const string& er_message : error_message) {
        cerr << RED << "[Error]: " << er_message << RESET << endl;
    }
}

void KDRPrinter::print_verbose(string &verbose_message) {
    cout << GREEN << "[Verbose]: " << verbose_message << RESET << endl;
}

void KDRPrinter::print_verbose(const string &verbose_message) {
    cout << GREEN << "[Verbose]: " << verbose_message << RESET << endl;
}

void KDRPrinter::print_verbose(vector<string> &verbose_message) {
    for (string vm : verbose_message) {
        cout << GREEN << "[Verbose]: " << vm << RESET << endl;
    }
}
