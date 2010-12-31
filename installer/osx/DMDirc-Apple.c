/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * JNI library for OS X url handling.
 * Compile with:
 *     gcc -dynamiclib -framework JavaVM -framework Carbon -o libDMDirc-Apple.jnilib DMDirc-Apple.c -arch x86_64
 */

#include <Carbon/Carbon.h>
#include <JavaVM/jni.h>

/** The method to callback */
static jmethodID callbackMethod;

/** The JVM our callback is in */
static JavaVM *jvm;

/** The global reference to the Apple object that wants the callback */
static jobject apple;

/** Callback from OS X with URL. */
static OSErr openURLCallback(const AppleEvent *theAppleEvent, AppleEvent* reply, long handlerRefcon);

/**
 * JNI Method to register interest in callback.
 * Obtained from:
 *     javah -classpath plugins/ui_swing.jar com.dmdirc.addons.ui_swing.Apple
 * Reference:
 *     http://developer.apple.com/documentation/Carbon/Reference/Apple_Event_Manager/Reference/reference.html#//apple_ref/c/func/AEInstallEventHandler
 *
 * @param env The JNIEnvironment for this callback.
 * @param this The object that is registering the callback
 */
JNIEXPORT jint JNICALL Java_com_dmdirc_addons_ui_1swing_Apple_registerOpenURLCallback (JNIEnv *env, jobject object) {
	// Find the callback in the object
	callbackMethod = (*env)->GetMethodID(env, (*env)->GetObjectClass(env, object), "handleOpenURL", "(Ljava/lang/String;)V");
	
	// Check if the callback exists.
	if (callbackMethod != 0) {
		// Store the JVM for this callback, and a global reference to the Apple object
		(*env)->GetJavaVM(env, &jvm);
		apple = (*env)->NewGlobalRef(env, object);
	
		// Now register the callback to ourself.
		return (jint)AEInstallEventHandler(kInternetEventClass, kAEGetURL, NewAEEventHandlerUPP((AEEventHandlerProcPtr)openURLCallback), 0, false);
	} else {
		return 1;
	}
}


/**
 * Callback from OS X with URL.
 * Reference:
 *     http://developer.apple.com/documentation/Carbon/Reference/Apple_Event_Manager/Reference/reference.html#//apple_ref/c/func/NewAEEventHandlerUPP
 *     http://developer.apple.com/documentation/Carbon/Reference/Apple_Event_Manager/Reference/reference.html#//apple_ref/c/tdef/AEEventHandlerUPP
 *     http://developer.apple.com/documentation/Carbon/Reference/Apple_Event_Manager/Reference/reference.html#//apple_ref/c/tdef/AEEventHandlerProcPtr
 *
 * @param theAppleEvent Pointer to apple event handle
 * @param reply Pointer to default reply event
 * @param handlerRefcon Reference constant for this callback. (Ignored)
 */
static OSErr openURLCallback(const AppleEvent *theAppleEvent, AppleEvent* reply, long handlerRefcon) {
	OSErr result = noErr;
	Size givenLength = 0;
	DescType type = typeChar;
	
	// Get the size of the string the callback wants to give us, and then
	// check that it is > 0 in length.
	result = AESizeOfParam(theAppleEvent, keyDirectObject, &type, &givenLength);
	if (result == noErr && givenLength != 0) {
		// Allocate a buffer for the result
		// givenLength +1 for the \0
		Size length = givenLength + 1;
		char *dataPtr = (char*)malloc(length);
		
		if (dataPtr != 0) {
			// Empty the buffer
			memset(dataPtr, 0, length);
			
			// Get the url
			result = AEGetParamPtr(theAppleEvent, keyDirectObject, typeChar, 0, dataPtr, givenLength, &givenLength);
			// Did we get it?
			if (result == noErr) {
				// Get the java environment for the jvm we want to callback to
				JNIEnv *env;
				(*jvm)->AttachCurrentThread(jvm, (void **)&env, NULL);
				
				// Convert the url into a java string
				jstring theURL = (*env)->NewStringUTF(env, dataPtr);
				
				// Call the method!
				(*env)->CallVoidMethod(env, apple, callbackMethod, theURL);
			}
			
			// Free the buffer
			free(dataPtr);
		}
	}
	
	// And return!
	return result;
}