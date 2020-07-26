# RiFRET Changelog
*This changelog only lists notable changes for the end user. For all changes, please see the [commit history](https://github.com/camlloyd/RiFRET/commits/master).*

## 2.0.0-SNAPSHOT (Unreleased)

### New features
* Pixel-wise autofluorescence correction.
* Save transfer (FRET) images from the main window.
* Copy region of interests (ROIs) in the spectral correction factor calculation windows.
* Instrument background subtraction in the spectral correction factor calculation windows.
* Reset thresholding steps in the spectral correction factor calculation windows.
* Save and load parameters as a CSV file (experimental).

### Important changes
* Negative pixel values are now included when calculating spectral correction factors.
* Default Gaussian blur values are now 0.4x previous values.
* Background subtraction and Gaussian blurring steps have been reordered.
* Spectrum LUT is no longer applied automatically.
* RiFRET now uses Maven. The result is a single distributable JAR.
* _Check for updates_ has been removed.
* _Help_ now points to https://imagej.net/RiFRET.

#### Deprecation
Old | New
--- | ---
`GaussianBlur.blur()` | `GaussianBlur.blurGaussian()`
`ResultsTable.addLabel()` | `ResultsTable.setValue()`
`Java.util.Date` | `Java.time`

### User Interface (UI) changes
* Added: Scroll bars to accommodate new features.
* Changed: Improved hard-coded window sizes, particularly on macOS.
* Changed: Text fields for instrument background/autofluorescence subtraction are now larger.
* Changed: Use native looking file chooser for I/O.
* Changed: Revised menu.
* Changed: Renamed _Use LSM_ to _Use stack_.
* Changed: Alpha factor calculation window UI.
* Changed: Semi-automatic processing window UI.
* Changed: Various tweaks.
* Fixed: Various typos.

### Logging improvements
* Log image names when setting channels.
* Log sigma values when applying Gaussian blur.
* Log saving the log!

### Bug fixes
* Fixed: Button color now changes to green once clicked on macOS.
