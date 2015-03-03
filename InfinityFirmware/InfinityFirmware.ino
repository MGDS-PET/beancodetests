#include <Adafruit_NeoPixel.h>

#define PIN 5

// Parameter 1 = number of pixels in strip
// Parameter 2 = pin number (most are valid)
// Parameter 3 = pixel type flags, add together as needed:
//   NEO_KHZ800  800 KHz bitstream (most NeoPixel products w/WS2812 LEDs)
//   NEO_KHZ400  400 KHz (classic 'v1' (not v2) FLORA pixels, WS2811 drivers)
//   NEO_GRB     Pixels are wired for GRB bitstream (most NeoPixel products)
//   NEO_RGB     Pixels are wired for RGB bitstream (v1 FLORA pixels, not v2)
Adafruit_NeoPixel strip = Adafruit_NeoPixel(13, PIN, NEO_GRB + NEO_KHZ800);

#define MAX_BEAN_SLEEP 0xFFFFFFFF

#define NEOPIXELRINGLENGTH 12

// The control inputs we will use from LightBlue iOS app
#define button1  13
#define button2  14
#define button3  15
#define button4  16
#define button5  17
#define slider29 29

// the number of the pushbutton pin
#define buttonPin 4
#define buzzpin 0

int buttonState = HIGH;  // current reading from the button
int activityLevel = 6;

int previous = HIGH;    // the previous reading from the input pin
int toggleState = HIGH;
int partyOn = LOW; // are we currently partying? 

void setup() 
{
  Serial.begin(57600);
  Serial.setTimeout(5);
  
  strip.begin();
  strip.show(); // Initialize all pixels to 'off'

  pinMode(buzzpin, OUTPUT); // 
  
  pinMode(buttonPin, INPUT_PULLUP);
  Bean.attachChangeInterrupt(buttonPin, togglelights);
}

void loop() {
  // Check for serial messages from LightBlue
  char buffer[10]; 
  size_t length = 64; 

  length = Serial.readBytes(buffer, 2);    
  strip.show();

  if ( length > 0 )
  {
    byte controlbyte = buffer [0];
    byte data = buffer[1];
    int r,g,b,activity;   
 
    switch (controlbyte)
    {
      case button1:
        r = 1 - data;
        Bean.setLed(255 * (1-data), 0, 0); 
        break;
      case button2:
        g = 1 - data;
        Bean.setLed(0, 255 * (1-data), 0); 
        break;
      case button3:
        b = 1 - data;
        Bean.setLed(0, 0, 255 * (1-data)); 
        break;
      case slider29:
        activity = data;
        break;
      case button4: // buzz motor 
        digitalWrite(0, !data);    
        break;
      case button5: // party! -- used for treasure unlock
        party();
        break;
    }
    
    int alevel = activity / 22; // divide 255 to get a number from 0-11 to go around the ring 

    for (int p = 0; p < alevel; p++)
        strip.setPixelColor(p, strip.Color(activity,0,0) );
    strip.show();
  }

  Bean.sleep(1000);
}

void party()
{
  
    partyOn = HIGH; 
    Bean.setLed(100,100,100);
    int buzzState = LOW;
    
    for(int i = 0; i < 12; i++)
    {
        strip.setPixelColor(i,  strip.Color(100,0,0) );
        strip.setPixelColor(12, strip.Color(100*buzzState,0,0) );
        digitalWrite(0, buzzState);
        buzzState = !buzzState;
        Bean.sleep(100);
    }
    
    digitalWrite(0, LOW);
    Bean.setLed(0,0,0);

    partyOn = LOW; 
}

void togglelights()
{
  buttonState = digitalRead(buttonPin);
  Serial.println(time);
  Serial.println(toggleState);

  if (buttonState == LOW && partOn == LOW) // fixed issue with bean.sleep and millis() 
  {
    if (toggleState == HIGH)
    {
      digitalWrite(buzzpin, HIGH);  

      for (int i = 0 ; i < activityLevel; i++)
      {
        strip.setPixelColor(i, strip.Color(0,127,0));
      }  
      for (int i = activityLevel ; i < strip.numPixels(); i++)
      {
        strip.setPixelColor(i, strip.Color(0,0,0));  
      }
      
      //toggleState = LOW; 
      //Serial.println("yyy");
    }
    else
    {
      digitalWrite(buzzpin, LOW); 
    
      for (int i = 0 ; i < strip.numPixels(); i++)
      {
         strip.setPixelColor(i, strip.Color(0,0,0));
       }
    
      //toggleState = HIGH; 
      //Serial.println("XXXXXX");
    }
    
    toggleState = !toggleState;
    strip.show(); 
  }
}
