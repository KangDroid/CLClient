//
// Created by KangDroid on 2021/02/09.
//

#include <iostream>
#include <boost/program_options.hpp>

#include "ServerManagement.h"
#include "KDRPrinter.h"

using namespace std;
using namespace boost::program_options;

int print_menu(ServerManagement& server_management) {
    string input_tmp;
    int menu_selection;
    cout << "KDR-Cloud Menu:" << endl;
    cout << "1. Login" << endl;
    cout << "2. Register" << endl;
    cout << "3. Request Image" << endl;
    cout << "0. Exit" << endl;
    cout << endl;
    cout << "Enter Menu Number: ";

    // Get input
    getline(cin, input_tmp);
    try {
        menu_selection = stoi(input_tmp);
    } catch (const exception& expn) {
        KDRPrinter::print_error(expn.what());
        KDRPrinter::print_error("Please input correct number!");
        return 0;
    }

    switch(menu_selection) {
        case 1: {
            if (!server_management.needs_login().inner_values) {
                KDRPrinter::print_error("Already Logged-In!");
                break;
            }
            Return<bool> response = server_management.login(false);
            if (!response.get_message().empty()) {
                KDRPrinter::print_error(response.get_message());
            } else {
                KDRPrinter::print_verbose("Login Succeed!");
            }
        }
            break;
        case 2: {
            if (!server_management.needs_login().inner_values) {
                KDRPrinter::print_error("Already Logged-In!");
                break;
            }
            Return<bool> response = server_management.login(true);
            if (!response.get_message().empty()) {
                KDRPrinter::print_error(response.get_message());
            } else {
                KDRPrinter::print_verbose("Register Succeed!");
            }
        }
            break;
        case 3: {
            Return<bool> response = server_management.create_image();
            if (!response.get_message().empty()) {
                KDRPrinter::print_error(response.get_message());
            } else {
                KDRPrinter::print_verbose("Image Creation Succeed!");
            }
        }
            break;
        default:
            break;
    }

    return menu_selection;
}

int main(int argc, char** argv) {
    int menu_selection;
    ServerManagement server_management;
    variables_map vm;
    options_description desc("Supported Options/Args");
    desc.add_options()
            ("help,h", "Display Help Message")
            ("master-ip", value<string>(), "Master IP to connect");
    try {
        store(parse_command_line(argc, argv, desc), vm);
    } catch (const exception& expn) {
        KDRPrinter::print_error(string(expn.what()));
        return 1;
    }

    if (vm.count("help")) {
        cout << desc << endl;
        return 0;
    }

    if (vm.contains("master-ip")) {
        server_management.server_base_url = vm["master-ip"].as<string>();
    } else {
        KDRPrinter::print_error("This Program needs Master-IP to continue!");
        return 1;
    }

    // do work
    Return<bool> server_alive = server_management.is_server_alive();
    if (!server_alive.get_message().empty()) {
        KDRPrinter::print_error(server_alive.get_message());
        return 1;
    } else {
        KDRPrinter::print_verbose("Connecting to server succeed!");
    }

    do {
        menu_selection = print_menu(server_management);
    } while(menu_selection != 0);
    return 0;
}
