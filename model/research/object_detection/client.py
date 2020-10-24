# import libraries
from vidgear.gears import VideoGear
from vidgear.gears import NetGear

import cv2

stream = cv2.VideoCapture(0)

# stream = VideoGear(source='AmongUs.mp4').start() #Open any video stream
server = NetGear(request_timeout=50000) #Define netgear server with default settings

# infinite loop until [Ctrl+C] is pressed
while True:
    try: 
      (grabbed, frame) = stream.read()
        # read frames

        # check if frame is None
      if grabbed is None:
        break

        # do something with frame here

        # send frame to server
      server.send(frame)

    except KeyboardInterrupt:
        #break the infinite loop
        break

# safely close video stream
stream.release()

# safely close server
server.close()

# # safely close video stream
# stream.stop()
# # safely close server
# writer.close()