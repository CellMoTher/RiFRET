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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 *
 */
public class RiHelpWindow extends JFrame {

    private final RiFRET_Plugin mainWindow;
    private JPanel panel;

    public RiHelpWindow(RiFRET_Plugin mainWindow) {
        setTitle("RiFRET Help");
        this.mainWindow = mainWindow;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        createGui();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(600, 800);
        setLocation((screen.width - getWidth()) / 2, (screen.height - getHeight()) / 2);
    }

    public void createGui() {
        GridBagLayout gridbaglayout = new GridBagLayout();
        GridBagConstraints gc = new GridBagConstraints();
        panel = new JPanel();
        panel.setLayout(gridbaglayout);
        setFont(new Font("Helvetica", Font.PLAIN, 12));

        gc.insets = new Insets(4, 4, 4, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 2;
        JLabel label0 = new JLabel("<html><center><b>RiFRET Help</b></center></html>");
        label0.setFont(new Font("Helvetica", Font.BOLD, 14));
        panel.add(label0, gc);

        gc.gridy = GridBagConstraints.RELATIVE;

        JLabel label1 = new JLabel("<html><b><br><u>Menu structure</u></b></html>");
        panel.add(label1, gc);
        JLabel label2 = new JLabel("<html>The \"File\" and \"Image\" menus include ImageJ commands that are likely to be frequently used in<br>the analysis process. Also, messages generated during image processing can be saved or cleared<br>from the \"File\" menu.<br>Furthermore, analysis data can be reset from the \"File\" menu and the plugin can be switched to<br>semi-automatic mode here as well.<br></html>");
        panel.add(label2, gc);

        JLabel label3 = new JLabel("<html><b><br><u>Calculation/setting of factors S1, S2, S3, S4 and \u03B1</u></b></html>");
        panel.add(label3, gc);
        JLabel label4 = new JLabel("<html>After pressing a \"Calculate\" button, a new window pops up, where the calculation of the<br>corresponding factor(s) can be done. These constants can be entered on the main program<br>window without calculation, too.<br></html>");
        panel.add(label4, gc);

        JLabel label5 = new JLabel("<html><b><br><u>Step 1: Opening and setting images</u></b></html>");
        panel.add(label5, gc);
        JLabel label6 = new JLabel("<html>Image files can be opened with the \"Open\" button, or with the \"Open image\" item in the \"File\"<br>menu. After opening, images can be set using the \"Set image\" buttons. If multichannel images are<br>used in stacked image files, the opened image has to be split (item available from the \"Image\"<br>menu) before setting. If the \"Use LSM\" checkbox is checked, the LSM image files (up to AIM v. 4.x,<br>files with *.lsm extension) containing donor, transfer and acceptor channels (in this order) are split<BR>and set automatically after opening with the \"Open & Set LSM\" button. Every previously opened<BR>image window will be closed after pressing this button.<br>If it is necessary, the images can be registered by selecting the \"Register to donor channel\" item<br>in the \"Image\" menu.<br></html>");
        panel.add(label6, gc);

        JLabel label7 = new JLabel("<html><b><br><u>Step 2: Subtraction of background of images</u></b></html>");
        panel.add(label7, gc);
        JLabel label8 = new JLabel("<html>To subtract background (the average of pixels in a selected ROI), the \"Subtract\" button has to<br>be pressed for each relevant image. The \"Copy\" button copies the ROI of the first image to the<br>others. This should be done after marking the ROI and before applying the subtraction. To avoid<br>incidental reusing of the ROI in further operations (such as Gaussian blurring), the ROI is<br>automatically deleted after applying the background correction.<br>If an autofluorescence value is given in the corresponding textfield, it is subtracted along with<br>the background.</html>");
        panel.add(label8, gc);

        JLabel label9 = new JLabel("<html><b><br><u>Step 3: Gaussian blurring of images</u></b></html>");
        panel.add(label9, gc);
        JLabel label10 = new JLabel("<html>Images can be blurred with the given radius by pressing the corresponding \"Blur\" button.<br>Blurring (together with thresholding) can be reverted using the \"Reset\" buttons in Step 4.</html>");
        panel.add(label10, gc);

        JLabel label11 = new JLabel("<html><b><br><u>Step 4: Setting thresholds for the images</u></b></html>");
        panel.add(label11, gc);
        JLabel label12 = new JLabel("<html>Thresholds can be applied to the images by pressing the corresponding \"Threshold\" button.<br>After setting the threshold, the \"Apply\" button has to be pressed on bottom menu of the<br>\"Threshold\" window. After this, select \"Set background pixels to NaN\" and press \"Ok\".<br>Closing \"Threshold\" window will apply the thresholding LUT to the active image and therefore<br>should be avoided.<br>The \"Reset\" buttons reset both blur and threshold settings of the corresponding image.</html>");
        panel.add(label12, gc);

        JLabel label13 = new JLabel("<html><b><br><u>Step 5: Creation of the transfer (FRET efficiency) image</u></b></html>");
        panel.add(label13, gc);
        JLabel label14 = new JLabel("<html>After pressing the \"Create\" button, the transfer (FRET) image will be calculated and displayed.<br>This image can be thresholded automatically by the given min and max values at Step 5. If the<br>\"Results\" window is not open it will be opened too.<BR>For details on the calculations please refer to Roszik et al., (citation to be added later).</html>");
        panel.add(label14, gc);

        JLabel label15 = new JLabel("<html><b><br><u>Step 6: Making measurements</u></b></html>");
        panel.add(label15, gc);
        JLabel label16 = new JLabel("<html>After the creation of the transfer (FRET) image, ROIs can be selected on it, and measurements can<br>be made by pressing the \"Measure\" button. FRET histograms can be most easily viewed and<br>exported by clicking the \"Histogram\" item in the \"Image\" menu. Images used in the current<br>analysis and the transfer image can be closed with the \"Close images\" button.</html>");
        panel.add(label16, gc);

        JLabel label17 = new JLabel("<html><br></html>");
        panel.add(label17, gc);

        JScrollPane logScrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        getContentPane().add(logScrollPane);
    }
}
