#include "opencv-utils.h"
#include <opencv2/imgproc.hpp>
#include <vector>
#include <cmath>
#include <queue>

Mat output;
int solution = 0;
Mat TempMat;
std::vector<int> finalPath;

float distance(int x1, int y1, int x2, int y2)
{
    // Calculating distance
    return sqrt(pow(x2 - x1, 2) +
                pow(y2 - y1, 2) * 1.0);
}

int getPathThroughMaze(const unsigned char *binaryImage,int w, int h, int startX, int startY, int endX, int endY)
{
    int return_value = 0;
    auto *visited = (unsigned char*)calloc(w * h, 1);
    auto *pathValue = (unsigned int*)calloc(w * h, sizeof(unsigned int));

    std::queue<std::pair<int, int>> currentPoints;
    currentPoints.push(std::pair<int, int>(startX, startY));
    pathValue[startX + startY * w] = 1;
    visited[startX + startY * w] = 1;

    //printf("Running Dijkstra's algorithm\n");

    bool endReached = false;
    unsigned int maxPathValue = 0;
    while (!currentPoints.empty())
    {
        std::pair<int, int> currPair = currentPoints.front();
        currentPoints.pop();

        int currX = currPair.first;
        int currY = currPair.second;

        unsigned int currPathValue = pathValue[currX + currY * w];
        if (currPathValue > maxPathValue)
            maxPathValue = currPathValue;

        if (currX == endX && currY == endY)
        {
            endReached = true;
            break;
        }

        for (int i = -1; i < 2; i++)
        {
            for (int j = -1; j < 2; j++)
            {
                if (currX + j < 0 || currY + i < 0 || currX + j >= w || currY + i >= h)
                    continue;
                if (binaryImage[(currX + j) + (currY + i) * w] && !visited[(currX + j) + (currY + i) * w])
                {
                    pathValue[(currX + j) + (currY + i) * w] = currPathValue + 1;
                    visited[(currX + j) + (currY + i) * w] = 1;
                    currentPoints.push(std::pair<int, int>(currX + j, currY + i));
                }
            }
        }
    }



    if (!endReached)
    {
        return_value = -1;
        //printf("Solution to the maze not found\n");
        return return_value;
    }

    //printf("Drawing path\n");
    unsigned int currPathValue = pathValue[endX + endY * w];
    int currX = endX;
    int currY = endY;
    int iteration = 0;
    for (;currPathValue > 1;)
    {
        if (iteration == 40) {
            iteration = 0;
        }
        if (iteration == 0) {
            finalPath.push_back(currX);
            finalPath.push_back(currY);
        }
        int bestMoveX = 0;
        int bestMoveY = 0;
        unsigned int bestMovePathVal = maxPathValue;
        for (int i = -1; i < 2; i++)
        {
            for (int j = -1; j < 2; j++)
            {
                if (i == 0 && j == 0)
                    continue;
                if (currX + j < 0 || currY + i < 0 || currX + j >= w || currY + i >= h)
                    continue;
                if (pathValue[(currX + j) + (currY + i) * w] == 0)
                    continue;
                if (pathValue[(currX + j) + (currY + i) * w] < bestMovePathVal)
                {
                    bestMovePathVal = pathValue[(currX + j) + (currY + i) * w];
                    bestMoveX = currX + j;
                    bestMoveY = currY + i;
                }
            }
        }

        currX = bestMoveX;
        currY = bestMoveY;

        currPathValue = pathValue[currX + currY * w];
        iteration++;
    }
    return 0;
}

int solve(Mat src, int start_x, int start_y, int end_x, int end_y) {
    int size;
    solution = 0;
    cvtColor(src, src, COLOR_BGR2GRAY);
    int w = src.cols;
    int h = src.rows;
    unsigned char *binaryImage;
    binaryImage = new unsigned char[w * h]();
    binaryImage = src.data;
    finalPath.clear();
    solution = getPathThroughMaze(binaryImage, w, h, start_x, start_y, end_x, end_y);
    size = finalPath.size();
    return size;
}

Mat BlackWhite(Mat src) {
    output.release();
    solution = 0;
    TempMat.release();
    finalPath.clear();
    Mat src_gray, final_mat;
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
    blur(src_gray, src_gray, Size(5, 5));
    final_mat = src_gray.clone();
    int rows = src_gray.rows;
    int cols = src_gray.cols;
    if (rows >= 200 && cols >= 200) {
        int threshold_val = 3;
        threshold_val = (rows >= cols ? rows : cols) / 25;
        if (threshold_val % 2 == 0) {
            threshold_val -= 1;
        }
        adaptiveThreshold(src_gray, final_mat, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY,
                          threshold_val, 3);
    }
    size_x = src_gray.rows;
    size_y = src_gray.cols;
    blur(src_gray, detected_edges, Size(3, 3));
    Canny(detected_edges, detected_edges, lowThreshold, lowThreshold * ratio, kernel_size);
    for (int i = 0; i < detected_edges.rows; i++) {
        uchar *pt = detected_edges.data + detected_edges.step[0] * i;
        for (int j = 0; j < detected_edges.cols; j++) {
            uchar *ptr = pt + detected_edges.step[1] * j;
            uchar blue = ptr[0];
            uchar green = ptr[1];
            uchar red = ptr[2];

            float pixelVal = blue + green + red;
            if (pixelVal > 250) {
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
    corner_points[2] = Point(y[BR_point_index], x[BR_point_index]);
    corner_points[3] = Point(y[BL_point_index], x[BL_point_index]);
    // Output Quadilateral or World plane coordinates
    Point2f outputQuad[4];
    // Lambda Matrix
    Mat lambda(2, 4, CV_32FC1);
    //Input and Output Image;
    Mat input;
    //Load the image
    input = final_mat;
    // Set the lambda matrix the same type and size as input

    lambda = Mat::zeros(input.rows, input.cols, input.type());
    // The 4 points where the mapping is to be done , from top-left in clockwise order
    outputQuad[0] = Point2f(0, 0);
    outputQuad[1] = Point2f(input.cols - 1, 0);
    outputQuad[2] = Point2f(input.cols - 1, input.rows - 1);
    outputQuad[3] = Point2f(0, input.rows - 1);

    // Get the Perspective Transform Matrix i.e. lambda
    lambda = getPerspectiveTransform(corner_points, outputQuad, 0);
    // Apply the Perspective Transform just found to the src image
    warpPerspective(input, output, lambda, output.size());

    int w = output.cols;
    int h = output.rows;
    unsigned char *outputImage, *binaryImage;
    binaryImage = new unsigned char[w * h]();
    binaryImage = output.data;
    outputImage = new unsigned char[w * h * 3]();
    for (int x = 0; x < w; x++) {
        for (int y = 0; y < h; y++) {
            if (binaryImage[x + y * w] ==0) {
                outputImage[3 * (x + y * w) + 0] = 65;
                outputImage[3 * (x + y * w) + 1] = 65;
                outputImage[3 * (x + y * w) + 2] = 65;
            }else{
                outputImage[3 * (x + y * w) + 0] = 255;
                outputImage[3 * (x + y * w) + 1] = 255;
                outputImage[3 * (x + y * w) + 2] = 255;
            }
        }
    }
    TempMat = Mat(h, w, CV_8UC3, outputImage);
    return output;
}

int solution_present(){
    return solution;
}

Mat mazefordisplay(){
    return TempMat;
}

int getSolution(int index){
    return(finalPath[index]);
}

