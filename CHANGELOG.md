# RiFRET Changelog
*This changelog only lists notable changes for the end user. For all changes, please see the [commit history](https://github.com/CellMoTher/RiFRET/commits/master).*

## 2.0.0

### New features
* Pixel-wise autofluorescence correction.
* Show corrected donor and acceptor images when creating a FRET image.
* Save FRET images from the main window.
* Copy region of interests (ROIs) in the spectral correction factor calculation windows.
* Background subtraction in the spectral correction factor calculation windows.
* Reset thresholding steps in the spectral correction factor calculation windows.
* Save and load parameters as a CSV file (experimental).

### Important changes
* Negative pixel values are now included when calculating spectral correction factors.
* Gaussian blur sigma (radius) values are now `0` by default.
* Background subtraction and Gaussian blurring steps have been reordered.
* Spectrum LUT is no longer applied automatically.
* RiFRET now uses Maven. The result is a single distributable JAR.
* _Check for updates_ has been removed in favor of the ImageJ updater.
* _Help_ now points to https://imagej.net/plugins/rifret.

#### Deprecation
Old | New
--- | ---
`GaussianBlur.blur()` | `GaussianBlur.blurGaussian()`
`ResultsTable.addLabel()` | `ResultsTable.setValue()`
`Java.util.Date` | `Java.time`

### User Interface (UI) changes
* Added: Scroll bars to accommodate new features.
* Changed: Improved hard-coded window sizes, particularly on macOS.
* Changed: Text fields for instrument background subtraction are now larger.
* Changed: Use native looking file chooser for I/O.
* Changed: Revised menu.
* Changed: Renamed _Use LSM_ to _Use stack_.
* Changed: Alpha factor calculation window UI.
* Changed: Semi-automatic has been revised significantly --> batch processing.
* Changed: Various tweaks.
* Fixed: Various typos.

### Logging improvements
* Log image names when setting channels.
* Log sigma values when applying Gaussian blur.
* Log saving the log!

### Bug fixes
* Fixed: Button color now changes to green once clicked on macOS.
