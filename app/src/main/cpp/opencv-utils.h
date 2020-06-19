#pragma once

#include <opencv2/core.hpp>

using namespace cv;

Mat BlackWhite(Mat src);
int solve(Mat src, int start_x, int start_y, int end_x, int end_y);
int solution_present();
Mat mazefordisplay();
int getSolution(int index);