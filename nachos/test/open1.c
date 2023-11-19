
#include "syscall.h"
int main() {
    // Create a temporary file
    const char *filename = "testfile.txt";
    int fd = creat(filename);
    if (fd == -1) {
        printf("Error creating file");
        return 1;
    }
    printf(fd);
    //close(fd);

    // int fd1 = open(filename);
    
    // if (fd1 == -1) {
    //     printf("Error opening file");
    //     return 1;
    // }
    // printf(fd1);
    // Write some content to the file
    const char *content = "This is a test file.\n";
    if (write(fd, content, sizeof(content) - 1) == -1) {
        printf("Error writing to file");
        close(fd);
        return 1;
    }

    // Close the file
    //return close(fd);

    // // Call the unlink function to remove the file
    // if (unlink(filename) == -1) {
    //     printf("Error unlinking file");
    //     return 1;
    // }

    // printf("File unlinked successfully.\n");

    return 0;
}