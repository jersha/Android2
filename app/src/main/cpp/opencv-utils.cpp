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

float distance(int x1, int y1, int x2, int y2)
{
    // Calculating distance
    return sqrt(pow(x2 - x1, 2) +
                pow(y2 - y1, 2) * 1.0);
}

Mat BlackWhite(Mat src){
    Mat src_gray;
    Mat dst, detected_edges;
    int lowThreshold = 50;
    const int ratio = 3;
    const int kernel_size = 3;
    int size_x = 0;
    int size_y = 0;
    std::vector<int> x;
    std::vector<int> y;
    std::vector<int> TL;
    std::vector<int> TR;
    std::vector<int> BL;
    std::vector<int> BR;
    Point2f corner_points[4];
    dst.create(src.size(), src.type());
    cvtColor(src, src_gray, COLOR_BGR2GRAY);
    size_x = src_gray.rows;
    size_y = src_gray.cols;
    blur(src_gray, detected_edges, Size(3, 3));
    Canny(detected_edges, detected_edges, lowThreshold, lowThreshold*ratio, kernel_size);
    threshold(detected_edges, detected_edges, 0, 255, THRESH_BINARY);
    for (int i = 0; i < detected_edges.rows; i++)
    {
        uchar* pt = detected_edges.data + detected_edges.step[0] * i;
        for (int j = 0; j < detected_edges.cols; j++)
        {
            uchar* ptr = pt + detected_edges.step[1] * j;
            uchar blue = ptr[0];
            uchar green = ptr[1];
            uchar red = ptr[2];

            float pixelVal = blue + green + red;
            if (pixelVal > 250)
            {
                x.push_back(i);
                y.push_back(j);
                TL.push_back(distance(0, 0, i, j));
                TR.push_back(distance(0, size_y, i, j));
                BL.push_back(distance(size_x, 0, i, j));
                BR.push_back(distance(size_x, size_y, i, j));
            }
        }
    }
    int TL_point_index = min_element(TL.begin(), TL.end()) - TL.begin();
    int TR_point_index = min_element(TR.begin(), TR.end()) - TR.begin();
    int BL_point_index = min_element(BL.begin(), BL.end()) - BL.begin();
    int BR_point_index = min_element(BR.begin(), BR.end()) - BR.begin();
    corner_points[0] = Point(y[TL_point_index], x[TL_point_index]);
    corner_points[1] = Point(y[TR_point_index], x[TR_point_index]);
    corner_points[3] = Point(y[BL_point_index], x[BL_point_index]);
    corner_points[2] = Point(y[BR_point_index], x[BR_point_index]);

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
    outputQuad[0] = Point2f(0, 0);
    outputQuad[1] = Point2f(input.cols - 1, 0);
    outputQuad[2] = Point2f(input.cols - 1, input.rows - 1);
    outputQuad[3] = Point2f(0, input.rows - 1);

    // Get the Perspective Transform Matrix i.e. lambda
    lambda = getPerspectiveTransform(corner_points, outputQuad, 0);
    // Apply the Perspective Transform just found to the src image
    warpPerspective(input, output, lambda, output.size());
    return output;
}

