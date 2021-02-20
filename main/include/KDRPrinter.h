//
// Created by KangDroid on 2021/02/14.
//

#ifndef CLCLIENT_KDRPRINTER_H
#define CLCLIENT_KDRPRINTER_H

#include <iostream>
#include <vector>

#include <termios.h>
#include <unistd.h>

#define RESET   "\033[0m"
#define RED     "\033[31m"
#define GREEN   "\033[32m"

using namespace std;

class KDRPrinter {
public:
    static void print_error(string &error_message);

    static void print_error(const string &error_message);

    static void print_error(vector<string> &error_message);

    static void print_verbose(string &verbose_message);

    static void print_verbose(const string &verbose_message);

    static void print_verbose(vector<string> &verbose_message);

    static void print_normal(string &normal_message, bool newline = true);

    static void print_normal(const string &normal_message, bool newline = true);

    static void print_normal(vector<string> normal_message, bool newline = true);

    static void press_enter();

    static void terminal_echo(const bool &disable_echo);
};


#endif //CLCLIENT_KDRPRINTER_H
