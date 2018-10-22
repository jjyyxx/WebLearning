package platform.win;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;

public class Imm32 {
    static {
        Native.register("Imm32.dll");
    }

    public static native Pointer ImmAssociateContext(
            WinDef.HWND hwnd,
            Pointer himc
    );
}
