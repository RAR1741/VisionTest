CC=g++
CFLAGS= -W -Wall -g -O2
LDFLAGS=
LDLIBS=

all: targeting

targeting: main.cpp
				$(CC) -std=c++1y $(CFLAGS) $(LDFLAGS) \
								-o targeting \
								main.cpp \
								-I /home/pi/opencv/include/ \
								-L /home/pi/opencv/build/lib \
								-lopencv_core \
								-lopencv_video \
								-lopencv_highgui \
								-DDEBUG

deploy:
	./deploy.sh 
