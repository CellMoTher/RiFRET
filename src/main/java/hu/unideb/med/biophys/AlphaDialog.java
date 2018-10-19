/*
 * Copyright (C) 2009 - 2018 RiFRET Developers.
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

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * 
 */
public class AlphaDialog extends JDialog implements ActionListener{
    private RiFRET_Plugin mainWindow;
    private ImagePlus donorBeforeImg, donorAfterImg;
    private JPanel panel;
    private JCheckBox calculateRatioEps;
    private JCheckBox setEblManually;
    private JButton setDonorBeforeButton, setDonorAfterButton;
    private JButton setDonorBThresholdButton, setDonorAThresholdButton;
    private JButton subtractDonorBButton, subtractDonorAButton;
    private JButton epsilonButton, calculateButton, setButton, resetButton;
    private JTextField i1dField, i2aField, ratioEpsilonsField, eBlField;
    private JTextField ldField, laField, bdField, baField;
    private JLabel alphaResultLabel;

    public AlphaDialog(RiFRET_Plugin mainWindow) {
        setTitle("Alpha factor calculation");
        this.mainWindow = mainWindow;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setModal(false);
        createDialogGui();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(300, 590);
        setLocation((screen.width - getWidth())/2, (screen.height - getHeight())/2);
    }

    public void createDialogGui() {
        GridBagLayout gridbaglayout = new GridBagLayout();
        GridBagConstraints gc = new GridBagConstraints();
        panel = new JPanel();
        panel.setLayout(gridbaglayout);

        gc.insets = new Insets(2,2,6,2);
        gc.fill = GridBagConstraints.BOTH;
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 0;
        JLabel infoLabel = new JLabel("<html><center>Alpha is calculated based on average of images of the donor (I1) and transfer (I2) channel images of donor and acceptor only samples, respectively, as well as on the Ld, La, Bd, Ba and \u03B5d / \u03B5a constants.</center></html>");
        panel.add(infoLabel, gc);
        gc.gridx = 0;
        gc.gridy = 1;
        calculateRatioEps = new JCheckBox("calculate the ratio of epsilons", false);
        calculateRatioEps.setActionCommand("calculateRatioEps");
        calculateRatioEps.addActionListener(this);
        calculateRatioEps.setToolTipText("<html>If this checkbox is checked, the ratio of epsilons is<br>calculated. It requires donor images of the double<br>labeled sample before and after photobleaching<br>the acceptor.</html>");
        panel.add(calculateRatioEps, gc);
        gc.insets = new Insets(2,2,2,2);
        gc.gridwidth = 3;
        gc.gridx = 0;
        gc.gridy = 2;
        setDonorBeforeButton = new JButton("Set donor before photobleaching");
        setDonorBeforeButton.addActionListener(this);
        setDonorBeforeButton.setActionCommand("setDonorBefore");
        setDonorBeforeButton.setEnabled(false);
        panel.add(setDonorBeforeButton, gc);
        gc.gridx = 0;
        gc.gridy = 3;
        setDonorAfterButton = new JButton("Set donor after photobleaching");
        setDonorAfterButton.addActionListener(this);
        setDonorAfterButton.setActionCommand("setDonorAfter");
        setDonorAfterButton.setEnabled(false);
        panel.add(setDonorAfterButton, gc);
        gc.gridx = 0;
        gc.gridy = 4;
        subtractDonorBButton = new JButton("Subtract background of donor before");
        subtractDonorBButton.addActionListener(this);
        subtractDonorBButton.setActionCommand("subtractAlphaDonorBefore");
        subtractDonorBButton.setEnabled(false);
        panel.add(subtractDonorBButton, gc);
        gc.gridx = 0;
        gc.gridy = 5;
        subtractDonorAButton = new JButton("Subtract background of donor after");
        subtractDonorAButton.addActionListener(this);
        subtractDonorAButton.setActionCommand("subtractAlphaDonorAfter");
        subtractDonorAButton.setEnabled(false);
        panel.add(subtractDonorAButton, gc);
        gc.gridx = 0;
        gc.gridy = 6;
        setDonorBThresholdButton = new JButton("Set threshold for donor before");
        setDonorBThresholdButton.addActionListener(this);
        setDonorBThresholdButton.setActionCommand("setAlphaDonorBThreshold");
        setDonorBThresholdButton.setEnabled(false);
        panel.add(setDonorBThresholdButton, gc);
        gc.gridx = 0;
        gc.gridy = 7;
        setDonorAThresholdButton = new JButton("Set threshold for donor after");
        setDonorAThresholdButton.addActionListener(this);
        setDonorAThresholdButton.setActionCommand("setAlphaDonorAThreshold");
        setDonorAThresholdButton.setEnabled(false);
        panel.add(setDonorAThresholdButton, gc);
        gc.gridwidth = 1;
        gc.gridx = 0;
        gc.gridy = 8;
        JLabel i1dLabel = new JLabel("I1(donor)", JLabel.CENTER);
        i1dLabel.setToolTipText("<HTML>The average of fluorescence intensities calculated from<BR>at least of 5-10 images (donor channel of donor only<BR>labeled sample).</HTML>");
        panel.add(i1dLabel, gc);
        gc.gridx = 1;
        gc.gridy = 8;
        i1dField = new JTextField("", 4);
        panel.add(i1dField, gc);
        gc.gridx = 0;
        gc.gridy = 9;
        JLabel i2aLabel = new JLabel("I2(acceptor)", JLabel.CENTER);
        i2aLabel.setToolTipText("<HTML>The average of fluorescence intensities calculated from<BR>at least of 5-10 images (transfer channel of acceptor<BR>only labeled sample).</HTML>");
        panel.add(i2aLabel, gc);
        gc.gridx = 1;
        gc.gridy = 9;
        i2aField = new JTextField("", 4);
        panel.add(i2aField, gc);
        gc.gridx = 0;
        gc.gridy = 10;
        JLabel ldLabel = new JLabel("Ld", JLabel.CENTER);
        ldLabel.setToolTipText("<HTML>The mean number of dye molecules attached to the<BR>donor antibody.</HTML>");
        panel.add(ldLabel, gc);
        gc.gridx = 1;
        gc.gridy = 10;
        ldField = new JTextField("", 4);
        panel.add(ldField, gc);
        gc.gridx = 0;
        gc.gridy = 11;
        JLabel laLabel = new JLabel("La", JLabel.CENTER);
        laLabel.setToolTipText("<HTML>The mean number of dye molecules attached to the<BR>acceptor antibody.</HTML>");
        panel.add(laLabel, gc);
        gc.gridx = 1;
        gc.gridy = 11;
        laField = new JTextField("", 4);
        panel.add(laField, gc);
        gc.gridx = 0;
        gc.gridy = 12;
        JLabel bdLabel = new JLabel("Bd", JLabel.CENTER);
        bdLabel.setToolTipText("<HTML>The mean number of receptors per cell labeled by the<BR>donor antibody.</HTML>");
        panel.add(bdLabel, gc);
        gc.gridx = 1;
        gc.gridy = 12;
        bdField = new JTextField("", 4);
        panel.add(bdField, gc);
        gc.gridx = 0;
        gc.gridy = 13;
        JLabel baLabel = new JLabel("Ba", JLabel.CENTER);
        baLabel.setToolTipText("<HTML>The mean number of receptors per cell labeled by the<BR>acceptor antibody.</HTML>");
        panel.add(baLabel, gc);
        gc.gridx = 1;
        gc.gridy = 13;
        baField = new JTextField("", 4);
        panel.add(baField, gc);
        gc.gridx = 0;
        gc.gridy = 14;
        JLabel lEBlvalue = new JLabel("Ebl", JLabel.CENTER);
        lEBlvalue.setToolTipText("<HTML>FRET efficiency calculated based on the donor<br>before and after photobleching images.</HTML>");
        panel.add(lEBlvalue, gc);
        gc.gridx = 1;
        gc.gridy = 14;
        eBlField = new JTextField("", 4);
        eBlField.setEditable(false);
        panel.add(eBlField, gc);
        gc.gridx = 2;
        gc.gridy = 14;
        setEblManually = new JCheckBox("manual set", false);
        setEblManually.setActionCommand("setEblManually");
        setEblManually.addActionListener(this);
        setEblManually.setToolTipText("<html>Don't check this checkbox unless you are really sure what you are doing.</html>");
        panel.add(setEblManually, gc);
        gc.gridx = 0;
        gc.gridy = 15;
        JLabel lRatio = new JLabel("\u03B5d / \u03B5a", JLabel.CENTER);
        lRatio.setToolTipText("<HTML>Ratio of molar absorption coefficients of the donor and<BR>acceptor dyes (for the wavelength of donor excitation).</HTML>");
        panel.add(lRatio, gc);
        gc.gridx = 1;
        gc.gridy = 15;
        ratioEpsilonsField = new JTextField("", 4);
        panel.add(ratioEpsilonsField, gc);
        gc.gridx = 2;
        gc.gridy = 15;
        epsilonButton = new JButton("Calculate");
        epsilonButton.addActionListener(this);
        epsilonButton.setMargin(new Insets(0,0,0,0));
        epsilonButton.setFont(new Font("Helvetica", Font.BOLD, 10));
        epsilonButton.setActionCommand("epsilonButton");
        epsilonButton.setEnabled(false);
        panel.add(epsilonButton, gc);

        gc.gridx = 0;
        gc.gridy = 16;
        JPanel radioPanel = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gcr = new GridBagConstraints();
        radioPanel.setLayout(gbl);
        gcr.insets = new Insets(0,4,4,4);
        gcr.fill = GridBagConstraints.BOTH;
        JLabel resultLabel = new JLabel("Result (\u03B1):");
        alphaResultLabel = new JLabel("", JLabel.LEFT);
        gcr.gridx = 0;
        gcr.gridy = 0;
        radioPanel.add(resultLabel, gcr);
        gcr.weightx = 10;
        gcr.gridx = 1;
        gcr.gridy = 0;
        radioPanel.add(alphaResultLabel, gcr);
        gc.gridwidth = GridBagConstraints.REMAINDER;
        panel.add(radioPanel, gc);
        gc.gridwidth = 1;
        gc.gridx = 0;
        gc.gridy = 17;
        calculateButton = new JButton("Calculate");
        calculateButton.addActionListener(this);
        calculateButton.setActionCommand("calculate");
        panel.add(calculateButton, gc);
        gc.gridx = 1;
        gc.gridy = 17;
        setButton = new JButton("Set Alpha");
        setButton.addActionListener(this);
        setButton.setActionCommand("setfactor");
        panel.add(setButton, gc);
        gc.gridx = 2;
        gc.gridy = 17;
        resetButton = new JButton("Reset");
        resetButton.addActionListener(this);
        resetButton.setActionCommand("reset");
        panel.add(resetButton, gc);

        getContentPane().add(panel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    	try {
                switch (e.getActionCommand()) {
                    case "reset":
                        donorBeforeImg = null;
                        donorAfterImg = null;
                        setDonorBeforeButton.setBackground(mainWindow.originalButtonColor);
                        setDonorAfterButton.setBackground(mainWindow.originalButtonColor);
                        subtractDonorBButton.setBackground(mainWindow.originalButtonColor);
                        subtractDonorAButton.setBackground(mainWindow.originalButtonColor);
                        setDonorBThresholdButton.setBackground(mainWindow.originalButtonColor);
                        setDonorAThresholdButton.setBackground(mainWindow.originalButtonColor);
                        calculateButton.setBackground(mainWindow.originalButtonColor);
                        setButton.setBackground(mainWindow.originalButtonColor);
                        alphaResultLabel.setText("");
                        break;
                    case "calculateRatioEps":
                        if (calculateRatioEps.isSelected()) {
                            mainWindow.logWarning("S1, S2, S3, S4 factors and thresholded images (Steps 1-4) are required in the main window to calculate the \u03B5d / \u03B5a ratio.");
                            setDonorBeforeButton.setEnabled(true);
                            setDonorAfterButton.setEnabled(true);
                            subtractDonorBButton.setEnabled(true);
                            subtractDonorAButton.setEnabled(true);
                            setDonorBThresholdButton.setEnabled(true);
                            setDonorAThresholdButton.setEnabled(true);
                            ratioEpsilonsField.setEditable(false);
                            epsilonButton.setEnabled(true);
                        } else {
                            setDonorBeforeButton.setEnabled(false);
                            setDonorAfterButton.setEnabled(false);
                            subtractDonorBButton.setEnabled(false);
                            subtractDonorAButton.setEnabled(false);
                            setDonorBThresholdButton.setEnabled(false);
                            setDonorAThresholdButton.setEnabled(false);
                            ratioEpsilonsField.setEditable(true);
                            epsilonButton.setEnabled(false);
                        }   break;
                    case "setEblManually":
                        if (setEblManually.isSelected()) {
                            eBlField.setEditable(true);
                        } else {
                            eBlField.setEditable(false);
                        }   break;
                    case "setDonorBefore":
                        donorBeforeImg = WindowManager.getCurrentImage();
                        if (donorBeforeImg == null) {
                            mainWindow.logError("No image is selected. (\u03B1 calc.)");
                            return;
                        }       if (donorBeforeImg.getImageStackSize() > 1) {
                            mainWindow.logError("Current image contains more than 1 channel ("+donorBeforeImg.getImageStackSize()+"). Please split it into parts. (\u03B1 calc.)");
                            donorBeforeImg = null;
                            return;
                        } else if (donorBeforeImg.getNSlices() > 1) {
                            mainWindow.logError("Current image contains more than 1 slice ("+donorBeforeImg.getNSlices()+"). Please split it into parts. (\u03B1 calc.)");
                            donorBeforeImg = null;
                            return;
                        }       donorBeforeImg.setTitle("Donor before bleaching (\u03B1 calc.) - " + new Date().toString());
                        new ImageConverter(donorBeforeImg).convertToGray32();
                        setDonorBeforeButton.setBackground(mainWindow.greenColor);
                        break;
                    case "setDonorAfter":
                        donorAfterImg = WindowManager.getCurrentImage();
                        if (donorAfterImg == null) {
                            mainWindow.logError("No image is selected. (\u03B1 calc.)");
                            return;
                        }       if (donorAfterImg.getImageStackSize() > 1) {
                            mainWindow.logError("Current image contains more than 1 channel ("+donorAfterImg.getImageStackSize()+"). Please split it into parts. (\u03B1 calc.)");
                            donorAfterImg = null;
                            return;
                        } else if (donorAfterImg.getNSlices() > 1) {
                            mainWindow.logError("Current image contains more than 1 slice ("+donorAfterImg.getNSlices()+"). Please split it into parts. (\u03B1 calc.)");
                            donorAfterImg = null;
                            return;
                        }       donorAfterImg.setTitle("Donor after bleaching (\u03B1 calc.) - " + new Date().toString());
                        new ImageConverter(donorAfterImg).convertToGray32();
                        setDonorAfterButton.setBackground(mainWindow.greenColor);
                        break;
                    case "subtractAlphaDonorBefore":
                        {
                            if (donorBeforeImg == null) {
                                mainWindow.logError("No image is set as donor before bleaching. (\u03B1 calc.)");
                                return;
                            } else if (donorBeforeImg.getRoi() == null) {
                                mainWindow.logError("No ROI is defined for donor before bleaching. (\u03B1 calc.)");
                                return;
                            }           ImageProcessor ipT = donorBeforeImg.getProcessor();
                            int width = donorBeforeImg.getWidth();
                            int height = donorBeforeImg.getHeight();
                            double sum = 0;
                            int count = 0;
                            for (int i=0; i<width; i++) {
                                for (int j=0; j<height; j++) {
                                    if (donorBeforeImg.getRoi().contains(i, j)) {
                                        sum += ipT.getPixelValue(i,j);
                                        count++;
                                    }
                                }
                            }   float backgroundAvgT = (float)(sum/count);
                            float value = 0;
                            for (int x=0; x < width; x++) {
                                for (int y=0; y < height; y++) {
                                    value = ipT.getPixelValue(x,y);
                                    value = value - backgroundAvgT;
                                    ipT.putPixelValue(x, y, value);
                                }
                            }   donorBeforeImg.updateAndDraw();
                            donorBeforeImg.killRoi();
                            mainWindow.log("Subtracted background ("+backgroundAvgT+") of donor before bleaching. (\u03B1 calc.)");
                            subtractDonorBButton.setBackground(mainWindow.greenColor);
                            break;
                        }
                    case "subtractAlphaDonorAfter":
                        {
                            if (donorAfterImg == null) {
                                mainWindow.logError("No image is set as donor after bleaching. (\u03B1 calc.)");
                                return;
                            } else if (donorAfterImg.getRoi() == null) {
                                mainWindow.logError("No ROI is defined for donor after bleaching. (\u03B1 calc.)");
                                return;
                            }           ImageProcessor ipA = donorAfterImg.getProcessor();
                            int width = donorAfterImg.getWidth();
                            int height = donorAfterImg.getHeight();
                            double sum = 0;
                            int count = 0;
                            for (int i=0; i<width; i++) {
                                for (int j=0; j<height; j++) {
                                    if (donorAfterImg.getRoi().contains(i, j)) {
                                        sum += ipA.getPixelValue(i,j);
                                        count++;
                                    }
                                }
                            }   float backgroundAvgA = (float)(sum/count);
                            float value = 0;
                            for (int x=0; x < width; x++) {
                                for (int y=0; y < height; y++) {
                                    value = ipA.getPixelValue(x,y);
                                    value = value - backgroundAvgA;
                                    ipA.putPixelValue(x, y, value);
                                }
                            }   donorAfterImg.updateAndDraw();
                            donorAfterImg.killRoi();
                            mainWindow.log("Subtracted background ("+backgroundAvgA+") of donor after bleaching. (\u03B1 calc.)");
                            subtractDonorAButton.setBackground(mainWindow.greenColor);
                            break;
                        }
                    case "setAlphaDonorBThreshold":
                        if (donorBeforeImg == null) {
                            mainWindow.logError("No image is set as donor before bleaching. (\u03B1 calc.)");
                            return;
                        }       IJ.selectWindow(donorBeforeImg.getTitle());
                        IJ.run("Threshold...");
                        setDonorBThresholdButton.setBackground(mainWindow.greenColor);
                        break;
                    case "setAlphaDonorAThreshold":
                        if (donorAfterImg == null) {
                            mainWindow.logError("No image is set as donor after bleaching. (\u03B1 calc.)");
                            return;
                        }       IJ.selectWindow(donorAfterImg.getTitle());
                        IJ.run("Threshold...");
                        setDonorAThresholdButton.setBackground(mainWindow.greenColor);
                        break;
                    case "epsilonButton":
                        DecimalFormat df = new DecimalFormat("#.###");
                        if(!setEblManually.isSelected()) {
                            if (donorBeforeImg == null) {
                                mainWindow.logError("No image is set as donor before bleaching. (\u03B1 calc.)");
                                return;
                            } else if (donorAfterImg == null) {
                                mainWindow.logError("No image is set as donor in after bleaching. (\u03B1 calc.)");
                                return;
                            }
                        } else {
                            if (eBlField.getText().trim().isEmpty()) {
                                mainWindow.logError("Ebl is not given. (\u03B1 calc.)");
                                return;
                            }
                        }   float s1 = mainWindow.getS1Factor();
                        if (s1 < 0) {
                            mainWindow.logError("Constant S1 is not given. (\u03B1 calc.)");
                            return;
                        }       float s2 = mainWindow.getS2Factor();
                        if (s2 < 0) {
                            mainWindow.logError("Constant S2 is not given. (\u03B1 calc.)");
                            return;
                        }       float s3 = mainWindow.getS3Factor();
                        if (s3 < 0) {
                            mainWindow.logError("Constant S3 is not given. (\u03B1 calc.)");
                            return;
                        }       float s4 = mainWindow.getS4Factor();
                        if (s4 < 0) {
                            mainWindow.logError("Constant S4 is not given. (\u03B1 calc.)");
                            return;
                        }       ImagePlus ddImage = mainWindow.getDonorInDImage();
                        if (ddImage == null) {
                            mainWindow.logError("Donor channel image is not given. (\u03B1 calc.)");
                            return;
                        }       ImagePlus dtImage = mainWindow.getDonorInAImage();
                        if (dtImage == null) {
                            mainWindow.logError("Transfer channel image is not given. (\u03B1 calc.)");
                            return;
                        }       ImagePlus aaImage = mainWindow.getAcceptorInAImage();
                        if (aaImage == null) {
                            mainWindow.logError("Acceptor channel image is not given. (\u03B1 calc.)");
                            return;
                        }       double sumDD = 0;
                        int countDD = 0;
                        ImageProcessor ipDD = ddImage.getStack().getProcessor(ddImage.getCurrentSlice());
                        for (int i=0; i<ddImage.getWidth(); i++) {
                            for (int j=0; j<ddImage.getHeight(); j++) {
                                if (!Float.isNaN(ipDD.getPixelValue(i, j))) {
                                    sumDD += ipDD.getPixelValue(i,j);
                                    countDD++;
                                }
                            }
                        }
                        float avgDD = (float)(sumDD/countDD);
                        double sumDT = 0;
                        int countDT = 0;
                        ImageProcessor ipDT = dtImage.getStack().getProcessor(dtImage.getCurrentSlice());
                        for (int i=0; i<dtImage.getWidth(); i++) {
                            for (int j=0; j<dtImage.getHeight(); j++) {
                                if (!Float.isNaN(ipDT.getPixelValue(i, j))) {
                                    sumDT += ipDT.getPixelValue(i,j);
                                    countDT++;
                                }
                            }
                        }
                        float avgDT = (float)(sumDT/countDT);
                        double sumAA = 0;
                        int countAA = 0;
                        ImageProcessor ipAA = aaImage.getStack().getProcessor(aaImage.getCurrentSlice());
                        for (int i=0; i<aaImage.getWidth(); i++) {
                            for (int j=0; j<aaImage.getHeight(); j++) {
                                if (!Float.isNaN(ipAA.getPixelValue(i, j))) {
                                    sumAA += ipAA.getPixelValue(i,j);
                                    countAA++;
                                }
                            }
                        }
                        float avgAA = (float)(sumAA/countAA);
                        float ebl = 0;
                        if(!setEblManually.isSelected()) {
                            double sumDBefore = 0;
                            int countDBefore = 0;
                            ImageProcessor ipDBefore = donorBeforeImg.getProcessor();
                            for (int i=0; i<donorBeforeImg.getWidth(); i++) {
                                for (int j=0; j<donorBeforeImg.getHeight(); j++) {
                                    if (!Float.isNaN(ipDBefore.getPixelValue(i, j))) {
                                        sumDBefore += ipDBefore.getPixelValue(i,j);
                                        countDBefore++;
                                    }
                                }
                            }
                            float avgDBefore = (float)(sumDBefore/countDBefore);
                            
                            double sumDAfter = 0;
                            int countDAfter = 0;
                            ImageProcessor ipDAfter = donorAfterImg.getProcessor();
                            for (int i=0; i<donorAfterImg.getWidth(); i++) {
                                for (int j=0; j<donorAfterImg.getHeight(); j++) {
                                    if (!Float.isNaN(ipDAfter.getPixelValue(i, j))) {
                                        sumDAfter += ipDAfter.getPixelValue(i,j);
                                        countDAfter++;
                                    }
                                }
                            }
                            float avgDAfter = (float)(sumDAfter/countDAfter);
                            
                            ebl = (float)(((double)avgDAfter-(((double)avgDBefore-(double)s4*(double)avgDT)/((double)1-(double)s1*(double)s4)))/(double)avgDAfter);
                            eBlField.setText(df.format(ebl).toString());
                        } else {
                            ebl = Float.parseFloat(eBlField.getText().trim());
                            eBlField.setText(df.format(ebl).toString());
                        }       float eRatio = (float)(((double)avgDT - (double)s1*(double)avgDD - ((double)1-(double)s1*(double)s4)*(double)s2*(double)avgAA)/(((double)1-(double)s1*(double)s4)*(double)s2*(double)avgAA*(double)ebl));
                        ratioEpsilonsField.setText(df.format(eRatio).toString());
                        break;
                    case "calculate":
                        if (i1dField.getText().trim().isEmpty()) {
                            mainWindow.logError("Constant I1(donor) is not given. (\u03B1 calc.)");
                            return;
                        } else if (i2aField.getText().trim().isEmpty()) {
                            mainWindow.logError("Constant I2(acceptor) is not given. (\u03B1 calc.)");
                            return;
                        } else if (ldField.getText().trim().isEmpty()) {
                            mainWindow.logError("Constant Ld is not given. (\u03B1 calc.)");
                            return;
                        } else if (laField.getText().trim().isEmpty()) {
                            mainWindow.logError("Constant La is not given. (\u03B1 calc.)");
                            return;
                        } else if (bdField.getText().trim().isEmpty()) {
                            mainWindow.logError("Constant Bd is not given. (\u03B1 calc.)");
                            return;
                        } else if (baField.getText().trim().isEmpty()) {
                            mainWindow.logError("Constant Ba is not given. (\u03B1 calc.)");
                            return;
                        } else if (ratioEpsilonsField.getText().trim().isEmpty()) {
                            mainWindow.logError("Ratio \u03B5d / \u03B5a is not given. (\u03B1 calc.)");
                            return;
                        } else {
                            df = new DecimalFormat("#.###");
                            float i1 = Float.parseFloat(i1dField.getText().trim());
                            float i2 = Float.parseFloat(i2aField.getText().trim());
                            float ld = Float.parseFloat(ldField.getText().trim());
                            float la = Float.parseFloat(laField.getText().trim());
                            float bd = Float.parseFloat(bdField.getText().trim());
                            float ba = Float.parseFloat(baField.getText().trim());
                            float er = Float.parseFloat(ratioEpsilonsField.getText().trim());
                            float alpha = i2*ld*bd*er/(i1*la*ba);
                            alphaResultLabel.setText(df.format(alpha).toString());
                            calculateButton.setBackground(mainWindow.greenColor);
                        }       break;
                    case "setfactor":
                        if (alphaResultLabel.getText().isEmpty()) {
                            mainWindow.logError("\u03B1 has to be calculated before setting it. (\u03B1 calc.)");
                            return;
                        }       mainWindow.setAlphaFactor(alphaResultLabel.getText());
                        setButton.setBackground(mainWindow.greenColor);
                        mainWindow.calculateAlphaButton.setBackground(mainWindow.greenColor);
                        break;
                    default:
                        break;
                }
        } catch (Throwable t) {
            mainWindow.logException(t.toString(), t);
        }
    }
}
