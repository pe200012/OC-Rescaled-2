/* oc.h - OpenComputers components from C, for bare-metal programs and Linux.
 *
 * Talks to the machine's component window, a page of registers and a buffer
 * carrying CBOR. Bare-metal programs find it at a fixed address; on Linux it is
 * a UIO device, which needs root to open. Not re-entrant: one program, one
 * call at a time, and none from interrupt handlers.
 *
 *     #include <oc.h>
 *
 *     int main(void) {
 *         char redstone[OC_ADDRESS_SIZE];
 *         if (oc_init() < 0 || oc_find("redstone", redstone) < 0)
 *             return 1;
 *         oc_callf(redstone, "setOutput", "ii", 1, 15);
 *         return 0;
 *     }
 *
 * Calls and the other requests leave their reply behind: read its values with
 * oc_count(), oc_type(), oc_int(), oc_double(), oc_bool() and oc_string().
 * Bare-metal programs are built with ocbuild (tcc, in the machine) or with
 * riscv64-unknown-elf-gcc and lib/oc/crt0.S and lib/oc/link.ld.
 */

#ifndef OC_H
#define OC_H

#if !defined(OC_BAREMETAL) && !defined(__linux__)
#define OC_BAREMETAL 1
#endif

#include <stddef.h>
#include <stdarg.h>

#ifdef OC_BAREMETAL
#define OC_WINDOW_ADDRESS 0x30000000UL
#elif defined(__TINYC__)
#include <tcclib.h>
/* Not declared by tcclib.h. */
int open(const char *path, int flags, ...);
int close(int fd);
long read(int fd, void *buffer, unsigned long count);
long write(int fd, const void *buffer, unsigned long count);
void *mmap(void *address, size_t length, int protection, int flags, int fd, long offset);
int usleep(unsigned int microseconds);
struct pollfd { int fd; short events; short revents; };
int poll(struct pollfd *fds, unsigned long count, int timeout);
struct timespec { long tv_sec; long tv_nsec; };
int clock_gettime(int clock, struct timespec *time);
#else
#include <fcntl.h>
#include <poll.h>
#include <stdio.h>
#include <sys/mman.h>
#include <time.h>
#include <unistd.h>
#endif

#define OC_ADDRESS_SIZE 37 /* a component address with its terminating zero */

#ifndef OC_REPLY_SIZE
#define OC_REPLY_SIZE 4096 /* the largest reply kept, in bytes */
#endif
#ifndef OC_MAX_VALUES
#define OC_MAX_VALUES 64 /* the most values a reply may have */
#endif

/* Value types, from oc_type(). */
#define OC_NONE 0 /* past the end of the reply */
#define OC_NIL 1
#define OC_BOOL 2
#define OC_INT 3
#define OC_DOUBLE 4
#define OC_STRING 5
#define OC_BYTES 6
#define OC_ARRAY 7
#define OC_MAP 8

/* The window's registers. */
#define OC_REG_MAGIC 0x00
#define OC_REG_BUFFER_SIZE 0x04
#define OC_REG_COMMAND 0x08
#define OC_REG_STATUS 0x0C
#define OC_REG_LENGTH 0x10
#define OC_REG_CONTROL 0x14
#define OC_REG_SIGNALS 0x18
#define OC_REG_GENERATION 0x1C
#define OC_REG_TIMEBASE 0x20
#define OC_BUFFER 0x1000
#define OC_WINDOW_SIZE 0x21000

#define OC_MAGIC 0x3142434FU

#define OC_COMMAND_LIST 1
#define OC_COMMAND_METHODS 2
#define OC_COMMAND_INVOKE 3
#define OC_COMMAND_SIGNAL 4
#define OC_COMMAND_PANIC 5

#define OC_STATUS_IDLE 0
#define OC_STATUS_BUSY 1
#define OC_STATUS_DONE 2
#define OC_STATUS_ERROR 3

