#include <Smartcar.h>
#include <BluetoothSerial.h>
#include <VL53L0X.h>
#include <Wire.h>

int input;
int currentSpeed; // The speed the car is set to move at, used for manual drive only
const int TRIGGER_PIN = 5; //D5 red cable
const int ECHO_PIN = 18; //D18 green cable
const unsigned int MAX_DISTANCE = 100;
SR04 sensorB(TRIGGER_PIN, ECHO_PIN, MAX_DISTANCE);
const int GYROSCOPE_OFFSET = 37;
const unsigned long PRINT_INTERVAL = 100;
unsigned long previousPrintout     = 0;
const auto pulsesPerMeter = 600;
const int fSpeed    = 50;  // 50% of the full speed forward
const int bSpeed    = -50; // 50% of the full speed backward
const int tankSpeed = 55;  // Speed at which the wheels turn on the spot
const int flDegrees = -28; // degrees to turn forward left
const int frDegrees = 28;  // degrees to turn forward right
const int blDegrees = -152; // degrees to turn backward left
const int brDegrees = 152; // degrees to turn backward right
unsigned long previousToggle = 0;

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
    currentSpeed = 0;
}

void loop()
{
    handleInput();
    Serial.println(input);

    Serial.println(sensor.readRangeContinuousMillimeters());
    Serial.println(sensorB.getDistance());
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
    while (bluetooth.available()){ input = bluetooth.read(); }        
    
    int back = sensorB.getDistance(); 
    int front = sensor.readRangeContinuousMillimeters();

    while((front != 0 && front < 200) || (back != 0 && back < 20)) { 

        while (bluetooth.available()){ input = bluetooth.read(); }

        // Object infront
        if((input == 1 || input == 9) && front != 0 && front < 200){
            rotateOnSpot(-180, 80);
        }
        // Object behind
        else if(input == 5 && back != 0 && back < 20){
            rotateOnSpot(-180, 80);
        }
        // Object behind, can move forward
        else if(input == 1 && front != 0 && front > 200){
            car.setSpeed(fSpeed);
            car.setAngle(0);
        }
        else {
            car.setSpeed(0);
            car.setAngle(0);
        }
        front = sensor.readRangeContinuousMillimeters();
        back = sensorB.getDistance(); 
    }
    
    // Converts inputs which were neagtive back to negative
    if (input > 127){

        input = (256 - input);
        input = (input * -1);   
    }

    switch (input) {
        //  manual  |   mindcontrol plus phone gyroscope
        //  8 1 2   |   9
        //  7   3   |   10
        //  6 5 4   |   -110 to -11 and 11 to 110

        case 1: // Move straight forward
            car.setAngle(0);
            car.setSpeed(fSpeed);
            break;
        
        case 2: // Turn right forward
            car.setAngle(frDegrees);
            car.setSpeed(fSpeed);
            break;

        case 3: // turn clockwise 
            car.setAngle(0);
            car.setSpeed(0);
            break;

        case 4: // Turn right backward
            car.setAngle(brDegrees);
            car.setSpeed(bSpeed);
            break;

        case 5: // Move straight backward
            car.setAngle(0);
            car.setSpeed(bSpeed);
            break;

        case 6: // Turn left backward
            car.setAngle(blDegrees);
            car.setSpeed(bSpeed);
            break;

        case 7: // turn counterclockwise
            car.setAngle(0);
            car.setSpeed(0);
            break;

        case 8: // turn left forward
            car.setAngle(flDegrees);
            car.setSpeed(fSpeed);
            break;

        case 9: // Mindcontrol: move forwards
            car.setSpeed(fSpeed);
            break;

        case 10: // Mindcontrol: stop
            car.setSpeed(0);
            break;

        default: // Inputs are angles for mindcontrol steering
            car.setAngle(-input);
    }
}

// Motors can't switch direction without delay
void changeSpeed(int targetSpeed)
{
    if(currentSpeed == 0){
        car.setSpeed(targetSpeed);
        currentSpeed = targetSpeed;
    } 
    else if (currentSpeed < targetSpeed){ // backward to forward
        car.setSpeed(0);
        delay(10);
        car.setSpeed(targetSpeed);
        currentSpeed = targetSpeed;
    }
    else if (currentSpeed > targetSpeed){ // forward to backward
        car.setSpeed(0);
        delay(10);
        car.setSpeed(targetSpeed);
        currentSpeed = targetSpeed;
    }
}
