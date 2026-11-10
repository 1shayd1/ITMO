#include <stdio.h>
#include <stdint.h>

int scan_int() {
	int n;
	scanf("%d", n);
	return n;
}

void scan_int_into(int* dst) {
	scanf("%d", dst);
}



int main() {
	int a = 8;

	scan_int_into(&a);

	printf("%d", a)
	return 0;
}
