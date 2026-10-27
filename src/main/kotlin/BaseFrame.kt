package de.leo160905

import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JTextField

abstract class BaseFrame(title: String) : JFrame(title) {
    fun getButtonBase(text: String = ""): JButton {
        return JButton(text).apply {
            isFocusPainted = false
            isFocusable = false
            font = this@BaseFrame.font

            background = Color.WHITE
            border = BorderFactory.createLineBorder(Color.BLACK, 3)
        }
    }

    fun getTextFieldBase(): JTextField {
        return JTextField().apply {
            border = BorderFactory.createLineBorder(Color.BLACK, 3)
            font = this@BaseFrame.font
        }
    }

    fun getControlPanelBase(parentPanel: JPanel): JPanel {
        return JPanel().apply {
            val pHeight = parentPanel.preferredSize.height / 2
            val pWidth = parentPanel.preferredSize.width
            preferredSize = Dimension(pWidth, pHeight)
            layout = FlowLayout(FlowLayout.LEFT, pWidth * 1 / 50, pHeight / 4)
            background = parentPanel.background
            font = this@BaseFrame.font
        }
    }
}