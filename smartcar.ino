#include <VL53L0X.h>
#include <Smartcar.h>
#include <Wire.h>
VL53L0X sensor;

BrushedMotor leftMotor(smartcarlib::pins::v2::leftMotorPins);
BrushedMotor rightMotor(smartcarlib::pins::v2::rightMotorPins);
DifferentialControl control(leftMotor, rightMotor);
SimpleCar car(control);

void setup(){
  Serial.begin(9600);
  Wire.begin();
  car.setSpeed(50);
  sensor.setTimeout(500);
  if(!sensor.init()){
    Serial.println("Failed to detect and initialize sensor!");
    while(1){}
  }
  sensor.startContinuous();
}
void loop(){
  Serial.println(sensor.readRangeContinuousMillimeters());
  if(sensor.timeoutOccurred()) {
  Serial.print("TIMEOUT");
  }

  if(sensor.readRangeContinuousMillimeters() < 200) {
    car.setSpeed(10);
    car.setAngle(45);
  } else {
    car.setSpeed(50);
  }
}