package org.bzdev.swing;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import java.nio.*;
import java.nio.charset.Charset;
import java.net.*;
import java.text.MessageFormat;

/**
 * Text area component loadable from multiple sources.
 * This component provides a text area whose contents can
 * be entered directly or loaded from a location specified
 * by a file name or a URL.  When a URL is used, the text
 * is not editable, although a control allows one to
 * dissociate the text from the URL, in which case the text
 * can be edited.
 */
public class URLTextAreaPane extends JComponent {
    static private final String resourceBundleName 
	= "org.bzdev.swing.lpack.URLTextAreaPane";
    static ResourceBundle bundle = 
	ResourceBundle.getBundle(resourceBundleName);
    static String localeString(String name) {
	return bundle.getString(name);
    }

    static final String DROP_URL = localeString("dropURL");
    static final String LOAD = localeString("load");

    JScrollPane scrollPane;
    String url;
    JTextArea textArea;
    boolean urlValid = false;

    /**
     * Get the URL.
     * @return the URL; null if there is none
     */
    public String getURL() {return url;}

    /**
     * Get the text for the component.
     * @return the text.
     */
    public String getText() {return textArea.getText();}

    /**
     * Determine if the text is associated with a URL.
     * @return true if the text is the contents of a uRL; false otherwise
     */
    public boolean urlInUse() {return urlValid;}


    JButton urlButton = new JButton(LOAD);

    static String[] options = {
	localeString("DeleteURLClear"),
	localeString("DeleteURLKeep"),
	localeString("Cancel")
    };

    static String[] options2 = {
	localeString("OK"),
	localeString("Cancel")
    };
	
    JPanel addURLQuestionPane = new JPanel();
    JLabel urlLabel = new JLabel(localeString("addURLQuestion"));
    JTextField urlTextField = new JTextField(50);
    JButton urlFileChooserButton = new JButton(localeString("chooseFile"));

    private void configureQuestionPane() {
	GridBagLayout gridbag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();

	c.gridwidth = GridBagConstraints.REMAINDER;
	c.insets = new Insets(2, 2, 2, 2);

	GridBagConstraints cc = new GridBagConstraints();
	cc.gridwidth = GridBagConstraints.REMAINDER;
	cc.anchor = GridBagConstraints.LINE_START;
	cc.insets = new Insets(3, 3, 3, 3);

	GridBagConstraints ccc = new GridBagConstraints();
	ccc.gridwidth = 1;
	ccc.anchor = GridBagConstraints.LINE_START;
	ccc.insets = new Insets(3, 3, 3, 3);
	addURLQuestionPane.setLayout(gridbag);
	
	gridbag.setConstraints(urlLabel, cc);
	addURLQuestionPane.add(urlLabel);
	gridbag.setConstraints(urlTextField, ccc);
	addURLQuestionPane.add(urlTextField);
	gridbag.setConstraints(urlFileChooserButton, c);
	addURLQuestionPane.add(urlFileChooserButton);
    }

    boolean enabled = true;
    public void setEnabled(boolean enabled) {
	this.enabled = enabled;
	if (enabled) {
	    urlButton.setEnabled(true);
	    textArea.setEnabled(!urlValid);
	} else {
	    urlButton.setEnabled(false);
	    textArea.setEnabled(false);
	}
    }

    public boolean isEnabled() {
	return enabled;
    }

    String errorTitle = null;
    
