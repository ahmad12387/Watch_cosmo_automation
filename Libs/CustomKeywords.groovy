
/**
 * This class is generated automatically by Katalon Studio and should not be modified or deleted.
 */

import java.lang.String



def static "smartWatch.SmartWatchValidation.verifyCallReceived"(
    	String logFilePath	) {
    (new smartWatch.SmartWatchValidation()).verifyCallReceived(
        	logFilePath)
}


def static "smartWatch.SmartWatchValidation.verifyMissedCall"() {
    (new smartWatch.SmartWatchValidation()).verifyMissedCall()
}


def static "smartWatch.SmartWatchCallingPage.callContactByIndex"(
    	int index	) {
    (new smartWatch.SmartWatchCallingPage()).callContactByIndex(
        	index)
}


def static "smartWatch.SmartWatchCallingPage.callContactByName"(
    	String name	) {
    (new smartWatch.SmartWatchCallingPage()).callContactByName(
        	name)
}


def static "smartWatch.SmartWatchCallingPage.dumpContactsXML"() {
    (new smartWatch.SmartWatchCallingPage()).dumpContactsXML()
}


def static "smartWatch.SmartWatchCallingPage.waitForContactsToLoad"(
    	int timeoutSec	) {
    (new smartWatch.SmartWatchCallingPage()).waitForContactsToLoad(
        	timeoutSec)
}


def static "smartWatch.SmartWatchCallingPage.validateCallConnected"() {
    (new smartWatch.SmartWatchCallingPage()).validateCallConnected()
}


def static "smartWatch.SmartWatchCallingPage.endCall"() {
    (new smartWatch.SmartWatchCallingPage()).endCall()
}


def static "smartWatch.SmartWatchCallingPage.waitForCallToEnd"(
    	int timeoutSec	) {
    (new smartWatch.SmartWatchCallingPage()).waitForCallToEnd(
        	timeoutSec)
}


def static "smartWatch.SmartWatchCallingPage.waitForContactsToLoad"() {
    (new smartWatch.SmartWatchCallingPage()).waitForContactsToLoad()
}


def static "smartWatch.SmartWatchCallingPage.waitForCallToEnd"() {
    (new smartWatch.SmartWatchCallingPage()).waitForCallToEnd()
}


def static "smartWatch.SmartWatchBase.runAdb"(
    	String command	
     , 	boolean isShell	) {
    (new smartWatch.SmartWatchBase()).runAdb(
        	command
         , 	isShell)
}


def static "smartWatch.SmartWatchBase.wakeScreen"() {
    (new smartWatch.SmartWatchBase()).wakeScreen()
}


def static "smartWatch.SmartWatchBase.tap"(
    	int x	
     , 	int y	) {
    (new smartWatch.SmartWatchBase()).tap(
        	x
         , 	y)
}


def static "smartWatch.SmartWatchBase.swipe"(
    	int x1	
     , 	int y1	
     , 	int x2	
     , 	int y2	) {
    (new smartWatch.SmartWatchBase()).swipe(
        	x1
         , 	y1
         , 	x2
         , 	y2)
}


def static "smartWatch.SmartWatchBase.runAdb"(
    	String command	) {
    (new smartWatch.SmartWatchBase()).runAdb(
        	command)
}


def static "smartWatch.SmartWatchNavigation.goToAppGrid"() {
    (new smartWatch.SmartWatchNavigation()).goToAppGrid()
}


def static "smartWatch.SmartWatchNavigation.openDialerApp"() {
    (new smartWatch.SmartWatchNavigation()).openDialerApp()
}


def static "smartWatch.SmartWatchNavigation.dumpNavigationXML"() {
    (new smartWatch.SmartWatchNavigation()).dumpNavigationXML()
}


def static "smartWatch.SmartWatchNavigation.verifyDialerAppPresent"() {
    (new smartWatch.SmartWatchNavigation()).verifyDialerAppPresent()
}


def static "smartWatch.SmartWatchNavigation.swipeBackToPreviousScreen"() {
    (new smartWatch.SmartWatchNavigation()).swipeBackToPreviousScreen()
}


def static "sample.Common.startApplication"() {
    (new sample.Common()).startApplication()
}
