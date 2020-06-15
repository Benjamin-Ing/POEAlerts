package utils;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.win32.W32APIOptions;

public interface User32DLL extends User32 {
	
	User32DLL INSTANCE = Native.load("user32", User32DLL.class, W32APIOptions.DEFAULT_OPTIONS);
	int GetWindowTextW(HWND hwnd, char[] lpString, int nMaxCount);
	int MessageBoxW(HWND hwnd, char[] lpText, char[] lpCaption, int uType);
}