#define OC_CONTROL_QUEUE 1
#define OC_CONTROL_INTERRUPT 2

#define OC_SIGNALS_OVERFLOW 0x80000000U

typedef struct {
    int type;
    long long integer; /* OC_INT, OC_BOOL; also the count of OC_ARRAY and OC_MAP */
    double number; /* OC_DOUBLE */
    const char *string; /* OC_STRING and OC_BYTES, zero-terminated */
    size_t length; /* OC_STRING and OC_BYTES */
    const unsigned char *raw; /* the encoded value */
} oc_value;

static volatile unsigned char *oc__window;
static unsigned char oc__reply[OC_REPLY_SIZE];
static char oc__strings[OC_REPLY_SIZE + OC_MAX_VALUES];
static oc_value oc__values[OC_MAX_VALUES];
static int oc__count;
static const char *oc__error = "";
static size_t oc__cursor; /* where the next request byte goes */
#ifndef OC_BAREMETAL
static int oc__fd = -1;
#endif

/* ------------------------------------------------------------------------ */
/* Registers and the buffer. */

static inline unsigned int oc_read_register(int offset) {
    return *(volatile unsigned int *) (oc__window + offset);
}

static inline void oc_write_register(int offset, unsigned int value) {
    *(volatile unsigned int *) (oc__window + offset) = value;
}

static inline void oc__put(unsigned char value) {
    if (oc__cursor < OC_WINDOW_SIZE - OC_BUFFER) {
        oc__window[OC_BUFFER + oc__cursor] = value;
    }
    oc__cursor++;
}

static inline void oc__put_head(int major, unsigned long long argument) {
    const int type = major << 5;
    if (argument < 24) {
        oc__put(type | (int) argument);
    } else if (argument <= 0xFF) {
        oc__put(type | 24);
        oc__put((unsigned char) argument);
    } else if (argument <= 0xFFFF) {
        oc__put(type | 25);
        oc__put((unsigned char) (argument >> 8));
        oc__put((unsigned char) argument);
    } else if (argument <= 0xFFFFFFFFULL) {
        oc__put(type | 26);
        for (int shift = 24; shift >= 0; shift -= 8) {
            oc__put((unsigned char) (argument >> shift));
        }
    } else {
        oc__put(type | 27);
        for (int shift = 56; shift >= 0; shift -= 8) {
            oc__put((unsigned char) (argument >> shift));
        }
    }
}

static inline void oc__put_data(int major, const void *data, size_t length) {
    oc__put_head(major, length);
    for (size_t i = 0; i < length; i++) {
        oc__put(((const unsigned char *) data)[i]);
    }
}

static inline size_t oc__strlen(const char *text) {
    size_t length = 0;
    while (text[length]) {
        length++;
    }
    return length;
}

/* ------------------------------------------------------------------------ */
/* Replies. */

static inline size_t oc__parse(const unsigned char *data, size_t length, size_t position, oc_value *value, char **strings);

static inline size_t oc__argument(const unsigned char *data, size_t length, size_t position, int info, unsigned long long *argument) {
    int count;
    if (info < 24) {
        *argument = info;
        return position;
    }
    switch (info) {
        case 24: count = 1; break;
        case 25: count = 2; break;
        case 26: count = 4; break;
        case 27: count = 8; break;
        default: return 0;
    }
    if (position + count > length) {
        return 0;
    }
    *argument = 0;
    for (int i = 0; i < count; i++) {
        *argument = (*argument << 8) | data[position++];
    }
    return position;
}

