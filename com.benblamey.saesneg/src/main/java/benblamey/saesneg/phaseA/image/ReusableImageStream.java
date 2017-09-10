package com.benblamey.saesneg.phaseA.image;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ReusableImageStream {

    String _url;
    InputStream _stream;

    public ReusableImageStream(String url) {
        _url = url;
    }

    public InputStream getInputStream() {
        if (_stream != null) {
            System.out.println("Re-using stream: " + _url);
            try {
                _stream.reset();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            System.out.println("Downloading: " + _url);
            URL url;
            try {
                url = new URL(_url);
                _stream = new BufferedInputStream(url.openConnection().getInputStream());
                _stream.mark(Integer.MAX_VALUE);
            } catch (IOException ex) {

                throw new RuntimeException(ex);
            }
        }
        return _stream;
    }

    public void close() {
        if (_stream != null) {
            try {
                _stream.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
