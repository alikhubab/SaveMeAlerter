package com.gray.vulf.savemealerter

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat

class WhatsappAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Log.e(TAG, "onAccessibilityEvent")
        if (rootInActiveWindow == null) {
            return
        }

//        getting root node
        val rootNodeInfo = AccessibilityNodeInfoCompat.wrap(rootInActiveWindow)

//        get edit text if message from whatsapp
        val messageNodeList =
            rootNodeInfo.findAccessibilityNodeInfosByViewId("com.whatsapp:id/entry")
        if (messageNodeList == null || messageNodeList.isEmpty()) return

//        Checking if message failed if filled with text and ending with our suffix
        val messageField = messageNodeList[0]
        if (messageField == null || messageField.text.isEmpty() || messageField.text.toString()
                .endsWith("   ")
        )
            return

//        get whatsapp send message button node list
        val sendMessageNodeList =
            rootNodeInfo.findAccessibilityNodeInfosByViewId("com.whatsapp:id/send")
        if (sendMessageNodeList == null || sendMessageNodeList.isEmpty())
            return

        val sendMessage = sendMessageNodeList[0]
        if (!sendMessage.isVisibleToUser)
            return

//        fire send button
        sendMessage.performAction(AccessibilityNodeInfo.ACTION_CLICK)

//        go back to our app by clicking back button twice
        try {
            Thread.sleep(2000)
            performGlobalAction(GLOBAL_ACTION_BACK)
            Thread.sleep(2000)
        } catch (ignored: InterruptedException) {
        }

        performGlobalAction(GLOBAL_ACTION_BACK)

    }

    override fun onInterrupt() {


    }
}