    /**
     * Constructor.
     * @param rows the number of rows in the text area
     * @param cols the number of columns in the text area
     * @param errorTitle the title to use for dialog boxes reporting errors
     */
    public URLTextAreaPane(int rows, int cols, String errorTitle) {
	this.errorTitle = errorTitle;
	textArea = new JTextArea(rows, cols);
	scrollPane = new 
	    JScrollPane(textArea,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

	init("", false, errorTitle);

	urlButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (urlValid) {
			int status = JOptionPane.showOptionDialog
			    (URLTextAreaPane.this,
			     localeString("dropURLQuestion"),
			     localeString("dropURLTitle"),
			     JOptionPane.DEFAULT_OPTION,
			     JOptionPane.QUESTION_MESSAGE,
			     null, options, null);
			switch (status) {
			case JOptionPane.CLOSED_OPTION:
			case 2:
			    break;
			case 0:
			    textArea.setText("");
			case 1:
			    urlValid = false;
			    url = null;
			    urlButton.setText(LOAD);
			    textArea.setEnabled(true);
			    break;
			}
		    } else {
			int status = JOptionPane.showOptionDialog
			    (URLTextAreaPane.this,
			     addURLQuestionPane,
			     localeString("dropURLTitle"),
			     JOptionPane.DEFAULT_OPTION,
			     JOptionPane.QUESTION_MESSAGE,
			     null, options2, null);
			String result = null;
			switch (status) {
			case JOptionPane.CLOSED_OPTION:
			case 1:
			    break;
			case 0:
			    result = urlTextField.getText().trim();
			    break;

			}
			if (result != null) {
			    try {
				URL theURL = new URL(result);
				if (loadText(theURL)) {
				    url = result;
				    urlValid = true;
				    textArea.setText(text.toString());
				    urlButton.setText(DROP_URL);
				    textArea.setEnabled(false);
				} else {
				    JOptionPane.showMessageDialog
					(URLTextAreaPane.this,
					 localeString("badCharsetOrNotText"),
					 localeString("addURLError"),
				     JOptionPane.ERROR_MESSAGE);
				}
			    } catch (MalformedURLException emurl) {
				JOptionPane.showMessageDialog
				    (URLTextAreaPane.this, 
				     localeString("malformedURL"),
				     localeString("addURLError"),
				     JOptionPane.ERROR_MESSAGE);
			    } catch (IOException eio) {
				JOptionPane.showMessageDialog
				    (URLTextAreaPane.this,
				     localeString("didNotLoadURL"),
				     localeString("addURLError"),
				     JOptionPane.ERROR_MESSAGE);
			    }
			}
		    }
		}
	    });

	urlFileChooserButton.addActionListener(new ActionListener() {
		File currentDir = new File(System.getProperty("user.dir"));
		public void actionPerformed(ActionEvent e) {
		    JFileChooser chooser = new JFileChooser(currentDir);
		    chooser.setDialogTitle(localeString("chooserTitle"));
		    chooser.setMultiSelectionEnabled(false);
		    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		    int status = chooser.showDialog(urlFileChooserButton,
						    localeString("toURL"));
		    switch (status) {
		    case JFileChooser.ERROR_OPTION:
		    case JFileChooser.CANCEL_OPTION:
			break;
		    case JFileChooser.APPROVE_OPTION:
			File file = chooser.getSelectedFile();
			if (file != null) {
			    if (file.isDirectory()) {
				currentDir = file;
			    } else {
				currentDir = file.getParentFile();
			    }
			    try {
				String url = file.toURI().toURL().toString();
				urlTextField.setText(url);
			    } catch (MalformedURLException emurl) {
				// should not happen given how URL is
				// constructed.
			    }
			}
			break;
		    }
		}
	    });	

	configureQuestionPane();

	setLayout(new FlowLayout());
	JPanel panel = new JPanel();
	GridBagLayout gridbag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();

	c.gridwidth = GridBagConstraints.REMAINDER;
	c.insets = new Insets(2, 2, 2, 2);

	GridBagConstraints cc = new GridBagConstraints();
	cc.gridwidth = GridBagConstraints.REMAINDER;
	cc.anchor = GridBagConstraints.LINE_START;
	cc.insets = new Insets(3, 3, 3, 3);

	GridBagConstraints ccc = new GridBagConstraints();
	ccc.gridwidth = 1;
	ccc.anchor = GridBagConstraints.LINE_START;
	ccc.insets = new Insets(3, 3, 3, 3);
	panel.setLayout(gridbag);

	gridbag.setConstraints(urlButton, cc);
	panel.add(urlButton);
	gridbag.setConstraints(scrollPane, c);
	panel.add(scrollPane);
	add(panel);
    }

    StringBuilder text = new StringBuilder();
    java.nio.CharBuffer cb = java.nio.CharBuffer.allocate(4096);

    private void loadText(InputStream is, String charset) throws IOException {
	cb.clear();
	text.setLength(0);
	Reader rd = new InputStreamReader(is, charset);
	while (rd.read(cb) != -1) {
	    cb.flip();
	    text.append(cb);
	    cb.clear();
	}
    }

    boolean loadText(File file) throws IOException {
	loadText(new FileInputStream(file), Charset.defaultCharset().name());
	return true;
    }
    private String getCharset(String contentType) {
	if (contentType == null) return Charset.defaultCharset().name();
	String[] fields = contentType.split("\\s*;\\s*");
	if (!fields[0].trim().toUpperCase(Locale.ENGLISH).startsWith("TEXT/"))
	    return null;
	if (fields.length < 2) return Charset.defaultCharset().name();
	for (int i = 1; i < fields.length; i++) {
	    String[] components = fields[i].split("=");
	    if (components[0].trim().toUpperCase(Locale.ENGLISH)
		.equals("CHARSET")) {
		String charset =
		    components[1].trim().toUpperCase(Locale.ENGLISH);
		if (Charset.isSupported(charset)) {
		    return charset;
		} else {
		    return null;
		}
	    }
	}
	return null;
    }

    boolean loadText(URL url) throws IOException {
	if (url == null) return false;
	String path = url.getPath();
	
	URLConnection c = url.openConnection();
	String contentType = c.getContentType();
	// System.out.println(contentType);
	String charset = getCharset(contentType);
	if (charset == null) {
	    if (path.endsWith(".shtml") || path.endsWith(".SHTML")) {
		charset = Charset.defaultCharset().name();
	    } else {
		return false;
	    }
	}
	loadText(c.getInputStream(), charset);
	return true;
    }

    /**
     * Initialize the component.
     * @param urlOrText either the URL or the component's text
     * @param isURL true of the first argument is a URL; false if it is text
     */
    public void init(String urlOrText, boolean isURL) {
	init(urlOrText, urlValid, errorTitle);
    }


    /**
     * Initialize the component specifying a new error title.
     * @param urlOrText either the URL or the component's text
     * @param isURL true of the first argument is a URL; false if it is text
     * @param errorTitle the title to use on error-related dialog boxes
     */
    public void init(String urlOrText, boolean isURL,
		     String errorTitle) {
	this.urlValid = isURL;
	if (isURL) {
	    try {
		this.url = urlOrText;
		loadText(new URL(urlOrText));
	    } catch (MalformedURLException emurl) {
		SwingErrorMessage.display(this, errorTitle,
				     String.format
				     (localeString("malformedURL2"),
				      url));
		this.urlValid = false;
		this.url = null;

		// System.err.println(emurl.getMessage());
	    } catch (IOException eio) {
		SwingErrorMessage.display(this, errorTitle,
				     MessageFormat.format
				     (localeString("ioErrorURL"),
				      eio.getMessage(),
				      url));
		// System.err.println(eio.getMessage());
	    }
	    urlButton.setText(DROP_URL);
	    textArea.setText(this.text.toString());
	    textArea.setEnabled(false);
	} else {
	    this.url = null;
	    urlButton.setText(LOAD);
	    textArea.setText(urlOrText);
	    textArea.setEnabled(true);
	}
    }
}

//  LocalWords:  dropURL uRL DeleteURLClear DeleteURLKeep chooseFile
//  LocalWords:  addURLQuestion errorTitle dropURLQuestion dir toURL
//  LocalWords:  dropURLTitle badCharsetOrNotText addURLError CHARSET
//  LocalWords:  malformedURL didNotLoadURL chooserTitle contentType
//  LocalWords:  shtml urlOrText isURL emurl getMessage ioErrorURL
//  LocalWords:  eio
