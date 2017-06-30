#include <stdlib.h>     /* srand, rand */
#include <time.h>       /* time */
#include <iostream>
#include <ctime>

using namespace std;

long lrand()
{
    if (sizeof(int) < sizeof(long))
        return (static_cast<long>(rand()) << (sizeof(int) * 8)) |
               rand();

    return rand();
}

int getHashPosition(long rawHash, int mask)
{
    // Avalanches the bits of a long integer by applying the finalisation step of MurmurHash3.
    //
    // This function implements the finalisation step of Austin Appleby's <a href="http://sites.google.com/site/murmurhash/">MurmurHash3</a>.
    // Its purpose is to avalanche the bits of the argument to within 0.25% bias. It is used, among other things, to scramble quickly (but deeply) the hash
    // values returned by {@link Object#hashCode()}.
    //

    rawHash ^= rawHash >> 33;
    rawHash *= 0xff51afd7ed558ccdL;
    rawHash ^= rawHash >> 33;
    rawHash *= 0xc4ceb9fe1a85ec53L;
    rawHash ^= rawHash >> 33;

    return rawHash & mask;
}

#define SIZE 1024

void test(long input[], int result[])
{
	for (int i = 0; i < SIZE; i++) {
		result[i] = getHashPosition(input[i], 1048575);
	}
}

int main(int argc, const char** argv) {
	long data[SIZE];
	int result[SIZE];

    /* initialize random seed: */
    srand (time(NULL));

    for (int i = 0; i < SIZE; i++) {
    	data[i] = lrand() % 288230376151711743;
    }

	int count = atoi(argv[1]);

	int row = rand() % SIZE;
	clock_t begin = clock();

    long long sum= 0;
	for (int j = 0; j < count; j++) {
		test(data, result);

		sum += result[row];
	}

	clock_t end = clock();

    cout << sum << endl;
	double elapsed_secs = double(end - begin) / CLOCKS_PER_SEC;
	cerr << (elapsed_secs / count) * 1000 * 1000 * 1000 << endl;
}