/* Skips one value. Returns the position after it, or 0 if it is malformed. */
static inline size_t oc__skip(const unsigned char *data, size_t length, size_t position, int depth) {
    unsigned long long argument;
    int major, info;
    if (position >= length || depth > 16) {
        return 0;
    }
    major = data[position] >> 5;
    info = data[position] & 0x1F;
    position++;
    if (info == 31) { /* indefinite length */
        if (major < 2 || major > 5) {
            return 0;
        }
        while (position < length && data[position] != 0xFF) {
            position = oc__skip(data, length, position, depth + 1);
            if (!position || (major == 5 && !(position = oc__skip(data, length, position, depth + 1)))) {
                return 0;
            }
        }
        return position < length ? position + 1 : 0;
    }
    if (major == 7) {
        if (info == 25) return position + 2 <= length ? position + 2 : 0;
        if (info == 26) return position + 4 <= length ? position + 4 : 0;
        if (info == 27) return position + 8 <= length ? position + 8 : 0;
        return position;
    }
    position = oc__argument(data, length, position, info, &argument);
    if (!position) {
        return 0;
    }
    switch (major) {
        case 2:
        case 3:
            return argument <= length - position ? position + (size_t) argument : 0;
        case 4:
        case 5:
            for (unsigned long long i = 0; i < argument * (major == 5 ? 2 : 1); i++) {
                position = oc__skip(data, length, position, depth + 1);
                if (!position) {
                    return 0;
                }
            }
            return position;
        case 6:
            return oc__skip(data, length, position, depth + 1);
        default:
            return position;
    }
}

static inline double oc__half(unsigned int bits) {
    const int exponent = (bits >> 10) & 0x1F;
    const int mantissa = bits & 0x3FF;
    double value;
    if (exponent == 0) {
        value = mantissa / 16777216.0;
    } else if (exponent == 31) {
        volatile double zero = 0;
        value = mantissa ? zero / zero : 1.0 / zero;
    } else {
        value = (mantissa + 1024) / 1024.0;
        for (int e = exponent - 15; e > 0; e--) value *= 2;
        for (int e = exponent - 15; e < 0; e++) value /= 2;
    }
    return (bits & 0x8000) ? -value : value;
}

/* Reads one value into *value. Strings are copied to *strings, zero-terminated. */
static inline size_t oc__parse(const unsigned char *data, size_t length, size_t position, oc_value *value, char **strings) {
    const size_t start = position;
    unsigned long long argument;
    int major, info;
    value->type = OC_NONE;
    value->integer = 0;
    value->number = 0;
    value->string = "";
    value->length = 0;
    value->raw = data + position;
    if (position >= length) {
        return 0;
    }
    major = data[position] >> 5;
    info = data[position] & 0x1F;
    position++;
    if (major == 6) { /* tags are ignored */
        position = oc__argument(data, length, position, info, &argument);
        return position ? oc__parse(data, length, position, value, strings) : 0;
    }
    if (major == 7) {
        switch (info) {
            case 20: value->type = OC_BOOL; value->integer = 0; return position;
            case 21: value->type = OC_BOOL; value->integer = 1; return position;
            case 22: case 23: value->type = OC_NIL; return position;
            case 25:
                if (position + 2 > length) return 0;
                value->type = OC_DOUBLE;
                value->number = oc__half((data[position] << 8) | data[position + 1]);
                return position + 2;
            case 26:
            case 27: {
                const int count = info == 26 ? 4 : 8;
                unsigned long long bits = 0;
                if (position + count > length) return 0;
                for (int i = 0; i < count; i++) bits = (bits << 8) | data[position + i];
                value->type = OC_DOUBLE;
                if (count == 4) {
                    union { unsigned int bits; float value; } single;
                    single.bits = (unsigned int) bits;
                    value->number = single.value;
                } else {
                    union { unsigned long long bits; double value; } dual;
                    dual.bits = bits;
                    value->number = dual.value;
                }
                return position + count;
            }
            default:
                return 0;
        }
    }
    if (info == 31) { /* indefinite length: only containers are counted */
        const size_t end = oc__skip(data, length, start, 0);
        if (!end) return 0;
        if (major == 4 || major == 5) {
            value->type = major == 4 ? OC_ARRAY : OC_MAP;
            while (data[position] != 0xFF) {
                position = oc__skip(data, length, position, 1);
                if (major == 5) position = oc__skip(data, length, position, 1);
                value->integer++;
            }
        }
        return end;
    }
    position = oc__argument(data, length, position, info, &argument);
    if (!position) {
        return 0;
    }
    switch (major) {
        case 0:
            value->type = OC_INT;
            value->integer = (long long) argument;
            return position;
        case 1:
            value->type = OC_INT;
            value->integer = -1 - (long long) argument;
            return position;
        case 2:
        case 3: {
            if (argument > length - position) return 0;
            value->type = major == 2 ? OC_BYTES : OC_STRING;
            value->length = (size_t) argument;
            for (size_t i = 0; i < value->length; i++) (*strings)[i] = (char) data[position + i];
            (*strings)[value->length] = 0;
            value->string = *strings;
            *strings += value->length + 1;
            return position + (size_t) argument;
        }
        default: {
            const size_t end = oc__skip(data, length, start, 0);
            value->type = major == 4 ? OC_ARRAY : OC_MAP;
            value->integer = (long long) argument;
            return end;
        }
    }
}

