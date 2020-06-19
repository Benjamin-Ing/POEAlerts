package utils;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class SysTray {

	private static final PopupMenu popup = new PopupMenu();
	
	public static void initialize(String trayIconPath, FunctionRef reloadAltertStringsSet) {
        final SystemTray tray = SystemTray.getSystemTray();
        
		MenuItem reloadItem = new MenuItem("Reload");
		reloadItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				reloadAltertStringsSet.execute();
			}
        });
        
		MenuItem exitItem = new MenuItem("Exit");
		exitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		
		try {
			BufferedImage trayIconImage = ImageIO.read(new File(trayIconPath));
			TrayIcon trayIcon = new TrayIcon(trayIconImage.getScaledInstance(tray.getTrayIconSize().width, -1, Image.SCALE_SMOOTH));
			trayIcon.setPopupMenu(popup);
            tray.add(trayIcon);
            popup.add(reloadItem);
			popup.add(exitItem);
		} catch (IOException | AWTException e) { 
			e.printStackTrace(); 
		}
	}
	
	public static void addMenuItem(MenuItem mi) {
		popup.add(mi);
	}
	
}
