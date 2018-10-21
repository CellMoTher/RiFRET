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
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.Roi;
import ij.io.FileSaver;
import ij.io.Opener;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.StackEditor;
import ij.plugin.filter.Analyzer;
import ij.plugin.filter.GaussianBlur;
import ij.process.FHT;
import ij.process.FloatProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.process.StackConverter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ToolTipManager;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class RiFRET_Plugin extends JFrame implements ActionListener, WindowListener {

    private float version = 1.83F;
    private String lastModified = "24 May 2010";
    private String imageJVersion = "1.42k";
    private String javaVersion = "1.6.0_05";
    private int windowWidth = 620;
    private int windowHeight = 800;
    private ImagePlus donorInDImage, donorInAImage, acceptorInAImage, transferImage = null;
    private ImageStack donorInDImageSave = null, donorInAImageSave = null, acceptorInAImageSave = null;
    private ResultsTable resultsTable;
    private Analyzer analyzer;
    private ApplyMaskRiDialog applyMaskRiDialog;
    private CalculateRatioDialog calculateRatioDialog;
    private AutoflDialog autoflDialog;
    private S1S3Dialog s1S3Dialog;
    private S2S4Dialog s2S4Dialog;
    private AlphaDialog alphaDialog;
    private RiHelpWindow helpWindow;
    private JMenuBar menuBar;
    private JMenu fileMenu, imageMenu, helpMenu;
    private JMenuItem openMenuItem, saveTiffMenuItem, saveBmpMenuItem, splitMenuItem, applyMaskMenuItem, registerMenuItem, calculateRatioMenuItem, calculateAFMenuItem, thresholdMenuItem;
    private JMenuItem lutFireMenuItem, lutSpectrumMenuItem, histogramMenuItem, convertMenuItem, blurMenuItem, exitMenuItem, helpMenuItem, aboutMenuItem;
    private JMenuItem saveMessagesMenuItem, clearMessagesMenuItem;
    private JMenuItem semiAutomaticMenuItem, resetImagesMenuItem;
    private JCheckBoxMenuItem debugMenuItem;
    private JButton setDonorInDImageButton, setDonorInAImageButton, setAcceptorInAImageButton;
    private JButton subtractDonorInDImageButton, subtractDonorInAImageButton, subtractAcceptorInAImageButton;
    private JButton thresholdDonorInDImageButton, thresholdDonorInAImageButton, thresholdAcceptorInAImageButton;
    private JButton smoothDonorInDImageButton, smoothDonorInAImageButton, smoothAcceptorInAImageButton;
    private JButton openImageButton, resetDDButton, resetDAButton, resetAAButton;
    private JButton copyRoiButton;
    public JTextField autoflDInDField, autoflAInDField, autoflAInAField;
    private JTextField radiusFieldDD, radiusFieldDA, radiusFieldAA;
    public JTextField autoThresholdMin, autoThresholdMax;
    private JButton createButton, measureButton, nextButton, closeImagesButton;
    private JCheckBox useLsmImages, autoThresholdingCB;
    private JTextField s1Field, s2Field, s3Field, s4Field, alphaField;
    public JButton calculateS1S3Button, calculateS2S4Button, calculateAlphaButton;
    private JTextPane log;
    private JScrollPane logScrollPane;
    private SimpleDateFormat format;
    private File[] automaticallyProcessedFiles = null;
    private int currentlyProcessedFile = 0;
    private String currentlyProcessedFileName = null;
    private String currentDirectory = null;
    public Color originalButtonColor = null;
    public Color greenColor = new Color(142, 207, 125);

    public RiFRET_Plugin() {
        super();
        setTitle("RiFRET v" + version + " - intensity-based ratiometric FRET imaging");
        IJ.versionLessThan(imageJVersion);
        Locale.setDefault(Locale.ENGLISH);
        ToolTipManager.sharedInstance().setDismissDelay(10000);
        format = new SimpleDateFormat("HH:mm:ss");
        createGui();
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(windowWidth, windowHeight);
        setLocation(screen.width - getWidth(), screen.height / 2 - getHeight() / 2);
        setVisible(true);
        currentDirectory = System.getProperty("user.home");
        originalButtonColor = setDonorInDImageButton.getBackground();
        openImageButton.requestFocus();
    }

    public void createGui() {
        setFont(new Font("Helvetica", Font.PLAIN, 12));
        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        imageMenu = new JMenu("Image");
        menuBar.add(imageMenu);
        helpMenu = new JMenu("Help");
        menuBar.add(helpMenu);
        openMenuItem = new JMenuItem("Open...");
        openMenuItem.setActionCommand("openImage");
        openMenuItem.addActionListener(this);
        fileMenu.add(openMenuItem);
        saveTiffMenuItem = new JMenuItem("Save as Tiff...");
        saveTiffMenuItem.setActionCommand("saveImageAsTiff");
        saveTiffMenuItem.addActionListener(this);
        fileMenu.add(saveTiffMenuItem);
        saveBmpMenuItem = new JMenuItem("Save as BMP...");
        saveBmpMenuItem.setActionCommand("saveImageAsBmp");
        saveBmpMenuItem.addActionListener(this);
        fileMenu.add(saveBmpMenuItem);
        saveMessagesMenuItem = new JMenuItem("Save Messages...");
        saveMessagesMenuItem.setActionCommand("saveMessages");
        saveMessagesMenuItem.addActionListener(this);
        fileMenu.add(saveMessagesMenuItem);
        clearMessagesMenuItem = new JMenuItem("Clear Messages");
        clearMessagesMenuItem.setActionCommand("clearMessages");
        clearMessagesMenuItem.addActionListener(this);
        fileMenu.add(clearMessagesMenuItem);
        semiAutomaticMenuItem = new JMenuItem("Semi-Automatic Processing...");
        semiAutomaticMenuItem.setActionCommand("semiAutomaticProcessing");
        semiAutomaticMenuItem.addActionListener(this);
        fileMenu.add(semiAutomaticMenuItem);
        resetImagesMenuItem = new JMenuItem("Reset All");
        resetImagesMenuItem.setActionCommand("resetImages");
        resetImagesMenuItem.addActionListener(this);
        fileMenu.add(resetImagesMenuItem);
        splitMenuItem = new JMenuItem("Stack to Images");
        splitMenuItem.setActionCommand("split");
        splitMenuItem.addActionListener(this);
        imageMenu.add(splitMenuItem);
        applyMaskMenuItem = new JMenuItem("Apply Mask...");
        applyMaskMenuItem.setActionCommand("applyMask");
        applyMaskMenuItem.addActionListener(this);
        imageMenu.add(applyMaskMenuItem);
        registerMenuItem = new JMenuItem("Register to Donor Channel");
        registerMenuItem.setActionCommand("registerTransferCh");
        registerMenuItem.addActionListener(this);
        imageMenu.add(registerMenuItem);
        calculateRatioMenuItem = new JMenuItem("Calculate Ratio of Images...");
        calculateRatioMenuItem.setActionCommand("calculateRatio");
        calculateRatioMenuItem.addActionListener(this);
        imageMenu.add(calculateRatioMenuItem);
        calculateAFMenuItem = new JMenuItem("Calculate Autofluorescence...");
        calculateAFMenuItem.setActionCommand("calculateAF");
        calculateAFMenuItem.addActionListener(this);
        imageMenu.add(calculateAFMenuItem);
        convertMenuItem = new JMenuItem("Convert Image to 32-bit");
        convertMenuItem.setActionCommand("convertto32bit");
        convertMenuItem.addActionListener(this);
        imageMenu.add(convertMenuItem);
        blurMenuItem = new JMenuItem("Gaussian Blur...");
        blurMenuItem.setActionCommand("gaussianblur-menu");
        blurMenuItem.addActionListener(this);
        imageMenu.add(blurMenuItem);
        thresholdMenuItem = new JMenuItem("Threshold...");
        thresholdMenuItem.setActionCommand("threshold");
        thresholdMenuItem.addActionListener(this);
        imageMenu.add(thresholdMenuItem);
        histogramMenuItem = new JMenuItem("Histogram");
        histogramMenuItem.setActionCommand("histogram");
        histogramMenuItem.addActionListener(this);
        imageMenu.add(histogramMenuItem);
        lutFireMenuItem = new JMenuItem("LUT Fire");
        lutFireMenuItem.setActionCommand("lutFire");
        lutFireMenuItem.addActionListener(this);
        imageMenu.add(lutFireMenuItem);
        lutSpectrumMenuItem = new JMenuItem("LUT Spectrum");
        lutSpectrumMenuItem.setActionCommand("lutSpectrum");
        lutSpectrumMenuItem.addActionListener(this);
        imageMenu.add(lutSpectrumMenuItem);
        exitMenuItem = new JMenuItem("Quit RiFRET");
        exitMenuItem.setActionCommand("exit");
        exitMenuItem.addActionListener(this);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);
        helpMenuItem = new JMenuItem("Help");
        helpMenuItem.setActionCommand("help");
        helpMenuItem.addActionListener(this);
        helpMenu.add(helpMenuItem);
        aboutMenuItem = new JMenuItem("About RiFRET");
        aboutMenuItem.setActionCommand("about");
        aboutMenuItem.addActionListener(this);
        helpMenu.add(aboutMenuItem);
        debugMenuItem = new JCheckBoxMenuItem("Debug Mode");
        debugMenuItem.setSelected(false);
        debugMenuItem.setActionCommand("debugmode");
        debugMenuItem.addActionListener(this);
        helpMenu.add(debugMenuItem);
        setJMenuBar(menuBar);
        addWindowListener(this);
        GridBagLayout gridbaglayout = new GridBagLayout();
        GridBagConstraints gc = new GridBagConstraints();
        Container contentPane = getContentPane();
        JScrollPane mainScrollPane = new JScrollPane();
        JPanel container = new JPanel();
        container.setLayout(gridbaglayout);

        JPanel donorInDImageBleachingPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        donorInDImageBleachingPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        gc.gridwidth = 9;
        gc.gridx = 0;
        gc.gridy = 0;
        gc.insets = new Insets(2, 2, 2, 2);
        container.add(new JLabel("Calculate / set S1 factor:"), gc);
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 0;
        s1Field = new JTextField("", 4);
        s1Field.setHorizontalAlignment(JTextField.RIGHT);
        container.add(s1Field, gc);
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 0;
        calculateS1S3Button = new JButton("Calculate S1, S3");
        calculateS1S3Button.setMargin(new Insets(2, 1, 2, 1));
        calculateS1S3Button.addActionListener(this);
        calculateS1S3Button.setActionCommand("calculateS1S3Button");
        container.add(calculateS1S3Button, gc);

        gc.gridwidth = 9;
        gc.gridx = 0;
        gc.gridy = 1;
        container.add(new JLabel("Calculate / set S2 factor:"), gc);
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 1;
        s2Field = new JTextField("", 4);
        s2Field.setHorizontalAlignment(JTextField.RIGHT);
        container.add(s2Field, gc);
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 1;
        calculateS2S4Button = new JButton("Calculate S2, S4");
        calculateS2S4Button.addActionListener(this);
        calculateS2S4Button.setActionCommand("calculateS2S4Button");
        container.add(calculateS2S4Button, gc);

        gc.gridwidth = 9;
        gc.insets = new Insets(5, 2, 5, 2);
        gc.gridx = 0;
        gc.gridy = 2;
        container.add(new JLabel("Calculate / set S3 factor:"), gc);
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 2;
        s3Field = new JTextField("", 4);
        s3Field.setHorizontalAlignment(JTextField.RIGHT);
        container.add(s3Field, gc);

        gc.gridwidth = 9;
        gc.insets = new Insets(8, 2, 8, 2);
        gc.gridx = 0;
        gc.gridy = 3;
        container.add(new JLabel("Calculate / set S4 factor:"), gc);
        gc.gridwidth = 1;
        gc.insets = new Insets(2, 2, 2, 2);
        gc.gridx = 9;
        gc.gridy = 3;
        s4Field = new JTextField("", 4);
        s4Field.setHorizontalAlignment(JTextField.RIGHT);
        container.add(s4Field, gc);

        gc.gridwidth = 9;
        gc.gridx = 0;
        gc.gridy = 4;
        container.add(new JLabel("Calculate / set \u03B1 (alpha) factor:"), gc);
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 4;
        alphaField = new JTextField("", 4);
        alphaField.setHorizontalAlignment(JTextField.RIGHT);
        container.add(alphaField, gc);
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 4;
        calculateAlphaButton = new JButton("Calculate \u03B1");
        calculateAlphaButton.addActionListener(this);
        calculateAlphaButton.setActionCommand("calculateAlphaButton");
        container.add(calculateAlphaButton, gc);

        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 5;
        JPanel lineFactors = new JPanel();
        lineFactors.setPreferredSize(new Dimension(windowWidth - 35, 1));
        lineFactors.setBackground(Color.lightGray);
        container.add(lineFactors, gc);

        gc.gridwidth = 9;
        gc.gridx = 0;
        gc.gridy = 6;
        gc.fill = GridBagConstraints.NONE;
        useLsmImages = new JCheckBox("use LSM", false);
        useLsmImages.setActionCommand("useLsmImages");
        useLsmImages.addActionListener(this);
        useLsmImages.setToolTipText("<html>If this checkbox is checked, the LSM image containing donor, transfer and<BR>acceptor channel images (in this order) are set automatically after opening.<BR>Every previously opened image window will be closed. The results window<BR>can be left opened.</html>");
        donorInDImageBleachingPanel.add(new JLabel("Step 1a: open and set the donor channel image  "));
        donorInDImageBleachingPanel.add(useLsmImages);
        setDonorInDImageButton = new JButton("Set image");
        setDonorInDImageButton.setMargin(new Insets(2, 2, 2, 2));
        setDonorInDImageButton.addActionListener(this);
        setDonorInDImageButton.setActionCommand("setDonorInDImage");
        container.add(donorInDImageBleachingPanel, gc);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 6;
        openImageButton = new JButton("Open");
        openImageButton.setToolTipText("Opens an arbitrary image.");
        openImageButton.setMargin(new Insets(0, 0, 0, 0));
        openImageButton.setFont(new Font("Helvetica", Font.BOLD, 10));
        openImageButton.addActionListener(this);
        openImageButton.setActionCommand("openImage");
        container.add(openImageButton, gc);
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 6;
        container.add(setDonorInDImageButton, gc);

        gc.gridwidth = 10;
        gc.gridx = 6;
        gc.gridy = 7;
        container.add(new JLabel("Step 1b: open and set the transfer channel image"), gc);
        setDonorInAImageButton = new JButton("Set image");
        setDonorInAImageButton.setMargin(new Insets(2, 2, 2, 2));
        setDonorInAImageButton.addActionListener(this);
        setDonorInAImageButton.setActionCommand("setDonorInAImage");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 7;
        container.add(setDonorInAImageButton, gc);

        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 8;
        container.add(new JLabel("Step 1c: open and set acceptor channel image"), gc);
        setAcceptorInAImageButton = new JButton("Set image");
        setAcceptorInAImageButton.setMargin(new Insets(2, 2, 2, 2));
        setAcceptorInAImageButton.addActionListener(this);
        setAcceptorInAImageButton.setActionCommand("setAcceptorInAImage");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 8;
        container.add(setAcceptorInAImageButton, gc);

        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 9;
        JPanel line1 = new JPanel();
        line1.setPreferredSize(new Dimension(windowWidth - 35, 1));
        line1.setBackground(Color.lightGray);
        container.add(line1, gc);

        gc.gridwidth = 9;
        gc.gridx = 0;
        gc.gridy = 10;
        container.add(new JLabel("Step 2a (optional): blur donor channel image, radius in pixels:"), gc);
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 10;
        radiusFieldDD = new JTextField("2", 4);
        radiusFieldDD.setHorizontalAlignment(JTextField.RIGHT);
        container.add(radiusFieldDD, gc);
        smoothDonorInDImageButton = new JButton("Blur");
        smoothDonorInDImageButton.addActionListener(this);
        smoothDonorInDImageButton.setActionCommand("smoothDD");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 10;
        container.add(smoothDonorInDImageButton, gc);

        gc.gridwidth = 9;
        gc.gridx = 0;
        gc.gridy = 11;
        container.add(new JLabel("Step 2b (optional): blur transfer channel image, radius in pixels:"), gc);
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 11;
        radiusFieldDA = new JTextField("2", 4);
        radiusFieldDA.setHorizontalAlignment(JTextField.RIGHT);
        container.add(radiusFieldDA, gc);
        smoothDonorInAImageButton = new JButton("Blur");
        smoothDonorInAImageButton.addActionListener(this);
        smoothDonorInAImageButton.setActionCommand("smoothDA");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 11;
        container.add(smoothDonorInAImageButton, gc);

        gc.gridwidth = 9;
        gc.gridx = 0;
        gc.gridy = 12;
        container.add(new JLabel("Step 2c (optional): blur acceptor channel image, radius in pixels:"), gc);
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 12;
        radiusFieldAA = new JTextField("2", 4);
        radiusFieldAA.setHorizontalAlignment(JTextField.RIGHT);
        container.add(radiusFieldAA, gc);
        smoothAcceptorInAImageButton = new JButton("Blur");
        smoothAcceptorInAImageButton.addActionListener(this);
        smoothAcceptorInAImageButton.setActionCommand("smoothAA");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 12;
        container.add(smoothAcceptorInAImageButton, gc);

        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 13;
        JPanel line4 = new JPanel();
        line4.setPreferredSize(new Dimension(windowWidth - 35, 1));
        line4.setBackground(Color.lightGray);
        container.add(line4, gc);

        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 14;
        container.add(new JLabel("Step 3a: subtract avg. of a background ROI of donor channel"), gc);
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 14;
        copyRoiButton = new JButton("Copy");
        copyRoiButton.setToolTipText("Sets the same ROI for the two other images.");
        copyRoiButton.setMargin(new Insets(0, 0, 0, 0));
        copyRoiButton.setFont(new Font("Helvetica", Font.BOLD, 10));
        copyRoiButton.addActionListener(this);
        copyRoiButton.setActionCommand("copyRoi");
        container.add(copyRoiButton, gc);
        autoflDInDField = new JTextField("0", 5);
        autoflDInDField.setToolTipText("<html><b>Correction for autofluorescence</b><br>If this value is set, it will be subtracted from each pixel along with<br>the background. Average autofluorescence can be calculated with<br>the \"Calculate autofluorescence\" item in the \"Image\" menu.<html>");
        gc.gridx = 10;
        gc.gridy = 14;
        container.add(autoflDInDField, gc);
        subtractDonorInDImageButton = new JButton("Subtract");
        subtractDonorInDImageButton.addActionListener(this);
        subtractDonorInDImageButton.setActionCommand("subtractDonorInDImage");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 13;
        gc.gridy = 14;
        container.add(subtractDonorInDImageButton, gc);

        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 15;
        container.add(new JLabel("Step 3b: subtract avg. of a background ROI of transfer channel"), gc);
        autoflAInDField = new JTextField("0", 5);
        autoflAInDField.setToolTipText("<html><b>Correction for autofluorescence</b><br>If this value is set, it will be subtracted from each pixel along with<br>the background. Average autofluorescence can be calculated with<br>the \"Calculate autofluorescence\" item in the \"Image\" menu.<html>");
        gc.gridwidth = 1;
        gc.gridx = 10;
        gc.gridy = 15;
        container.add(autoflAInDField, gc);
        gc.gridwidth = GridBagConstraints.REMAINDER;
        subtractDonorInAImageButton = new JButton("Subtract");
        subtractDonorInAImageButton.addActionListener(this);
        subtractDonorInAImageButton.setActionCommand("subtractDonorInAImage");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 13;
        gc.gridy = 15;
        container.add(subtractDonorInAImageButton, gc);

        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 16;
        container.add(new JLabel("Step 3c: subtract avg. of a background ROI of acceptor channel"), gc);
        autoflAInAField = new JTextField("0", 5);
        autoflAInAField.setToolTipText("<html><b>Correction for autofluorescence</b><br>If this value is set, it will be subtracted from each pixel along with<br>the background. Average autofluorescence can be calculated with<br>the \"Calculate autofluorescence\" item in the \"Image\" menu.<html>");
        gc.gridwidth = 1;
        gc.gridx = 10;
        gc.gridy = 16;
        container.add(autoflAInAField, gc);
        subtractAcceptorInAImageButton = new JButton("Subtract");
        subtractAcceptorInAImageButton.addActionListener(this);
        subtractAcceptorInAImageButton.setActionCommand("subtractAcceptorInAImage");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 13;
        gc.gridy = 16;
        container.add(subtractAcceptorInAImageButton, gc);
        gc.gridx = 0;
        gc.gridy = 17;
        JLabel afInfo = new JLabel("Autofluorescence fields (if necessary) can be filled in using the \"Calculate autofluorescence\" item in the \"Image\" menu.");
        afInfo.setFont(new Font("Helvetica", Font.PLAIN, 10));
        container.add(afInfo, gc);

        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 18;
        JPanel line3 = new JPanel();
        line3.setPreferredSize(new Dimension(windowWidth - 35, 1));
        line3.setBackground(Color.lightGray);
        container.add(line3, gc);

        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 19;
        container.add(new JLabel("Step 4a: set threshold for donor channel image"), gc);
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 19;
        resetDDButton = new JButton("Reset");
        resetDDButton.setToolTipText("Resets blur and threshold settings");
        resetDDButton.setMargin(new Insets(0, 0, 0, 0));
        resetDDButton.setFont(new Font("Helvetica", Font.BOLD, 10));
        resetDDButton.addActionListener(this);
        resetDDButton.setActionCommand("resetDD");
        container.add(resetDDButton, gc);
        thresholdDonorInDImageButton = new JButton("Set threshold");
        thresholdDonorInDImageButton.addActionListener(this);
        thresholdDonorInDImageButton.setActionCommand("thresholdDonorInDImage");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 19;
        container.add(thresholdDonorInDImageButton, gc);

        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 20;
        container.add(new JLabel("Step 4b: set threshold for transfer channel image"), gc);
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 20;
        resetDAButton = new JButton("Reset");
        resetDAButton.setToolTipText("Resets blur and threshold settings");
        resetDAButton.setMargin(new Insets(0, 0, 0, 0));
        resetDAButton.setFont(new Font("Helvetica", Font.BOLD, 10));
        resetDAButton.addActionListener(this);
        resetDAButton.setActionCommand("resetDA");
        container.add(resetDAButton, gc);
        thresholdDonorInAImageButton = new JButton("Set threshold");
        thresholdDonorInAImageButton.addActionListener(this);
        thresholdDonorInAImageButton.setActionCommand("thresholdDonorInAImage");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 20;
        container.add(thresholdDonorInAImageButton, gc);

        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 21;
        container.add(new JLabel("Step 4c: set threshold for acceptor channel image"), gc);
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 21;
        resetAAButton = new JButton("Reset");
        resetAAButton.setToolTipText("Resets blur and threshold settings");
        resetAAButton.setMargin(new Insets(0, 0, 0, 0));
        resetAAButton.setFont(new Font("Helvetica", Font.BOLD, 10));
        resetAAButton.addActionListener(this);
        resetAAButton.setActionCommand("resetAA");
        container.add(resetAAButton, gc);
        thresholdAcceptorInAImageButton = new JButton("Set threshold");
        thresholdAcceptorInAImageButton.addActionListener(this);
        thresholdAcceptorInAImageButton.setActionCommand("thresholdAcceptorInAImage");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 21;
        container.add(thresholdAcceptorInAImageButton, gc);
        gc.gridx = 0;
        gc.gridy = 22;
        JLabel thInfo = new JLabel("Threshold setting: set threshold, press \"Apply\", select \"Set bg pixels to NaN\", press \"Ok\" (and then \"No\" in case of stacks)");
        thInfo.setFont(new Font("Helvetica", Font.PLAIN, 10));
        container.add(thInfo, gc);

        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 23;
        JPanel line5 = new JPanel();
        line5.setPreferredSize(new Dimension(windowWidth - 35, 1));
        line5.setBackground(Color.lightGray);
        container.add(line5, gc);

        JPanel createFretImgPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 24;
        gc.insets = new Insets(2, 2, 2, 2);
        gc.fill = GridBagConstraints.NONE;
        createFretImgPanel.add(new JLabel("Step 5: create FRET image   "));
        autoThresholdingCB = new JCheckBox("thresholding with min: ", true);
        autoThresholdingCB.setToolTipText("<html>If this checkbox is checked, the FRET image will be thresholded<br>with the given min and max values to exclude pixels with extreme<br>FRET efficiencies.</html>");
        autoThresholdingCB.setSelected(true);
        createFretImgPanel.add(autoThresholdingCB);
        autoThresholdMin = new JTextField("-2", 2);
        createFretImgPanel.add(autoThresholdMin);
        createFretImgPanel.add(new JLabel(" and max: "));
        autoThresholdMax = new JTextField("2", 2);
        createFretImgPanel.add(autoThresholdMax);
        container.add(createFretImgPanel, gc);
        createButton = new JButton("Create");
        createButton.addActionListener(this);
        createButton.setActionCommand("createFretImage");
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 24;
        container.add(createButton, gc);

        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 25;
        JPanel line7 = new JPanel();
        line7.setPreferredSize(new Dimension(windowWidth - 35, 1));
        line7.setBackground(Color.lightGray);
        container.add(line7, gc);

        gc.gridwidth = 7;
        gc.gridx = 0;
        gc.gridy = 26;
        container.add(new JLabel("Step 6: select ROIs and make measurements"), gc);
        gc.gridx = 9;
        gc.gridwidth = 1;
        closeImagesButton = new JButton("Close images");
        closeImagesButton.setToolTipText("Closes the source and transfer images and resets button colors");
        closeImagesButton.setMargin(new Insets(0, 0, 0, 0));
        closeImagesButton.setFont(new Font("Helvetica", Font.BOLD, 10));
        closeImagesButton.addActionListener(this);
        closeImagesButton.setActionCommand("closeImages");
        container.add(closeImagesButton, gc);
        gc.fill = GridBagConstraints.HORIZONTAL;
        measureButton = new JButton("Measure");
        measureButton.setMargin(new Insets(2, 2, 2, 2));
        measureButton.addActionListener(this);
        measureButton.setActionCommand("measureFretImage");
        gc.gridx = 10;
        gc.gridy = 26;
        gc.gridwidth = 6;
        container.add(measureButton, gc);
        nextButton = new JButton("Next");
        nextButton.setMargin(new Insets(2, 2, 2, 2));
        nextButton.addActionListener(this);
        nextButton.setActionCommand("nextImage");
        gc.gridx = 16;
        gc.gridy = 26;
        gc.gridwidth = GridBagConstraints.REMAINDER;
        container.add(nextButton, gc);
        nextButton.setVisible(false);

        gc.weighty = 20;
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.fill = GridBagConstraints.BOTH;
        gc.gridx = 0;
        gc.gridy = GridBagConstraints.RELATIVE;
        log = new JTextPane();
        log.setEditable(false);
        Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        Style style;
        style = log.addStyle("RED", defaultStyle);
        StyleConstants.setForeground(style, Color.red.darker());
        style = log.addStyle("BLUE", defaultStyle);
        StyleConstants.setForeground(style, Color.blue.darker());
        style = log.addStyle("BLACK", defaultStyle);
        StyleConstants.setForeground(style, Color.black.darker());
        logScrollPane = new JScrollPane(log);
        logScrollPane.setBorder(BorderFactory.createTitledBorder("Messages"));
        logScrollPane.setPreferredSize(new Dimension(10, 60));
        contentPane.add(logScrollPane, BorderLayout.SOUTH);
        mainScrollPane.setViewportView(container);
        contentPane.add(mainScrollPane, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            switch (e.getActionCommand()) {
                case "exit":
                    exit();
                    break;
                case "split":
                    if (WindowManager.getCurrentImage() == null) {
                        logError("No open image.");
                        return;
                    }
                    if (WindowManager.getCurrentImage().isHyperStack()) {
                        ij.plugin.HyperStackConverter hc = new ij.plugin.HyperStackConverter();
                        hc.run("hstostack");
                    }
                    StackEditor se = new StackEditor();
                    se.run("toimages");
                    break;
                case "applyMask":
                    if (applyMaskRiDialog != null) {
                        applyMaskRiDialog.setVisible(false);
                        applyMaskRiDialog.dispose();
                    }
                    applyMaskRiDialog = new ApplyMaskRiDialog(this);
                    applyMaskRiDialog.setVisible(true);
                    break;
                case "registerTransferCh":
                    if (donorInDImage == null) {
                        logError("No image is set as donor channel.");
                    } else if (donorInAImage == null) {
                        logError("No image is set as transfer channel.");
                    } else {
                        FHT fht1 = new FHT(donorInDImage.getProcessor().duplicate());
                        fht1.transform();
                        FHT fht2 = new FHT(donorInAImage.getProcessor().duplicate());
                        fht2.transform();
                        FHT res = fht1.conjugateMultiply(fht2);
                        res.inverseTransform();
                        ImagePlus image = new ImagePlus("Result of registration", res);
                        ImageProcessor ip = image.getProcessor();
                        int width = ip.getWidth();
                        int height = ip.getHeight();
                        int maximum = 0;
                        int maxx = -1;
                        int maxy = -1;
                        for (int i = 0; i < width; i++) {
                            for (int j = 0; j < height; j++) {
                                if (ip.getPixel(i, j) > maximum) {
                                    maximum = ip.getPixel(i, j);
                                    maxx = i;
                                    maxy = j;
                                }
                            }
                        }
                        int shiftX = 0;
                        int shiftY = 0;
                        if (maxx != 0 || maxy != 0) {
                            ShiftDialogRi sd = new ShiftDialogRi(this);
                            if (maxy > height / 2) {
                                log("Shifting transfer channel image up " + (height - maxy) + " pixel" + ((height - maxy) > 1 ? "s" : "") + ".");
                                sd.shiftUp(donorInAImage, height - maxy);
                            } else if (maxy != 0) {
                                log("Shifting transfer channel image down " + maxy + " pixel" + (maxy > 1 ? "s" : "") + ".");
                                sd.shiftDown(donorInAImage, maxy);
                            }
                            if (maxx > width / 2) {
                                log("Shifting transfer channel image to the left " + (width - maxx) + " pixel" + ((width - maxx) > 1 ? "s" : "") + ".");
                                sd.shiftLeft(donorInAImage, width - maxx);
                            } else if (maxx != 0) {
                                log("Shifting transfer channel image to the right " + maxx + " pixel" + (maxx > 1 ? "s" : "") + ".");
                                sd.shiftRight(donorInAImage, maxx);
                            }
                            actionPerformed(new ActionEvent(registerMenuItem, 1, "registerTransferCh"));
                        } else {
                            log("Registration of transfer channel has been finished.");
                            actionPerformed(new ActionEvent(registerMenuItem, 1, "registerAcceptorCh"));
                        }
                    }
                    break;
                case "registerAcceptorCh":
                    if (donorInDImage == null) {
                        logError("No image is set as donor channel.");
                    } else if (acceptorInAImage == null) {
                        logError("No image is set as acceptor channel.");
                    } else {
                        FHT fht1 = new FHT(donorInDImage.getProcessor().duplicate());
                        fht1.transform();
                        FHT fht2 = new FHT(acceptorInAImage.getProcessor().duplicate());
                        fht2.transform();
                        FHT res = fht1.conjugateMultiply(fht2);
                        res.inverseTransform();
                        ImagePlus image = new ImagePlus("Result of registration", res);
                        ImageProcessor ip = image.getProcessor();
                        int width = ip.getWidth();
                        int height = ip.getHeight();
                        int maximum = 0;
                        int maxx = -1;
                        int maxy = -1;
                        for (int i = 0; i < width; i++) {
                            for (int j = 0; j < height; j++) {
                                if (ip.getPixel(i, j) > maximum) {
                                    maximum = ip.getPixel(i, j);
                                    maxx = i;
                                    maxy = j;
                                }
                            }
                        }
                        int shiftX = 0;
                        int shiftY = 0;
                        if (maxx != 0 || maxy != 0) {
                            ShiftDialogRi sd = new ShiftDialogRi(this);
                            if (maxy > height / 2) {
                                log("Shifting acceptor channel image up " + (height - maxy) + " pixel" + ((height - maxy) > 1 ? "s" : "") + ".");
                                sd.shiftUp(acceptorInAImage, height - maxy);
                            } else if (maxy != 0) {
                                log("Shifting acceptor channel image down " + maxy + " pixel" + (maxy > 1 ? "s" : "") + ".");
                                sd.shiftDown(acceptorInAImage, maxy);
                            }
                            if (maxx > width / 2) {
                                log("Shifting acceptor channel image to the left " + (width - maxx) + " pixel" + ((width - maxx) > 1 ? "s" : "") + ".");
                                sd.shiftLeft(acceptorInAImage, width - maxx);
                            } else if (maxx != 0) {
                                log("Shifting acceptor channel image to the right " + maxx + " pixel" + (maxx > 1 ? "s" : "") + ".");
                                sd.shiftRight(acceptorInAImage, maxx);
                            }
                            actionPerformed(new ActionEvent(registerMenuItem, 1, "registerAcceptorCh"));
                        } else {
                            log("Registration of acceptor channel has been finished.");
                        }
                    }
                    break;
                case "calculateRatio":
                    if (calculateRatioDialog != null) {
                        calculateRatioDialog.setVisible(false);
                        calculateRatioDialog.dispose();
                    }
                    calculateRatioDialog = new CalculateRatioDialog(this);
                    calculateRatioDialog.setVisible(true);
                    break;
                case "calculateAF":
                    if (autoflDialog != null) {
                        autoflDialog.setVisible(false);
                        autoflDialog.dispose();
                    }
                    autoflDialog = new AutoflDialog(this);
                    autoflDialog.setVisible(true);
                    break;
                case "threshold":
                    if (WindowManager.getCurrentImage() == null) {
                        logError("No open image.");
                        return;
                    }
                    IJ.run("Threshold...");
                    break;
                case "convertto32bit":
                    if (WindowManager.getCurrentImage() == null) {
                        logError("No open image.");
                        return;
                    }
                    new ImageConverter(WindowManager.getCurrentImage()).convertToGray32();
                    break;
                case "gaussianblur-menu": {
                    if (WindowManager.getCurrentImage() == null) {
                        logError("No open image.");
                        return;
                    }
                    String radiusString = JOptionPane.showInputDialog(this, "Enter radius (in pixels) for Gaussian blur", "Gaussian blur", JOptionPane.QUESTION_MESSAGE);
                    if (radiusString == null || radiusString.trim().isEmpty()) {
                        return;
                    }
                    GaussianBlur gb = new GaussianBlur();
                    int nSlices = WindowManager.getCurrentImage().getImageStackSize();
                    for (int currentSlice = 1; currentSlice <= nSlices; currentSlice++) {
                        if (!gb.blur(WindowManager.getCurrentImage().getStack().getProcessor(currentSlice), Float.parseFloat(radiusString))) {
                            return;
                        }
                    }
                    WindowManager.getCurrentImage().updateAndDraw();
                    log("Gaussian blurred the current image with radius " + Float.parseFloat(radiusString) + ".");
                    break;
                }
                case "lutFire":
                    if (WindowManager.getCurrentImage() == null) {
                        logError("No open image.");
                        return;
                    }
                    IJ.run("Fire");
                    break;
                case "lutSpectrum":
                    if (WindowManager.getCurrentImage() == null) {
                        logError("No open image.");
                        return;
                    }
                    IJ.run("Spectrum");
                    break;
                case "histogram":
                    if (WindowManager.getCurrentImage() == null) {
                        logError("No open image.");
                        return;
                    }
                    IJ.run("Histogram");
                    break;
                case "openImage":
                    (new Opener()).open();
                    break;
                case "saveImageAsTiff": {
                    ImagePlus image = WindowManager.getCurrentImage();
                    if (image == null) {
                        logError("No open image.");
                        return;
                    }
                    FileSaver fs = new FileSaver(image);
                    if (fs.saveAsTiff()) {
                        log("Tiff file is saved.");
                    }
                    image.updateAndDraw();
                    break;
                }
                case "saveImageAsBmp": {
                    ImagePlus image = WindowManager.getCurrentImage();
                    if (image == null) {
                        logError("No open image.");
                        return;
                    }
                    FileSaver fs = new FileSaver(image);
                    if (fs.saveAsBmp()) {
                        log("Bmp file is saved.");
                    }
                    image.updateAndDraw();
                    break;
                }
                case "saveMessages": {
                    JFileChooser jfc = new JFileChooser(currentDirectory);
                    jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    jfc.setDialogTitle("Save messages...");
                    jfc.showSaveDialog(this);
                    if (jfc.getSelectedFile() == null) {
                        return;
                    }
                    if (jfc.getSelectedFile().exists()) {
                        currentDirectory = jfc.getCurrentDirectory().toString();
                        int resp = JOptionPane.showConfirmDialog(this,
                                "Overwrite existing file?", "Confirmation",
                                JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.QUESTION_MESSAGE);
                        if (resp == JOptionPane.CANCEL_OPTION) {
                            return;
                        }
                    }
                    try {
                        try (BufferedWriter out = new BufferedWriter(new FileWriter(jfc.getSelectedFile().getAbsolutePath()))) {
                            out.write(log.getText());
                        }
                    } catch (IOException ioe) {
                        logError("Could not save messages.");
                    }
                    break;
                }
                case "clearMessages":
                    log.setText("");
                    break;
                case "openLsmImage": {
                    JFileChooser jfc = new JFileChooser(currentDirectory);
                    jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    jfc.setDialogTitle("Open LSM image...");
                    jfc.showOpenDialog(this);
                    if (jfc.getSelectedFile() == null) {
                        return;
                    }
                    if (!jfc.getSelectedFile().exists()) {
                        logError("Selected file does not exist.");
                        return;
                    }
                    try {
                        currentDirectory = jfc.getCurrentDirectory().toString();
                        boolean close = false;
                        boolean resultsWindow = false;
                        while (WindowManager.getCurrentImage() != null) {
                            WindowManager.getCurrentImage().close();
                        }

                        resetAllButtonColors();

                        File imageFile = jfc.getSelectedFile();
                        (new Opener()).open(imageFile.getAbsolutePath());
                        WindowManager.putBehind();
                        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "split"));
                        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "setAcceptorInAImage"));
                        WindowManager.putBehind();
                        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "setDonorInAImage"));
                        WindowManager.putBehind();
                        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "setDonorInDImage"));
                    } catch (Exception ex) {
                        logError("Could not open and set the selected LSM image.");
                        logException(ex.getMessage(), ex);
                    }
                    break;
                }
                case "setDonorInDImage": {
                    ImagePlus ip = WindowManager.getCurrentImage();
                    if (ip == null) {
                        logError("No image is selected.");
                        setDonorInDImageButton.setBackground(originalButtonColor);
                        setDonorInDImageButton.setOpaque(false);
                        setDonorInDImageButton.setBorderPainted(true);
                        return;
                    }
                    if (ip.isHyperStack()) {
                        logError("Current image is a hyperstack.");
                        donorInDImage = null;
                        setDonorInDImageButton.setBackground(originalButtonColor);
                        setDonorInDImageButton.setOpaque(false);
                        setDonorInDImageButton.setBorderPainted(true);
                        return;
                    }
                    if (donorInAImage != null && donorInAImage.equals(ip)) {
                        logError("This image has already been set as transfer channel.");
                        donorInDImage = null;
                        setDonorInDImageButton.setBackground(originalButtonColor);
                        setDonorInDImageButton.setOpaque(false);
                        setDonorInDImageButton.setBorderPainted(true);
                        return;
                    } else if (acceptorInAImage != null && acceptorInAImage.equals(ip)) {
                        logError("This image has already been set as acceptor channel.");
                        donorInDImage = null;
                        setDonorInDImageButton.setBackground(originalButtonColor);
                        setDonorInDImageButton.setOpaque(false);
                        setDonorInDImageButton.setBorderPainted(true);
                        return;
                    }
                    if (donorInAImage != null && ip.getImageStackSize() != donorInAImage.getImageStackSize()) {
                        logError("Transfer channel contains " + donorInAImage.getImageStackSize() + " image(s), not " + ip.getImageStackSize() + ".");
                        donorInDImage = null;
                        setDonorInDImageButton.setBackground(originalButtonColor);
                        setDonorInDImageButton.setOpaque(false);
                        setDonorInDImageButton.setBorderPainted(true);
                        return;
                    } else if (acceptorInAImage != null && ip.getImageStackSize() != acceptorInAImage.getImageStackSize()) {
                        logError("Acceptor channel contains " + acceptorInAImage.getImageStackSize() + " image(s), not " + ip.getImageStackSize() + ".");
                        donorInDImage = null;
                        setDonorInDImageButton.setBackground(originalButtonColor);
                        setDonorInDImageButton.setOpaque(false);
                        setDonorInDImageButton.setBorderPainted(true);
                        return;
                    } else if (ip.getImageStackSize() > 1) {
                        logWarning("A stack has been set. Thresholds have to be set one by one for the images in it.");
                    }
                    donorInDImage = ip;
                    donorInDImage.setTitle("Donor channel - " + new Date().toString());
                    if (ip.getImageStackSize() > 1) {
                        new StackConverter(donorInDImage).convertToGray32();
                    } else {
                        new ImageConverter(donorInDImage).convertToGray32();
                    }
                    if (automaticallyProcessedFiles == null) {
                        currentlyProcessedFileName = null;
                    }
                    setDonorInDImageButton.setBackground(greenColor);
                    setDonorInDImageButton.setOpaque(true);
                    setDonorInDImageButton.setBorderPainted(false);
                    break;
                }
                case "setDonorInAImage": {
                    ImagePlus ip = WindowManager.getCurrentImage();
                    if (ip == null) {
                        logError("No image is selected.");
                        setDonorInAImageButton.setBackground(originalButtonColor);
                        setDonorInAImageButton.setOpaque(false);
                        setDonorInAImageButton.setBorderPainted(true);
                        return;
                    }
                    if (ip.isHyperStack()) {
                        logError("Current image is a hyperstack.");
                        donorInAImage = null;
                        setDonorInAImageButton.setBackground(originalButtonColor);
                        setDonorInAImageButton.setOpaque(false);
                        setDonorInAImageButton.setBorderPainted(true);
                        return;
                    }
                    if (donorInDImage != null && donorInDImage.equals(ip)) {
                        logError("This image has already been set as donor channel.");
                        donorInAImage = null;
                        setDonorInAImageButton.setBackground(originalButtonColor);
                        setDonorInAImageButton.setOpaque(false);
                        setDonorInAImageButton.setBorderPainted(true);
                        return;
                    } else if (acceptorInAImage != null && acceptorInAImage.equals(ip)) {
                        logError("This image has already been set as acceptor channel.");
                        donorInAImage = null;
                        setDonorInAImageButton.setBackground(originalButtonColor);
                        setDonorInAImageButton.setOpaque(false);
                        setDonorInAImageButton.setBorderPainted(true);
                        return;
                    }
                    if (donorInDImage != null && ip.getImageStackSize() != donorInDImage.getImageStackSize()) {
                        logError("Donor channel contains " + donorInDImage.getImageStackSize() + " image(s), not " + ip.getImageStackSize() + ".");
                        donorInAImage = null;
                        setDonorInAImageButton.setBackground(originalButtonColor);
                        setDonorInAImageButton.setOpaque(false);
                        setDonorInAImageButton.setBorderPainted(true);
                        return;
                    } else if (acceptorInAImage != null && ip.getImageStackSize() != acceptorInAImage.getImageStackSize()) {
                        logError("Acceptor channel contains " + acceptorInAImage.getImageStackSize() + " image(s), not " + ip.getImageStackSize() + ".");
                        donorInAImage = null;
                        setDonorInAImageButton.setBackground(originalButtonColor);
                        setDonorInAImageButton.setOpaque(false);
                        setDonorInAImageButton.setBorderPainted(true);
                        return;
                    } else if (ip.getImageStackSize() > 1) {
                        logWarning("A stack has been set. Thresholds have to be set one by one for the images in it.");
                    }
                    donorInAImage = ip;
                    donorInAImage.setTitle("Transfer channel - " + new Date().toString());
                    if (ip.getImageStackSize() > 1) {
                        new StackConverter(donorInAImage).convertToGray32();
                    } else {
                        new ImageConverter(donorInAImage).convertToGray32();
                    }
                    setDonorInAImageButton.setBackground(greenColor);
                    setDonorInAImageButton.setOpaque(true);
                    setDonorInAImageButton.setBorderPainted(false);
                    break;
                }
                case "setAcceptorInAImage": {
                    ImagePlus ip = WindowManager.getCurrentImage();
                    if (ip == null) {
                        logError("No image is selected.");
                        setAcceptorInAImageButton.setBackground(originalButtonColor);
                        setAcceptorInAImageButton.setOpaque(false);
                        setAcceptorInAImageButton.setBorderPainted(true);
                        return;
                    }
                    if (ip.isHyperStack()) {
                        logError("Current image is a hyperstack.");
                        acceptorInAImage = null;
                        setAcceptorInAImageButton.setBackground(originalButtonColor);
                        setAcceptorInAImageButton.setOpaque(false);
                        setAcceptorInAImageButton.setBorderPainted(true);
                        return;
                    }
                    if (donorInDImage != null && donorInDImage.equals(ip)) {
                        logError("This image has already been set as donor channel.");
                        acceptorInAImage = null;
                        setAcceptorInAImageButton.setBackground(originalButtonColor);
                        setAcceptorInAImageButton.setOpaque(false);
                        setAcceptorInAImageButton.setBorderPainted(true);

                        return;
                    } else if (donorInAImage != null && donorInAImage.equals(ip)) {
                        logError("This image has already been set as transfer channel.");
                        acceptorInAImage = null;
                        setAcceptorInAImageButton.setBackground(originalButtonColor);
                        setAcceptorInAImageButton.setOpaque(false);
                        setAcceptorInAImageButton.setBorderPainted(true);
                        return;
                    }
                    if (donorInDImage != null && ip.getImageStackSize() != donorInDImage.getImageStackSize()) {
                        logError("Donor channel contains " + donorInDImage.getImageStackSize() + " image(s), not " + ip.getImageStackSize() + ".");
                        acceptorInAImage = null;
                        setAcceptorInAImageButton.setBackground(originalButtonColor);
                        setAcceptorInAImageButton.setOpaque(false);
                        setAcceptorInAImageButton.setBorderPainted(true);
                        return;
                    } else if (donorInAImage != null && ip.getImageStackSize() != donorInAImage.getImageStackSize()) {
                        logError("Transfer channel contains " + donorInAImage.getImageStackSize() + " image(s), not " + ip.getImageStackSize() + ".");
                        acceptorInAImage = null;
                        setAcceptorInAImageButton.setBackground(originalButtonColor);
                        setAcceptorInAImageButton.setOpaque(false);
                        setAcceptorInAImageButton.setBorderPainted(true);
                        return;
                    } else if (ip.getImageStackSize() > 1) {
                        logWarning("A stack has been set. Thresholds have to be set one by one for the images in it.");
                    }
                    acceptorInAImage = ip;
                    acceptorInAImage.setTitle("Acceptor channel - " + new Date().toString());
                    if (ip.getImageStackSize() > 1) {
                        new StackConverter(acceptorInAImage).convertToGray32();
                    } else {
                        new ImageConverter(acceptorInAImage).convertToGray32();
                    }
                    setAcceptorInAImageButton.setBackground(greenColor);
                    setAcceptorInAImageButton.setOpaque(true);
                    setAcceptorInAImageButton.setBorderPainted(false);
                    break;
                }
                case "copyRoi":
                    if (donorInDImage == null) {
                        logError("No image is set as donor channel.");
                        return;
                    }
                    if (donorInDImage.getRoi() != null) {
                        if (donorInAImage != null) {
                            donorInAImage.setRoi(donorInDImage.getRoi());
                        }
                        if (acceptorInAImage != null) {
                            acceptorInAImage.setRoi(donorInDImage.getRoi());
                        }
                    } else {
                        if (donorInAImage != null) {
                            donorInAImage.killRoi();
                        }
                        if (acceptorInAImage != null) {
                            acceptorInAImage.killRoi();
                        }
                    }
                    break;
                case "subtractDonorInDImage": {
                    if (donorInDImage == null) {
                        logError("No image is set as donor channel.");
                        return;
                    } else if (donorInDImage.getRoi() == null) {
                        logError("No ROI is defined for donor channel.");
                        return;
                    }
                    float autofl = 0;
                    if (!autoflDInDField.getText().trim().isEmpty()) {
                        autofl = Float.parseFloat(autoflDInDField.getText().trim());
                    }
                    DecimalFormat df = new DecimalFormat("#.#");
                    int width = donorInDImage.getWidth();
                    int height = donorInDImage.getHeight();
                    int nSlices = donorInDImage.getImageStackSize();
                    donorInDImageSave = new ImageStack(donorInDImage.getProcessor().getWidth(), donorInDImage.getProcessor().getHeight());
                    for (int currentSlice = 1; currentSlice <= nSlices; currentSlice++) {
                        double sum = 0;
                        int count = 0;
                        for (int i = 0; i < width; i++) {
                            for (int j = 0; j < height; j++) {
                                if (donorInDImage.getRoi().contains(i, j)) {
                                    sum += donorInDImage.getStack().getProcessor(currentSlice).getPixelValue(i, j);
                                    count++;
                                }
                            }
                        }
                        float backgroundAvg = (float) (sum / count);

                        backgroundAvg = backgroundAvg + autofl;

                        float i = 0;
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                i = donorInDImage.getStack().getProcessor(currentSlice).getPixelValue(x, y);
                                i = i - backgroundAvg;
                                if (i < 0) {
                                    i = 0;
                                }
                                donorInDImage.getStack().getProcessor(currentSlice).putPixelValue(x, y, i);
                            }
                        }
                        FloatProcessor flp = new FloatProcessor(donorInDImage.getStack().getProcessor(currentSlice).getWidth(), donorInDImage.getStack().getProcessor(currentSlice).getHeight());
                        flp.setPixels(currentSlice, (FloatProcessor) donorInDImage.getStack().getProcessor(currentSlice).duplicate());
                        donorInDImageSave.addSlice("" + currentSlice, flp);
                        if (nSlices == 1) {
                            log("Subtracted background " + (autofl > 0 ? "and autofluorescence " : "") + "(" + df.format(backgroundAvg) + ") of donor channel.");
                        } else {
                            log("Subtracted background " + (autofl > 0 ? "and autofluorescence " : "") + "(" + df.format(backgroundAvg) + ") of slice " + currentSlice + " of donor channel.");
                        }
                    }
                    donorInDImage.updateAndDraw();
                    donorInDImage.killRoi();
                    donorInDImageSave.setColorModel(donorInDImage.getProcessor().getColorModel());
                    subtractDonorInDImageButton.setBackground(greenColor);
                    subtractDonorInDImageButton.setOpaque(true);
                    subtractDonorInDImageButton.setBorderPainted(false);
                    break;
                }
                case "subtractDonorInAImage": {
                    if (donorInAImage == null) {
                        logError("No image is set as transfer channel.");
                        return;
                    } else if (donorInAImage.getRoi() == null) {
                        logError("No ROI is defined for transfer channel.");
                        return;
                    }
                    float autofl = 0;
                    if (!autoflAInDField.getText().trim().isEmpty()) {
                        autofl = Float.parseFloat(autoflAInDField.getText().trim());
                    }
                    DecimalFormat df = new DecimalFormat("#.#");
                    int width = donorInAImage.getWidth();
                    int height = donorInAImage.getHeight();
                    int nSlices = donorInAImage.getImageStackSize();
                    donorInAImageSave = new ImageStack(donorInAImage.getProcessor().getWidth(), donorInAImage.getProcessor().getHeight());
                    for (int currentSlice = 1; currentSlice <= nSlices; currentSlice++) {
                        double sum = 0;
                        int count = 0;
                        for (int i = 0; i < width; i++) {
                            for (int j = 0; j < height; j++) {
                                if (donorInAImage.getRoi().contains(i, j)) {
                                    sum += donorInAImage.getStack().getProcessor(currentSlice).getPixelValue(i, j);
                                    count++;
                                }
                            }
                        }
                        float backgroundAvg = (float) (sum / count);

                        backgroundAvg = backgroundAvg + autofl;

                        float i = 0;
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                i = donorInAImage.getStack().getProcessor(currentSlice).getPixelValue(x, y);
                                i = i - backgroundAvg;
                                if (i < 0) {
                                    i = 0;
                                }
                                donorInAImage.getStack().getProcessor(currentSlice).putPixelValue(x, y, i);
                            }
                        }
                        FloatProcessor flp = new FloatProcessor(donorInAImage.getStack().getProcessor(currentSlice).getWidth(), donorInAImage.getStack().getProcessor(currentSlice).getHeight());
                        flp.setPixels(currentSlice, (FloatProcessor) donorInAImage.getStack().getProcessor(currentSlice).duplicate());
                        donorInAImageSave.addSlice("" + currentSlice, flp);
                        if (nSlices == 1) {
                            log("Subtracted background " + (autofl > 0 ? "and autofluorescence " : "") + "(" + df.format(backgroundAvg) + ") of transfer channel.");
                        } else {
                            log("Subtracted background " + (autofl > 0 ? "and autofluorescence " : "") + "(" + df.format(backgroundAvg) + ") of slice " + currentSlice + " of transfer channel.");
                        }
                    }
                    donorInAImage.updateAndDraw();
                    donorInAImage.killRoi();
                    donorInAImageSave.setColorModel(donorInAImage.getProcessor().getColorModel());
                    subtractDonorInAImageButton.setBackground(greenColor);
                    subtractDonorInAImageButton.setOpaque(true);
                    subtractDonorInAImageButton.setBorderPainted(false);
                    break;
                }
                case "subtractAcceptorInAImage": {
                    if (acceptorInAImage == null) {
                        logError("No image is set as acceptor channel.");
                        return;
                    } else if (acceptorInAImage.getRoi() == null) {
                        logError("No ROI is defined for acceptor channel.");
                        return;
                    }
                    float autofl = 0;
                    if (!autoflAInAField.getText().trim().isEmpty()) {
                        autofl = Float.parseFloat(autoflAInAField.getText().trim());
                    }
                    DecimalFormat df = new DecimalFormat("#.#");
                    int width = acceptorInAImage.getWidth();
                    int height = acceptorInAImage.getHeight();
                    int nSlices = acceptorInAImage.getImageStackSize();
                    acceptorInAImageSave = new ImageStack(acceptorInAImage.getProcessor().getWidth(), acceptorInAImage.getProcessor().getHeight());
                    for (int currentSlice = 1; currentSlice <= nSlices; currentSlice++) {
                        double sum = 0;
                        int count = 0;
                        for (int i = 0; i < width; i++) {
                            for (int j = 0; j < height; j++) {
                                if (acceptorInAImage.getRoi().contains(i, j)) {
                                    sum += acceptorInAImage.getStack().getProcessor(currentSlice).getPixelValue(i, j);
                                    count++;
                                }
                            }
                        }
                        float backgroundAvg = (float) (sum / count);

                        backgroundAvg = backgroundAvg + autofl;

                        float i = 0;
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                i = acceptorInAImage.getStack().getProcessor(currentSlice).getPixelValue(x, y);
                                i = i - backgroundAvg;
                                if (i < 0) {
                                    i = 0;
                                }
                                acceptorInAImage.getStack().getProcessor(currentSlice).putPixelValue(x, y, i);
                            }
                        }
                        FloatProcessor flp = new FloatProcessor(acceptorInAImage.getStack().getProcessor(currentSlice).getWidth(), acceptorInAImage.getStack().getProcessor(currentSlice).getHeight());
                        flp.setPixels(currentSlice, (FloatProcessor) acceptorInAImage.getStack().getProcessor(currentSlice).duplicate());
                        acceptorInAImageSave.addSlice("" + currentSlice, flp);
                        if (nSlices == 1) {
                            log("Subtracted background " + (autofl > 0 ? "and autofluorescence " : "") + "(" + df.format(backgroundAvg) + ") of acceptor channel.");
                        } else {
                            log("Subtracted background " + (autofl > 0 ? "and autofluorescence " : "") + "(" + df.format(backgroundAvg) + ") of slice " + currentSlice + " of acceptor channel.");
                        }
                    }
                    acceptorInAImage.updateAndDraw();
                    acceptorInAImage.killRoi();
                    acceptorInAImageSave.setColorModel(acceptorInAImage.getProcessor().getColorModel());
                    subtractAcceptorInAImageButton.setBackground(greenColor);
                    subtractAcceptorInAImageButton.setOpaque(true);
                    subtractAcceptorInAImageButton.setBorderPainted(false);
                    break;
                }
                case "thresholdDonorInDImage":
                    if (donorInDImage == null) {
                        logError("No image is set as donor channel.");
                        return;
                    }
                    IJ.selectWindow(donorInDImage.getTitle());
                    IJ.run("Threshold...");
                    thresholdDonorInDImageButton.setBackground(greenColor);
                    thresholdDonorInDImageButton.setOpaque(true);
                    thresholdDonorInDImageButton.setBorderPainted(false);
                    break;
                case "thresholdDonorInAImage":
                    if (donorInAImage == null) {
                        logError("No image is set as transfer channel.");
                        return;
                    }
                    IJ.selectWindow(donorInAImage.getTitle());
                    IJ.run("Threshold...");
                    thresholdDonorInAImageButton.setBackground(greenColor);
                    thresholdDonorInAImageButton.setOpaque(true);
                    thresholdDonorInAImageButton.setBorderPainted(false);
                    break;
                case "thresholdAcceptorInAImage":
                    if (acceptorInAImage == null) {
                        logError("No image is set as acceptor channel.");
                        return;
                    }
                    IJ.selectWindow(acceptorInAImage.getTitle());
                    IJ.run("Threshold...");
                    thresholdAcceptorInAImageButton.setBackground(greenColor);
                    thresholdAcceptorInAImageButton.setOpaque(true);
                    thresholdAcceptorInAImageButton.setBorderPainted(false);
                    break;
                case "resetDD": {
                    if (donorInDImage == null) {
                        logError("No image is set as donor channel.");
                        return;
                    }
                    if (donorInDImageSave == null) {
                        logError("No saved image.");
                        return;
                    }
                    int nSlices = donorInDImage.getImageStackSize();
                    ImageStack newStack = new ImageStack(donorInDImageSave.getWidth(), donorInDImageSave.getHeight());
                    for (int currentSlice = 1; currentSlice <= nSlices; currentSlice++) {
                        FloatProcessor flp = new FloatProcessor(donorInDImageSave.getProcessor(currentSlice).getWidth(), donorInDImageSave.getProcessor(currentSlice).getHeight());
                        flp.setPixels(currentSlice, (FloatProcessor) donorInDImageSave.getProcessor(currentSlice).duplicate());
                        newStack.addSlice("" + currentSlice, flp);
                    }
                    donorInDImage.setStack(donorInDImage.getTitle(), newStack);
                    donorInDImage.getProcessor().setColorModel(donorInDImageSave.getColorModel());
                    donorInDImage.updateAndDraw();
                    thresholdDonorInDImageButton.setBackground(originalButtonColor);
                    thresholdDonorInDImageButton.setOpaque(false);
                    thresholdDonorInDImageButton.setBorderPainted(true);
                    smoothDonorInDImageButton.setBackground(originalButtonColor);
                    smoothDonorInDImageButton.setOpaque(false);
                    smoothDonorInDImageButton.setBorderPainted(true);
                    break;
                }
                case "resetDA": {
                    if (donorInAImage == null) {
                        logError("No image is set as transfer channel.");
                        return;
                    }
                    if (donorInAImageSave == null) {
                        logError("No saved image.");
                        return;
                    }
                    int nSlices = donorInAImage.getImageStackSize();
                    ImageStack newStack = new ImageStack(donorInAImageSave.getWidth(), donorInAImageSave.getHeight());
                    for (int currentSlice = 1; currentSlice <= nSlices; currentSlice++) {
                        FloatProcessor flp = new FloatProcessor(donorInAImageSave.getProcessor(currentSlice).getWidth(), donorInAImageSave.getProcessor(currentSlice).getHeight());
                        flp.setPixels(currentSlice, (FloatProcessor) donorInAImageSave.getProcessor(currentSlice).duplicate());
                        newStack.addSlice("" + currentSlice, flp);
                    }
                    donorInAImage.setStack(donorInAImage.getTitle(), newStack);
                    donorInAImage.getProcessor().setColorModel(donorInAImageSave.getColorModel());
                    donorInAImage.updateAndDraw();
                    thresholdDonorInAImageButton.setBackground(originalButtonColor);
                    thresholdDonorInAImageButton.setOpaque(false);
                    thresholdDonorInAImageButton.setBorderPainted(true);
                    smoothDonorInAImageButton.setBackground(originalButtonColor);
                    smoothDonorInAImageButton.setOpaque(false);
                    smoothDonorInAImageButton.setBorderPainted(true);
                    break;
                }
                case "resetAA": {
                    if (acceptorInAImage == null) {
                        logError("No image is set as acceptor channel.");
                        return;
                    }
                    if (acceptorInAImageSave == null) {
                        logError("No saved image.");
                        return;
                    }
                    int nSlices = acceptorInAImage.getImageStackSize();
                    ImageStack newStack = new ImageStack(acceptorInAImageSave.getWidth(), acceptorInAImageSave.getHeight());
                    for (int currentSlice = 1; currentSlice <= nSlices; currentSlice++) {
                        FloatProcessor flp = new FloatProcessor(acceptorInAImageSave.getProcessor(currentSlice).getWidth(), acceptorInAImageSave.getProcessor(currentSlice).getHeight());
                        flp.setPixels(currentSlice, (FloatProcessor) acceptorInAImageSave.getProcessor(currentSlice).duplicate());
                        newStack.addSlice("" + currentSlice, flp);
                    }
                    acceptorInAImage.setStack(acceptorInAImage.getTitle(), newStack);
                    acceptorInAImage.getProcessor().setColorModel(acceptorInAImageSave.getColorModel());
                    acceptorInAImage.updateAndDraw();
                    thresholdAcceptorInAImageButton.setBackground(originalButtonColor);
                    thresholdAcceptorInAImageButton.setOpaque(false);
                    thresholdAcceptorInAImageButton.setBorderPainted(true);
                    smoothAcceptorInAImageButton.setBackground(originalButtonColor);
                    smoothAcceptorInAImageButton.setOpaque(false);
                    smoothAcceptorInAImageButton.setBorderPainted(true);
                    break;
                }
                case "smoothDD":
                    if (donorInDImage == null) {
                        logError("No image is set as donor channel.");
                    } else {
                        if (radiusFieldDD.getText().trim().isEmpty()) {
                            logError("Radius has to be given for Gaussian blur.");
                        } else {
                            double radius = 0;
                            try {
                                radius = Double.parseDouble(radiusFieldDD.getText().trim());
                            } catch (NumberFormatException ex) {
                                logError("Radius has to be given for Gaussian blur.");
                                return;
                            }
                            GaussianBlur gb = new GaussianBlur();
                            int nSlices = donorInDImage.getImageStackSize();
                            for (int currentSlice = 1; currentSlice <= nSlices; currentSlice++) {
                                if (!gb.blur(donorInDImage.getStack().getProcessor(currentSlice), radius)) {
                                    return;
                                }
                            }
                            donorInDImage.updateAndDraw();
                            smoothDonorInDImageButton.setBackground(greenColor);
                            smoothDonorInDImageButton.setOpaque(true);
                            smoothDonorInDImageButton.setBorderPainted(false);
                            log("Gaussian blurred donor channel.");
                        }
                    }
                    break;
                case "smoothDA":
                    if (donorInAImage == null) {
                        logError("No image is set as transfer channel.");
                    } else {
                        if (radiusFieldDA.getText().trim().isEmpty()) {
                            logError("Radius has to be given for Gaussian blur.");
                        } else {
                            double radius = 0;
                            try {
                                radius = Double.parseDouble(radiusFieldDA.getText().trim());
                            } catch (NumberFormatException ex) {
                                logError("Radius has to be given for Gaussian blur.");
                                return;
                            }
                            GaussianBlur gb = new GaussianBlur();
                            int nSlices = donorInAImage.getImageStackSize();
                            for (int currentSlice = 1; currentSlice <= nSlices; currentSlice++) {
                                if (!gb.blur(donorInAImage.getStack().getProcessor(currentSlice), radius)) {
                                    return;
                                }
                            }
                            donorInAImage.updateAndDraw();
                            smoothDonorInAImageButton.setBackground(greenColor);
                            smoothDonorInAImageButton.setOpaque(true);
                            smoothDonorInAImageButton.setBorderPainted(false);
                            log("Gaussian blurred transfer channel.");
                        }
                    }
                    break;
                case "smoothAA":
                    if (acceptorInAImage == null) {
                        logError("No image is set as acceptor channel.");
                    } else {
                        if (radiusFieldAA.getText().trim().isEmpty()) {
                            logError("Radius has to be given for Gaussian blur.");
                        } else {
                            double radius = 0;
                            try {
                                radius = Double.parseDouble(radiusFieldAA.getText().trim());
                            } catch (NumberFormatException ex) {
                                logError("Radius has to be given for Gaussian blur.");
                                return;
                            }
                            GaussianBlur gb = new GaussianBlur();
                            int nSlices = acceptorInAImage.getImageStackSize();
                            for (int currentSlice = 1; currentSlice <= nSlices; currentSlice++) {
                                if (!gb.blur(acceptorInAImage.getStack().getProcessor(currentSlice), radius)) {
                                    return;
                                }
                            }
                            acceptorInAImage.updateAndDraw();
                            smoothAcceptorInAImageButton.setBackground(greenColor);
                            smoothAcceptorInAImageButton.setOpaque(true);
                            smoothAcceptorInAImageButton.setBorderPainted(false);
                            log("Gaussian blurred acceptor channel.");
                        }
                    }
                    break;
                case "useLsmImages":
                    if (useLsmImages.isSelected()) {
                        setDonorInDImageButton.setText("Open & Set LSM");
                        setDonorInDImageButton.setActionCommand("openLsmImage");
                        setDonorInAImageButton.setEnabled(false);
                        setAcceptorInAImageButton.setEnabled(false);
                    } else {
                        setDonorInDImageButton.setText("Set image");
                        setDonorInDImageButton.setActionCommand("setDonorInDImage");
                        setDonorInAImageButton.setEnabled(true);
                        setAcceptorInAImageButton.setEnabled(true);
                    }
                    break;
                case "calculateS1S3Button":
                    if (s1S3Dialog != null) {
                        s1S3Dialog.setVisible(false);
                        s1S3Dialog.dispose();
                    }
                    s1S3Dialog = new S1S3Dialog(this);
                    s1S3Dialog.setVisible(true);
                    break;
                case "calculateS2S4Button":
                    if (s2S4Dialog != null) {
                        s2S4Dialog.setVisible(false);
                        s2S4Dialog.dispose();
                    }
                    s2S4Dialog = new S2S4Dialog(this);
                    s2S4Dialog.setVisible(true);
                    break;
                case "calculateAlphaButton":
                    if (alphaDialog != null) {
                        alphaDialog.setVisible(true);
                    } else {
                        alphaDialog = new AlphaDialog(this);
                        alphaDialog.setVisible(true);
                    }
                    break;
                case "createFretImage":
                    if (donorInDImage == null) {
                        logError("No image is set as donor channel.");
                        return;
                    } else if (donorInAImage == null) {
                        logError("No image is set as transfer channel.");
                        return;
                    } else if (acceptorInAImage == null) {
                        logError("No image is set as acceptor channel.");
                        return;
                    } else {
                        if (autoThresholdingCB.isSelected()) {
                            if (autoThresholdMin.getText().trim().isEmpty()) {
                                logError("Auto-threshold min value has to be given.");
                                return;
                            } else if (autoThresholdMax.getText().trim().isEmpty()) {
                                logError("Auto-threshold max value has to be given.");
                                return;
                            }
                        }
                        if (s1Field.getText().trim().isEmpty()) {
                            logError("S1 factor has to be given.");
                            return;
                        } else if (s2Field.getText().trim().isEmpty()) {
                            logError("S2 factor has to be given.");
                            return;
                        } else if (s3Field.getText().trim().isEmpty()) {
                            logError("S3 factor has to be given.");
                            return;
                        } else if (s4Field.getText().trim().isEmpty()) {
                            logError("S4 factor has to be given.");
                            return;
                        } else if (alphaField.getText().trim().isEmpty()) {
                            logError("Alpha factor has to be given.");
                            return;
                        } else {
                            double s1Factor = 0;
                            try {
                                s1Factor = Double.parseDouble(s1Field.getText().trim());
                            } catch (NumberFormatException ex) {
                                logError("S1 factor has to be given.");
                                return;
                            }
                            if (s1Factor <= 0) {
                                logWarning("S1 factor should be higher than 0.");
                            }
                            double s2Factor = 0;
                            try {
                                s2Factor = Double.parseDouble(s2Field.getText().trim());
                            } catch (NumberFormatException ex) {
                                logError("S2 factor has to be given.");
                                return;
                            }
                            if (s2Factor <= 0) {
                                logWarning("S2 factor should be higher than 0.");
                            }
                            double s3Factor = 0;
                            try {
                                s3Factor = Double.parseDouble(s3Field.getText().trim());
                            } catch (NumberFormatException ex) {
                                logError("S3 factor has to be given.");
                                return;
                            }
                            if (s3Factor < 0) {
                                logWarning("S3 factor should be higher than 0.");
                            }
                            double s4Factor = 0;
                            try {
                                s4Factor = Double.parseDouble(s4Field.getText().trim());
                            } catch (NumberFormatException ex) {
                                logError("S4 factor has to be given.");
                                return;
                            }
                            if (s4Factor < 0) {
                                logWarning("S4 factor should be higher than 0.");
                            }
                            double alphaFactor = 0;
                            try {
                                alphaFactor = Double.parseDouble(alphaField.getText().trim());
                            } catch (NumberFormatException ex) {
                                logError("Alpha factor has to be given.");
                                return;
                            }

                            double autoThMin = 0;
                            double autoThMax = 0;
                            if (autoThresholdingCB.isSelected()) {
                                try {
                                    autoThMin = Double.parseDouble(autoThresholdMin.getText().trim());
                                } catch (NumberFormatException ex) {
                                    logError("Auto-threshold min value has to be given.");
                                    return;
                                }

                                try {
                                    autoThMax = Double.parseDouble(autoThresholdMax.getText().trim());
                                } catch (NumberFormatException ex) {
                                    logError("Auto-threshold max value has to be given.");
                                    return;
                                }
                            }

                            int nSlices = donorInDImage.getImageStackSize();
                            ImageStack transferStack = new ImageStack(donorInDImage.getProcessor().getWidth(), donorInDImage.getProcessor().getHeight());
                            for (int currentSlice = 1; currentSlice <= nSlices; currentSlice++) {
                                ImageProcessor ipDD = donorInDImage.getStack().getProcessor(currentSlice).duplicate();
                                ImageProcessor ipDA = donorInAImage.getStack().getProcessor(currentSlice).duplicate();
                                ImageProcessor ipAA = acceptorInAImage.getStack().getProcessor(currentSlice).duplicate();

                                float[] ipDDP = (float[]) ipDD.getPixels();
                                float[] ipDAP = (float[]) ipDA.getPixels();
                                float[] ipAAP = (float[]) ipAA.getPixels();

                                for (int i = 0; i < ipDDP.length; i++) {
                                    if (!Float.isNaN(ipDDP[i]) && !Float.isNaN(ipDAP[i]) && !Float.isNaN(ipAAP[i])) {
                                        ipDDP[i] = (float) ((s1Factor * s2Factor * (ipDAP[i] * ((double) 1 - s3Factor * s4Factor) - ipDDP[i] * (s1Factor - s2Factor * s3Factor) - ipAAP[i] * (s2Factor - s1Factor * s4Factor))) / ((s1Factor - s2Factor * s3Factor) * (ipDDP[i] * s2Factor - ipDAP[i] * s4Factor) * alphaFactor));
                                    } else {
                                        ipDDP[i] = Float.NaN;
                                    }
                                }

                                for (int i = 0; i < ipDDP.length; i++) {
                                    ipDDP[i] = ipDDP[i] / ((float) 1 + ipDDP[i]);
                                }

                                int width = ipDD.getWidth();
                                int height = ipDD.getHeight();
                                float[][] tiPoints = new float[width][height];
                                for (int i = 0; i < width; i++) {
                                    for (int j = 0; j < height; j++) {
                                        if (autoThresholdingCB.isSelected()) {
                                            if (ipDDP[width * j + i] >= autoThMin && ipDDP[width * j + i] <= autoThMax) {
                                                tiPoints[i][j] = ipDDP[width * j + i];
                                            } else {
                                                tiPoints[i][j] = Float.NaN;
                                            }
                                        } else {
                                            tiPoints[i][j] = ipDDP[width * j + i];
                                        }
                                    }
                                }

                                FloatProcessor tiFp = new FloatProcessor(tiPoints);
                                transferStack.addSlice("" + currentSlice, tiFp);
                            }
                            if (transferImage != null) {
                                transferImage.close();
                            }
                            transferImage = new ImagePlus("Transfer (FRET) image", transferStack);
                            transferImage.setCalibration(donorInDImage.getCalibration());
                            transferImage.show();

                            analyzer = new Analyzer();
                            resultsTable = Analyzer.getResultsTable();
                            resultsTable.setPrecision(3);
                            resultsTable.incrementCounter();
                            int widthTi = transferImage.getWidth();
                            int heightTi = transferImage.getHeight();
                            if (currentlyProcessedFileName != null) {
                                resultsTable.addLabel("File", currentlyProcessedFileName);
                            }
                            if (transferImage.getRoi() != null) {
                                Roi roi = transferImage.getRoi();
                                int count = 0;
                                int notNan = 0;
                                for (int i = 0; i < widthTi; i++) {
                                    for (int j = 0; j < heightTi; j++) {
                                        if (roi.contains(i, j)) {
                                            count++;
                                            if (transferImage.getStack().getProcessor(1).getPixelValue(i, j) >= -1) {
                                                notNan++;
                                            }
                                        }
                                    }
                                }
                                resultsTable.addValue("Pixels", count);
                                resultsTable.addValue("Not NaN p.", notNan);
                            } else {
                                int notNan = 0;
                                for (int i = 0; i < widthTi; i++) {
                                    for (int j = 0; j < heightTi; j++) {
                                        if (transferImage.getStack().getProcessor(1).getPixelValue(i, j) >= -1) {
                                            notNan++;
                                        }
                                    }
                                }
                                resultsTable.addValue("Pixels", widthTi * heightTi);
                                resultsTable.addValue("Not NaN p.", notNan);
                            }
                            ImageStatistics isMean = ImageStatistics.getStatistics(transferImage.getStack().getProcessor(1), Measurements.MEAN, null);
                            resultsTable.addValue("Mean", (float) isMean.mean);
                            ImageStatistics isMedian = ImageStatistics.getStatistics(transferImage.getStack().getProcessor(1), Measurements.MEDIAN, null);
                            resultsTable.addValue("Median", (float) isMedian.median);
                            ImageStatistics isStdDev = ImageStatistics.getStatistics(transferImage.getStack().getProcessor(1), Measurements.STD_DEV, null);
                            resultsTable.addValue("Std. dev.", (float) isStdDev.stdDev);
                            ImageStatistics isMinMax = ImageStatistics.getStatistics(transferImage.getStack().getProcessor(1), Measurements.MIN_MAX, null);
                            resultsTable.addValue("Min", (float) isMinMax.min);
                            resultsTable.addValue("Max", (float) isMinMax.max);
                            if (transferImage.getRoi() != null) {
                                donorInDImage.setRoi(transferImage.getRoi());
                                donorInAImage.setRoi(transferImage.getRoi());
                                acceptorInAImage.setRoi(transferImage.getRoi());
                            } else {
                                donorInDImage.killRoi();
                                donorInAImage.killRoi();
                                acceptorInAImage.killRoi();
                            }
                            analyzer.displayResults();
                            analyzer.updateHeadings();
                        }
                    }
                    donorInDImage.changes = false;
                    donorInAImage.changes = false;
                    acceptorInAImage.changes = false;
                    break;
                case "measureFretImage": {
                    if (transferImage == null) {
                        logError("Transfer (FRET) image is required.");
                        return;
                    }
                    resultsTable.incrementCounter();
                    int currentSlice = transferImage.getCurrentSlice();
                    int width = transferImage.getWidth();
                    int height = transferImage.getHeight();
                    if (currentlyProcessedFileName != null) {
                        resultsTable.addLabel("File", currentlyProcessedFileName);
                    }
                    ImageProcessor trProc = transferImage.getStack().getProcessor(currentSlice);
                    trProc.setRoi(transferImage.getRoi());
                    if (transferImage.getRoi() != null) {
                        Roi roi = transferImage.getRoi();
                        int count = 0;
                        int notNan = 0;
                        for (int i = 0; i < width; i++) {
                            for (int j = 0; j < height; j++) {
                                if (roi.contains(i, j)) {
                                    count++;
                                    if (trProc.getPixelValue(i, j) >= -1) {
                                        notNan++;
                                    }
                                }
                            }
                        }
                        resultsTable.addValue("Pixels", count);
                        resultsTable.addValue("Not NaN p.", notNan);
                    } else {
                        int notNan = 0;
                        for (int i = 0; i < width; i++) {
                            for (int j = 0; j < height; j++) {
                                if (!Float.isNaN(trProc.getPixelValue(i, j))) {
                                    notNan++;
                                }
                            }
                        }
                        resultsTable.addValue("Pixels", width * height);
                        resultsTable.addValue("Not NaN p.", notNan);
                    }
                    ImageStatistics isMean = ImageStatistics.getStatistics(trProc, Measurements.MEAN, null);
                    resultsTable.addValue("Mean", (float) isMean.mean);
                    ImageStatistics isMedian = ImageStatistics.getStatistics(trProc, Measurements.MEDIAN, null);
                    resultsTable.addValue("Median", (float) isMedian.median);
                    ImageStatistics isStdDev = ImageStatistics.getStatistics(trProc, Measurements.STD_DEV, null);
                    resultsTable.addValue("Std. dev.", (float) isStdDev.stdDev);
                    ImageStatistics isMinMax = ImageStatistics.getStatistics(trProc, Measurements.MIN_MAX, null);
                    resultsTable.addValue("Min", (float) isMinMax.min);
                    resultsTable.addValue("Max", (float) isMinMax.max);
                    if (transferImage.getRoi() != null) {
                        donorInDImage.setRoi(transferImage.getRoi());
                        donorInAImage.setRoi(transferImage.getRoi());
                        acceptorInAImage.setRoi(transferImage.getRoi());
                    } else {
                        donorInDImage.killRoi();
                        donorInAImage.killRoi();
                        acceptorInAImage.killRoi();
                    }
                    analyzer.displayResults();
                    analyzer.updateHeadings();
                    break;
                }
                case "semiAutomaticProcessing":
                    int choice = JOptionPane.showConfirmDialog(this, "Semi-automatic processing of images\n\nOpens and processes FRET images in a given directory. It works with\n"
                            + "Zeiss LSM images (tested with LSM 510 Version 4.0), which contain three\n"
                            + "channels:\n"
                            + "1. donor channel\n"
                            + "2. transfer channel\n"
                            + "3. acceptor channel\n\n"
                            + "The upper left corner (1/6 x 1/6 of the image) is considered as background.\n"
                            + "Threshold settings, creation of FRET image and measurements have to be\n"
                            + "made manually.\n\n"
                            + "Every previously opened image and result window will be closed when you\n"
                            + "press \"Ok\".\n\n"
                            + "Press \"Ok\" to select the directory. To continue with the next "
                            + "image, do\nnot close any windows, just press the \"Next\" button.\n", "Semi-automatic processing of images", JOptionPane.OK_CANCEL_OPTION);
                    if (choice == JOptionPane.YES_OPTION) {
                        currentlyProcessedFile = 0;
                        automaticallyProcessedFiles = null;
                        currentlyProcessedFileName = null;
                        WindowManager.closeAllWindows();
                        JFileChooser chooser = new JFileChooser(currentDirectory);
                        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                        chooser.setDialogTitle("Select directory");
                        chooser.setAcceptAllFileFilterUsed(false);
                        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                            log("Processing files in directory: " + chooser.getSelectedFile());
                            currentDirectory = chooser.getSelectedFile().toString();
                        } else {
                            log("Semi-automatic processing: no directory is selected.");
                            return;
                        }
                        nextButton.setVisible(true);
                        useLsmImages.setSelected(true);
                        automaticallyProcessedFiles = chooser.getSelectedFile().listFiles();
                        processFile(0);
                    }
                    break;
                case "nextImage":
                    if (transferImage != null) {
                        transferImage.changes = false;
                        transferImage.close();
                    }
                    if (donorInDImage != null) {
                        donorInDImage.changes = false;
                        donorInDImage.close();
                    }
                    if (donorInAImage != null) {
                        donorInAImage.changes = false;
                        donorInAImage.close();
                    }
                    if (acceptorInAImage != null) {
                        acceptorInAImage.changes = false;
                        acceptorInAImage.close();
                    }
                    IJ.selectWindow("Results");
                    WindowManager.putBehind();
                    if (WindowManager.getCurrentImage() != null) {
                        WindowManager.getCurrentImage().close();
                    }
                    processFile(++currentlyProcessedFile);
                    break;
                case "resetImages":
                    resetAll();
                    break;
                case "closeImages":
                    if (transferImage != null) {
                        transferImage.changes = false;
                        transferImage.close();
                    }
                    if (donorInDImage != null) {
                        donorInDImage.changes = false;
                        donorInDImage.close();
                    }
                    if (donorInAImage != null) {
                        donorInAImage.changes = false;
                        donorInAImage.close();
                    }
                    if (acceptorInAImage != null) {
                        acceptorInAImage.changes = false;
                        acceptorInAImage.close();
                    }
                    resetAll();
                    break;
                case "help":
                    if (helpWindow != null) {
                        helpWindow.setVisible(false);
                        helpWindow.dispose();
                    }
                    helpWindow = new RiHelpWindow(this);
                    helpWindow.setVisible(true);
                    break;
                case "about":
                    JOptionPane optionPane = new JOptionPane();
                    optionPane.setMessage("RiFRET - an ImageJ plugin for intensity-based ratiometric FRET imaging\n"
                            + "Homepage: http://biophys.med.unideb.hu/rifret/\n"
                            + "Written by: János Roszik (janosr@med.unideb.hu), Duarte Lisboa (duarte@med.unideb.hu),\n"
                            + "János Szöllõsi (szollo@med.unideb.hu) and György Vereb (vereb@med.unideb.hu)\n"
                            + "Version: " + version + " (" + lastModified + ")\n"
                            + "The plugin was tested with ImageJ version " + imageJVersion + " using Java " + javaVersion + ".\n");
                    optionPane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
                    JDialog dialog = optionPane.createDialog(this, "About");
                    dialog.setVisible(true);
                    break;
                default:
                    break;
            }
        } catch (HeadlessException | NumberFormatException t) {
            logException(t.toString(), t);
        }
    }

    private void processFile(int currentFile) {
        resetAllButtonColors();
        if (currentFile >= automaticallyProcessedFiles.length) {
            log("Processing files has been finished.");
            nextButton.setVisible(false);
            IJ.selectWindow("Results");
            currentlyProcessedFile = 0;
            automaticallyProcessedFiles = null;
            currentlyProcessedFileName = null;
            return;
        }
        if (!automaticallyProcessedFiles[currentFile].isFile() || !(automaticallyProcessedFiles[currentFile].getName().endsWith(".lsm") || automaticallyProcessedFiles[currentFile].getName().endsWith(".LSM"))) {
            processFile(++currentlyProcessedFile);
            return;
        }
        log("Current file is: " + automaticallyProcessedFiles[currentFile].getName());
        currentlyProcessedFileName = automaticallyProcessedFiles[currentFile].getName();
        (new Opener()).open(automaticallyProcessedFiles[currentFile].getAbsolutePath());
        WindowManager.putBehind();
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "split"));
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "setAcceptorInAImage"));
        WindowManager.putBehind();
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "setDonorInAImage"));
        WindowManager.putBehind();
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "setDonorInDImage"));
        donorInDImage.setRoi(new Roi(0, 0, donorInDImage.getWidth() / 6, donorInDImage.getHeight() / 6));
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "copyRoi"));
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "subtractDonorInDImage"));
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "subtractDonorInAImage"));
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "subtractAcceptorInAImage"));
        donorInDImage.setRoi(new Roi(0, 0, donorInDImage.getWidth() / 6, donorInDImage.getHeight() / 6));
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "copyRoi"));
        donorInDImage.getProcessor().setValue(0);
        donorInDImage.getProcessor().fill();
        donorInAImage.getProcessor().setValue(0);
        donorInAImage.getProcessor().fill();
        acceptorInAImage.getProcessor().setValue(0);
        acceptorInAImage.getProcessor().fill();
        donorInDImage.killRoi();
        donorInAImage.killRoi();
        acceptorInAImage.killRoi();
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "smoothDD"));
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "smoothDA"));
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "smoothAA"));
    }

    void registerToDonorChannel() {

    }

    private void resetAll() {
        donorInDImage = null;
        donorInDImageSave = null;
        donorInAImage = null;
        donorInAImageSave = null;
        acceptorInAImage = null;
        acceptorInAImageSave = null;
        setDonorInDImageButton.setBackground(originalButtonColor);
        setDonorInDImageButton.setOpaque(false);
        setDonorInDImageButton.setBorderPainted(true);
        setDonorInAImageButton.setBackground(originalButtonColor);
        setDonorInAImageButton.setOpaque(false);
        setDonorInAImageButton.setBorderPainted(true);
        setAcceptorInAImageButton.setBackground(originalButtonColor);
        setAcceptorInAImageButton.setOpaque(false);
        setAcceptorInAImageButton.setBorderPainted(true);
        subtractDonorInDImageButton.setBackground(originalButtonColor);
        subtractDonorInDImageButton.setOpaque(false);
        subtractDonorInDImageButton.setBorderPainted(true);
        subtractDonorInAImageButton.setBackground(originalButtonColor);
        subtractDonorInAImageButton.setOpaque(false);
        subtractDonorInAImageButton.setBorderPainted(true);
        subtractAcceptorInAImageButton.setBackground(originalButtonColor);
        subtractAcceptorInAImageButton.setOpaque(false);
        subtractAcceptorInAImageButton.setBorderPainted(true);
        smoothDonorInDImageButton.setBackground(originalButtonColor);
        smoothDonorInDImageButton.setOpaque(false);
        smoothDonorInDImageButton.setBorderPainted(true);
        smoothDonorInAImageButton.setBackground(originalButtonColor);
        smoothDonorInAImageButton.setOpaque(false);
        smoothDonorInAImageButton.setBorderPainted(true);
        smoothAcceptorInAImageButton.setBackground(originalButtonColor);
        smoothAcceptorInAImageButton.setOpaque(false);
        smoothAcceptorInAImageButton.setBorderPainted(true);
        thresholdDonorInDImageButton.setBackground(originalButtonColor);
        thresholdDonorInDImageButton.setOpaque(false);
        thresholdDonorInDImageButton.setBorderPainted(true);
        thresholdDonorInAImageButton.setBackground(originalButtonColor);
        thresholdDonorInAImageButton.setOpaque(false);
        thresholdDonorInAImageButton.setBorderPainted(true);
        thresholdAcceptorInAImageButton.setBackground(originalButtonColor);
        thresholdAcceptorInAImageButton.setOpaque(false);
        thresholdAcceptorInAImageButton.setBorderPainted(true);
        calculateS1S3Button.setBackground(originalButtonColor);
        calculateS1S3Button.setOpaque(false);
        calculateS1S3Button.setBorderPainted(true);
        calculateS2S4Button.setBackground(originalButtonColor);
        calculateS2S4Button.setOpaque(false);
        calculateS2S4Button.setBorderPainted(true);
        calculateAlphaButton.setBackground(originalButtonColor);
        calculateAlphaButton.setOpaque(false);
        calculateAlphaButton.setBorderPainted(true);

        nextButton.setVisible(false);
        currentlyProcessedFile = 0;
        automaticallyProcessedFiles = null;
        currentlyProcessedFileName = null;
    }

    private void resetAllButtonColors() {
        setDonorInDImageButton.setBackground(originalButtonColor);
        setDonorInDImageButton.setOpaque(false);
        setDonorInDImageButton.setBorderPainted(true);
        setDonorInAImageButton.setBackground(originalButtonColor);
        setDonorInAImageButton.setOpaque(false);
        setDonorInAImageButton.setBorderPainted(true);
        setAcceptorInAImageButton.setBackground(originalButtonColor);
        setAcceptorInAImageButton.setOpaque(false);
        setAcceptorInAImageButton.setBorderPainted(true);
        subtractDonorInDImageButton.setBackground(originalButtonColor);
        subtractDonorInDImageButton.setOpaque(false);
        subtractDonorInDImageButton.setBorderPainted(true);
        subtractDonorInAImageButton.setBackground(originalButtonColor);
        subtractDonorInAImageButton.setOpaque(false);
        subtractDonorInAImageButton.setBorderPainted(true);
        subtractAcceptorInAImageButton.setBackground(originalButtonColor);
        subtractAcceptorInAImageButton.setOpaque(false);
        subtractAcceptorInAImageButton.setBorderPainted(true);
        smoothDonorInDImageButton.setBackground(originalButtonColor);
        smoothDonorInDImageButton.setOpaque(false);
        smoothDonorInDImageButton.setBorderPainted(true);
        smoothDonorInAImageButton.setBackground(originalButtonColor);
        smoothDonorInAImageButton.setOpaque(false);
        smoothDonorInAImageButton.setBorderPainted(true);
        smoothAcceptorInAImageButton.setBackground(originalButtonColor);
        smoothAcceptorInAImageButton.setOpaque(false);
        smoothAcceptorInAImageButton.setBorderPainted(true);
        thresholdDonorInDImageButton.setBackground(originalButtonColor);
        thresholdDonorInDImageButton.setOpaque(false);
        thresholdDonorInDImageButton.setBorderPainted(true);
        thresholdDonorInAImageButton.setBackground(originalButtonColor);
        thresholdDonorInAImageButton.setOpaque(false);
        thresholdDonorInAImageButton.setBorderPainted(true);
        thresholdAcceptorInAImageButton.setBackground(originalButtonColor);
        thresholdAcceptorInAImageButton.setOpaque(false);
        thresholdAcceptorInAImageButton.setBorderPainted(true);
        calculateS1S3Button.setBackground(originalButtonColor);
        calculateS1S3Button.setOpaque(false);
        calculateS1S3Button.setBorderPainted(true);
        calculateS2S4Button.setBackground(originalButtonColor);
        calculateS2S4Button.setOpaque(false);
        calculateS2S4Button.setBorderPainted(true);
        calculateAlphaButton.setBackground(originalButtonColor);
        calculateAlphaButton.setOpaque(false);
        calculateAlphaButton.setBorderPainted(true);
    }

    public void log(String text) {
        try {
            log.getDocument().insertString(log.getDocument().getLength(), "\n" + format.format(new Date()) + " " + text, log.getStyle("BLACK"));
            log.setCaretPosition(log.getDocument().getLength());
        } catch (javax.swing.text.BadLocationException e) {
        }
    }

    public void logError(String text) {
        try {
            log.getDocument().insertString(log.getDocument().getLength(), "\n" + format.format(new Date()) + " ERROR: " + text, log.getStyle("RED"));
            log.setCaretPosition(log.getDocument().getLength());
        } catch (javax.swing.text.BadLocationException e) {
        }
    }

    public void logWarning(String text) {
        try {
            log.getDocument().insertString(log.getDocument().getLength(), "\n" + format.format(new Date()) + " WARNING: " + text, log.getStyle("BLUE"));
            log.setCaretPosition(log.getDocument().getLength());
        } catch (javax.swing.text.BadLocationException e) {
        }
    }

    public void logException(String message, Throwable t) {
        try {
            if (debugMenuItem.isSelected()) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                t.printStackTrace(pw);
                pw.flush();
                log.getDocument().insertString(log.getDocument().getLength(), "\n" + format.format(new Date()) + " ERROR: " + sw.toString(), log.getStyle("RED"));
                log.setCaretPosition(log.getDocument().getLength());
            } else {
                log.getDocument().insertString(log.getDocument().getLength(), "\n" + format.format(new Date()) + " ERROR: " + message, log.getStyle("RED"));
                log.setCaretPosition(log.getDocument().getLength());
            }
        } catch (javax.swing.text.BadLocationException e) {
        }
    }

    public void exit() {
        int choice = JOptionPane.showConfirmDialog(this, "Do you really want to exit?", "Exit", JOptionPane.OK_CANCEL_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            if (s1S3Dialog != null) {
                s1S3Dialog.setVisible(false);
                s1S3Dialog.dispose();
            }
            if (s2S4Dialog != null) {
                s2S4Dialog.setVisible(false);
                s2S4Dialog.dispose();
            }
            if (alphaDialog != null) {
                alphaDialog.setVisible(false);
                alphaDialog.dispose();
            }
            if (applyMaskRiDialog != null) {
                applyMaskRiDialog.setVisible(false);
                applyMaskRiDialog.dispose();
            }
            if (calculateRatioDialog != null) {
                calculateRatioDialog.setVisible(false);
                calculateRatioDialog.dispose();
            }
            if (autoflDialog != null) {
                autoflDialog.setVisible(false);
                autoflDialog.dispose();
            }
            if (helpWindow != null) {
                helpWindow.setVisible(false);
                helpWindow.dispose();
            }
            setVisible(false);
            dispose();
        }
    }

    @Override
    public void windowClosing(WindowEvent e) {
        exit();
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    public ImagePlus getDonorInDImage() {
        return donorInDImage;
    }

    public ImagePlus getDonorInAImage() {
        return donorInAImage;
    }

    public ImagePlus getAcceptorInAImage() {
        return acceptorInAImage;
    }

    public float getS1Factor() {
        float s1Factor = -1;
        try {
            s1Factor = Float.parseFloat(s1Field.getText().trim());
        } catch (NumberFormatException ex) {
            return -1;
        }
        return s1Factor;
    }

    public void setS1Factor(String value) {
        s1Field.setText(value);
    }

    public float getS2Factor() {
        float s2Factor = -1;
        try {
            s2Factor = Float.parseFloat(s2Field.getText().trim());
        } catch (NumberFormatException ex) {
            return -1;
        }
        return s2Factor;
    }

    public void setS2Factor(String value) {
        s2Field.setText(value);
    }

    public float getS3Factor() {
        float s3Factor = -1;
        try {
            s3Factor = Float.parseFloat(s3Field.getText().trim());
        } catch (NumberFormatException ex) {
            return -1;
        }
        return s3Factor;
    }

    public void setS3Factor(String value) {
        s3Field.setText(value);
    }

    public float getS4Factor() {
        float s4Factor = -1;
        try {
            s4Factor = Float.parseFloat(s4Field.getText().trim());
        } catch (NumberFormatException ex) {
            return -1;
        }
        return s4Factor;
    }

    public void setS4Factor(String value) {
        s4Field.setText(value);
    }

    public void setAlphaFactor(String value) {
        alphaField.setText(value);
    }

    public static void main(String args[]) {
        new RiFRET_Plugin();
    }

}
