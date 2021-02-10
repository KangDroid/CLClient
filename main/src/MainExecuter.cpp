//
// Created by KangDroid on 2021/02/09.
//

#include "MainExecuter.h"

void MainExecutor::parse_main() {
    variables_map vm;
    options_description desc("Supported Options/Args");
    desc.add_options()
            ("help,h", "Display Help Message")
            ("create-container", "Create Container from server")
            ("master-address,I", value<string>(), "Master Server Address[With port]");
    store(parse_command_line(argc, argv, desc), vm);

    if (vm.empty() || vm.count("help")) {
        cout << desc << endl;
        return;
    }

    if (vm.count("master-address")) {
        this->master_url = vm["master-address"].as<string>();
    }

    if (vm.count("create-container")) {
        // Show Region!
        if (!show_regions()) {
            cerr << "Exiting.." << endl;
            return;
        }

        // Get Data from STDIN
        get_data_stdin();

        // Request Container[Server Call]
        if (!request_container()) {
            cerr << "Error Occurred, Exiting!" << endl;
        }
    }
}

MainExecutor::MainExecutor(int argc, char **argv) {
    this->master_url = "http://localhost:8080";
    this->argc = argc;
    this->argv = argv;

    this->parse_main();
}

bool MainExecutor::request_container() {
    string url = master_url + "/api/client/register";

    // The Client
    http_client client_req(url);

    // The body
    http_request request_type(methods::POST);

    json::value main_post = json::value::object();
    main_post["id"] = json::value::number(10);
    main_post["userName"] = json::value::string(dto.userName);
    main_post["userPassword"] = json::value::string(dto.userPassword);
    main_post["dockerId"] = json::value::string("");
    main_post["computeRegion"] = json::value::string(dto.computeRegion);

    request_type.set_body(main_post);
    json::value response_data;

    try {
        client_req.request(request_type).then([&response_data](http_response hr) {
            response_data = hr.extract_json().get();
        }).wait();
    } catch (const exception &expn) {
        cerr << "Error: ";
        cerr << expn.what() << endl;
        return false;
    }

    cout << endl << endl;
    string err_message = response_data["errorMessage"].as_string();

    if (!err_message.empty()) {
        cerr << "Error Occurred: " << err_message << endl;
        return false;
    }

    cout << "Container Region: " << response_data["regionLocation"].as_string() << "." << endl;
    cout << "Container ID: " << response_data["containerId"].as_string() << "." << endl;
    cout << "Container Successfully Created!" << endl;
    cout << "Now you can ssh into: \"ssh root@" << response_data["targetIpAddress"].as_string() << " -p "
         << response_data["targetPort"].as_string() << "\"";

    return true;
}

void MainExecutor::get_data_stdin() {
    // User Name
    cout << "Input UserName: ";
    getline(cin, dto.userName);
    // User Password
    cout << "Input Password: ";
    getline(cin, dto.userPassword);
    // ComputeRegion
    cout << "Enter Compute Region: ";
    getline(cin, dto.computeRegion);
}

bool MainExecutor::show_regions() {
    string url = master_url + "/api/client/node/load";

    // The Client
    http_client client_req(url);

    // The body
    http_request request_type(methods::GET);
    http_response response;

    try {
        client_req.request(request_type).then([&response](http_response hr) {
            response = hr;
        }).wait();
    } catch (const exception &expn) {
        cerr << "Error: ";
        cerr << expn.what() << endl;
        return false;
    }

    json::value main_object = response.extract_json().get();

    if (main_object.size() == 0) {
        cerr << "No registered compute node found on master server!" << endl;
        return false;
    }

    // Object Array
    for (int i = 0; i < main_object.size(); i++) {
        cout << "Region Name: " << main_object[i]["regionName"].as_string() << endl;
        cout << "Region Load[%]: " << main_object[i]["nodeLoadPercentage"].as_string() << endl;
        cout << endl;
    }

    return (response.status_code() == http::status_codes::OK);
}