/* Waits for the window to finish and takes its reply. Returns 0, or -1 on errors. */
static inline int oc__finish(void) {
    unsigned int status;
    size_t length;
    char *strings = oc__strings;
    oc__count = 0;
    while ((status = oc_read_register(OC_REG_STATUS)) == OC_STATUS_BUSY) {
#ifndef OC_BAREMETAL
        usleep(1000);
#endif
    }
    length = oc_read_register(OC_REG_LENGTH);
    if (length > OC_REPLY_SIZE) {
        oc__error = "reply larger than OC_REPLY_SIZE";
        return -1;
    }
    for (size_t i = 0; i < length; i++) {
        oc__reply[i] = oc__window[OC_BUFFER + i];
    }
    if (status == OC_STATUS_ERROR) {
        oc_value message;
        oc__error = oc__parse(oc__reply, length, 0, &message, &strings) && message.type == OC_STRING
                    ? message.string : "unknown error";
        return -1;
    }
    if (length == 0) {
        return 0;
    }
    {
        oc_value list;
        size_t position = oc__parse(oc__reply, length, 0, &list, &strings);
        if (!position || list.type != OC_ARRAY) {
            oc__error = "malformed reply";
            return -1;
        }
        position = list.raw - oc__reply + 1;
        if ((list.raw[0] & 0x1F) >= 24 && (list.raw[0] & 0x1F) != 31) {
            position += 1 << ((list.raw[0] & 0x1F) - 24);
        }
        for (long long i = 0; i < list.integer && oc__count < OC_MAX_VALUES; i++) {
            position = oc__parse(oc__reply, length, position, &oc__values[oc__count], &strings);
            if (!position) {
                oc__error = "malformed reply";
                oc__count = 0;
                return -1;
            }
            oc__count++;
        }
    }
    return 0;
}

static inline int oc__command(int command) {
    oc_write_register(OC_REG_LENGTH, (unsigned int) oc__cursor);
    oc_write_register(OC_REG_COMMAND, command);
    return oc__finish();
}

/* ------------------------------------------------------------------------ */
/* Reading replies. */

/* The number of values in the last reply. */
static inline int oc_count(void) {
    return oc__count;
}

/* The type of the value at index, OC_NONE past the end. */
static inline int oc_type(int index) {
    return index >= 0 && index < oc__count ? oc__values[index].type : OC_NONE;
}

static inline const oc_value *oc_value_at(int index) {
    static const oc_value none = {OC_NONE, 0, 0, "", 0, 0};
    return index >= 0 && index < oc__count ? &oc__values[index] : &none;
}

/* Numbers and booleans as an integer; 0 for anything else. */
static inline long long oc_int(int index) {
    const oc_value *value = oc_value_at(index);
    return value->type == OC_DOUBLE ? (long long) value->number : value->integer;
}

