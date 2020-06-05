#include "opencv-utils.h"
#include <opencv2/imgproc.hpp>
#include <vector>

void myFlip(Mat src){
    flip(src, src, 8);
}

void myBlur(Mat src, float sigma){
    GaussianBlur(src, src, Size(), sigma);
}

bool compareContourAreas ( std::vector<cv::Point> contour1, std::vector<cv::Point> contour2 ) {
    double i = fabs( contourArea(cv::Mat(contour1)) );
    double j = fabs( contourArea(cv::Mat(contour2)) );
    return ( i < j );
}

Mat BlackWhite(Mat src){
    Mat src_gray;
    int thresh = 100;
    RNG rng(12345);
    Mat canny_output;
    cvtColor(src, src_gray, COLOR_BGR2GRAY);
    blur(src_gray, src_gray, Size(3, 3));
    Canny(src_gray, canny_output, thresh, thresh * 2);
    std::vector<std::vector<Point2i> > contours;
    std::vector<std::vector<Point2i> > result;
    std::vector<std::vector<Point2i> > target;
    std::vector<Vec4i> hierarchy;
    findContours(canny_output, contours, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
    Mat drawing = Mat::zeros(canny_output.size(), CV_8UC3);

    int biggestContourIdx = -1;
    double biggestContourArea = 0;
    for (int i = 0; i < contours.size(); ++i)
    {
        auto area = contourArea(contours[i]);
        if (area > biggestContourArea)
        {
            biggestContourArea = area;
            biggestContourIdx = i;
        }
    }
    //first solution:
    std::vector<Point2f> approx;
    approxPolyDP(contours[biggestContourIdx], approx, 30, true);

    // Input Quadilateral or Image plane coordinates
    Point2f inputQuad[4];
    // Output Quadilateral or World plane coordinates
    Point2f outputQuad[4];

    // Lambda Matrix
    Mat lambda(2, 4, CV_32FC1);
    //Input and Output Image;
    Mat input, output;

    //Load the image
    input = src_gray;
    // Set the lambda matrix the same type and size as input

    lambda = Mat::zeros(input.rows, input.cols, input.type());

    // The 4 points that select quadilateral on the input , from top-left in clockwise order
    // These four pts are the sides of the rect box used as input
    //inputQuad[0] = approx[0];
    //inputQuad[1] = approx[1];
    //inputQuad[2] = approx[2];
    //inputQuad[3] = approx[3];
    // The 4 points where the mapping is to be done , from top-left in clockwise order
    outputQuad[0] = Point2f(0, 0);
    outputQuad[1] = Point2f(input.cols - 1, 0);
    outputQuad[2] = Point2f(input.cols - 1, input.rows - 1);
    outputQuad[3] = Point2f(0, input.rows - 1);


    Point2f *thearray = &approx[0];
    // Get the Perspective Transform Matrix i.e. lambda
    lambda = getPerspectiveTransform(thearray, outputQuad, 0);
    // Apply the Perspective Transform just found to the src image
    warpPerspective(input, output, lambda, output.size());
    return output;
}

