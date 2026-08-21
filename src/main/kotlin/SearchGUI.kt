package de.leo160905

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
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
        isResizable = false

        val cp = contentPane
        cp.layout = BorderLayout()
        cp.add(getControlBar(), BorderLayout.NORTH)

        contentPanel.preferredSize = Dimension(screenDim.width * 2 / 3, screenDim.height * 3 / 5)
        contentPanel.layout = BorderLayout()
        contentPanel.add(getPictureScrollPane(), BorderLayout.CENTER)
        cp.add(contentPanel, BorderLayout.CENTER)

        pack()
        revalidate()
        setLocationRelativeTo(null)
        Toolkit.getDefaultToolkit().sync()
        isVisible = true
    }

    fun getControlBar(): JPanel {
        return JPanel().apply {
            preferredSize = Dimension(
                screenDim.width * 2 / 3,
                if (screenDim.height * 1 / 10 >= 100) screenDim.height * 1 / 10 else 100
            )
            background = Color.decode("000050")
            val myLayout = GridLayout(2, 1)
            myLayout.columns
            layout = myLayout

            add(getSearchPanel(this))

            add(getSettingsBar(this))
        }
    }

    fun createBaseButton(): JButton {
        return JButton().apply {
            isFocusPainted = false
            isFocusable = false

            background = Color.WHITE
            border = BorderFactory.createLineBorder(Color.BLACK, 3)
        }
    }

    fun getControlPanelBase(parentPanel: JPanel): JPanel {
        return JPanel().apply {
            val pHeight = parentPanel.preferredSize.height / 2
            val pWidth = parentPanel.preferredSize.width
            preferredSize = Dimension(pWidth, pHeight)
            layout = FlowLayout(FlowLayout.LEFT, pWidth * 1 / 25, pHeight / 4)
            background = parentPanel.background
        }
    }

    fun getSearchPanel(parentPanel: JPanel): JPanel {
        return getControlPanelBase(parentPanel).apply {
            val pHeight = this.preferredSize.height
            val pWidth = this.preferredSize.width

            // TextFieldSize pWidth - 3x Padding 1/25 - Button 1/10 = 11/50
            searchField = createSearchField(Dimension(pWidth - pWidth * 11 / 50, pHeight * 3 / 4))
            add(searchField)
            searchBtn = createSearchBTN(Dimension(pWidth * 1 / 10, pHeight * 3 / 4))
            add(searchBtn)
        }
    }

    fun createSearchBTN(tButtonSize: Dimension): JButton {
        return createBaseButton().apply {
            text = "Search"
            preferredSize = tButtonSize
            addActionListener {
                controller.search(searchField.text)
            }
        }
    }

    fun createSearchField(tFieldSize: Dimension): JTextField {
        return JTextField().apply {
            preferredSize = tFieldSize
//            bounds = Rectangle((pWidth - tFieldWidth) / 2, pHeight / 8, tFieldWidth, tFieldHeight)
            border = BorderFactory.createLineBorder(Color.BLACK, 3)

            addKeyListener(object : KeyAdapter() {
                override fun keyPressed(e: KeyEvent?) {
                    super.keyPressed(e)
                    if (e?.keyCode == KeyEvent.VK_ENTER) searchBtn.doClick()
                }
            })
        }
    }

    fun getSettingsBar(parentPanel: JPanel): JPanel {
        return getControlPanelBase(parentPanel).apply {
            val pHeight = this.preferredSize.height
            val pWidth = this.preferredSize.width

            switchTabButton = createSwitchTabButton(Dimension(pWidth * 1 / 15, pHeight * 2))
            add(switchTabButton)
        }
    }

    fun createSwitchTabButton(tButtonSize: Dimension): JButton {
        return createBaseButton().apply {
            text = "open library"
            preferredSize = tButtonSize
            addActionListener {
                controller.openPictureLibrary()
            }
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
            picBTN.addActionListener {
                controller.handelSelectedPicture(id)
            }
            picturesGridPanel.add(picBTN)
            revalidatePics()
        }
    }

    fun removePictures() {
        SwingUtilities.invokeLater {

            picturesGridPanel.removeAll()
            revalidatePics()
        }
    }

    fun revalidatePics() {
        picturesGridPanel.revalidate()
        picturesGridPanel.repaint()
    }
}