static inline double oc_double(int index) {
    const oc_value *value = oc_value_at(index);
    return value->type == OC_DOUBLE ? value->number : (double) value->integer;
}

/* Truthiness as in Lua: false only for nil, false and missing values. */
static inline int oc_bool(int index) {
    const oc_value *value = oc_value_at(index);
    return value->type != OC_NONE && value->type != OC_NIL && (value->type != OC_BOOL || value->integer);
}

/* Strings and bytes, zero-terminated; "" for anything else. */
static inline const char *oc_string(int index) {
    return oc_value_at(index)->string;
}

/* Whether the value at index is the string text. */
static inline int oc_equals(int index, const char *text) {
    const oc_value *value = oc_value_at(index);
    size_t i = 0;
    if (value->type != OC_STRING && value->type != OC_BYTES) {
        return 0;
    }
    for (; i < value->length && text[i]; i++) {
        if (value->string[i] != text[i]) return 0;
    }
    return i == value->length && !text[i];
}

/* The byte length of strings and bytes, the element count of arrays and maps. */
static inline size_t oc_length(int index) {
    const oc_value *value = oc_value_at(index);
    return value->type == OC_ARRAY || value->type == OC_MAP ? (size_t) value->integer : value->length;
}

/* Why the last request failed. */
static inline const char *oc_error(void) {
    return oc__error;
}

/* ------------------------------------------------------------------------ */
/* Calls. */

/* Starts a call; add arguments with oc_push_*(), then run it with oc_call(). */
static inline void oc_begin(const char *address, const char *method) {
    oc__cursor = 0;
    oc__put(0x9F); /* an array of unknown length */
    oc__put_data(3, address, oc__strlen(address));
    oc__put_data(3, method, oc__strlen(method));
}

static inline void oc_push_nil(void) {
    oc__put(0xF6);
}

static inline void oc_push_bool(int value) {
    oc__put(value ? 0xF5 : 0xF4);
}

static inline void oc_push_int(long long value) {
    if (value >= 0) {
        oc__put_head(0, (unsigned long long) value);
    } else {
        oc__put_head(1, (unsigned long long) (-1 - value));
    }
}

static inline void oc_push_double(double value) {
    union { double value; unsigned long long bits; } dual;
    dual.value = value;
    oc__put(0xFB);
    for (int shift = 56; shift >= 0; shift -= 8) {
        oc__put((unsigned char) (dual.bits >> shift));
    }
}

static inline void oc_push_string(const char *value) {
    oc__put_data(3, value, oc__strlen(value));
}

static inline void oc_push_bytes(const void *data, size_t length) {
    oc__put_data(2, data, length);
}

/* Runs the call. Returns the number of results, or -1 if it failed (see oc_error()).
 * Calls that must run on the server thread take until the next tick. */
static inline int oc_call(void) {
    oc__put(0xFF);
    if (oc__cursor > OC_WINDOW_SIZE - OC_BUFFER) {
        oc__error = "arguments too large";
        return -1;
    }
    return oc__command(OC_COMMAND_INVOKE) < 0 ? -1 : oc__count;
}

/* Calls with arguments described by format, one character each:
 * i int, l long long, d double, b bool (int), s string, y bytes (pointer, size_t), n nil.
 * Returns like oc_call(). */
static inline int oc_callf(const char *address, const char *method, const char *format, ...) {
    va_list args;
    oc_begin(address, method);
    va_start(args, format);
    for (; *format; format++) {
        switch (*format) {
            case 'i': oc_push_int(va_arg(args, int)); break;
            case 'l': oc_push_int(va_arg(args, long long)); break;
            case 'd': oc_push_double(va_arg(args, double)); break;
            case 'b': oc_push_bool(va_arg(args, int)); break;
            case 's': oc_push_string(va_arg(args, const char *)); break;
            case 'y': {
                const void *data = va_arg(args, const void *);
                oc_push_bytes(data, va_arg(args, size_t));
                break;
            }
            case 'n': oc_push_nil(); break;
            default:
                va_end(args);
                oc__error = "unknown format character";
                return -1;
        }
    }
    va_end(args);
    return oc_call();
}

