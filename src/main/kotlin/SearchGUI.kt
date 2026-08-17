package de.leo160905

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.Rectangle
import java.awt.Toolkit
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.BorderFactory
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextField
import javax.swing.SwingUtilities

class SearchGUI(val controller: Controller) : JFrame("Pixora") {
    val screenDim: Dimension = Toolkit.getDefaultToolkit().screenSize

    val contentPanel = JPanel()
    val picturesGridPanel = JPanel()
    lateinit var searchBtn: JButton
    lateinit var searchField: JTextField

    lateinit var switchTabButton: JButton

    init {
        defaultCloseOperation = EXIT_ON_CLOSE

        val cp = contentPane
        cp.layout = BorderLayout()
        cp.add(getControllBar(), BorderLayout.NORTH)

        contentPanel.preferredSize = Dimension(screenDim.width * 2 / 3, screenDim.height * 3 / 5)
        contentPanel.layout = BorderLayout()
        contentPanel.add(getPictureScrollPane(contentPanel), BorderLayout.CENTER)
        cp.add(contentPanel, BorderLayout.CENTER)

        pack()
        revalidate()
        setLocationRelativeTo(null)
        Toolkit.getDefaultToolkit().sync()
        isVisible = true
    }

    fun getControllBar(): JPanel {
        return JPanel().apply {
            preferredSize = Dimension(
                screenDim.width * 2 / 3,
                if (screenDim.height * 1 / 10 >= 100) screenDim.height * 1 / 10 else 100
            )
            background = Color.decode("000050")
            layout = null

            searchBtn = createSearchBTN(this)
            add(searchBtn)
            searchField = createSearchField(this)
            add(searchField)

            switchTabButton = createSwitchTabButton(this)
            add(switchTabButton)
        }
    }

    fun createSwitchTabButton(parentPanel: JPanel): JButton {
        return createBaseButton().apply {
            text = "open library"
            bounds = Rectangle(650, (parentPanel.preferredSize.height - 30) / 2, 200, 30)
            addActionListener {
                controller.openPictureLibrary()
            }
        }
    }


    fun createSearchBTN(parentPanel: JPanel): JButton {
        return createBaseButton().apply {
            text = "Search"
            bounds = Rectangle(400, (parentPanel.preferredSize.height - 30) / 2, 200, 30)
            addActionListener {
                controller.search(searchField.text)
            }
        }
    }

    fun createBaseButton() : JButton {
        return JButton().apply {
            isFocusPainted = false
            isFocusable = false

            background = Color.WHITE
            border = BorderFactory.createLineBorder(Color.BLACK, 3)
        }
    }

    fun createSearchField(parentPanel: JPanel): JTextField {
        return JTextField().apply {
            bounds = Rectangle(50, (parentPanel.preferredSize.height - 30) / 2, 300, 30)
            border = BorderFactory.createLineBorder(Color.BLACK, 3)

            addKeyListener(object : KeyAdapter() {
                override fun keyPressed(e: KeyEvent?) {
                    super.keyPressed(e)
                    if (e?.keyCode == KeyEvent.VK_ENTER) searchBtn.doClick()
                }
            })
        }
    }

    fun getPictureScrollPane(parentPanel: JPanel): JScrollPane {
        return JScrollPane(picturesGridPanel.apply {
            background = Color.ORANGE
            layout = GridLayout(0, 3, 5, 5)

            val thirdWidth = parentPanel.preferredSize.width / 3
            controller.thumbnailWidth = thirdWidth
        }).apply {
            setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS)
            verticalScrollBar.unitIncrement = 15
        }
    }

    fun CreatePictureButton(icon: ImageIcon, id: String) {
        SwingUtilities.invokeLater {
            val picBTN = JButton(icon)
            picBTN.preferredSize = Dimension(icon.image.getWidth(null), icon.image.getHeight(null))
            picBTN.addActionListener {
                controller.handelSelectedPicture(id)
            }
            picturesGridPanel.add(picBTN)
            revalidatePics()
        }
    }

    fun removePictures() {
        picturesGridPanel.removeAll()
        revalidatePics()
    }

    fun revalidatePics() {
        picturesGridPanel.revalidate()
        picturesGridPanel.repaint()
    }
}