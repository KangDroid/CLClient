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
    try {
        store(parse_command_line(argc, argv, desc), vm);
    } catch (const exception& expn) {
        print_error(string(expn.what()));
    }

    if (vm.empty() || vm.count("help")) {
        cout << desc << endl;
        return;
    }

    if (vm.count("master-address")) {
        this->master_url = vm["master-address"].as<string>();
    }

    if (vm.count("create-container")) {
        // Show Region!
        auto region_response = show_regions();
        if (!region_response.get_message().empty()) {
            print_error(region_response.get_message());
            return;
        }

        // Get Data from STDIN
        get_data_stdin();

        // Request Container[Server Call]
        auto request_response = request_container();
        if (!request_response.get_message().empty()) {
            print_error(request_response.get_message());
        }
    }
}

MainExecutor::MainExecutor(int argc, char **argv) {
    this->master_url = "http://localhost:8080";
    this->argc = argc;
    this->argv = argv;

    this->parse_main();
}

Return<bool> MainExecutor::request_container() {
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

    // The Response
    http_response web_response = get_response(client_req, request_type);
    json::value response_data = web_response.extract_json().get();

    cout << endl << endl;
    string err_message = response_data["errorMessage"].as_string();

    if (!err_message.empty()) {
        return Return<bool>(err_message);
    }

    cout << "Container Region: " << response_data["regionLocation"].as_string() << "." << endl;
    cout << "Container ID: " << response_data["containerId"].as_string() << "." << endl;
    cout << "Container Successfully Created!" << endl;
    cout << "Now you can ssh into: \"ssh root@" << response_data["targetIpAddress"].as_string() << " -p "
         << response_data["targetPort"].as_string() << "\"";

    return Return<bool>(true);
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

Return<bool> MainExecutor::show_regions() {
    string url = master_url + "/api/client/node/load";

    // The Client
    http_client client_req(url);

    // The body
    http_request request_type(methods::GET);
    http_response response = get_response(client_req, request_type);

    json::value main_object = response.extract_json().get();

    if (main_object.size() == 0) {
        return Return<bool>("No registered compute node found on master server!");
    }

    // Object Array
    for (int i = 0; i < main_object.size(); i++) {
        cout << "Region Name: " << main_object[i]["regionName"].as_string() << endl;
        cout << "Region Load[%]: " << main_object[i]["nodeLoadPercentage"].as_string() << endl;
        cout << endl;
    }

    return Return<bool>((response.status_code() == http::status_codes::OK));
}

http_response MainExecutor::get_response(http_client &client, http_request &request_type) {
    http_response ret_response;
    try {
        ret_response = client.request(request_type).get();
    } catch (const exception &expn) {
        print_error(expn.what());
    }
    return ret_response;
}

void MainExecutor::print_error(string &message) {
    cerr << RED << "Error: " << message << RESET << endl;
}

void MainExecutor::print_error(const string &message) {
    cerr << RED << "Error: " << message << RESET << endl;
}
