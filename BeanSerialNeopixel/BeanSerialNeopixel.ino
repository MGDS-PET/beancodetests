#include <Adafruit_NeoPixel.h>

#define PIN 5

// Parameter 1 = number of pixels in strip
// Parameter 2 = pin number (most are valid)
// Parameter 3 = pixel type flags, add together as needed:
//   NEO_KHZ800  800 KHz bitstream (most NeoPixel products w/WS2812 LEDs)
//   NEO_KHZ400  400 KHz (classic 'v1' (not v2) FLORA pixels, WS2811 drivers)
//   NEO_GRB     Pixels are wired for GRB bitstream (most NeoPixel products)
//   NEO_RGB     Pixels are wired for RGB bitstream (v1 FLORA pixels, not v2)
Adafruit_NeoPixel strip = Adafruit_NeoPixel(12, PIN, NEO_GRB + NEO_KHZ800);


// The control inputs we will use from LightBlue
#define button1  13
#define button2  14
#define button3  15
#define slider29 29

void setup() 
{
  Serial.begin(57600);
  Serial.setTimeout(5);
  
  strip.begin();
  strip.show(); // Initialize all pixels to 'off'

}

void loop() {
  // Check for serial messages from LightBlue
  char buffer[10];
  size_t length = 64; 

  length = Serial.readBytes(buffer, 2);    


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
        Bean.setLed(0, 255 * (1-data), 0); 
        break;
    }

    for (int p = 0; p < 12; p++)
        strip.setPixelColor(p, strip.Color(r,g,b) );
    strip.show();
  }

  //Bean.sleep(500);
}
