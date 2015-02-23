package frontend;

/**
 * Created by Ivan on 2/11/2015.
 */
public class RegisterAllocator {

    public static boolean[] openRegs = new boolean[32];

    static {
        openRegs[0] = true;        // allocated to 0
        openRegs[31] = true;       // store return address for branching

        openRegs[28] = true;        // stack pointer
        openRegs[29] = true;        // frame pointer
    }

    // returns lowest available register or -1 for none
    public static int AllocateReg() {
        for(int i = 0; i < openRegs.length; i++) {
            if(!openRegs[i]) {
                openRegs[i] = true;
                return i;
            }
        }
        return -1;
    }

    public static void deallocate(int i) throws Exception {
        if(i > 0 && i < openRegs.length) {
            openRegs[i] = false;
        } else {
            //throw new Exception("Invalid dellocate call on reg"+i);
        }
    }
}
