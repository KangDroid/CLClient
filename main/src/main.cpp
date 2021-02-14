//
// Created by KangDroid on 2021/02/09.
//

#include <iostream>
#include <boost/program_options.hpp>

#include "ServerManagement.h"
#include "KDRPrinter.h"

using namespace std;
using namespace boost::program_options;

int main(int argc, char** argv) {
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
    return 0;
}
