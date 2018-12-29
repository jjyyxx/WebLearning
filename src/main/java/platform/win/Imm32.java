package platform.win;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;

/**
 * 开启或停用输入法的native函数
 */
public final class Imm32 {
    static {
        Native.register("Imm32.dll");
    }

    private static WinDef.HWND hwnd;
    private static Pointer himc = null;

    public static native Pointer ImmAssociateContext(
            WinDef.HWND hwnd,
            Pointer himc
    );

    /**
     * 初始化
     */
    public static void init() {
        hwnd = User32.INSTANCE.FindWindow(null, "网络学堂");
    }

    /**
     * 在禁用与启用间切换
     * @param disable 禁用或启用
     */
    public static void set(boolean disable) {
        if (hwnd == null) {
            init();
        }
        if (disable) {
            himc = Imm32.ImmAssociateContext(hwnd, null);
        } else {
            Imm32.ImmAssociateContext(hwnd, himc);
        }
    }
}
