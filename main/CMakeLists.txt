project(CLClient)

include_directories(include)

if (APPLE)
    set(OPENSSL_ROOT_DIR "/usr/local/opt/openssl")
    set(OPENSSL_LIBRARIES "/usr/local/opt/openssl/lib")
endif ()

find_package(cpprestsdk REQUIRED)
find_package(OpenSSL REQUIRED)
find_package(Boost REQUIRED COMPONENTS system program_options)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -D_TURN_OFF_PLATFORM_STRING")

add_executable(CLClient src/main.cpp include/Return.h src/KDRPrinter.cpp include/KDRPrinter.h)
TARGET_LINK_LIBRARIES(CLClient PRIVATE cpprestsdk::cpprest OpenSSL::SSL Boost::system Boost::program_options OpenSSL::Crypto)