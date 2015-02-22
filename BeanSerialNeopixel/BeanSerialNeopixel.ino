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

  for (int p = 0; p < 12; p++)
    strip.setPixelColor(p, strip.Color(30,100,30) );
  strip.show();

  if ( length > 0 )
  {
    /*for (int i = 0; i < length - 1; i += 2 )
    {
      // Check if button1 has been pressed or released...
      if ( buffer[i] == button1 )
      {
        // If the button is held down, buffer[i+1] will be 0
        // If it's released, buffer[i+1] is 1
        // Set pin 0 to 1 when the button is held down
        // and to 0 when released        
        Bean.setLed(255 * (1-buffer[i+1]), 0, 0);        
      }
      else if ( buffer[i] == button2 )
      {
        Bean.setLed(0, 255* (1-buffer[i+1]), 0); 
      }
      else if ( buffer[i] == button3 )
      {
        Bean.setLed(0, 0, 255 * (1-buffer[i+1]));
      }
      else if ( buffer[i] == slider29)
      {
        Bean.setLed(buffer[i+1], 0, 0);
        /*
        for(uint16_t h=0; i<strip.numPixels(); h++) {
          strip.setPixelColor(h, buffer[i+1], 0,0);
        }
        strip.show();        
       /
       
       //strip.setPixelColor(5, strip.Color(255,0,0) );
       //strip.show();
       
      }
    }*/
    
    byte controlbyte = buffer [0];
    byte data = buffer[1];
    
    
    switch (controlbyte){
      case button1:
        Bean.setLed(255 * (1-data), 0, 0); 
        break;
      case button2:
        Bean.setLed(0, 255 * (1-data), 0); 
        break;
    
    
    
    }
    
    
  }
  
  //Bean.sleep(500);
}


