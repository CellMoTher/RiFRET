/*-
 * #%L
 * an ImageJ plugin for intensity-based three-filter set FRET.
 * %%
 * Copyright (C) 2009 - 2023 RiFRET developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package hu.unideb.med.biophys;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.process.FloatProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class S1S3S5Dialog extends JDialog implements ActionListener {

    private final RiFRET_Plugin mainWindow;
    private ImagePlus donorImg;
    private ImagePlus transferImg;
    private ImagePlus acceptorImg;
    private ImagePlus autofluorescenceImg;
    private ImageStack donorImgSave = null;
    private ImageStack transferImgSave = null;
    private ImageStack acceptorImgSave = null;
    private ImageStack autofluorescenceImgSave = null;
    private JPanel panel;
    private JButton setDonorButton;
    private JButton setTransferButton;
    private JButton setAcceptorButton;
    private JButton setAutofluorescenceButton;
    private JButton setDonorThresholdButton;
    private JButton setTransferThresholdButton;
    private JButton setAcceptorThresholdButton;
    private JButton setAutofluorescenceThresholdButton;
    private JButton resetDonorThresholdButton;
    private JButton resetTransferThresholdButton;
    private JButton resetAcceptorThresholdButton;
    private JButton resetAutofluorescenceThresholdButton;
    private JButton copyRoiButton;
    private JButton subtractDonorButton;
    private JButton subtractTransferButton;
    private JButton subtractAcceptorButton;
    private JButton subtractAutofluorescenceButton;
    private JButton calculateButton;
    private JButton setButton;
    private JButton resetButton;
    private JCheckBox showSImagesCB;
    private JLabel s1ResultLabel;
    private JLabel s3ResultLabel;
    private JLabel s5ResultLabel;
    private JTextField autoflDInDField;
    private JTextField autoflAInDField;
    private JTextField autoflAInAField;
    private JTextField autoflAFField;
    private final DateTimeFormatter dateTimeFormat;

    public S1S3S5Dialog(RiFRET_Plugin mainWindow) {
        setTitle("S1/S3/S5 Factor Calculation");
        this.mainWindow = mainWindow;
        dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setModal(false);
        createDialogGui();
        if (IJ.isMacOSX()) {
            setSize(360, 610);
        } else {
            setSize(330, 590);
        }
        setLocationRelativeTo(null);
    }

    public void createDialogGui() {
        GridBagLayout gridbaglayout = new GridBagLayout();
        GridBagConstraints gc = new GridBagConstraints();
        panel = new JPanel();
        panel.setLayout(gridbaglayout);

        gc.insets = new Insets(2, 2, 6, 2);
        gc.fill = GridBagConstraints.BOTH;
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 0;
        JLabel infoLabel = new JLabel("<html><center>S1, S3 and S5 are calculated based on images of the donor, transfer, acceptor and autofluorescence channels of a donor only labeled sample.</center></html>");
        panel.add(infoLabel, gc);

        gc.insets = new Insets(2, 2, 2, 2);
        gc.gridwidth = 3;
        gc.gridx = 0;
        gc.gridy = 1;
        setDonorButton = new JButton("Set donor channel image");
        setDonorButton.addActionListener(this);
        setDonorButton.setActionCommand("setS1S3S5Donor");
        panel.add(setDonorButton, gc);

        gc.gridx = 0;
        gc.gridy = 2;
        setTransferButton = new JButton("Set transfer channel image");
        setTransferButton.addActionListener(this);
        setTransferButton.setActionCommand("setS1S3S5Transfer");
        panel.add(setTransferButton, gc);

        gc.gridx = 0;
        gc.gridy = 3;
        setAcceptorButton = new JButton("Set acceptor channel image");
        setAcceptorButton.addActionListener(this);
        setAcceptorButton.setActionCommand("setS1S3S5Acceptor");
        panel.add(setAcceptorButton, gc);

        gc.gridx = 0;
        gc.gridy = 4;
        setAutofluorescenceButton = new JButton("Set autofluorescence channel image");
        setAutofluorescenceButton.addActionListener(this);
        setAutofluorescenceButton.setActionCommand("setS1S3S5Autofluorescence");
        panel.add(setAutofluorescenceButton, gc);

        gc.gridx = 0;
        gc.gridy = 5;
        copyRoiButton = new JButton("(Optional:) Copy background ROI");
        copyRoiButton.addActionListener(this);
        copyRoiButton.setActionCommand("copyS1S3S5Roi");
        panel.add(copyRoiButton, gc);

        gc.gridwidth = 2;
        gc.gridx = 0;
        gc.gridy = 6;
        JLabel subtractionLabel = new JLabel("Subtract bg. (avg. of ROI or constant):");
        panel.add(subtractionLabel, gc);

        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 2;
        gc.gridy = 6;
        JLabel autofluorescenceLabel = new JLabel("Constant:");
        panel.add(autofluorescenceLabel, gc);

        gc.gridwidth = 2;
        gc.gridx = 0;
        gc.gridy = 7;
        subtractDonorButton = new JButton("Subtract from donor channel");
        subtractDonorButton.addActionListener(this);
        subtractDonorButton.setActionCommand("subtractS1S3S5Donor");
        panel.add(subtractDonorButton, gc);

        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 2;
        gc.gridy = 7;
        autoflDInDField = new JTextField("0", 5);
        autoflDInDField.setHorizontalAlignment(JTextField.RIGHT);
        panel.add(autoflDInDField, gc);

        gc.gridwidth = 2;
        gc.gridx = 0;
        gc.gridy = 8;
        subtractTransferButton = new JButton("Subtract from transfer channel");
        subtractTransferButton.addActionListener(this);
        subtractTransferButton.setActionCommand("subtractS1S3S5Transfer");
        panel.add(subtractTransferButton, gc);

        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 2;
        gc.gridy = 8;
        autoflAInDField = new JTextField("0", 5);
        autoflAInDField.setHorizontalAlignment(JTextField.RIGHT);
        panel.add(autoflAInDField, gc);

        gc.gridwidth = 2;
        gc.gridx = 0;
        gc.gridy = 9;
        subtractAcceptorButton = new JButton("Subtract from acceptor channel");
        subtractAcceptorButton.addActionListener(this);
        subtractAcceptorButton.setActionCommand("subtractS1S3S5Acceptor");
        panel.add(subtractAcceptorButton, gc);

        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 2;
        gc.gridy = 9;
        autoflAInAField = new JTextField("0", 5);
        autoflAInAField.setHorizontalAlignment(JTextField.RIGHT);
        panel.add(autoflAInAField, gc);

        gc.gridwidth = 2;
        gc.gridx = 0;
        gc.gridy = 10;
        subtractAutofluorescenceButton = new JButton("Subtract from autofluor. channel");
        subtractAutofluorescenceButton.addActionListener(this);
        subtractAutofluorescenceButton.setActionCommand("subtractS1S3S5Autofluorescence");
        panel.add(subtractAutofluorescenceButton, gc);

        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 2;
        gc.gridy = 10;
        autoflAFField = new JTextField("0", 5);
        autoflAFField.setHorizontalAlignment(JTextField.RIGHT);
        panel.add(autoflAFField, gc);

        gc.gridwidth = 2;
        gc.gridx = 0;
        gc.gridy = 11;
        setDonorThresholdButton = new JButton("Set threshold for donor channel");
        setDonorThresholdButton.addActionListener(this);
        setDonorThresholdButton.setActionCommand("setS1S3S5DonorThreshold");
        panel.add(setDonorThresholdButton, gc);
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 2;
        gc.gridy = 11;
        resetDonorThresholdButton = new JButton("Reset");
        resetDonorThresholdButton.setToolTipText("Resets threshold settings for donor channel");
        resetDonorThresholdButton.addActionListener(this);
        resetDonorThresholdButton.setActionCommand("resetS1S3S5DonorThreshold");
        panel.add(resetDonorThresholdButton, gc);

        gc.gridwidth = 2;
        gc.gridx = 0;
        gc.gridy = 12;
        setTransferThresholdButton = new JButton("Set threshold for transfer channel");
        setTransferThresholdButton.addActionListener(this);
        setTransferThresholdButton.setActionCommand("setS1S3S5TransferThreshold");
        panel.add(setTransferThresholdButton, gc);
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 2;
        gc.gridy = 12;
        resetTransferThresholdButton = new JButton("Reset");
        resetTransferThresholdButton.setToolTipText("Resets threshold settings for transfer channel");
        resetTransferThresholdButton.addActionListener(this);
        resetTransferThresholdButton.setActionCommand("resetS1S3S5TransferThreshold");
        panel.add(resetTransferThresholdButton, gc);

        gc.gridwidth = 2;
        gc.gridx = 0;
        gc.gridy = 13;
        setAcceptorThresholdButton = new JButton("Set threshold for acceptor channel");
        setAcceptorThresholdButton.addActionListener(this);
        setAcceptorThresholdButton.setActionCommand("setS1S3S5AcceptorThreshold");
        panel.add(setAcceptorThresholdButton, gc);
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 2;
        gc.gridy = 13;
        resetAcceptorThresholdButton = new JButton("Reset");
        resetAcceptorThresholdButton.setToolTipText("Resets threshold settings for acceptor channel");
        resetAcceptorThresholdButton.addActionListener(this);
        resetAcceptorThresholdButton.setActionCommand("resetS1S3S5AcceptorThreshold");
        panel.add(resetAcceptorThresholdButton, gc);

        gc.gridwidth = 2;
        gc.gridx = 0;
        gc.gridy = 14;
        setAutofluorescenceThresholdButton = new JButton("Set threshold for autofluor. channel");
        setAutofluorescenceThresholdButton.addActionListener(this);
        setAutofluorescenceThresholdButton.setActionCommand("setS1S3S5AutofluorescenceThreshold");
        panel.add(setAutofluorescenceThresholdButton, gc);
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 2;
        gc.gridy = 14;
        resetAutofluorescenceThresholdButton = new JButton("Reset");
        resetAutofluorescenceThresholdButton.setToolTipText("Resets threshold settings for autofluorescence channel");
        resetAutofluorescenceThresholdButton.addActionListener(this);
        resetAutofluorescenceThresholdButton.setActionCommand("resetS1S3S5AutofluorescenceThreshold");
        panel.add(resetAutofluorescenceThresholdButton, gc);

        gc.gridx = 0;
        gc.gridy = 15;
        JPanel radioPanel = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gcr = new GridBagConstraints();
        radioPanel.setLayout(gbl);
        gcr.insets = new Insets(0, 4, 4, 4);
        gcr.fill = GridBagConstraints.BOTH;
        JLabel resultLabel = new JLabel("Results (S1 S3 S5):");
        s1ResultLabel = new JLabel("", JLabel.CENTER);
        s3ResultLabel = new JLabel("", JLabel.CENTER);
        s5ResultLabel = new JLabel("", JLabel.CENTER);
        gcr.gridx = 0;
        gcr.gridy = 0;
        radioPanel.add(resultLabel, gcr);
        gcr.weightx = 10;
        gcr.gridx = 1;
        gcr.gridy = 0;
        radioPanel.add(s1ResultLabel, gcr);
        gcr.gridx = 2;
        gcr.gridy = 0;
        radioPanel.add(s3ResultLabel, gcr);
        gcr.gridx = 3;
        gcr.gridy = 0;
        radioPanel.add(s5ResultLabel, gcr);
        panel.add(radioPanel, gc);

        gc.gridx = 0;
        gc.gridy = 16;
        showSImagesCB = new JCheckBox("Show S1, S3 and S5 images (for manual calc.)");
        panel.add(showSImagesCB, gc);

        gc.gridwidth = 1;
        gc.gridx = 0;
        gc.gridy = 17;
        calculateButton = new JButton("Calculate");
        calculateButton.addActionListener(this);
        calculateButton.setActionCommand("calculate");
        panel.add(calculateButton, gc);

        gc.gridx = 1;
        gc.gridy = 17;
        setButton = new JButton("Set S1, S3 and S5");
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
                    donorImg = null;
                    transferImg = null;
                    acceptorImg = null;
                    autofluorescenceImg = null;
                    setDonorButton.setBackground(mainWindow.originalButtonColor);
                    setDonorButton.setOpaque(false);
                    setDonorButton.setBorderPainted(true);
                    setTransferButton.setBackground(mainWindow.originalButtonColor);
                    setTransferButton.setOpaque(false);
                    setTransferButton.setBorderPainted(true);
                    setAcceptorButton.setBackground(mainWindow.originalButtonColor);
                    setAcceptorButton.setOpaque(false);
                    setAcceptorButton.setBorderPainted(true);
                    setAutofluorescenceButton.setBackground(mainWindow.originalButtonColor);
                    setAutofluorescenceButton.setOpaque(false);
                    setAutofluorescenceButton.setBorderPainted(true);
                    copyRoiButton.setBackground(mainWindow.originalButtonColor);
                    copyRoiButton.setOpaque(false);
                    copyRoiButton.setBorderPainted(true);
                    subtractDonorButton.setBackground(mainWindow.originalButtonColor);
                    subtractDonorButton.setOpaque(false);
                    subtractDonorButton.setBorderPainted(true);
                    subtractTransferButton.setBackground(mainWindow.originalButtonColor);
                    subtractTransferButton.setOpaque(false);
                    subtractTransferButton.setBorderPainted(true);
                    subtractAcceptorButton.setBackground(mainWindow.originalButtonColor);
                    subtractAcceptorButton.setOpaque(false);
                    subtractAcceptorButton.setBorderPainted(true);
                    subtractAutofluorescenceButton.setBackground(mainWindow.originalButtonColor);
                    subtractAutofluorescenceButton.setOpaque(false);
                    subtractAutofluorescenceButton.setBorderPainted(true);
                    setDonorThresholdButton.setBackground(mainWindow.originalButtonColor);
                    setDonorThresholdButton.setOpaque(false);
                    setDonorThresholdButton.setBorderPainted(true);
                    setTransferThresholdButton.setBackground(mainWindow.originalButtonColor);
                    setTransferThresholdButton.setOpaque(false);
                    setTransferThresholdButton.setBorderPainted(true);
                    setAcceptorThresholdButton.setBackground(mainWindow.originalButtonColor);
                    setAcceptorThresholdButton.setOpaque(false);
                    setAcceptorThresholdButton.setBorderPainted(true);
                    setAutofluorescenceThresholdButton.setBackground(mainWindow.originalButtonColor);
                    setAutofluorescenceThresholdButton.setOpaque(false);
                    setAutofluorescenceThresholdButton.setBorderPainted(true);
                    calculateButton.setBackground(mainWindow.originalButtonColor);
                    calculateButton.setOpaque(false);
                    calculateButton.setBorderPainted(true);
                    setButton.setBackground(mainWindow.originalButtonColor);
                    setButton.setOpaque(false);
                    setButton.setBorderPainted(true);
                    s1ResultLabel.setText("");
                    s3ResultLabel.setText("");
                    s5ResultLabel.setText("");
                    break;
                case "setS1S3S5Donor":
                    donorImg = WindowManager.getCurrentImage();
                    if (donorImg == null) {
                        mainWindow.logError("No image is selected. (S1/S3/S5 calc.)");
                        return;
                    }
                    if (donorImg.getImageStackSize() > 1) {
                        mainWindow.logError("Current image contains more than 1 channel (" + donorImg.getImageStackSize() + "). Please split it into parts. (S1/S3/S5 calc.)");
                        donorImg = null;
                        return;
                    } else if (donorImg.getNSlices() > 1) {
                        mainWindow.logError("Current image contains more than 1 slice (" + donorImg.getNSlices() + "). Please split it into parts. (S1/S3/S5 calc.)");
                        donorImg = null;
                        return;
                    }
                    mainWindow.log("Set " + donorImg.getTitle() + " as donor channel. (S1/S3/S5 calc.)");
                    donorImg.setTitle("Donor channel (S1/S3/S5 calc.) - " + dateTimeFormat.format(OffsetDateTime.now()));
                    new ImageConverter(donorImg).convertToGray32();
                    setDonorButton.setBackground(mainWindow.greenColor);
                    setDonorButton.setOpaque(true);
                    setDonorButton.setBorderPainted(false);
                    break;
                case "setS1S3S5Transfer":
                    transferImg = WindowManager.getCurrentImage();
                    if (transferImg == null) {
                        mainWindow.logError("No image is selected. (S1/S3/S5 calc.)");
                        return;
                    }
                    if (transferImg.getImageStackSize() > 1) {
                        mainWindow.logError("Current image contains more than 1 channel (" + transferImg.getImageStackSize() + "). Please split it into parts. (S1/S3/S5 calc.)");
                        transferImg = null;
                        return;
                    } else if (transferImg.getNSlices() > 1) {
                        mainWindow.logError("Current image contains more than 1 slice (" + transferImg.getNSlices() + "). Please split it into parts. (S1/S3/S5 calc.)");
                        transferImg = null;
                        return;
                    }
                    mainWindow.log("Set " + transferImg.getTitle() + " as transfer channel. (S1/S3/S5 calc.)");
                    transferImg.setTitle("Transfer channel (S1/S3/S5 calc.) - " + dateTimeFormat.format(OffsetDateTime.now()));
                    new ImageConverter(transferImg).convertToGray32();
                    setTransferButton.setBackground(mainWindow.greenColor);
                    setTransferButton.setOpaque(true);
                    setTransferButton.setBorderPainted(false);
                    break;
                case "setS1S3S5Acceptor":
                    acceptorImg = WindowManager.getCurrentImage();
                    if (acceptorImg == null) {
                        mainWindow.logError("No image is selected. (S1/S3/S5 calc.)");
                        return;
                    }
                    if (acceptorImg.getImageStackSize() > 1) {
                        mainWindow.logError("Current image contains more than 1 channel (" + acceptorImg.getImageStackSize() + "). Please split it into parts. (S1/S3/S5 calc.)");
                        acceptorImg = null;
                        return;
                    } else if (acceptorImg.getNSlices() > 1) {
                        mainWindow.logError("Current image contains more than 1 slice (" + acceptorImg.getNSlices() + "). Please split it into parts. (S1/S3/S5 calc.)");
                        acceptorImg = null;
                        return;
                    }
                    mainWindow.log("Set " + acceptorImg.getTitle() + " as acceptor channel. (S1/S3/S5 calc.)");
                    acceptorImg.setTitle("Acceptor channel (S1/S3/S5 calc.) - " + dateTimeFormat.format(OffsetDateTime.now()));
                    new ImageConverter(acceptorImg).convertToGray32();
                    setAcceptorButton.setBackground(mainWindow.greenColor);
                    setAcceptorButton.setOpaque(true);
                    setAcceptorButton.setBorderPainted(false);
                    break;
                case "setS1S3S5Autofluorescence":
                    autofluorescenceImg = WindowManager.getCurrentImage();
                    if (autofluorescenceImg == null) {
                        mainWindow.logError("No image is selected. (S1/S3/S5 calc.)");
                        return;
                    }
                    if (autofluorescenceImg.getImageStackSize() > 1) {
                        mainWindow.logError("Current image contains more than 1 channel (" + autofluorescenceImg.getImageStackSize() + "). Please split it into parts. (S1/S3/S5 calc.)");
                        autofluorescenceImg = null;
                        return;
                    } else if (autofluorescenceImg.getNSlices() > 1) {
                        mainWindow.logError("Current image contains more than 1 slice (" + autofluorescenceImg.getNSlices() + "). Please split it into parts. (S1/S3/S5 calc.)");
                        autofluorescenceImg = null;
                        return;
                    }
                    mainWindow.log("Set " + autofluorescenceImg.getTitle() + " as autofluorescence channel. (S1/S3/S5 calc.)");
                    autofluorescenceImg.setTitle("Autofluorescence channel (S1/S3/S5 calc.) - " + dateTimeFormat.format(OffsetDateTime.now()));
                    new ImageConverter(autofluorescenceImg).convertToGray32();
                    setAutofluorescenceButton.setBackground(mainWindow.greenColor);
                    setAutofluorescenceButton.setOpaque(true);
                    setAutofluorescenceButton.setBorderPainted(false);
                    break;
                case "subtractS1S3S5Donor": {
                    if (donorImg == null) {
                        mainWindow.logError("No image is set as donor channel. (S1/S3/S5 calc.)");
                    } else if (autoflDInDField.getText().trim().isEmpty()) {
                        autoflDInDField.setText("0");
                    } else if (donorImg.getRoi() == null && autoflDInDField.getText().trim().equals("0")) {
                        mainWindow.logError("No ROI or constant is defined for donor channel. (S1/S3/S5 calc.)");
                        // if autofluorescence value other than zero is entered, and no ROI is selected
                    } else if (donorImg.getRoi() == null && !autoflDInDField.getText().trim().equals("0")) {
                        float autofl = 0;
                        if (!autoflDInDField.getText().trim().isEmpty()) {
                            autofl = Float.parseFloat(autoflDInDField.getText().trim());
                        }
                        DecimalFormat df = new DecimalFormat("#.#");
                        ImageProcessor ipD = donorImg.getProcessor();
                        int width = donorImg.getWidth();
                        int height = donorImg.getHeight();
                        donorImgSave = new ImageStack(ipD.getWidth(), ipD.getHeight());
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                float value = ipD.getPixelValue(x, y);
                                value -= autofl;
                                ipD.putPixelValue(x, y, value);
                            }
                        }
                        FloatProcessor flp = new FloatProcessor(ipD.getWidth(), ipD.getHeight());
                        flp.setPixels(1, (FloatProcessor) ipD.duplicate());
                        donorImgSave.addSlice("" + 1, flp);
                        donorImg.updateAndDraw();
                        donorImgSave.setColorModel(ipD.getColorModel());
                        mainWindow.log("Subtracted background (constant: " + df.format(autofl) + ") of donor channel. (S1/S3/S5 calc.)");
                        subtractDonorButton.setBackground(mainWindow.greenColor);
                        subtractDonorButton.setOpaque(true);
                        subtractDonorButton.setBorderPainted(false);
                    } else if (donorImg.getRoi() != null) { // if donor channel ROI is selected
                        float autofl = 0;
                        if (!autoflDInDField.getText().trim().isEmpty()) {
                            autofl = Float.parseFloat(autoflDInDField.getText().trim());
                        }
                        DecimalFormat df = new DecimalFormat("#.#");
                        ImageProcessor ipD = donorImg.getProcessor();
                        int width = donorImg.getWidth();
                        int height = donorImg.getHeight();
                        donorImgSave = new ImageStack(ipD.getWidth(), ipD.getHeight());
                        double sum = 0;
                        int count = 0;
                        for (int i = 0; i < width; i++) {
                            for (int j = 0; j < height; j++) {
                                if (donorImg.getRoi().contains(i, j)) {
                                    sum += ipD.getPixelValue(i, j);
                                    count++;
                                }
                            }
                        }
                        float backgroundAvgD = (float) (sum / count);
                        backgroundAvgD += autofl;
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                float value = ipD.getPixelValue(x, y);
                                value -= backgroundAvgD;
                                ipD.putPixelValue(x, y, value);
                            }
                        }
                        FloatProcessor flp = new FloatProcessor(ipD.getWidth(), ipD.getHeight());
                        flp.setPixels(1, (FloatProcessor) ipD.duplicate());
                        donorImgSave.addSlice("" + 1, flp);
                        donorImg.updateAndDraw();
                        donorImg.killRoi();
                        donorImgSave.setColorModel(ipD.getColorModel());
                        mainWindow.log("Subtracted background (avg. ROI" + (autofl > 0 ? " and constant" : "") + ": " + df.format(backgroundAvgD) + ") of donor channel. (S1/S3/S5 calc.)");
                        subtractDonorButton.setBackground(mainWindow.greenColor);
                        subtractDonorButton.setOpaque(true);
                        subtractDonorButton.setBorderPainted(false);
                    }
                    break;
                }
                case "subtractS1S3S5Transfer": {
                    if (transferImg == null) {
                        mainWindow.logError("No image is set as transfer channel. (S1/S3/S5 calc.)");
                    } else if (autoflAInDField.getText().trim().isEmpty()) {
                        autoflAInDField.setText("0");
                    } else if (transferImg.getRoi() == null && autoflAInDField.getText().trim().equals("0")) {
                        mainWindow.logError("No ROI or constant is defined for transfer channel. (S1/S3/S5 calc.)");
                        // if autofluorescence value other than zero is entered, and no ROI is selected
                    } else if (transferImg.getRoi() == null && !autoflAInDField.getText().trim().equals("0")) {
                        float autofl = 0;
                        if (!autoflAInDField.getText().trim().isEmpty()) {
                            autofl = Float.parseFloat(autoflAInDField.getText().trim());
                        }
                        DecimalFormat df = new DecimalFormat("#.#");
                        ImageProcessor ipT = transferImg.getProcessor();
                        int width = transferImg.getWidth();
                        int height = transferImg.getHeight();
                        transferImgSave = new ImageStack(ipT.getWidth(), ipT.getHeight());
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                float value = ipT.getPixelValue(x, y);
                                value -= autofl;
                                ipT.putPixelValue(x, y, value);
                            }
                        }
                        FloatProcessor flp = new FloatProcessor(ipT.getWidth(), ipT.getHeight());
                        flp.setPixels(1, (FloatProcessor) ipT.duplicate());
                        transferImgSave.addSlice("" + 1, flp);
                        transferImg.updateAndDraw();
                        transferImgSave.setColorModel(ipT.getColorModel());
                        mainWindow.log("Subtracted background (constant: " + df.format(autofl) + ") of transfer channel. (S1/S3/S5 calc.)");
                        subtractTransferButton.setBackground(mainWindow.greenColor);
                        subtractTransferButton.setOpaque(true);
                        subtractTransferButton.setBorderPainted(false);
                    } else if (transferImg.getRoi() != null) { // if transfer channel ROI is selected
                        float autofl = 0;
                        if (!autoflAInDField.getText().trim().isEmpty()) {
                            autofl = Float.parseFloat(autoflAInDField.getText().trim());
                        }
                        DecimalFormat df = new DecimalFormat("#.#");
                        ImageProcessor ipT = transferImg.getProcessor();
                        int width = transferImg.getWidth();
                        int height = transferImg.getHeight();
                        transferImgSave = new ImageStack(ipT.getWidth(), ipT.getHeight());
                        double sum = 0;
                        int count = 0;
                        for (int i = 0; i < width; i++) {
                            for (int j = 0; j < height; j++) {
                                if (transferImg.getRoi().contains(i, j)) {
                                    sum += ipT.getPixelValue(i, j);
                                    count++;
                                }
                            }
                        }
                        float backgroundAvgT = (float) (sum / count);
                        backgroundAvgT += autofl;
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                float value = ipT.getPixelValue(x, y);
                                value -= backgroundAvgT;
                                ipT.putPixelValue(x, y, value);
                            }
                        }
                        FloatProcessor flp = new FloatProcessor(ipT.getWidth(), ipT.getHeight());
                        flp.setPixels(1, (FloatProcessor) ipT.duplicate());
                        transferImgSave.addSlice("" + 1, flp);
                        transferImg.updateAndDraw();
                        transferImg.killRoi();
                        transferImgSave.setColorModel(ipT.getColorModel());
                        mainWindow.log("Subtracted background (avg. ROI" + (autofl > 0 ? " and constant" : "") + ": " + df.format(backgroundAvgT) + ") of transfer channel. (S1/S3/S5 calc.)");
                        subtractTransferButton.setBackground(mainWindow.greenColor);
                        subtractTransferButton.setOpaque(true);
                        subtractTransferButton.setBorderPainted(false);
                    }
                    break;
                }
                case "subtractS1S3S5Acceptor": {
                    if (acceptorImg == null) {
                        mainWindow.logError("No image is set as acceptor channel. (S1/S3/S5 calc.)");
                    } else if (autoflAInAField.getText().trim().isEmpty()) {
                        autoflAInAField.setText("0");
                    } else if (acceptorImg.getRoi() == null && autoflAInAField.getText().trim().equals("0")) {
                        mainWindow.logError("No ROI or constant is defined for acceptor channel. (S1/S3/S5 calc.)");
                        // if autofluorescence value other than zero is entered, and no ROI is selected
                    } else if (acceptorImg.getRoi() == null && !autoflAInAField.getText().trim().equals("0")) {
                        float autofl = 0;
                        if (!autoflAInAField.getText().trim().isEmpty()) {
                            autofl = Float.parseFloat(autoflAInAField.getText().trim());
                        }
                        DecimalFormat df = new DecimalFormat("#.#");
                        ImageProcessor ipA = acceptorImg.getProcessor();
                        int width = acceptorImg.getWidth();
                        int height = acceptorImg.getHeight();
                        acceptorImgSave = new ImageStack(ipA.getWidth(), ipA.getHeight());
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                float value = ipA.getPixelValue(x, y);
                                value -= autofl;
                                ipA.putPixelValue(x, y, value);
                            }
                        }
                        FloatProcessor flp = new FloatProcessor(ipA.getWidth(), ipA.getHeight());
                        flp.setPixels(1, (FloatProcessor) ipA.duplicate());
                        acceptorImgSave.addSlice("" + 1, flp);
                        acceptorImg.updateAndDraw();
                        acceptorImgSave.setColorModel(ipA.getColorModel());
                        mainWindow.log("Subtracted background (constant: " + df.format(autofl) + ") of acceptor channel. (S1/S3/S5 calc.)");
                        subtractAcceptorButton.setBackground(mainWindow.greenColor);
                        subtractAcceptorButton.setOpaque(true);
                        subtractAcceptorButton.setBorderPainted(false);
                    } else if (acceptorImg.getRoi() != null) { // if acceptor channel ROI is selected
                        float autofl = 0;
                        if (!autoflAInAField.getText().trim().isEmpty()) {
                            autofl = Float.parseFloat(autoflAInAField.getText().trim());
                        }
                        DecimalFormat df = new DecimalFormat("#.#");
                        ImageProcessor ipA = acceptorImg.getProcessor();
                        int width = acceptorImg.getWidth();
                        int height = acceptorImg.getHeight();
                        acceptorImgSave = new ImageStack(ipA.getWidth(), ipA.getHeight());
                        double sum = 0;
                        int count = 0;
                        for (int i = 0; i < width; i++) {
                            for (int j = 0; j < height; j++) {
                                if (acceptorImg.getRoi().contains(i, j)) {
                                    sum += ipA.getPixelValue(i, j);
                                    count++;
                                }
                            }
                        }
                        float backgroundAvgA = (float) (sum / count);
                        backgroundAvgA += autofl;
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                float value = ipA.getPixelValue(x, y);
                                value -= backgroundAvgA;
                                ipA.putPixelValue(x, y, value);
                            }
                        }
                        FloatProcessor flp = new FloatProcessor(ipA.getWidth(), ipA.getHeight());
                        flp.setPixels(1, (FloatProcessor) ipA.duplicate());
                        acceptorImgSave.addSlice("" + 1, flp);
                        acceptorImg.updateAndDraw();
                        acceptorImg.killRoi();
                        acceptorImgSave.setColorModel(ipA.getColorModel());
                        mainWindow.log("Subtracted background (avg. ROI" + (autofl > 0 ? " and constant" : "") + ": " + df.format(backgroundAvgA) + ") of acceptor channel. (S1/S3/S5 calc.)");
                        subtractAcceptorButton.setBackground(mainWindow.greenColor);
                        subtractAcceptorButton.setOpaque(true);
                        subtractAcceptorButton.setBorderPainted(false);
                    }
                    break;
                }
                case "subtractS1S3S5Autofluorescence": {
                    if (autofluorescenceImg == null) {
                        mainWindow.logError("No image is set as autofluorescence channel. (S1/S3/S5 calc.)");
                    } else if (autoflAFField.getText().trim().isEmpty()) {
                        autoflAFField.setText("0");
                    } else if (autofluorescenceImg.getRoi() == null && autoflAFField.getText().trim().equals("0")) {
                        mainWindow.logError("No ROI or constant is defined for autofluorescence channel. (S1/S3/S5 calc.)");
                        // if autofluorescence value other than zero is entered, and no ROI is selected
                    } else if (autofluorescenceImg.getRoi() == null && !autoflAFField.getText().trim().equals("0")) {
                        float autofl = 0;
                        if (!autoflAFField.getText().trim().isEmpty()) {
                            autofl = Float.parseFloat(autoflAFField.getText().trim());
                        }
                        DecimalFormat df = new DecimalFormat("#.#");
                        ImageProcessor ipAF = autofluorescenceImg.getProcessor();
                        int width = autofluorescenceImg.getWidth();
                        int height = autofluorescenceImg.getHeight();
                        autofluorescenceImgSave = new ImageStack(ipAF.getWidth(), ipAF.getHeight());
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                float value = ipAF.getPixelValue(x, y);
                                value -= autofl;
                                ipAF.putPixelValue(x, y, value);
                            }
                        }
                        FloatProcessor flp = new FloatProcessor(ipAF.getWidth(), ipAF.getHeight());
                        flp.setPixels(1, (FloatProcessor) ipAF.duplicate());
                        autofluorescenceImgSave.addSlice("" + 1, flp);
                        autofluorescenceImg.updateAndDraw();
                        autofluorescenceImgSave.setColorModel(ipAF.getColorModel());
                        mainWindow.log("Subtracted background (constant: " + df.format(autofl) + ") of autofluorescence channel. (S1/S3/S5 calc.)");
                        subtractAutofluorescenceButton.setBackground(mainWindow.greenColor);
                        subtractAutofluorescenceButton.setOpaque(true);
                        subtractAutofluorescenceButton.setBorderPainted(false);
                    } else if (autofluorescenceImg.getRoi() != null) { // if autofluorescence channel ROI is selected
                        float autofl = 0;
                        if (!autoflAFField.getText().trim().isEmpty()) {
                            autofl = Float.parseFloat(autoflAFField.getText().trim());
                        }
                        DecimalFormat df = new DecimalFormat("#.#");
                        ImageProcessor ipAF = autofluorescenceImg.getProcessor();
                        int width = autofluorescenceImg.getWidth();
                        int height = autofluorescenceImg.getHeight();
                        autofluorescenceImgSave = new ImageStack(ipAF.getWidth(), ipAF.getHeight());
                        double sum = 0;
                        int count = 0;
                        for (int i = 0; i < width; i++) {
                            for (int j = 0; j < height; j++) {
                                if (autofluorescenceImg.getRoi().contains(i, j)) {
                                    sum += ipAF.getPixelValue(i, j);
                                    count++;
                                }
                            }
                        }
                        float backgroundAvgAF = (float) (sum / count);
                        backgroundAvgAF += autofl;
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                float value = ipAF.getPixelValue(x, y);
                                value -= backgroundAvgAF;
                                ipAF.putPixelValue(x, y, value);
                            }
                        }
                        FloatProcessor flp = new FloatProcessor(ipAF.getWidth(), ipAF.getHeight());
                        flp.setPixels(1, (FloatProcessor) ipAF.duplicate());
                        autofluorescenceImgSave.addSlice("" + 1, flp);
                        autofluorescenceImg.updateAndDraw();
                        autofluorescenceImg.killRoi();
                        autofluorescenceImgSave.setColorModel(ipAF.getColorModel());
                        mainWindow.log("Subtracted bg. (avg. ROI" + (autofl > 0 ? " and constant" : "") + ": " + df.format(backgroundAvgAF) + ") of autofluorescence channel. (S1/S3/S5 calc.)");
                        subtractAutofluorescenceButton.setBackground(mainWindow.greenColor);
                        subtractAutofluorescenceButton.setOpaque(true);
                        subtractAutofluorescenceButton.setBorderPainted(false);
                    }
                    break;
                }
                case "setS1S3S5DonorThreshold":
                    if (donorImg == null) {
                        mainWindow.logError("No image is set as donor channel. (S1/S3/S5 calc.)");
                        return;
                    }
                    IJ.selectWindow(donorImg.getTitle());
                    IJ.run("Threshold...");
                    setDonorThresholdButton.setBackground(mainWindow.greenColor);
                    setDonorThresholdButton.setOpaque(true);
                    setDonorThresholdButton.setBorderPainted(false);
                    break;
                case "setS1S3S5TransferThreshold":
                    if (transferImg == null) {
                        mainWindow.logError("No image is set as transfer channel. (S1/S3/S5 calc.)");
                        return;
                    }
                    IJ.selectWindow(transferImg.getTitle());
                    IJ.run("Threshold...");
                    setTransferThresholdButton.setBackground(mainWindow.greenColor);
                    setTransferThresholdButton.setOpaque(true);
                    setTransferThresholdButton.setBorderPainted(false);
                    break;
                case "setS1S3S5AcceptorThreshold":
                    if (acceptorImg == null) {
                        mainWindow.logError("No image is set as acceptor channel. (S1/S3/S5 calc.)");
                        return;
                    }
                    IJ.selectWindow(acceptorImg.getTitle());
                    IJ.run("Threshold...");
                    setAcceptorThresholdButton.setBackground(mainWindow.greenColor);
                    setAcceptorThresholdButton.setOpaque(true);
                    setAcceptorThresholdButton.setBorderPainted(false);
                    break;
                case "setS1S3S5AutofluorescenceThreshold":
                    if (autofluorescenceImg == null) {
                        mainWindow.logError("No image is set as autofluorescence channel. (S1/S3/S5 calc.)");
                        return;
                    }
                    IJ.selectWindow(autofluorescenceImg.getTitle());
                    IJ.run("Threshold...");
                    setAutofluorescenceThresholdButton.setBackground(mainWindow.greenColor);
                    setAutofluorescenceThresholdButton.setOpaque(true);
                    setAutofluorescenceThresholdButton.setBorderPainted(false);
                    break;
                case "resetS1S3S5DonorThreshold": {
                    if (donorImg == null) {
                        mainWindow.logError("No image is set as donor channel. (S1/S3/S5 calc.)");
                        return;
                    }
                    if (donorImgSave == null) {
                        mainWindow.logError("No saved image. (S1/S3/S5 calc.)");
                        return;
                    }
                    ImageStack newStack = new ImageStack(donorImgSave.getWidth(), donorImgSave.getHeight());
                    FloatProcessor flp = new FloatProcessor(donorImgSave.getProcessor(1).getWidth(), donorImgSave.getProcessor(1).getHeight());
                    flp.setPixels(1, (FloatProcessor) donorImgSave.getProcessor(1).duplicate());
                    newStack.addSlice("" + 1, flp);
                    donorImg.setStack(donorImg.getTitle(), newStack);
                    donorImg.getProcessor().setColorModel(donorImgSave.getColorModel());
                    donorImg.updateAndDraw();
                    setDonorThresholdButton.setBackground(mainWindow.originalButtonColor);
                    setDonorThresholdButton.setOpaque(false);
                    setDonorThresholdButton.setBorderPainted(true);
                    break;
                }
                case "resetS1S3S5TransferThreshold": {
                    if (transferImg == null) {
                        mainWindow.logError("No image is set as transfer channel. (S1/S3/S5 calc.)");
                        return;
                    }
                    if (transferImgSave == null) {
                        mainWindow.logError("No saved image. (S1/S3/S5 calc.)");
                        return;
                    }
                    ImageStack newStack = new ImageStack(transferImgSave.getWidth(), transferImgSave.getHeight());
                    FloatProcessor flp = new FloatProcessor(transferImgSave.getProcessor(1).getWidth(), transferImgSave.getProcessor(1).getHeight());
                    flp.setPixels(1, (FloatProcessor) transferImgSave.getProcessor(1).duplicate());
                    newStack.addSlice("" + 1, flp);
                    transferImg.setStack(transferImg.getTitle(), newStack);
                    transferImg.getProcessor().setColorModel(transferImgSave.getColorModel());
                    transferImg.updateAndDraw();
                    setTransferThresholdButton.setBackground(mainWindow.originalButtonColor);
                    setTransferThresholdButton.setOpaque(false);
                    setTransferThresholdButton.setBorderPainted(true);
                    break;
                }
                case "resetS1S3S5AcceptorThreshold": {
                    if (acceptorImg == null) {
                        mainWindow.logError("No image is set as acceptor channel. (S1/S3/S5 calc.)");
                        return;
                    }
                    if (acceptorImgSave == null) {
                        mainWindow.logError("No saved image. (S1/S3/S5 calc.)");
                        return;
                    }
                    ImageStack newStack = new ImageStack(acceptorImgSave.getWidth(), acceptorImgSave.getHeight());
                    FloatProcessor flp = new FloatProcessor(acceptorImgSave.getProcessor(1).getWidth(), acceptorImgSave.getProcessor(1).getHeight());
                    flp.setPixels(1, (FloatProcessor) acceptorImgSave.getProcessor(1).duplicate());
                    newStack.addSlice("" + 1, flp);
                    acceptorImg.setStack(acceptorImg.getTitle(), newStack);
                    acceptorImg.getProcessor().setColorModel(acceptorImgSave.getColorModel());
                    acceptorImg.updateAndDraw();
                    setAcceptorThresholdButton.setBackground(mainWindow.originalButtonColor);
                    setAcceptorThresholdButton.setOpaque(false);
                    setAcceptorThresholdButton.setBorderPainted(true);
                    break;
                }
                case "resetS1S3S5AutofluorescenceThreshold": {
                    if (autofluorescenceImg == null) {
                        mainWindow.logError("No image is set as autofluorescence channel. (S1/S3/S5 calc.)");
                        return;
                    }
                    if (autofluorescenceImgSave == null) {
                        mainWindow.logError("No saved image. (S1/S3/S5 calc.)");
                        return;
                    }
                    ImageStack newStack = new ImageStack(autofluorescenceImgSave.getWidth(), autofluorescenceImgSave.getHeight());
                    FloatProcessor flp = new FloatProcessor(autofluorescenceImgSave.getProcessor(1).getWidth(), autofluorescenceImgSave.getProcessor(1).getHeight());
                    flp.setPixels(1, (FloatProcessor) autofluorescenceImgSave.getProcessor(1).duplicate());
                    newStack.addSlice("" + 1, flp);
                    autofluorescenceImg.setStack(autofluorescenceImg.getTitle(), newStack);
                    autofluorescenceImg.getProcessor().setColorModel(autofluorescenceImgSave.getColorModel());
                    autofluorescenceImg.updateAndDraw();
                    setAutofluorescenceThresholdButton.setBackground(mainWindow.originalButtonColor);
                    setAutofluorescenceThresholdButton.setOpaque(false);
                    setAutofluorescenceThresholdButton.setBorderPainted(true);
                    break;
                }
                case "calculate":
                    if (donorImg == null) {
                        mainWindow.logError("No image is set as donor channel. (S1/S3/S5 calc.)");
                    } else if (transferImg == null) {
                        mainWindow.logError("No image is set as transfer channel. (S1/S3/S5 calc.)");
                    } else if (acceptorImg == null) {
                        mainWindow.logError("No image is set as acceptor channel. (S1/S3/S5 calc.)");
                    } else if (autofluorescenceImg == null) {
                        mainWindow.logError("No image is set as autofluorescence channel. (S1/S3/S5 calc.)");
                    } else {
                        DecimalFormat df = new DecimalFormat("#.###");
                        ImageProcessor ipDP = donorImg.getProcessor();
                        ImageProcessor ipTP = transferImg.getProcessor();
                        ImageProcessor ipAP = acceptorImg.getProcessor();
                        ImageProcessor ipAFP = autofluorescenceImg.getProcessor();
                        double s1c = 0;
                        double s3c = 0;
                        double s5c = 0;
                        double countc = 0;
                        float[][] imgS1Points = null;
                        float[][] imgS3Points = null;
                        float[][] imgS5Points = null;
                        int width = ipDP.getWidth();
                        int height = ipDP.getHeight();
                        if (showSImagesCB.isSelected()) {
                            imgS1Points = new float[width][height];
                            imgS3Points = new float[width][height];
                            imgS5Points = new float[width][height];
                        }
                        float currentS1 = 0;
                        float currentS3 = 0;
                        float currentS5 = 0;
                        for (int i = 0; i < width; i++) {
                            for (int j = 0; j < height; j++) {
                                currentS1 = ipTP.getPixelValue(i, j) / ipDP.getPixelValue(i, j);
                                currentS3 = ipAP.getPixelValue(i, j) / ipDP.getPixelValue(i, j);
                                currentS5 = ipAFP.getPixelValue(i, j) / ipDP.getPixelValue(i, j);
                                if (!Float.isNaN(currentS1) && !Float.isNaN(currentS3) && !Float.isNaN(currentS5)) {
                                    s1c += currentS1;
                                    s3c += currentS3;
                                    s5c += currentS5;
                                    countc++;
                                }
                                if (showSImagesCB.isSelected()) {
                                    imgS1Points[i][j] = currentS1;
                                    imgS3Points[i][j] = currentS3;
                                    imgS5Points[i][j] = currentS5;
                                }
                            }
                        }
                        if (showSImagesCB.isSelected()) {
                            ImagePlus s1Img = new ImagePlus("S1 image", new FloatProcessor(imgS1Points));
                            s1Img.show();
                            ImagePlus s3Img = new ImagePlus("S3 image", new FloatProcessor(imgS3Points));
                            s3Img.show();
                            ImagePlus s5Img = new ImagePlus("S5 image", new FloatProcessor(imgS5Points));
                            s5Img.show();
                        }
                        float avgS1 = (float) (s1c / countc);
                        float avgS3 = (float) (s3c / countc);
                        float avgS5 = (float) (s5c / countc);
                        s1ResultLabel.setText(df.format(avgS1));
                        s3ResultLabel.setText(df.format(avgS3));
                        s5ResultLabel.setText(df.format(avgS5));
                        calculateButton.setBackground(mainWindow.greenColor);
                        calculateButton.setOpaque(true);
                        calculateButton.setBorderPainted(false);
                        donorImg.changes = false;
                        transferImg.changes = false;
                        acceptorImg.changes = false;
                        autofluorescenceImg.changes = false;
                    }
                    break;
                case "setfactor":
                    if (s1ResultLabel.getText().isEmpty() || s3ResultLabel.getText().isEmpty() || s5ResultLabel.getText().isEmpty()) {
                        mainWindow.logError("S1, S3 and S5 have to be calculated before setting them. (S1/S3/S5 calc.)");
                        return;
                    }
                    mainWindow.setS1Factor(s1ResultLabel.getText());
                    mainWindow.setS3Factor(s3ResultLabel.getText());
                    mainWindow.setS5Factor(s5ResultLabel.getText());
                    setButton.setBackground(mainWindow.greenColor);
                    setButton.setOpaque(true);
                    setButton.setBorderPainted(false);
                    mainWindow.calculateS1S3S5Button.setBackground(mainWindow.greenColor);
                    mainWindow.calculateS1S3S5Button.setOpaque(true);
                    mainWindow.calculateS1S3S5Button.setBorderPainted(false);
                    break;
                case "copyS1S3S5Roi":
                    if (donorImg == null) {
                        mainWindow.logError("No image is set as donor channel. (S1/S3/S5 calc.)");
                        return;
                    }
                    if (donorImg.getRoi() != null) {
                        if (transferImg != null) {
                            transferImg.setRoi(donorImg.getRoi());
                        }
                        if (acceptorImg != null) {
                            acceptorImg.setRoi(donorImg.getRoi());
                        }
                        if (autofluorescenceImg != null) {
                            autofluorescenceImg.setRoi(donorImg.getRoi());
                        }
                        copyRoiButton.setBackground(mainWindow.greenColor);
                        copyRoiButton.setOpaque(true);
                        copyRoiButton.setBorderPainted(false);
                    } else {
                        if (transferImg != null) {
                            transferImg.killRoi();
                        }
                        if (acceptorImg != null) {
                            acceptorImg.killRoi();
                        }
                        if (autofluorescenceImg != null) {
                            autofluorescenceImg.killRoi();
                        }
                    }
                    break;
                default:
                    break;
            }
        } catch (NumberFormatException t) {
            mainWindow.logException(t.toString(), t);
        }
    }
}
