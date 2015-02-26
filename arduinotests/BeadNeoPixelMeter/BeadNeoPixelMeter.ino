#include <Adafruit_NeoPixel.h>

#define PIN 5

// Parameter 1 = number of pixels in strip
// Parameter 2 = Arduino pin number (most are valid)
// Parameter 3 = pixel type flags, add together as needed:
//   NEO_KHZ800  800 KHz bitstream (most NeoPixel products w/WS2812 LEDs)
//   NEO_KHZ400  400 KHz (classic 'v1' (not v2) FLORA pixels, WS2811 drivers)
//   NEO_GRB     Pixels are wired for GRB bitstream (most NeoPixel products)
//   NEO_RGB     Pixels are wired for RGB bitstream (v1 FLORA pixels, not v2)
Adafruit_NeoPixel strip = Adafruit_NeoPixel(12, PIN, NEO_GRB + NEO_KHZ800);

// IMPORTANT: To reduce NeoPixel burnout risk, add 1000 uF capacitor across
// pixel power leads, add 300 - 500 Ohm resistor on first pixel's data input
// and minimize distance between Arduino and first pixel.  Avoid connecting
// on a live circuit...if you must, connect GND first.

int mypin = 0;

void setup() {
  strip.begin();
  //strip.adjustBrightness(40);
  strip.show(); // Initialize all pixels to 'off'
  pinMode(0, OUTPUT);
}

void loop() {
  
  
  if ( mypin == 0 )
  {
    for (int i = 0; i < 12; i++)
      strip.setPixelColor(i, strip.Color(0,0,0) );
    strip.show();
    digitalWrite(0, HIGH);
    delay(1000);

  }
  else
  {
  digitalWrite(0,LOW);
  // Some example procedures showing how to display to the pixels:
  for (int i = 0; i < 12; i++)
  {
    if ( i < mypin)
    {
      strip.setPixelColor(i, strip.Color(30,100,30) );
    }
    else
    {
      strip.setPixelColor(i, strip.Color(0,0,0) );
    }
  }
  
  
  
    strip.show();
  }
  mypin = (mypin + 1) % 12;
  delay(200);

}

