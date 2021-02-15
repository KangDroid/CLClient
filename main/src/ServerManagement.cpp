//
// Created by KangDroid on 2021/02/14.
//

#include "ServerManagement.h"

Return<http_response> ServerManagement::get_response(http_client &client, http_request &request_type) {
    http_response ret_response;
    try {
        ret_response = client.request(request_type).get();
    } catch (const exception& expn) {
        return Return<http_response>(expn.what());
    }
    return Return<http_response>(ret_response);
}

Return<bool> ServerManagement::is_server_alive() {
    string final_url = server_base_url + "/api/client/alive";

    // Create http_client
    Return<http_client*> client_request_r = create_client(final_url);
    if (!client_request_r.get_message().empty() || client_request_r.inner_values == nullptr) {
        return Return<bool>(false, client_request_r.get_message());
    }
    http_client* client_request = client_request_r.inner_values;
    http_request request_type(methods::GET);

    Return<http_response> response = get_response(*client_request, request_type);

    if (!response.get_message().empty()) {
        // Error Occured
        return Return<bool>(false, response.get_message());
    }

    delete client_request;
    return Return<bool>(true);
}

Return<http_client *> ServerManagement::create_client(string &url, int timeout) {
    http_client_config client_config;
    client_config.set_timeout(chrono::seconds(timeout));
    http_client* client_return = nullptr;

    try {
        client_return = new http_client(url, client_config);
    } catch (const exception& expn) {
        return Return<http_client*>(nullptr, vector<string> {expn.what(), "i.e: http://localhost:8080"});
    }

    return Return<http_client*>(client_return);
}

ServerManagement::ServerManagement() {
    this->user_token = nullptr;
}

Return<bool> ServerManagement::login(const bool& is_register) {
    string final_url;
    if (is_register) {
        final_url = server_base_url + "/api/client/register";
    } else {
        final_url = server_base_url + "/api/client/login";
    }

    // Input Password
    string* id = new string();
    string* password = new string();
    input_password(id, password);

    // Login User!
    http_client* client_request = nullptr;
    http_request request_type(methods::POST);
    Return<http_client*> client_response = create_client(final_url);
    if (!client_response.get_message().empty()) {
        return Return<bool>(false, client_response.get_message());
    }
    client_request = client_response.inner_values;

    // Put Information
    json::value login_dto = json::value::object();
    login_dto["userName"] = json::value::string(*id);
    login_dto["userPassword"] = json::value::string(*password);
    request_type.set_body(login_dto);

    // Get Response
    Return<http_response> response = get_response(*client_request, request_type);
    if (!response.get_message().empty()) {
        return Return<bool>(false, response.get_message());
    }

    // Parse Response
    json::value login_response_dto = response.inner_values.extract_json().get();
    string error_message = login_response_dto["errorMessage"].as_string();
    if (!error_message.empty()) {
        return Return<bool>(false, error_message);
    } else {
        if (!is_register) {
            user_token = new string();
            *user_token = login_response_dto["token"].as_string();
        }
    }

    // Remove All Information[Dynamically]
    delete client_request; client_request = nullptr;
    delete id; id = nullptr;
    delete password; password = nullptr;

    return Return<bool>(true);
}

Return<bool> ServerManagement::needs_login() {
    return Return<bool>((user_token == nullptr));
}

void ServerManagement::input_password(string *id, string* password) {
    // Get ID
    KDRPrinter::print_normal("Input ID: ", false);
    getline(cin, *id);

    // Get Password
    KDRPrinter::print_normal("Input Password: ", false);
    // Disable Terminal Echo
    KDRPrinter::terminal_echo(true);
    getline(cin, *password);
    KDRPrinter::terminal_echo(false); // Reset Terminal Echo
    KDRPrinter::print_normal("");
}

ServerManagement::~ServerManagement() {
    if (user_token != nullptr) {
        delete user_token;
    }
}

Return<bool> ServerManagement::show_regions() {
    string final_url = server_base_url + "/api/client/node/load";
    if (needs_login().inner_values) {
        return Return<bool>(false, "User did not logged in!");
    }
    Return<http_client*> client_get = create_client(final_url);
    if (!client_get.get_message().empty()) {
        return Return<bool>(false, client_get.get_message());
    }
    http_client* client = client_get.inner_values;
    http_request client_request(methods::GET);

    // Request!
    Return<http_response> response = get_response(*client, client_request);
    if (!response.get_message().empty()) {
        return Return<bool>(false, response.get_message());
    }

    // Parse Output
    json::value response_json;
    response_json = response.inner_values.extract_json().get();

    if (response_json.size() == 0) {
        return Return<bool>(false, "Compute Node is not registered on master server!");
    }

    for (int i = 0; i < response_json.size(); i++) {
        KDRPrinter::print_normal("Node " + to_string(i + 1) + ":");
        KDRPrinter::print_normal("Region: " + response_json[i]["regionName"].as_string());
        KDRPrinter::print_normal("Load: " + response_json[i]["nodeLoadPercentage"].as_string());
        KDRPrinter::print_normal("");
    }

    // Remove Information
    delete client; client = nullptr;

    return Return<bool>(true);
}

Return<bool> ServerManagement::create_image() {
    if (needs_login().inner_values) {
        return Return<bool>(false, "User did not logged in!");
    }
    string final_url = server_base_url + "/api/client/node/create";
    string region;
    Return<bool> region_response = show_regions();
    if (!region_response.inner_values) {
        return region_response;
    }

    KDRPrinter::print_normal("Input Compute Region: ", false);
    getline(cin, region);

    Return<http_client*> client_response = create_client(final_url);
    if (client_response.inner_values == nullptr) {
        return Return<bool>(false, client_response.get_message());
    }

    // Set up Client Request
    http_client* client = client_response.inner_values;
    http_request client_req(methods::POST);
    json::value main_send = json::value::object();
    main_send["userToken"] = json::value::string(*user_token);
    main_send["dockerId"] = json::value::string("");
    main_send["computeRegion"] = json::value::string(region);
    client_req.set_body(main_send);

    // Request!
    Return<http_response> ret_response = get_response(*client, client_req);
    if (!ret_response.get_message().empty()) {
        return Return<bool>(false, ret_response.get_message());
    }

    // Response?
    json::value response_main = ret_response.inner_values.extract_json().get();
    if (!response_main["errorMessage"].as_string().empty()) {
        return Return<bool>(false, response_main["errorMessage"].as_string());
    }
    KDRPrinter::print_normal("Container-ID: " + response_main["containerId"].as_string());
    KDRPrinter::print_normal("Successfully created on: " + response_main["regionLocation"].as_string());
    KDRPrinter::print_normal("You can ssh within: \"ssh " + response_main["targetIpAddress"].as_string() +
    " -p " + response_main["targetPort"].as_string() + "\"");

    delete client; client = nullptr;

    return Return<bool>(true);
}