/* ------------------------------------------------------------------------ */
/* Components. */

/* Lists the components. Returns how many there are, or -1. The reply holds an address
 * and a type for each: oc_string(2 * i) and oc_string(2 * i + 1). */
static inline int oc_list(void) {
    oc__cursor = 0;
    return oc__command(OC_COMMAND_LIST) < 0 ? -1 : oc__count / 2;
}

/* Finds the first component of a type, by address. Returns 0, or -1 if there is none. */
static inline int oc_find(const char *type, char address[OC_ADDRESS_SIZE]) {
    const int count = oc_list();
    for (int i = 0; i < count; i++) {
        const char *a = oc_string(2 * i + 1), *b = type;
        while (*a && *a == *b) a++, b++;
        if (*a == *b) {
            const char *source = oc_string(2 * i);
            int j = 0;
            for (; j < OC_ADDRESS_SIZE - 1 && source[j]; j++) address[j] = source[j];
            address[j] = 0;
            return 0;
        }
    }
    oc__error = "no such component";
    return -1;
}

/* Lists a component's methods. Returns how many there are, or -1. The reply holds a
 * name and documentation for each: oc_string(2 * i) and oc_string(2 * i + 1). */
static inline int oc_methods(const char *address) {
    oc__cursor = 0;
    oc__put_data(3, address, oc__strlen(address));
    return oc__command(OC_COMMAND_METHODS) < 0 ? -1 : oc__count / 2;
}

/* Changes whenever components are added or removed. */
static inline unsigned int oc_generation(void) {
    return oc_read_register(OC_REG_GENERATION);
}

/* ------------------------------------------------------------------------ */
/* Signals and time. */

/* Takes the oldest queued signal into the reply: oc_string(0) is its name, then come its
 * arguments. Returns 1, 0 if none is queued, or -1. */
static inline int oc_signal(void) {
    oc__cursor = 0;
    if (oc__command(OC_COMMAND_SIGNAL) < 0) {
        return -1;
    }
    return oc__count > 0;
}

#ifdef OC_BAREMETAL

#define OC_CLINT_MTIMECMP 0x02004000UL
#define OC_CLINT_MTIME 0x0200BFF8UL
#define OC_WINDOW_INTERRUPT 4
#define OC_PLIC_PRIORITY (0x0C000000UL + 4 * OC_WINDOW_INTERRUPT)
#define OC_PLIC_ENABLE 0x0C002000UL /* machine mode */
#define OC_PLIC_THRESHOLD 0x0C200000UL
#define OC_PLIC_CLAIM 0x0C200004UL
#define OC_SYSCON 0x01000000UL

/* Memory-mapped registers at fixed addresses. Use these rather than storing through a constant
 * address: tcc 0.9.27 uses the frame pointer as scratch register for that, after which all
 * local variables read garbage. tcc passes the address as an argument instead. */
static inline unsigned int oc_peek32(unsigned long address) {
    return *(volatile unsigned int *) address;
}

static inline void oc_poke32(unsigned long address, unsigned int value) {
    *(volatile unsigned int *) address = value;
}

static inline unsigned long long oc_peek64(unsigned long address) {
    return *(volatile unsigned long long *) address;
}

static inline void oc_poke64(unsigned long address, unsigned long long value) {
    *(volatile unsigned long long *) address = value;
}

#ifdef __TINYC__
/* tcc cannot assemble RISC-V, so instructions are spelled out. The argument is still in a0
 * where the body starts. */
