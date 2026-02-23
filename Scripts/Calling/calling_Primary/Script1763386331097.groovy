import com.kms.katalon.core.util.KeywordUtil

// ========== STEP 1: Wake up the smartwatch ==========
def wakeCommand = 'adb shell am start -n io.senlab.cosmo/io.senlab.cosmo.MainActivity'
def process1 = wakeCommand.execute()
process1.waitFor()
KeywordUtil.logInfo("✅ Screen awakened successfully.")

// Wait for the home screen to load
Thread.sleep(2000)

// ========== STEP 2: Swipe left to reach apps ==========
def swipeCommand = 'adb shell input swipe 200 120 40 120'
def process2 = swipeCommand.execute()
process2.waitFor()
KeywordUtil.logInfo("✅ Swipe left performed successfully.")

// Wait for icons animation to complete
Thread.sleep(2000)

// ========== STEP 3: Open Dialer App ==========
def openDialer = 'adb shell input tap 60 80'
def process3 = openDialer.execute()
process3.waitFor()
KeywordUtil.logInfo("✅ Dialer app opened successfully.")

// Wait for contact list to load
Thread.sleep(2000)

// ========== STEP 4: Tap on "Ahmad Alam" (top contact) ==========
def tapContact = 'adb shell input tap 120 50'   // 👈 adjusted Y coordinate for top contact
def process4 = tapContact.execute()
process4.waitFor()
KeywordUtil.logInfo("📞 Call initiated to Ahmad Alam.")

// Let the call stay active for 5 seconds
Thread.sleep(5000)

// ========== STEP 5: End the Call ==========
def endCall = 'adb shell input tap 120 200'   // red hangup button
def process5 = endCall.execute()
process5.waitFor()
KeywordUtil.logInfo("📴 Call ended successfully.")

// ========== STEP 6: Final Message ==========
println("🎯 Flow completed successfully: Wake → Swipe Left → Open Dialer → Call Ahmad Alam → End Call")