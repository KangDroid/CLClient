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
    for (const string &er_message : error_message) {
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

void KDRPrinter::terminal_echo(const bool &disable_echo) {
    static struct termios old_termios, new_termios;

    // Get Current TERMIOS
    tcgetattr(STDIN_FILENO, &old_termios);
    new_termios = old_termios;

    // Disable Echo
    if (disable_echo) {
        new_termios.c_lflag &= ~(ECHO);
    } else {
        new_termios.c_lflag |= ECHO;
    }

    // Set
    tcsetattr(STDIN_FILENO, TCSANOW, &new_termios);
}

void KDRPrinter::print_normal(string &normal_message, bool newline) {
    if (newline) {
        cout << GREEN << normal_message << RESET << endl;
    } else {
        cout << GREEN << normal_message << RESET;
    }
}

void KDRPrinter::print_normal(const string &normal_message, bool newline) {
    if (newline) {
        cout << GREEN << normal_message << RESET << endl;
    } else {
        cout << GREEN << normal_message << RESET;
    }
}

void KDRPrinter::print_normal(vector<string> normal_message, bool newline) {
    for (string tmp : normal_message) {
        if (newline) {
            cout << GREEN << tmp << RESET << endl;
        } else {
            cout << GREEN << tmp << RESET;
        }
    }
}

void KDRPrinter::press_enter() {
    KDRPrinter::print_normal("Press enter key to continue...", false);
    string random_value;
    getline(cin, random_value);
}