static void oc__mie_set(unsigned long bits) { __asm__(".int 0x30452073"); } /* csrs mie, a0 */
static void oc__mie_clear(unsigned long bits) { __asm__(".int 0x30453073"); } /* csrc mie, a0 */
static void oc__wfi(void) { __asm__(".int 0x10500073"); } /* wfi */
#else
static inline void oc__mie_set(unsigned long bits) { __asm__ volatile("csrs mie, %0" :: "r"(bits)); }
static inline void oc__mie_clear(unsigned long bits) { __asm__ volatile("csrc mie, %0" :: "r"(bits)); }
static inline void oc__wfi(void) { __asm__ volatile("wfi"); }
#endif

/* Timer ticks since the machine started. */
static inline unsigned long long oc_ticks(void) {
    return oc_peek64(OC_CLINT_MTIME);
}

static inline unsigned int oc_ticks_per_second(void) {
    return oc_read_register(OC_REG_TIMEBASE);
}

static inline unsigned long long oc_millis(void) {
    return oc_ticks() / (oc_ticks_per_second() / 1000);
}

/* Sleeps until a signal is queued or timeout_ms passed; negative waits forever.
 * Returns 1 if a signal is queued, 0 on timeout. The machine idles meanwhile. */
static inline int oc_wait(long long timeout_ms) {
    const int forever = timeout_ms < 0;
    unsigned long long deadline = ~0ULL;
    if (!forever) {
        deadline = oc_ticks() + (unsigned long long) timeout_ms * (oc_ticks_per_second() / 1000);
    }
    oc_poke32(OC_PLIC_PRIORITY, 1);
    oc_poke32(OC_PLIC_ENABLE, oc_peek32(OC_PLIC_ENABLE) | 1U << OC_WINDOW_INTERRUPT);
    oc_poke32(OC_PLIC_THRESHOLD, 0);
    oc_write_register(OC_REG_CONTROL, OC_CONTROL_QUEUE | OC_CONTROL_INTERRUPT);
    for (;;) {
        unsigned int claimed;
        if (oc_read_register(OC_REG_SIGNALS) & ~OC_SIGNALS_OVERFLOW) {
            return 1;
        }
        if (!forever && oc_ticks() >= deadline) {
            return 0;
        }
        oc_poke64(OC_CLINT_MTIMECMP, deadline);
        oc__mie_set((1UL << 11) | (1UL << 7)); /* external and timer interrupts wake us */
        oc__wfi();
        oc__mie_clear((1UL << 11) | (1UL << 7));
        oc_poke64(OC_CLINT_MTIMECMP, ~0ULL);
        claimed = oc_peek32(OC_PLIC_CLAIM);
        if (claimed) {
            oc_poke32(OC_PLIC_CLAIM, claimed);
        }
    }
}

static inline void oc_sleep(long long ms) {
    const unsigned long long deadline = oc_ticks() + (unsigned long long) ms * (oc_ticks_per_second() / 1000);
    oc__mie_set(1UL << 7);
    while (oc_ticks() < deadline) {
        oc_poke64(OC_CLINT_MTIMECMP, deadline);
        oc__wfi();
    }
    oc_poke64(OC_CLINT_MTIMECMP, ~0ULL);
    oc__mie_clear(1UL << 7);
}

/* Turns the machine off. */
static inline void oc_shutdown(void) {
    oc_poke32(OC_SYSCON, 0x2000);
    for (;;) oc__wfi();
}

/* Restarts the machine, which reads the EEPROM again. */
static inline void oc_reboot(void) {
    oc_poke32(OC_SYSCON, 0x1000);
    for (;;) oc__wfi();
}

/* Stops the machine with an error message, shown like any other crash. */
static inline void oc_panic(const char *message) {
    oc__cursor = 0;
    oc__put_data(3, message, oc__strlen(message));
    oc_write_register(OC_REG_LENGTH, (unsigned int) oc__cursor);
    oc_write_register(OC_REG_COMMAND, OC_COMMAND_PANIC);
    oc_shutdown();
}

