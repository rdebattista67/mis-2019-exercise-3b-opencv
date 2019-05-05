# Briefly describe the issues you faced in 3a and how you resolved them.
Some issues I faced were matching the SDK versions in the android manifest file, and having imported too much of the OpenCV module at first. this required me to clear all prior data concerning the OpenCV SDK and reinstall OpenCV in order for it to work correctly.

# Briefly describe how your app determines the correct size for the red circle.
A circle with a radius 10% of the height of the face results in a nice effect, so we calculate 10% of the face's height.
