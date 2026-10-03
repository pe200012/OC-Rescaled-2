/* crt0.c - startup for bare-metal programs.
 *
 * The machine copies the EEPROM to the start of RAM and jumps there, with the
 * device tree's address in a1, near the end of RAM. This sets up a stack below
 * it, switches the FPU on, catches traps and runs main(). When main() returns
 * the machine turns off; a trap crashes it with a message saying why.
 *
 * Must be the first file linked, so it starts the image. Builds with tcc (see
 * ocbuild), which cannot assemble RISC-V: there the instructions are spelled
 * out and fall through into the function right after them, so nothing may be
 * defined before the first one. Also builds with gcc and clang (see link.ld).
 */

#ifdef __TINYC__
__asm__(".text\n"
        ".int 0xff05f113\n"   /* andi sp, a1, -16  the stack ends at the device tree */
        ".int 0x34011073\n"   /* csrw mscratch, sp  kept for the trap handler */
        ".int 0x000022b7\n"   /* lui t0, 0x2 */
        ".int 0x3002a073\n"); /* csrs mstatus, t0  FPU on */
#else
__asm__(".section .text.start, \"ax\"\n"
        ".globl _start\n"
        "_start:\n"
        "    andi sp, a1, -16\n"
        "    csrw mscratch, sp\n"
        "    li t0, 0x2000\n"
        "    csrs mstatus, t0\n"
        "    la t0, __bss_start\n"
        "    la t1, _end\n"
        "1:  bgeu t0, t1, 2f\n"
        "    sd zero, 0(t0)\n"
        "    addi t0, t0, 8\n"
        "    j 1b\n"
        "2:  j _oc_start\n"
        ".text\n");
#endif

void _oc_main(void);

void _oc_start(void) {
    _oc_main();
}

/* Only oc_panic() and oc_shutdown() are used here, so keep the reply buffers small. */
#define OC_REPLY_SIZE 1
#define OC_MAX_VALUES 1
#include <oc.h>

int main();
extern char _oc_trap_entry[];

#ifdef __TINYC__
static void oc__set_mtvec(unsigned long address) { __asm__(".int 0x30551073"); } /* csrw mtvec, a0 */
#else
static inline void oc__set_mtvec(unsigned long address) { __asm__ volatile("csrw mtvec, %0" :: "r"(address)); }
#endif

void _oc_main(void) {
    oc__set_mtvec((unsigned long) _oc_trap_entry);
    main();
    oc_shutdown();
}

static char *oc__append(char *out, const char *text) {
    while (*text) *out++ = *text++;
    return out;
}

static char *oc__append_hex(char *out, unsigned long value) {
    int shift = 60;
    *out++ = '0';
    *out++ = 'x';
    while (shift > 0 && !((value >> shift) & 0xF)) shift -= 4;
    for (; shift >= 0; shift -= 4) *out++ = "0123456789abcdef"[(value >> shift) & 0xF];
    return out;
}

/* What compilers expect of the C library. */

#ifndef __TINYC__
#define OC__NO_LIBCALLS __attribute__((optimize("no-tree-loop-distribute-patterns")))
#else
#define OC__NO_LIBCALLS
#endif

OC__NO_LIBCALLS void *memset(void *target, int value, size_t length) {
    unsigned char *out = target;
    while (length--) *out++ = (unsigned char) value;
    return target;
}

OC__NO_LIBCALLS void *memcpy(void *target, const void *source, size_t length) {
    unsigned char *out = target;
    const unsigned char *in = source;
    while (length--) *out++ = *in++;
    return target;
}

OC__NO_LIBCALLS void *memmove(void *target, const void *source, size_t length) {
    unsigned char *out = target;
    const unsigned char *in = source;
    if (out < in) {
        while (length--) *out++ = *in++;
    } else {
        while (length--) out[length] = in[length];
    }
    return target;
}

int memcmp(const void *a, const void *b, size_t length) {
    const unsigned char *x = a, *y = b;
    for (; length; length--, x++, y++) {
        if (*x != *y) return *x - *y;
    }
    return 0;
}

size_t strlen(const char *text) {
    return oc__strlen(text);
}

/* Traps end up here; with tcc this must come last, falling through into _oc_trap. */

#ifdef __TINYC__
__asm__(".globl _oc_trap_entry\n"
        "_oc_trap_entry:\n"
        ".int 0x34002173\n"   /* csrr sp, mscratch */
        ".int 0x34202573\n"   /* csrr a0, mcause */
        ".int 0x341025f3\n"   /* csrr a1, mepc */
        ".int 0x34302673\n"); /* csrr a2, mtval */
#else
__asm__(".text\n"
        ".p2align 2\n"
        ".globl _oc_trap_entry\n"
        "_oc_trap_entry:\n"
        "    csrr sp, mscratch\n"
        "    csrr a0, mcause\n"
        "    csrr a1, mepc\n"
        "    csrr a2, mtval\n"
        "    j _oc_trap\n");
#endif

void _oc_trap(unsigned long cause, unsigned long pc, unsigned long value) {
    static const char *const names[] = {
        "misaligned instruction", "instruction access fault", "illegal instruction", "breakpoint",
        "misaligned load", "load access fault", "misaligned store", "store access fault",
        "ecall", "ecall", "ecall", "ecall",
        "instruction page fault", "load page fault", "reserved", "store page fault",
    };
    char message[128], *out = message;
    if ((long) cause < 0) {
        out = oc__append(out, "unexpected interrupt ");
        out = oc__append_hex(out, cause & 0xFF);
    } else {
        out = oc__append(out, cause < 16 ? names[cause] : "trap");
        out = oc__append(out, " at ");
        out = oc__append_hex(out, pc);
        out = oc__append(out, " (");
        out = oc__append_hex(out, value);
        out = oc__append(out, ")");
    }
    *out = 0;
    oc__window = (volatile unsigned char *) OC_WINDOW_ADDRESS;
    oc_panic(message);
}
