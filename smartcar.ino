#include <Smartcar.h>

BrushedMotor leftMotor(smartcarlib::pins::v2::leftMotorPins);
BrushedMotor rightMotor(smartcarlib::pins::v2::rightMotorPins);

DifferentialControl control(leftMotor, rightMotor);

SimpleCar car(control);

void setup() {
    car.setSpeed(100);
    delay(1000);
    car.setSpeed(-50);
    delay(2000);
    car.setSpeed(0);
}

void loop() {

}