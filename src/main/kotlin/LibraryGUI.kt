package de.leo160905

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Cursor
import java.awt.Dimension
import java.awt.Font
import java.awt.GridLayout
import java.awt.Toolkit
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.SwingUtilities

class LibraryGUI(controller: Controller, isVisibleAtStart: Boolean) : JFrame() {

    var contentPanel: JPanel
    val screenDim: Dimension = Toolkit.getDefaultToolkit().screenSize
    val picturesGridPanel = JPanel()

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        isResizable = false
        font = Font("Arial",Font.BOLD, 20)

        val cp = contentPane
        cp.layout = BorderLayout()
        contentPanel = createContentPanel()
        cp.add(contentPanel, BorderLayout.CENTER)

        pack()
        revalidate()
        setLocationRelativeTo(null)
        Toolkit.getDefaultToolkit().sync()


        isVisible = isVisibleAtStart
    }

    fun createContentPanel(): JPanel {
        return JPanel().apply {
            preferredSize = Dimension(screenDim.width * 2 / 3, screenDim.height * 3 / 5)
            layout = BorderLayout()

            add(getPictureScrollPane(), BorderLayout.CENTER)
        }
    }

    fun getPictureScrollPane(): JScrollPane {
        return JScrollPane(picturesGridPanel.apply {
            background = Color.ORANGE
            layout = GridLayout(0, 3, 5, 5)
        }).apply {
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            verticalScrollBar.unitIncrement = 15
        }
    }

    fun CreatePictureButton(icon: ImageIcon, id: String) {
        SwingUtilities.invokeLater {
            val picBTN = JButton(icon)
            picBTN.preferredSize = Dimension(icon.image.getWidth(null), icon.image.getHeight(null))

            picturesGridPanel.add(picBTN)
            revalidatePics()
        }
    }

    fun revalidatePics() {
        picturesGridPanel.revalidate()
        picturesGridPanel.repaint()
    }

    fun setCursorToLoading() {
        SwingUtilities.invokeLater {
            cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
        }
    }

    fun setCursorToPointer() {
        SwingUtilities.invokeLater {
            cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
        }
    }
}