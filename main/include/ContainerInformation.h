//
// Created by KangDroid on 2021/02/21.
//

#ifndef CLCLIENT_CONTAINERINFORMATION_H
#define CLCLIENT_CONTAINERINFORMATION_H

#include <iostream>

using namespace std;

class ContainerInformation {
public:
    string container_id;
    string compute_region;

    ContainerInformation(const string& id, const string& region) {
        this->container_id = id;
        this->compute_region = region;
    }
};

#endif //CLCLIENT_CONTAINERINFORMATION_H
