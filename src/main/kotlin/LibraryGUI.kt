package de.leo160905

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Cursor
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.GridLayout
import java.awt.Toolkit
import javax.swing.BorderFactory
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.SwingUtilities
import javax.swing.UIManager

class LibraryGUI(val controller: Controller, isVisibleAtStart: Boolean) : BaseFrame("Library") {

    var contentPanel: JPanel
    val screenDim: Dimension = Toolkit.getDefaultToolkit().screenSize
    val picturesGridPanel = JPanel()

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        isResizable = false
        font = Font("Arial", Font.BOLD, 20)

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

    fun createContentPanel() = JPanel().apply {
        preferredSize = Dimension(screenDim.width * 2 / 3, screenDim.height * 3 / 5)
        layout = BorderLayout()

        add(createSettingsPanel(), BorderLayout.NORTH)
        add(getPictureScrollPane(), BorderLayout.CENTER)
    }

    fun createSettingsPanel(): JPanel {
        return JPanel().apply {
            preferredSize = Dimension(
                screenDim.width * 2 / 3,
                if (screenDim.height * 1 / 20 >= 50) screenDim.height * 1 / 20 else 50
            )

            val pHeight = this.preferredSize.height / 2
            val pWidth = this.preferredSize.width

            font = Font("Arial", Font.BOLD, 12)

            background = Color.decode("000050")

            layout = FlowLayout(FlowLayout.LEFT, pWidth * 1 / 50, pHeight / 2)

            add(getSearchTabButton(Dimension(pWidth * 1/15, pHeight), this))
            add(getFileChooseButton(Dimension(pWidth * 1/15, pHeight), this))
        }
    }

    fun getFileChooseButton(size: Dimension, parentPanel: JPanel) = getButtonBase("choose File").apply {
        preferredSize = size
        font = parentPanel.font
        addActionListener {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
            val fc = JFileChooser()
            fc.fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES
            fc.preferredSize = Dimension(screenDim.width * 2/4, screenDim.height * 2/5)
            if(fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                val selectedFile = fc.selectedFile
                println("Selected File: $selectedFile")
            }
        }
    }

    fun getSearchTabButton(size: Dimension, parentPanel: JPanel) = getButtonBase("open Search").apply {
        preferredSize = size
        font = parentPanel.font
        addActionListener { controller.openSeachFrame() }
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

    fun createPictureButton(icon: ImageIcon, id: String) {
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