package de.leo160905

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.GridLayout
import java.awt.Toolkit
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.text.NumberFormat
import java.util.Locale
import javax.swing.BorderFactory
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextField
import javax.swing.SwingUtilities
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class SearchGUI(val controller: Controller) : JFrame("Pixora") {
    val screenDim: Dimension = Toolkit.getDefaultToolkit().screenSize

    val contentPanel = JPanel()
    val picturesGridPanel = JPanel()
    lateinit var searchBtn: JButton
    lateinit var searchField: JTextField

    lateinit var switchTabButton: JButton
    lateinit var pageFilterField: JTextField

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        isResizable = false
        font = Font("Arial",Font.BOLD, 20)

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


    fun getButtonBase(): JButton {
        return JButton().apply {
            isFocusPainted = false
            isFocusable = false
            font = this@SearchGUI.font

            background = Color.WHITE
            border = BorderFactory.createLineBorder(Color.BLACK, 3)
        }
    }

    fun getTextFieldBase(): JTextField {
        return JTextField().apply {
            border = BorderFactory.createLineBorder(Color.BLACK, 3)
            font = this@SearchGUI.font
        }
    }

    fun getControlPanelBase(parentPanel: JPanel): JPanel {
        return JPanel().apply {
            val pHeight = parentPanel.preferredSize.height / 2
            val pWidth = parentPanel.preferredSize.width
            preferredSize = Dimension(pWidth, pHeight)
            layout = FlowLayout(FlowLayout.LEFT, pWidth * 1 / 50, pHeight / 4)
            background = parentPanel.background
            font = this@SearchGUI.font
        }
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

    fun getSearchPanel(parentPanel: JPanel): JPanel {
        return getControlPanelBase(parentPanel).apply {
            val pHeight = this.preferredSize.height
            val pWidth = this.preferredSize.width

            // TextFieldSize pWidth - 3x Padding 1/50 - Button 1/10 = 8/50
            searchField = createSearchField(Dimension(pWidth - pWidth * 8 / 50, pHeight * 3 / 4))
            add(searchField)
            searchBtn = createSearchBTN(Dimension(pWidth * 1 / 10, pHeight * 3 / 4))
            add(searchBtn)
        }
    }

    fun createSearchBTN(tButtonSize: Dimension): JButton {
        return getButtonBase().apply {
            text = "Search"
            preferredSize = tButtonSize
            addActionListener {
                controller.search(searchField.text, pageFilterField.text)
            }
        }
    }

    fun createSearchField(tFieldSize: Dimension): JTextField {
        return getTextFieldBase().apply {
            preferredSize = tFieldSize


            addKeyListener(object : KeyAdapter() {
                override fun keyPressed(e: KeyEvent?) {
                    if (e?.keyCode == KeyEvent.VK_ENTER) searchBtn.doClick()
                }
            })
        }
    }

    fun getSettingsBar(parentPanel: JPanel): JPanel {
        return getControlPanelBase(parentPanel).apply {
            val pHeight = this.preferredSize.height
            val pWidth = this.preferredSize.width
            val componentsHeight = pHeight / 2
            font = Font(this@SearchGUI.font.name, this@SearchGUI.font.style, 12)

            switchTabButton = createSwitchTabButton(Dimension(pWidth * 1 / 15, componentsHeight), font)
            add(switchTabButton)

            pageFilterField = createPageFilterField(Dimension(pWidth * 1 / 15, componentsHeight), font)
            add(pageFilterField)

            add(createCheckMaxPagesButton(Dimension(pWidth * 1 / 15, componentsHeight), font))
        }
    }

    fun createSwitchTabButton(size: Dimension, font: Font): JButton {
        return getButtonBase().apply {
            text = "open library"
            preferredSize = size
            this.font = font
            addActionListener {
                controller.openPictureLibrary()
            }
        }
    }

    fun createPageFilterField(size: Dimension, font: Font): JTextField {
        return getTextFieldBase().apply {
            toolTipText = "0..Infinity | -1 is max"
            preferredSize = size
            text = "0..-1"
            this.font = font

            document.addDocumentListener(object : DocumentListener {
                override fun insertUpdate(e: DocumentEvent?) = onChange()
                override fun removeUpdate(e: DocumentEvent?) = onChange()
                override fun changedUpdate(e: DocumentEvent?) = onChange()
                fun onChange() {
                    val result = controller.checkPageFilter(text)
                    println("Check for: $text got reuslt: $result")
                    searchBtn.isEnabled = result
                }
            })

            addKeyListener(object : KeyAdapter() {
                override fun keyPressed(e: KeyEvent?) {
                    if(e?.keyCode == KeyEvent.VK_ENTER) {
                        searchBtn.doClick()
                    }
                }
            })
        }
    }

    fun createCheckMaxPagesButton(size: Dimension, font: Font): JButton {
        return getButtonBase().apply {
            text = "max Pages"
            preferredSize = size
            this.font = font

            addActionListener {
                val maxPages = controller.getMaxPageOfQuery(searchField.text)
                val formater = NumberFormat.getNumberInstance(Locale.GERMANY)
                text = formater.format(maxPages)
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