/*
 * Copyright (C) 2009 - 2018 RiFRET developers.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package hu.unideb.med.biophys;

import ij.ImagePlus;
import ij.WindowManager;
import ij.process.FloatProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 *
 */
public class CalculateRatioDialog extends JDialog implements ActionListener {

    private final RiFRET_Plugin mainWindow;
    private ImagePlus firstImg, secondImg;
    private JPanel panel;
    private JButton setFirstImgButton, setSecondImgButton, createRatioImageButton;
    private JCheckBox useMainWindowImages;

    public CalculateRatioDialog(RiFRET_Plugin mainWindow) {
        setTitle("Calculate Ratio of Two Images");
        this.mainWindow = mainWindow;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setModal(false);
        createDialogGui();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(275, 250);
        setLocation((screen.width - getWidth()) / 2, (screen.height - getHeight()) / 2);
    }

    public void createDialogGui() {
        GridBagLayout gridbaglayout = new GridBagLayout();
        GridBagConstraints gc = new GridBagConstraints();
        panel = new JPanel();
        panel.setLayout(gridbaglayout);

        gc.insets = new Insets(2, 2, 6, 2);
        gc.fill = GridBagConstraints.BOTH;
        gc.gridwidth = 2;
        gc.gridx = 0;
        gc.gridy = 0;
        JLabel infoLabel = new JLabel("<html><center>After setting the two images and pressing the \"Create ratio image\" button, the ratio of the images (image 1 / image 2) will be calculated pixel-by-pixel and displayed as a new 32-bit image.</center></html>");
        panel.add(infoLabel, gc);
        gc.insets = new Insets(2, 2, 2, 2);
        gc.gridx = 0;
        gc.gridy = 1;
        useMainWindowImages = new JCheckBox("Use images of the main window (1a/1b)", false);
        useMainWindowImages.setActionCommand("useMainWindowImages");
        useMainWindowImages.addActionListener(this);
        useMainWindowImages.setToolTipText("<html>If this checkbox is checked, donor and transfer channel images<BR>which are set in the main window will be used as image 1 and<BR>image 2.</html>");
        panel.add(useMainWindowImages, gc);
        gc.gridx = 0;
        gc.gridy = 2;
        setFirstImgButton = new JButton("Set first image (numerator)");
        setFirstImgButton.addActionListener(this);
        setFirstImgButton.setActionCommand("setFirstImage");
        panel.add(setFirstImgButton, gc);
        gc.gridx = 0;
        gc.gridy = 3;
        setSecondImgButton = new JButton("Set second image (denominator)");
        setSecondImgButton.addActionListener(this);
        setSecondImgButton.setActionCommand("setSecondImage");
        panel.add(setSecondImgButton, gc);
        gc.gridx = 0;
        gc.gridy = 4;
        createRatioImageButton = new JButton("Create ratio image");
        createRatioImageButton.addActionListener(this);
        createRatioImageButton.setActionCommand("createRatioImage");
        panel.add(createRatioImageButton, gc);

        getContentPane().add(panel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            switch (e.getActionCommand()) {
                case "setFirstImage":
                    firstImg = WindowManager.getCurrentImage();
                    if (firstImg == null) {
                        mainWindow.logError("No image is selected. (Ratio)");
                        return;
                    }
                    if (firstImg.getImageStackSize() > 1) {
                        mainWindow.logError("Current image contains more than 1 channel (" + firstImg.getImageStackSize() + "). Please split it into parts. (Ratio)");
                        firstImg = null;
                        return;
                    } else if (firstImg.getNSlices() > 1) {
                        mainWindow.logError("Current image contains more than 1 slice (" + firstImg.getNSlices() + "). Please split it into parts. (Ratio)");
                        firstImg = null;
                        return;
                    }
                    firstImg.setTitle("Image 1 - " + new Date().toString());
                    new ImageConverter(firstImg).convertToGray32();
                    setFirstImgButton.setBackground(mainWindow.greenColor);
                    setFirstImgButton.setOpaque(true);
                    setFirstImgButton.setBorderPainted(false);
                    break;
                case "setSecondImage":
                    secondImg = WindowManager.getCurrentImage();
                    if (secondImg == null) {
                        mainWindow.logError("No image is selected. (Ratio)");
                        return;
                    }
                    if (secondImg.getImageStackSize() > 1) {
                        mainWindow.logError("Current image contains more than 1 channel (" + secondImg.getImageStackSize() + "). Please split it into parts. (Ratio)");
                        secondImg = null;
                        return;
                    } else if (secondImg.getNSlices() > 1) {
                        mainWindow.logError("Current image contains more than 1 slice (" + secondImg.getNSlices() + "). Please split it into parts. (Ratio)");
                        secondImg = null;
                        return;
                    }
                    secondImg.setTitle("Image 2 - " + new Date().toString());
                    new ImageConverter(secondImg).convertToGray32();
                    setSecondImgButton.setBackground(mainWindow.greenColor);
                    setSecondImgButton.setOpaque(true);
                    setSecondImgButton.setBorderPainted(false);
                    break;
                case "useMainWindowImages":
                    if (useMainWindowImages.isSelected()) {
                        setFirstImgButton.setEnabled(false);
                        setSecondImgButton.setEnabled(false);
                    } else {
                        setFirstImgButton.setEnabled(true);
                        setSecondImgButton.setEnabled(true);
                    }
                    break;
                case "createRatioImage":
                    ImageProcessor ip1 = null;
                    ImageProcessor ip2 = null;
                    if (!useMainWindowImages.isSelected()) {
                        if (firstImg == null) {
                            mainWindow.logError("No image 1 is set. (Ratio)");
                            return;
                        } else if (secondImg == null) {
                            mainWindow.logError("No image 2 is set. (Ratio)");
                            return;
                        }
                        ip1 = firstImg.getProcessor();
                        ip2 = secondImg.getProcessor();
                    } else {
                        if (mainWindow.getDonorInDImage() == null) {
                            mainWindow.logError("No donor channel image is set. (Ratio)");
                            return;
                        } else if (mainWindow.getDonorInAImage() == null) {
                            mainWindow.logError("No transfer channel image is set. (Ratio)");
                            return;
                        }
                        ip1 = mainWindow.getDonorInDImage().getProcessor();
                        ip2 = mainWindow.getDonorInAImage().getProcessor();
                    }
                    float[] ip1P = (float[]) ip1.getPixels();
                    float[] ip2P = (float[]) ip2.getPixels();
                    int width = ip1.getWidth();
                    int height = ip1.getHeight();
                    float[][] ratioImgPoints = new float[width][height];
                    for (int i = 0; i < width; i++) {
                        for (int j = 0; j < height; j++) {
                            ratioImgPoints[i][j] = ip1P[width * j + i] / ip2P[width * j + i];
                        }
                    }
                    FloatProcessor fp = new FloatProcessor(ratioImgPoints);
                    ImagePlus ratioImg = new ImagePlus("Ratio of images", fp);
                    ratioImg.show();
                    break;
                default:
                    break;
            }
        } catch (Throwable t) {
            mainWindow.logException(t.toString(), t);
        }
    }
}
