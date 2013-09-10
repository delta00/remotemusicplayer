package application.controller;

import java.io.IOException;

public interface ControllerErrorListener {
	public void addIOException(IOException exception);
}
