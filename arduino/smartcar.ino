#include <Smartcar.h>
#include <BluetoothSerial.h>
#include <VL53L0X.h>
#include <Wire.h>

void handleInput();
char input; 
const int TRIGGER_PIN = 5; //D5 red cable
const int ECHO_PIN = 18; //D18 green cable
const unsigned int MAX_DISTANCE = 100;
SR04 front(TRIGGER_PIN, ECHO_PIN, MAX_DISTANCE);
const int GYROSCOPE_OFFSET = 37;
const unsigned long PRINT_INTERVAL = 100;
unsigned long previousPrintout     = 0;
const auto pulsesPerMeter = 600;
const int fSpeed   = 50;  // 50% of the full speed forward
const int bSpeed   = -50; // 50% of the full speed backward
const int lDegrees = -55; // degrees to turn left
const int rDegrees = 55;  // degrees to turn right
BluetoothSerial bluetooth;
BrushedMotor leftMotor(smartcarlib::pins::v2::leftMotorPins);
BrushedMotor rightMotor(smartcarlib::pins::v2::rightMotorPins);
DifferentialControl control(leftMotor, rightMotor);
VL53L0X sensor;
GY50 gyroscope(GYROSCOPE_OFFSET);
DirectionlessOdometer leftOdometer(
    smartcarlib::pins::v2::leftOdometerPin, []() { leftOdometer.update(); }, pulsesPerMeter);
DirectionlessOdometer rightOdometer(
    smartcarlib::pins::v2::rightOdometerPin, []() { rightOdometer.update(); }, pulsesPerMeter);
SmartCar car(control, gyroscope, leftOdometer, rightOdometer);
void setup()
{
    Serial.begin(9600);
    bluetooth.begin("Car"); // Device name
    Wire.begin();
     sensor.setTimeout(500);
  if (!sensor.init())
  {
    Serial.println("Failed to detect and initialize sensor!");
    while (1) {}
  }
  sensor.startContinuous();
}

void loop()
{
    handleInput();
     Serial.println(sensor.readRangeContinuousMillimeters());
     Serial.println(front.getDistance());
  if (sensor.timeoutOccurred()) { Serial.print(" TIMEOUT"); }
  Serial.println();
 
}


void rotateOnSpot(int targetDegrees, int speed) // taken from smartcar library
{
    speed = smartcarlib::utils::getAbsolute(speed);
    targetDegrees %= 360; // put it on a (-360,360) scale
    if (!targetDegrees)
        return; // if the target degrees is 0, don't bother doing anything
    /* Let's set opposite speed on each side of the car, so it rotates on spot */
    if (targetDegrees > 0)
    { // positive value means we should rotate clockwise
        car.overrideMotorSpeed(speed,
                               -speed); // left motors spin forward, right motors spin backward
    }
    else
    { // rotate counter clockwise
        car.overrideMotorSpeed(-speed,
                               speed); // left motors spin backward, right motors spin forward
    }
    const auto initialHeading = car.getHeading(); // the initial heading we'll use as offset to
                                                  // calculate the absolute displacement
    int degreesTurnedSoFar
        = 0; // this variable will hold the absolute displacement from the beginning of the rotation
    while (abs(degreesTurnedSoFar) < abs(targetDegrees))
    { // while absolute displacement hasn't reached the (absolute) target, keep turning
        car.update(); // update to integrate the latest heading sensor readings
        auto currentHeading = car.getHeading(); // in the scale of 0 to 360
        if ((targetDegrees < 0) && (currentHeading > initialHeading))
        { // if we are turning left and the current heading is larger than the
            // initial one (e.g. started at 10 degrees and now we are at 350), we need to substract
            // 360, so to eventually get a signed
            currentHeading -= 360; // displacement from the initial heading (-20)
        }
        else if ((targetDegrees > 0) && (currentHeading < initialHeading))
        { // if we are turning right and the heading is smaller than the
            // initial one (e.g. started at 350 degrees and now we are at 20), so to get a signed
            // displacement (+30)
            currentHeading += 360;
        }
        degreesTurnedSoFar
            = initialHeading - currentHeading; // degrees turned so far is initial heading minus
                                               // current (initial heading
        // is at least 0 and at most 360. To handle the "edge" cases we substracted or added 360 to
        // currentHeading)
    }
    car.setSpeed(0); // we have reached the target, so stop the car
}





void handleInput()
{ 
        while (bluetooth.available()){input = bluetooth.read();
        
        }        
                 int back = front.getDistance(); 
                 int distance = sensor.readRangeContinuousMillimeters();
  while(distance !=0 && distance<200 || back !=0 && back <20)
  { 
    while (bluetooth.available()){input = bluetooth.read();}
    if(input=='x' && distance !=0 && distance<200){
      rotateOnSpot(-180, 80);
      
    }
    
    if(input=='y'&& back !=0 && back <20){
      rotateOnSpot(-180, 80);
    }

    if(input=='f'&& distance> 200){
      car.setSpeed(fSpeed);
      car.setAngle(0);
    }
    else {
      car.setSpeed(0);
      car.setAngle(0);
    }
    distance = sensor.readRangeContinuousMillimeters();
    back = front.getDistance(); 
  }
  
                       
        switch (input){
        
        case 'l': // rotate counter-clockwise going forward
            car.setSpeed(fSpeed);
            car.setAngle(lDegrees);
            break;
        case 'r': // turn clock-wise
            car.setSpeed(fSpeed);
            car.setAngle(rDegrees);
            break;
        case 'f': // go ahead
            car.setSpeed(fSpeed);
            car.setAngle(0);
            break;
        case 'b': // go back
            car.setSpeed(bSpeed);
            car.setAngle(0);
            break;
        default: // if you receive something that you don't know, just stop
            car.setSpeed(0);
            car.setAngle(0);
        }
  
}