/* Sets up the window and starts queueing signals. Returns 0, or -1 if there is none. */
static inline int oc_init(void) {
    oc__window = (volatile unsigned char *) OC_WINDOW_ADDRESS;
    if (oc_read_register(OC_REG_MAGIC) != OC_MAGIC) {
        oc__error = "no component window";
        return -1;
    }
    oc_write_register(OC_REG_CONTROL, OC_CONTROL_QUEUE);
    return 0;
}

#else /* Linux */

static inline unsigned long long oc_millis(void) {
    struct timespec now;
    clock_gettime(1 /* CLOCK_MONOTONIC */, &now);
    return (unsigned long long) now.tv_sec * 1000 + now.tv_nsec / 1000000;
}

/* Sleeps until a signal is queued or timeout_ms passed; negative waits forever.
 * Returns 1 if a signal is queued, 0 on timeout. */
static inline int oc_wait(long long timeout_ms) {
    const int forever = timeout_ms < 0;
    unsigned long long deadline = oc_millis();
    if (!forever) {
        deadline += (unsigned long long) timeout_ms;
    }
    oc_write_register(OC_REG_CONTROL, OC_CONTROL_QUEUE | OC_CONTROL_INTERRUPT);
    for (;;) {
        struct pollfd ready;
        unsigned int enable = 1, events;
        long long left;
        if (write(oc__fd, &enable, sizeof enable) != sizeof enable) {
            return 0;
        }
        if (oc_read_register(OC_REG_SIGNALS) & ~OC_SIGNALS_OVERFLOW) {
            return 1;
        }
        left = -1;
        if (!forever) {
            const unsigned long long now = oc_millis();
            if (now >= deadline) {
                return 0;
            }
            left = (long long) (deadline - now);
        }
        ready.fd = oc__fd;
        ready.events = 1; /* POLLIN */
        ready.revents = 0;
        if (poll(&ready, 1, (int) left) > 0) {
            read(oc__fd, &events, sizeof events);
        }
    }
}

static inline void oc_sleep(long long ms) {
    usleep((unsigned int) ms * 1000);
}

/* Maps the window. Returns 0, or -1 if there is none or it cannot be opened. */
static inline int oc_init(void) {
    char path[48], name[32];
    for (int i = 0; i < 32; i++) {
        long count;
        int fd;
        void *page;
        snprintf(path, sizeof path, "/sys/class/uio/uio%d/name", i);
        fd = open(path, 0 /* O_RDONLY */);
        if (fd < 0) {
            continue;
        }
        count = read(fd, name, sizeof name - 1);
        close(fd);
        if (count < 0) {
            continue;
        }
        name[count] = 0;
        if (count > 0 && name[count - 1] == '\n') {
            name[count - 1] = 0;
        }
        {
            const char *a = name, *b = "component-bus";
            while (*a && *a == *b) a++, b++;
            if (*a != *b) {
                continue;
            }
        }

        snprintf(path, sizeof path, "/dev/uio%d", i);
        fd = open(path, 2 /* O_RDWR */);
        if (fd < 0) {
            oc__error = "cannot open the component window, are you root?";
            return -1;
        }
        page = mmap(NULL, OC_WINDOW_SIZE, 3 /* PROT_READ | PROT_WRITE */, 1 /* MAP_SHARED */, fd, 0);
        if (page == (void *) -1) {
            close(fd);
            oc__error = "cannot map the component window";
            return -1;
        }
        oc__window = page;
        oc__fd = fd;
        if (oc_read_register(OC_REG_MAGIC) != OC_MAGIC) {
            oc__error = "no component window";
            return -1;
        }
        oc_write_register(OC_REG_CONTROL, OC_CONTROL_QUEUE);
        return 0;
    }
    oc__error = "no component window";
    return -1;
}

#endif

/* Waits for the next signal like oc_wait() and takes it like oc_signal(). */
static inline int oc_pull(long long timeout_ms) {
    return oc_wait(timeout_ms) > 0 ? oc_signal() : 0;
}

#endif
