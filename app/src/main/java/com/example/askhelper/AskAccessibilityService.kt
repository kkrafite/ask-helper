package com.example.askhelper

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class AskAccessibilityService : AccessibilityService() {

    companion object {
        var isServiceEnabled = false
        private const val TAKE_TEXT = "Взять"
        private const val REPLY_TEXT = "Здравствуйте, анализирую ваш вопрос подождите!"
    }

    private var lastActionTime = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!isServiceEnabled || event == null) return

        try {
            val rootNode = rootInActiveWindow ?: return
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastActionTime < 250) return

            val takeNodes = rootNode.findAccessibilityNodeInfosByText(TAKE_TEXT)
            if (!takeNodes.isNullOrEmpty()) {
                if (clickNodeOrParent(takeNodes[0])) {
                    lastActionTime = currentTime
                    return
                }
            }

            val editTextNode = findEditText(rootNode)
            if (editTextNode != null) {
                val currentText = editTextNode.text?.toString() ?: ""
                if (currentText != REPLY_TEXT) {
                    val args = Bundle().apply {
                        putCharSequence(
                            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                            REPLY_TEXT
                        )
                    }
                    editTextNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
                    lastActionTime = currentTime
                }

                val sendButtons = rootNode.findAccessibilityNodeInfosByText("Отправить")
                    .ifEmpty { rootNode.findAccessibilityNodeInfosByText("Далее") }
                    .ifEmpty { rootNode.findAccessibilityNodeInfosByText("ОК") }
                    .ifEmpty { rootNode.findAccessibilityNodeInfosByText("Ок") }

                if (sendButtons.isNotEmpty() && clickNodeOrParent(sendButtons[0])) {
                    lastActionTime = currentTime
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun clickNodeOrParent(node: AccessibilityNodeInfo?): Boolean {
        var current = node
        while (current != null) {
            if (current.isClickable) {
                return current.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            current = current.parent
        }
        return false
    }

    private fun findEditText(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        if (node.className == "android.widget.EditText") {
            return node
        }
        val count = node.childCount
        for (i in 0 until count) {
            if (i < node.childCount) {
                val child = node.getChild(i)
                val found = findEditText(child)
                if (found != null) return found
            }
        }
        return null
    }

    fun clickAtCoordinates(x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 40))
            .build()
        dispatchGesture(gesture, null, null)
    }

    override fun onInterrupt() {}
}
