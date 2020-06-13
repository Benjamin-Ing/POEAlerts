package mouse;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;
import com.sun.jna.platform.win32.BaseTSD.ULONG_PTR;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.POINT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser.HOOKPROC;

public interface LowLevelMouseProc extends HOOKPROC {
	
	public class MSLLHOOKSTRUCT extends Structure {
		public static class ByReference extends MSLLHOOKSTRUCT implements Structure.ByReference {};
		
		public POINT pt;
	    public DWORD mouseData;
	    public DWORD flags;
	    public DWORD time;
	    public ULONG_PTR dwExtraInfo;
		
		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("pt", "mouseData", "flags", "time", "dwExtraInfo");
		}
	}
	
	public LRESULT callback(int nCode, WPARAM wParam, LPARAM lParam);
	
}