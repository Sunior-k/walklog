cmake_minimum_required(VERSION 3.22.1)
project("walking_insights" LANGUAGES CXX)

add_library(
    walking_insights
    SHARED
    walking_insights_engine.cpp
    walking_insights_jni.cpp
)

target_include_directories(walking_insights PRIVATE .)

find_library(log-lib log)

target_link_libraries(
    walking_insights
    ${log-lib}
)
