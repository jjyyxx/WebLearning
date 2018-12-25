package platform.win;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WTypes;
import com.sun.jna.platform.win32.WinCrypt;
import com.sun.jna.platform.win32.WinDef;

/**
 * 加密与解密的native函数
 */
public class Crypt32 {
    static {
        Native.register("Crypt32.dll");
    }

    public static native boolean CryptProtectData(
            WinCrypt.DATA_BLOB pDataIn,
            WTypes.LPWSTR szDataDescr,
            WinCrypt.DATA_BLOB pOptionalEntropy,
            WinDef.PVOID pvReserved,
            WinCrypt.CRYPTPROTECT_PROMPTSTRUCT pPromptStruct,
            WinDef.DWORD dwFlags,
            WinCrypt.DATA_BLOB pDataOut
    );

    public static native boolean CryptUnprotectData(
            WinCrypt.DATA_BLOB pDataIn,
            WTypes.LPWSTR ppszDataDescr,
            WinCrypt.DATA_BLOB pOptionalEntropy,
            WinDef.PVOID pvReserved,
            WinCrypt.CRYPTPROTECT_PROMPTSTRUCT pPromptStruct,
            WinDef.DWORD dwFlags,
            WinCrypt.DATA_BLOB pDataOut
    );
}
