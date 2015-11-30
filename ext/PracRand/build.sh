g++ -c src/*.cpp src/RNGs/*.cpp src/RNGs/other/*.cpp -O3 -Iinclude -pthread
ar rcs libPractRand.a *.o
rm *.o
g++ -o RNG_test tools/RNG_test.cpp libPractRand.a -O3 -Iinclude -pthread -std=c++11
g++ -o RNG_output tools/RNG_output.cpp libPractRand.a -O3 -Iinclude -pthread -std=c++11
rm libPractRand.a
