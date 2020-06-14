package poealerts;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.HHOOK;
import com.sun.jna.platform.win32.WinUser.MSG;

import mouse.LowLevelMouseProc;
import utils.Beeper;
import utils.SysTray;
import utils.User32DLL;
import utils.WavPlayer;

public class POEAlerts {
	
	private static final User32DLL user32 = utils.User32DLL.INSTANCE;
	private static final ExecutorService clipboardCheckThread = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>(), new ThreadPoolExecutor.DiscardPolicy());
	private static final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	private static final StringSelection EMPTY_STRING = new StringSelection("");
	private static final String POE_WINDOW_NAME = "Path of Exile";
	private static final String ALERT_FILEPATH = "alerts.txt";
	private static final String TRAY_ICON_PATH = "trayicon.png";
	private static final String BEEP_FILEPATH = "beep.wav";
	private static final int LEFT_MOUSE_DOWN = 513;
	private static final int MAX_TITLE_LENGTH = 512;
	private static final char[] TITLE_BUFFER = new char[MAX_TITLE_LENGTH * 2];
	private static volatile HHOOK hhk;
	
	public static void main(String[] args) throws AWTException, FileNotFoundException, IOException, LineUnavailableException, UnsupportedAudioFileException {
		
		SysTray.initialize(TRAY_ICON_PATH);
		
		final Set<Set<String>> alertStringsSet = new HashSet<>();
		try (Stream<String> lines = Files.lines(Paths.get(ALERT_FILEPATH))) {
			lines.forEach(line -> {
				Set<String> alertStrings = new HashSet<>();
				for (String alertString : line.split(","))
					alertStrings.add(alertString.trim().toLowerCase());
				alertStringsSet.add(alertStrings);
			});
		}
		
		File beepFile = new File(BEEP_FILEPATH);
		WavPlayer beep = beepFile.exists() ? new WavPlayer(beepFile) : null;
		clipboard.addFlavorListener(new FlavorListener() {
			@Override
			public void flavorsChanged(FlavorEvent event) {
				if (poeIsActive()) {
					clipboardCheckThread.execute(() -> {
						Transferable contents = clipboard.getContents(null);
						if (contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
							try {
								String itemText = ((String) contents.getTransferData(DataFlavor.stringFlavor)).toLowerCase();
								for (Set<String> alertStrings : alertStringsSet) {
									boolean alert = true;
									for (String alertString : alertStrings) {
										if (!itemText.contains(alertString)) {
											alert = false;
											break;
										}
									}
									if (alert) {
										if (beep != null)
											beep.play();
										else
											Beeper.beep(800, 75, 0.25);
										break;
									}
								}
							} catch (UnsupportedFlavorException | IOException | LineUnavailableException e) {
							} finally {
								clipboard.setContents(EMPTY_STRING, EMPTY_STRING);
							}
						}
					});
				}
			}
		});
		
		final Robot robot = new Robot();
		LowLevelMouseProc mouseHook = new LowLevelMouseProc() {
			public LRESULT callback(int nCode, WPARAM wParam, LPARAM lParam) {
				if (wParam.intValue() == LEFT_MOUSE_DOWN) {
					if (poeIsActive()) {
						robot.keyPress(KeyEvent.VK_CONTROL);
						robot.keyPress(KeyEvent.VK_C);
						robot.keyRelease(KeyEvent.VK_C);
						robot.keyRelease(KeyEvent.VK_CONTROL);
						try { Thread.sleep(10); } catch (InterruptedException e) {}
					}
				}
				return user32.CallNextHookEx(hhk, nCode, wParam, lParam);
			};
		};
		
		Thread t = new Thread(() -> {
			HMODULE hMod = Kernel32.INSTANCE.GetModuleHandle(null);
			hhk = user32.SetWindowsHookEx(WinUser.WH_MOUSE_LL, mouseHook, hMod, 0);
			int result;
			MSG msg = new MSG();
			while ((result = user32.GetMessage(msg, null, 0, 0)) != 0) {
				if (result != -1) {
					user32.TranslateMessage(msg);
					user32.DispatchMessage(msg);
				} else {
					break;
				}
			}
		});
		t.setDaemon(false);
		t.start();
	}
	
	private static boolean poeIsActive() {
		user32.GetWindowTextW(user32.GetForegroundWindow(), TITLE_BUFFER, MAX_TITLE_LENGTH);
		return Native.toString(TITLE_BUFFER).equals(POE_WINDOW_NAME);
	}

}
