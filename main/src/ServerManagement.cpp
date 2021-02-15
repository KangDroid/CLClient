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
    Return<http_client*> client_request_r = create_client(final_url, 5);
    if (!client_request_r.get_message().empty() || client_request_r.inner_values == nullptr) {
        Return<bool> error_return(false);
        error_return.append_err_message(client_request_r.get_message());
        return error_return;
    }
    http_client* client_request = client_request_r.inner_values;
    http_request request_type(methods::GET);

    Return<http_response> response = get_response(*client_request, request_type);

    if (!response.get_message().empty()) {
        // Error Occured
        Return<bool> tmp_value = Return<bool>(false);
        tmp_value.append_err_message(response.get_message());
        return tmp_value;
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
        Return<http_client*> ret_value(nullptr);
        ret_value.append_err_message(expn.what());
        ret_value.append_err_message("i.e: http://localhost:8080");
        return ret_value;
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
    Return<http_client*> client_response = create_client(final_url, 5);
    if (!client_response.get_message().empty()) {
        Return<bool> error_return(false);
        error_return.append_err_message(client_response.get_message());
        return error_return;
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
        Return<bool> error_return(false);
        error_return.append_err_message(response.get_message());
        return error_return;
    }

    // Parse Response
    json::value login_response_dto = response.inner_values.extract_json().get();
    string error_message = login_response_dto["errorMessage"].as_string();
    if (!error_message.empty()) {
        Return<bool> error_return(false);
        error_return.append_err_message(error_message);
        return error_return;
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
    cout << "Input ID: ";
    getline(cin, *id);

    // Get Password
    cout << "Input Password: ";
    // Disable Terminal Echo
    KDRPrinter::terminal_echo(true);
    getline(cin, *password);
    KDRPrinter::terminal_echo(false); // Reset Terminal Echo
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
    Return<http_client*> client_get = create_client(final_url, 5);
    if (!client_get.get_message().empty()) {
        Return<bool> error_return(false);
        error_return.append_err_message(client_get.get_message());
        return error_return;
    }
    http_client* client = client_get.inner_values;
    http_request client_request(methods::GET);

    // Request!
    Return<http_response> response = get_response(*client, client_request);
    if (!response.get_message().empty()) {
        Return<bool> error_return(false);
        error_return.append_err_message(response.get_message());
        return error_return;
    }

    // Parse Output
    json::value response_json;
    response_json = response.inner_values.extract_json().get();

    if (response_json.size() == 0) {
        return Return<bool>(false, "Compute Node is not registered on master server!");
    }

    for (int i = 0; i < response_json.size(); i++) {
        cout << "Node " << i + 1 << ":" << endl;
        cout << "Region: " << response_json[i]["regionName"].as_string() << endl;
        cout << "Load: " << response_json[i]["nodeLoadPercentage"].as_string() << endl;
        cout << endl;
    }

    // Remove Information
    delete client; client = nullptr;
}
