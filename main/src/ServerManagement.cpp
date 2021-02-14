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
    http_client* client_request = nullptr;

    try {
        client_request = new http_client(final_url);
    } catch (const exception& expn) {
        Return<bool> tmp_value = Return<bool>(false);
        tmp_value.append_err_message(expn.what());
        tmp_value.append_err_message("\ni.e: http://localhost:8080");
        return tmp_value;
    }

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
