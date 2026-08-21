package de.leo160905

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Image
import java.awt.Rectangle
import java.awt.Toolkit
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import kotlin.math.roundToInt

class PreviewGUI(wallpaper: Wallpaper, val controller: Controller) : JFrame() {

    val contentPanel = JPanel()
    val controllerPanel = JPanel()
    val screenDim = Toolkit.getDefaultToolkit().screenSize
    val frameHeight = screenDim.height * 7 / 10

    init {
        isUndecorated = true
        defaultCloseOperation = EXIT_ON_CLOSE

        initContentPanel(wallpaper)
        initControllerPanel(wallpaper)
        val cp = contentPane
        cp.add(controllerPanel, BorderLayout.NORTH)
        cp.add(contentPanel, BorderLayout.CENTER)

        pack()
        setLocationRelativeTo(null)
        Toolkit.getDefaultToolkit().sync()
        isVisible = true
    }

    fun initControllerPanel(wallpaper: Wallpaper) {
        controllerPanel.apply {
            preferredSize = Dimension(contentPanel.preferredSize.width, frameHeight * 1 / 10)
            layout = null

            add(downloadButton(this, wallpaper))
            add(returnButton(this))

        }
    }

    fun downloadButton(parentPanel: JPanel, wallpaper: Wallpaper): JButton{
        return controller.gui.getButtonBase().apply {
            text = "download"
            background = Color.GREEN
            val width = if(parentPanel.preferredSize.width > 100) 100 else parentPanel.preferredSize.width
            bounds = Rectangle(parentPanel.preferredSize.width / 2 - width - 20, frameHeight * 1 / 25, width, frameHeight * 2 / 50)
            addActionListener {
                controller.saveWallpaper(wallpaper)
                this@PreviewGUI.dispose()
            }
        }
    }

    fun returnButton(parentPanel: JPanel): JButton{
        return controller.gui.getButtonBase().apply {
            text = "return"
            background = Color.RED
            val width = if(parentPanel.preferredSize.width > 100) 100 else parentPanel.preferredSize.width
            bounds = Rectangle(parentPanel.preferredSize.width / 2 + 20, frameHeight * 1 / 25, width, frameHeight * 2 / 50)
            addActionListener { this@PreviewGUI.dispose() }
        }
    }

    fun initContentPanel(wallpaper: Wallpaper) {
        contentPanel.apply {
            val panelHeight = frameHeight * 9 / 10
            val image = scaleWallpaper(wallpaper, panelHeight)
            preferredSize = Dimension(image.getWidth(null), panelHeight)
            add(JLabel(ImageIcon(image)), BorderLayout.CENTER)
        }
    }

    fun scaleWallpaper (wallpaper: Wallpaper, height: Int): Image {
        val width = wallpaper.size.width / (wallpaper.size.height / height.toFloat())
        return wallpaper.image.getScaledInstance(width.roundToInt(), height, Image.SCALE_SMOOTH)
    }
}