int hits = 0; // impact count
int gAllTrig = 1000; // impact sensitivity

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
      
  Bean.setScratchData( 1, buffer, 1 );
}

void loop() {
  
  Bean.setLed(255,0,0);
  
  /* ================= WRITING ========================== */
   
  //send 13 characters max when calling serial.println();
  AccelerationReading accel = { 0, 0, 0 };
  accel = Bean.getAcceleration();
  uint16_t gAll = (abs(accel.xAxis)+abs(accel.yAxis)+abs(accel.zAxis));
  if (gAll > gAllTrig) {
    delay(50);
    Bean.setLed(0,0,255); // blue
    Serial.println("HELLOUNITY123");
   
    Bean.sleep(500);
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
      Bean.setLed(255,255,255); // white
    else if (textReceived == "B")
      Bean.setLed(0,0,255); // blue
    else if (textReceived == "HELLOGREEN1234567890")
      Bean.setLed(0,255,0); // green
    else if (textReceived == "TURNORANGE1234567890")
      Bean.setLed(255,102,0); // orange
    else
      Bean.setLed(255,0,255); // pink
    
   // clear scratch data
    Bean.setScratchData( 1, buffer, 1 );
    
    Bean.sleep(500);
  }

}
