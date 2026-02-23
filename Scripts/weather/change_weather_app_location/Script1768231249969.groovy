import com.kms.katalon.core.util.KeywordUtil
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.util.KeywordUtil
import groovy.xml.XmlSlurper

// ===================================================================
// HELPER — Dump UI XML from watch and parse it
// ===================================================================
def dumpAndParseXML() {

	File dir = new File("C:\\cosmo_xml")
	if (!dir.exists()) {
		dir.mkdirs()
	}

	"adb shell uiautomator dump /sdcard/ui.xml".execute().waitFor()
	"adb pull /sdcard/ui.xml C:\\cosmo_xml\\ui.xml".execute().waitFor()

	File xmlFile = new File("C:\\cosmo_xml\\ui.xml")
	return new XmlSlurper().parse(xmlFile)
}

// ========== STEP 1: Wake up the smartwatch ==========
def wakeCommand = 'adb shell am start -n io.senlab.cosmo/io.senlab.cosmo.MainActivity'
def process1 = wakeCommand.execute()
process1.waitFor()
KeywordUtil.logInfo("✅ Screen awakened successfully.")

// Small delay to allow UI to load
Thread.sleep(2000)

// ========== STEP 2: Swipe left Till it reaches weather App==========
def swipeCommand = 'adb shell input swipe 200 120 40 120'
def process2 = swipeCommand.execute()
Thread.sleep(2000)
swipeCommand.execute()
Thread.sleep(5000)
process2.waitFor()
KeywordUtil.logInfo("✅ Swipe left performed successfully.")
Thread.sleep(5000)
//=== Tapping Weather App ===
KeywordUtil.logInfo("=== Tapping Weather App ===")

def xml = dumpAndParseXML()

// Weather app is bottom-left app in grid
def weatherNode = xml.depthFirst().find {
    it.@class == 'android.widget.RelativeLayout' &&
    it.@clickable == 'true' &&
    it.@bounds?.toString()?.startsWith('[4,') &&
    it.@bounds?.toString()?.endsWith('[120,236]')
}


if (!weatherNode) {
	KeywordUtil.markFailed("❌ Weather app not found in app grid")
}

// Extract bounds
String bounds = weatherNode.@bounds.toString()
def matcher = bounds =~ /\[(\d+),(\d+)\]\[(\d+),(\d+)\]/
matcher.find()

int x1 = matcher.group(1) as int
int y1 = matcher.group(2) as int
int x2 = matcher.group(3) as int
int y2 = matcher.group(4) as int

int centerX = (x1 + x2) / 2
int centerY = (y1 + y2) / 2

"adb shell input tap ${centerX} ${centerY}".execute().waitFor()

KeywordUtil.logInfo("✅ Weather app tapped successfully")


// ========== STEP 3: Scroll to settings==========
// ===================================================================
// STEP 3 — SCROLL DOWN TO REVEAL settings in weather app
// ===================================================================

KeywordUtil.logInfo("=== STEP 5: Scrolling down to reveal Settings ===")

// Swipe 1 — initial safe swipe (top area)
"adb shell input swipe 120 140 120 90 300".execute().waitFor()
Thread.sleep(2000)

// Swipe 2 — bottom swipe to move past time selector
"adb shell input swipe 120 215 120 130 400".execute().waitFor()
Thread.sleep(2000)

// Swipe 3 — repeat bottom swipe to fully reveal button
"adb shell input swipe 120 215 120 130 400".execute().waitFor()
Thread.sleep(2000)

// Swipe 4 — initial safe swipe (top area)
"adb shell input swipe 120 140 120 90 300".execute().waitFor()
Thread.sleep(2000)

// Swipe 5 — initial safe swipe (top area)
"adb shell input swipe 120 140 120 90 300".execute().waitFor()
Thread.sleep(2000)

// Swipe 6 — initial safe swipe (top area)
"adb shell input swipe 120 140 120 90 300".execute().waitFor()
Thread.sleep(2000)

// Swipe 7 — initial safe swipe (top area)
"adb shell input swipe 120 140 120 90 300".execute().waitFor()
Thread.sleep(2000)

// Swipe 8 — bottom swipe to move past time selector
"adb shell input swipe 120 215 120 130 400".execute().waitFor()
Thread.sleep(2000)

// Swipe 9 — repeat bottom swipe to fully reveal button
"adb shell input swipe 120 215 120 130 400".execute().waitFor()
Thread.sleep(2000)

// Swipe 10 — initial safe swipe (top area)
"adb shell input swipe 120 140 120 90 300".execute().waitFor()
Thread.sleep(2000)

// Swipe 11 — initial safe swipe (top area)
"adb shell input swipe 120 140 120 90 300".execute().waitFor()
Thread.sleep(2000)

// Swipe 12 — initial safe swipe (top area)
"adb shell input swipe 120 140 120 90 300".execute().waitFor()
Thread.sleep(2000)

KeywordUtil.logInfo("⬇️ Completed controlled scrolls — Settings button should be visible.")


//=== Step 4: Tapping Weather Settings button ===
KeywordUtil.logInfo("=== Tapping Weather Settings button ===")

def xmlSettings = dumpAndParseXML()

def settingsNode = xmlSettings.depthFirst().find {
    it.@'resource-id' == 'com.cosmotogether.weather:id/settingsLinearLayout'
}

