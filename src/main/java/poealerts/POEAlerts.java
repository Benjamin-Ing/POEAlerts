package poealerts;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    private static final String POE_WINDOW_NAME = "Path of Exile";
    private static final String ALERT_FILEPATH = "alerts.json";
    private static final String TRAY_ICON_PATH = "trayicon.png";
    private static final int LEFT_MOUSE_DOWN = 513;
    private static final int MAX_TITLE_LENGTH = 512;
    private static final char[] TITLE_BUFFER = new char[MAX_TITLE_LENGTH * 2];
    private static volatile HHOOK hhk;
    private static volatile boolean alertsLoaded = false;
    private static volatile boolean checking = false;
    private static volatile Transferable previousClip;
    
    public static void main(String[] args) throws AWTException, FileNotFoundException, IOException, LineUnavailableException, UnsupportedAudioFileException {
        
        final List<Alert> alertList = new ArrayList<Alert>();
        Runnable reloadAltertStringsSet = () -> {
            alertList.clear();
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String rawJson = Files.readString(Paths.get(ALERT_FILEPATH));
                List<Alert> parsedJson = objectMapper.readValue(rawJson, objectMapper.getTypeFactory().constructCollectionType(List.class, Alert.class));
                alertList.addAll(parsedJson);
                if(alertsLoaded)
                    showPopup("Success", "Alert settings reloaded");
                else
                	alertsLoaded = true;
            } catch (IOException | IllegalStateException e) {
                showPopup("An Error Occured", "Failed to load (or parse) " + ALERT_FILEPATH);
                System.err.println(e);
                if (!alertsLoaded)
                	System.exit(-1);
            }
        };

		SysTray.initialize(TRAY_ICON_PATH, reloadAltertStringsSet);
		reloadAltertStringsSet.run();
		clipboard.addFlavorListener(new FlavorListener() {
			@Override
			public void flavorsChanged(FlavorEvent event) {
				if (poeIsActive()) {
					clipboardCheckThread.execute(() -> {
                        Transferable contents = clipboard.getContents(null);
						if (contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
							try {
                                String itemText = ((String) contents.getTransferData(DataFlavor.stringFlavor));
                                String previousItemText = (previousClip != null && previousClip.isDataFlavorSupported(DataFlavor.stringFlavor)) ? (String) previousClip.getTransferData(DataFlavor.stringFlavor) : "";
                                if (itemText != null && !itemText.equals(previousItemText) && !"".equals(itemText)) {
                                    String[] clipboardLines = itemText.split("\\r?\\n");
                                    for (Alert alert : alertList) {
                                        if (!alert.enabled) continue;
                                        int andMatchCount = 0;
                                        boolean orMatched = false;
                                        boolean executeAlert = false;
                                        for (String line : clipboardLines) {
                                            for (String regexItem : alert.matchAll) {
                                                if (line.matches(regexItem)) {
                                                    andMatchCount++;
                                                    break;
                                                }
                                            }
                                            if (!orMatched) {
                                                for(String regexItem : alert.matchAny) {
                                                    if(line.matches(regexItem)) {
                                                        orMatched = true;
                                                        break;
                                                    }
                                                }
                                            }
                                            if ((orMatched || alert.matchAny.size() == 0) && andMatchCount >= alert.matchAll.size()) {
                                                executeAlert = true;
                                                break;
                                            }
                                        }
                                        if (executeAlert) {
                                            File alertSoundFile = new File(alert.sound);
                                            WavPlayer alertSound = alertSoundFile.exists() ? new WavPlayer(alertSoundFile) : null;
                                            if (alertSound != null)
                                                alertSound.play();
                                            else
                                                Beeper.beep(800, 75, 0.25);
                                            break;
                                        }
                                    }
                                }
							} catch (UnsupportedFlavorException | IOException | LineUnavailableException | UnsupportedAudioFileException e) {
                                System.err.println(e);
                            } finally {
                            	// This isn't entirely safe, since the user can copy something to clipboard 
                            	// while the clipboard check is running, and we would be reverting their clipboard
                            	// to the state when they left-clicked, and not to the present state.
                            	clipboard.setContents(previousClip, null);
                            }
						}
					});
				}
				checking = false;
			}
		});
		
		final Robot robot = new Robot();
		LowLevelMouseProc mouseHook = new LowLevelMouseProc() {
			public LRESULT callback(int nCode, WPARAM wParam, LPARAM lParam) {
				if (!checking)
					previousClip = clipboard.getContents(null);
				if (wParam.intValue() == LEFT_MOUSE_DOWN) {
					if (poeIsActive()) {
						checking = true;
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

    private static void showPopup(String title, String body) {
        user32.MessageBoxW(null, Native.toCharArray(body), Native.toCharArray(title), 0x00000000);
    }

}
