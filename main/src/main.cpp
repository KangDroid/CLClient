//
// Created by KangDroid on 2021/02/09.
//

#include <iostream>
#include <boost/program_options.hpp>

#include "KDRPrinter.h"

using namespace std;
using namespace boost::program_options;

int main(int argc, char** argv) {
    variables_map vm;
    options_description desc("Supported Options/Args");
    desc.add_options()
            ("help,h", "Display Help Message")
            ("master-ip", value<string>(), "Master IP to connect");
    try {
        store(parse_command_line(argc, argv, desc), vm);
    } catch (const exception& expn) {
        KDRPrinter::print_error(string(expn.what()));
    }

    if (vm.empty() || vm.count("help")) {
        cout << desc << endl;
    }
    return 0;
}
