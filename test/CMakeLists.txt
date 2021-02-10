cmake_minimum_required(VERSION 3.15)
project(CLClientTest)

if (APPLE)
    set(OPENSSL_ROOT_DIR "/usr/local/opt/openssl")
    set(OPENSSL_LIBRARIES "/usr/local/opt/openssl/lib")
endif()

find_package(cpprestsdk REQUIRED)
find_package (OpenSSL REQUIRED)
find_package(Boost REQUIRED COMPONENTS system program_options)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -D_TURN_OFF_PLATFORM_STRING")

#Add the google test subdirectory
add_subdirectory(googletest)

include_directories(../main/include)

#include googletest/include dir
include_directories(googletest/googletest/include)

#include the googlemock/include dir
include_directories(googletest/googlemock/include)

#Define your executable
add_executable(CLClientTest src/test_main.cpp ../main/src/MainExecuter.cpp ../main/include/MainExecuter.h ../main/include/Return.h src/MainExecuterTest.cpp)

#Link with GoogleTest
target_link_libraries(CLClientTest gtest gtest_main)

#Link with GoogleMock
target_link_libraries(CLClientTest gmock gmock_main)

TARGET_LINK_LIBRARIES(CLClientTest cpprestsdk::cpprest OpenSSL::SSL Boost::system Boost::program_options OpenSSL::Crypto)