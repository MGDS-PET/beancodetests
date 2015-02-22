/*  
  This sketch controls a string of holiday lights using
  the LigthBlue Bean and the iOS app LightBlue.

  This code is in the public domain.
 */

// The control inputs we will use from LightBlue
#define button1  13
#define button2  14
#define button3  15
#define slider29 29

void setup() 
{
  Serial.begin();
  Serial.setTimeout(5);
 }

void loop() {
  // Check for serial messages from LightBlue
  char buffer[64];
  size_t length = 64; 

  length = Serial.readBytes(buffer, length);    

  if ( length > 0 )
  {
    for (int i = 0; i < length - 1; i += 2 )
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
      }
    }
  }
}
