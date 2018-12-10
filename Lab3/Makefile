# Project: GLSLprimer
# Makefile created by stegu 2013-11-22

# Makefile for Windows mingw32, Linux and MacOSX (gcc environments)

CC   = gcc
OBJ  = GLSLprimer.o pollRotator.o tgaloader.o tnm084.o triangleSoup.o
INC  = -I. -IC:/Dev-Cpp/include -I/usr/X11/include -I/usr/include
OPT = -Wall -O3 -ffast-math -g3

Usage:
	@echo "Usage: make Win32 | Linux | MacOSX | clean | distclean"

GLSLprimer.o: GLSLprimer.c
	$(CC) $(OPT) $(INC) -c GLSLprimer.c -o GLSLprimer.o

pollRotator.o: pollRotator.c
	$(CC) $(OPT) $(INC) -c pollRotator.c -o pollRotator.o

tgaloader.o: tgaloader.c
	$(CC) $(OPT) $(INC) -c tgaloader.c -o tgaloader.o

tnm084.o: tnm084.c
	$(CC) $(OPT) $(INC) -c  tnm084.c -o tnm084.o

triangleSoup.o: triangleSoup.c
	$(CC) $(OPT) $(INC) -c  triangleSoup.c -o triangleSoup.o

Win32: $(OBJ)
	$(CC) $(OBJ) -o GLSLprimer.exe -L. -LC:/Dev-Cpp/lib -mwindows -lglfw3 -lopengl32 -mconsole -g3

Linux: $(OBJ)
	$(CC) $(OBJ) -lglfw3 -o GLSLprimer

MacOSX: $(OBJ)
	bash bundle.sh GLSLprimer
	$(CC) -L. $(OBJ) -o GLSLprimer.app/Contents/MacOS/GLSLprimer -lglfw3_macosx -framework Cocoa -framework OpenGL -framework IOKit -framework CoreVideo

clean:
	del $(OBJ)

distclean:
	del $(OBJ) GLSLprimer GLSLprimer.exe GLSLprimer.app
