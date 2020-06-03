#include "opencv-utils.h"
#include <opencv2/imgproc.hpp>

void myFlip(Mat src){
    flip(src, src, 8);
}

void myBlur(Mat src, float sigma){
    GaussianBlur(src, src, Size(), sigma);
}

void BlackWhite(Mat src){
    cvtColor(src, src, COLOR_RGB2GRAY);
}