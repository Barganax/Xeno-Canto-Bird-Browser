# Xeno-Canto-Bird-Browser
Java Swing app to download and manipulate bird recordings from xeno-canto.org and populate a database of sonograms

 This app is (currently) in 5 sections:  The online manual browser, the automated scraper, the onset preference edit screen,                 
the sonogram preference edit screen and the analyzer.

The manual browser starts at the first page of https://xeno-canto.org/explore but you can select any page for viewing by entering
the page number in the input at the top. You can listen to the recordings one at a time or import one or more recordings.

In the automated section you can import all the recordings of a species that has been recorded in a particular country (set by
main.browser.XenoCantoAutoCard.COUNTRY_PATTERN)  Select a starting page by entering the page number into the input - if no starting
page is given the program starts at page 1.  The program emits status messages to the textarea covering most of the screen and also 
to the console.  It will continue to the last page (set by main.browser.Browser.LAST_XENDO_CANTO_PAGE) Since this value changes
all the time you should find and enter the correct value before you run either of the first 2 sections.

The onset preference editing screen lets you set the global gain and the parameters for the Tarsos complex onset detector.  The
tag field lets you enter an identifier for your settings.

Likewise, the sonogram preference editing screen lets you define the parameters for generating the sonograms.  Similar to the onset
preference screen you can enter an identifier for your settings.  Since I switched
to using multiple-sized buffers the buffer overlap has been fixed at 50%.  This seems like a workable value.  The buffer size
is for type 0 sonograms.  Types 1, 2 and 3 have sizes 2, 4 and 8 times the size of the type 0.  The window functions haven't been
tested yet.  The components field determines the number of frequency components from the fft to save.  Resynthesizing a recording
from 20 components still yields an identifiable sound.
