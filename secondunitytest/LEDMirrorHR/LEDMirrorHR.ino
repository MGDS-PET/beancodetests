int hits = 0; // impact count
int gAllTrig = 1000; // impact sensitivity
int cycleCount = 0;
boolean runningDelay;
int ledColor[3];


//  PULSE SENSOR VARIABLES
int pulsePin = A0;                 // Pulse Sensor purple wire connected to analog pin 0
int buzzpin = 4;                 // Pulse Sensor purple wire connected to analog pin 0
int blinkPin = 5;                // pin to blink led at each beat
int fadePin = 3;                  // pin to do fancy classy fading blink at each beat
int fadeRate = 0;                 // used to fade LED on with PWM on fadePin

// these variables are volatile because they are used during the interrupt service routine!
volatile int BPM;                   // used to hold the pulse rate
volatile int Signal;                // holds the incoming raw data
volatile int IBI = 600;             // holds the time between beats, must be seeded! 
volatile boolean Pulse = false;     // true when pulse wave is high, false when it's low
volatile boolean QS = false;        // becomes true when Arduoino finds a beat.

int pulseCount = 0;

void ledFadeToBeat(){
    fadeRate -= 15;                         //  set LED fade value
    fadeRate = constrain(fadeRate,0,255);   //  keep LED fade value from going into negative numbers!
    analogWrite(fadePin,fadeRate);          //  fade LED
  }
  
 void sendUnityPulse(){
   Serial.println("DEB3:" + String(BPM));   // send heart rate with a 'BPM:' prefix                       //  set LED fade value
    fadeRate = constrain(fadeRate,0,255);   //  keep LED fade value from going into negative numbers!
    analogWrite(fadePin,fadeRate);          //  fade LED
  }
  
uint8_t buffer[1] = {' '};
  
ScratchData lastScratch;

bool compareScratch( ScratchData * scratch1, ScratchData * scratch2 )
{
  bool matched = true;
  
  if ( scratch1->length != scratch2->length )
  {
    matched = false;
  }
  else
  {
    int length = min( scratch1->length, scratch2->length );
    int i = 0;
    
    while ( i < length )
    {
      if ( scratch1->data[i] != scratch2->data[i] )
      {
        matched = false;
        i = length;
      }
      i++;
    }
  }
  
    return matched;
}

void setup() {
  Serial.begin(57600); // initialize serial
  cycleCount = 0;
  Bean.setScratchData( 1, buffer, 1 );
  runningDelay = false;
  
  //HEART RATE
  pinMode(10, OUTPUT);
  pinMode(11, OUTPUT);
  pinMode(12, OUTPUT);

  pinMode(buzzpin,OUTPUT);          // pin that will blink to your heartbeat!  
  pinMode(blinkPin,OUTPUT);         // pin that will blink to your heartbeat!
  pinMode(fadePin,OUTPUT);          // pin that will fade to your heartbeat!
  //Serial.begin(115200);           // we agree to talk fast!
  interruptSetup();                 // sets up to read Pulse Sensor signal every 2mS 
   // UN-COMMENT THE NEXT LINE IF YOU ARE POWERING The Pulse Sensor AT LOW VOLTAGE, 
   // AND APPLY THAT VOLTAGE TO THE A-REF PIN
   //analogReference(EXTERNAL);

}

void loop() {
  
  /* ================= HR to UNITY ========================== */
  
  //IBI - time between beats; Signal = raw data... use these variables if want to send to unity
  if (QS == true){                       // Quantified Self flag is true when arduino finds a heartbeat
        fadeRate = 255;                  // Set 'fadeRate' Variable to 255 to fade LED with pulse
        Serial.println("DEB2:" + String(BPM));   // send heart rate with a 'BPM:' prefix
        Serial.println("DEB0:" + String(IBI));
        pulseCount++;
        Serial.println("DEB3:" + String(pulseCount));
        //sendDataToProcessing('Q',IBI);   // send time between beats with a 'Q' prefix
        QS = false;                      // reset the Quantified Self flag for next time    
        
     }
  int a=analogRead(0);
  ledFadeToBeat();
  
  /* ================= DELAY SETUP ========================== */
  
  if (runningDelay == false)
    Bean.setLed(255, 0, 0);
  else
    Bean.setLed(ledColor[0], ledColor[1], ledColor[2]);
  
  /* ================= WRITING ========================== */
   
  //send 13 characters max when calling serial.println();
  AccelerationReading accel = { 0, 0, 0 };
  accel = Bean.getAcceleration();
  uint16_t gAll = (abs(accel.xAxis)+abs(accel.yAxis)+abs(accel.zAxis));
  if (gAll > gAllTrig) {
    ledColor[0] = 0;
    ledColor[1] = 0;
    ledColor[2] = 255; // blue
    runningDelay = true;
    cycleCount = 0;
    Serial.println("HELLOUNITY123");
  }
  
  /* ================= READING ========================== */
  
  ScratchData thisScratch = Bean.readScratchData(1);
  
  if (thisScratch.length > 1 || thisScratch.data[0] != buffer[0])
  {
    
  //bool matched = compareScratch( &thisScratch, &lastScratch );
  
  //if (matched == false)
  //{
  //  lastScratch = thisScratch;
    String textReceived = "";
    for (int i = 0; i < thisScratch.length; i++) //scratchdata contains 20 bytes // keep appending until hit null char
      textReceived = textReceived + char(thisScratch.data[i]);

    Serial.println(textReceived);
    
    if (textReceived == "W")
    {
      ledColor[0] = 255;
      ledColor[1] = 255;
      ledColor[2] = 255;// white
      runningDelay = true;
      cycleCount = 0;
    }
    else if (textReceived == "B")
    {
      ledColor[0] = 0;
      ledColor[1] = 0;
      ledColor[2] = 255; // blue
      runningDelay = true;
      cycleCount = 0;
    }
    else if (textReceived == "HELLOGREEN1234567890")
    {
      ledColor[0] = 0;
      ledColor[1] = 255;
      ledColor[2] = 0; // green
      runningDelay = true;
      cycleCount = 0;
    }
    else if (textReceived == "TURNORANGE1234567890")
    {
      ledColor[0] = 255;
      ledColor[1] = 102;
      ledColor[2] = 0; // orange
      runningDelay = true;
      cycleCount = 0;
    }
    else
    {
      ledColor[0] = 255;
      ledColor[1] = 0;
      ledColor[2] = 255; // pink
      runningDelay = true;
      cycleCount = 0;
    }
    
   // clear scratch data
    Bean.setScratchData( 1, buffer, 1 );
  
  }
  
  if (runningDelay == true)
    {
      if (cycleCount < 5)
      {
        cycleCount = cycleCount+1;
      }
      else
      {
        runningDelay = false;
        cycleCount = 0;
      }
    }
 
  /* ================= SEND Analog 1 reading ========================== */
  
  //Serial.println("DEB0:" + String(analogRead(A1)));
  Serial.println("DEB1:" + String(cycleCount));
  
}
