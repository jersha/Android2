#include "opencv-utils.h"
#include <opencv2/imgproc.hpp>

void myFlip(Mat src){
    flip(src, src, 8);
}

void myBlur(Mat src, float sigma){
    GaussianBlur(src, src, Size(), sigma);
}

void BlackWhite(Mat src){
    threshold(src,  src, 70, 255, THRESH_BINARY);
}

/*int FindTopBorder(Mat src){
    int input_height = src.rows;
    int input_width = src.cols;
    int top_border_array[] = {};
    int top_border = -1;
    int value = 0;

    for(int pixel_LR = 0; pixel_LR < input_width; pixel_LR++){
        for(int pixel_TB = 0; pixel_TB < input_height; pixel_TB++){
            value = src[pixel_TB][pixel_LR];
        }
    }

    if(value == 0):
    break
    top_border_array = np.hstack((top_border_array, np.array(pixel_TB)))
    top_border = np.bincount(top_border_array).argmax()

    if(top_border == -1 or bottom_border == -1 or left_border == -1 or right_border == -1):
    print('Error:Not able to find the border')
    exit()
    else:
    return top_border;
}*/