if (!settingsNode) {
    KeywordUtil.markFailed("❌ Weather Settings button not found in XML")
}

// Extract bounds (use unique variable names)
String settingsBounds = settingsNode.@bounds.toString()
def settingsMatcher = settingsBounds =~ /\[(\d+),(\d+)\]\[(\d+),(\d+)\]/
settingsMatcher.find()

int sx1 = settingsMatcher.group(1) as int
int sy1 = settingsMatcher.group(2) as int
int sx2 = settingsMatcher.group(3) as int
int sy2 = settingsMatcher.group(4) as int

int settingsCenterX = (sx1 + sx2) / 2
int settingsCenterY = (sy1 + sy2) / 2

"adb shell input tap ${settingsCenterX} ${settingsCenterY}".execute().waitFor()
Thread.sleep(2000)

KeywordUtil.logInfo("✅ Weather Settings button tapped successfully")




// ==================================================
// STEP 5: Verify and tap Location in Weather Settings
// ==================================================

KeywordUtil.logInfo("=== STEP 5: Verifying Location card presence ===")

def xmlLocation = dumpAndParseXML()

def locationNode = xmlLocation.depthFirst().find {
	it.@'resource-id' == 'com.cosmotogether.weather:id/locationCard' &&
	it.@clickable == 'true'
}

if (!locationNode) {
	KeywordUtil.markFailed("❌ Location card not found on Weather Settings screen")
}

// Extract bounds safely
String locationBounds = locationNode.@bounds.toString()
def locationMatcher = locationBounds =~ /\[(\d+),(\d+)\]\[(\d+),(\d+)\]/
locationMatcher.find()

int lx1 = locationMatcher.group(1) as int
int ly1 = locationMatcher.group(2) as int
int lx2 = locationMatcher.group(3) as int
int ly2 = locationMatcher.group(4) as int

int locationCenterX = (lx1 + lx2) / 2
int locationCenterY = (ly1 + ly2) / 2

KeywordUtil.logInfo("📍 Tapping Location at (${locationCenterX}, ${locationCenterY})")

"adb shell input tap ${locationCenterX} ${locationCenterY}".execute().waitFor()
Thread.sleep(2000)

KeywordUtil.logInfo("✅ Location card tapped successfully")



// ==================================================
// STEP 6: Verify and tap Select Location
// ==================================================

KeywordUtil.logInfo("=== STEP 6: Verifying Select Location option ===")

def xmlSelectLocation = dumpAndParseXML()

def selectLocationNode = xmlSelectLocation.depthFirst().find {
	it.@'resource-id' == 'com.cosmotogether.weather:id/enterLocationCard' &&
	it.@clickable == 'true'
}

if (!selectLocationNode) {
	KeywordUtil.markFailed("❌ Select Location card not found on Weather Location screen")
}

// Extract bounds
String selectBounds = selectLocationNode.@bounds.toString()
def selectMatcher = selectBounds =~ /\[(\d+),(\d+)\]\[(\d+),(\d+)\]/
selectMatcher.find()

int slx1 = selectMatcher.group(1) as int
int sly1 = selectMatcher.group(2) as int
int slx2 = selectMatcher.group(3) as int
int sly2 = selectMatcher.group(4) as int

int selectCenterX = (slx1 + slx2) / 2
int selectCenterY = (sly1 + sly2) / 2

KeywordUtil.logInfo("📍 Tapping Select Location at (${selectCenterX}, ${selectCenterY})")

"adb shell input tap ${selectCenterX} ${selectCenterY}".execute().waitFor()
Thread.sleep(2000)

KeywordUtil.logInfo("✅ Select Location tapped successfully")


// ==================================================
// STEP 7: Verify and tap Enter Location
// ==================================================

KeywordUtil.logInfo("=== STEP 7: Verifying Enter Location field ===")

def xmlEnterLocation = dumpAndParseXML()

// Find Enter Location card (most stable parent)
def enterLocationNode = xmlEnterLocation.depthFirst().find {
	it.@'resource-id' == 'com.cosmotogether.weather:id/enterLocationCard' &&
	it.@clickable == 'true'
}

if (!enterLocationNode) {
	KeywordUtil.markFailed("❌ Enter Location card not found on screen")
}

// Extract bounds safely
String enterBounds = enterLocationNode.@bounds.toString()
def enterMatcher = enterBounds =~ /\[(\d+),(\d+)\]\[(\d+),(\d+)\]/
enterMatcher.find()

int elx1 = enterMatcher.group(1) as int
int ely1 = enterMatcher.group(2) as int
int elx2 = enterMatcher.group(3) as int
int ely2 = enterMatcher.group(4) as int

int enterCenterX = (elx1 + elx2) / 2
int enterCenterY = (ely1 + ely2) / 2

KeywordUtil.logInfo("📍 Tapping Enter Location at (${enterCenterX}, ${enterCenterY})")

// Tap Enter Location
"adb shell input tap ${enterCenterX} ${enterCenterY}".execute().waitFor()
Thread.sleep(2000)

// Tap Enter Location
"adb shell input tap ${enterCenterX} ${enterCenterY}".execute().waitFor()
Thread.sleep(2000)

// Tap Enter Location
"adb shell input tap ${enterCenterX} ${enterCenterY}".execute().waitFor()
Thread.sleep(2000)

KeywordUtil.logInfo("✅ Enter Location tapped successfully")
