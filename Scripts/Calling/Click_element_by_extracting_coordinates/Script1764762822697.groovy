import groovy.xml.XmlSlurper
import com.kms.katalon.core.util.KeywordUtil

def xmlFile = new File("C:\\Cosmo\\screen1_xml\\window_dump.xml")
def xml = new XmlSlurper().parse(xmlFile)

// Unique Dialer icon bounds
def dialerBounds = "[4,19][120,129]"

def dialerNode = xml.depthFirst().find { 
    it.@bounds.toString() == dialerBounds &&
    it.@class.toString() == "android.widget.RelativeLayout"
}

if (!dialerNode) {
    KeywordUtil.markFailed("❌ Dialer icon NOT found — bounds mismatch!")
}

def bounds = dialerNode.@bounds.toString()

def matcher = bounds =~ /\[(\d+),(\d+)\]\[(\d+),(\d+)\]/
matcher.find()

int x = ((matcher[0][1] as int) + (matcher[0][3] as int)) / 2
int y = ((matcher[0][2] as int) + (matcher[0][4] as int)) / 2

KeywordUtil.logInfo("🎯 Clicking Dialer at X: ${x}, Y: ${y}")
"adb shell input tap ${x} ${y}".execute().waitFor()
