#!/usr/bin/env python3
"""Run one command in its own process group with a hard host-side timeout."""

from __future__ import annotations

import os
import signal
import subprocess
import sys
import time


GRACE_SECONDS = 15


class ForwardedSignal(Exception):
    def __init__(self, signum: int) -> None:
        self.signum = signum


def stop_process_group(process: subprocess.Popen[bytes]) -> None:
    if process.poll() is not None:
        return
    try:
        os.killpg(process.pid, signal.SIGTERM)
    except ProcessLookupError:
        return
    try:
        process.wait(timeout=GRACE_SECONDS)
        return
    except subprocess.TimeoutExpired:
        pass
    try:
        os.killpg(process.pid, signal.SIGKILL)
    except ProcessLookupError:
        return
    process.wait()


def main() -> int:
    if len(sys.argv) < 3:
        print("Usage: run_with_watchdog.py TIMEOUT_SECONDS COMMAND [ARG ...]", file=sys.stderr)
        return 2
    try:
        timeout_seconds = int(sys.argv[1])
    except ValueError:
        print("TIMEOUT_SECONDS must be an integer.", file=sys.stderr)
        return 2
    if timeout_seconds < 60:
        print("TIMEOUT_SECONDS must be at least 60.", file=sys.stderr)
        return 2

    command = sys.argv[2:]
    process = subprocess.Popen(command, start_new_session=True)

    def forward_signal(signum: int, _frame: object) -> None:
        raise ForwardedSignal(signum)

    signal.signal(signal.SIGINT, forward_signal)
    signal.signal(signal.SIGTERM, forward_signal)
    started = time.monotonic()
    try:
        return_code = process.wait(timeout=timeout_seconds)
    except subprocess.TimeoutExpired:
        elapsed = int(time.monotonic() - started)
        print(
            f"WATCHDOG_TIMEOUT: command exceeded {timeout_seconds}s "
            f"(elapsed={elapsed}s); terminating process group {process.pid}.",
            file=sys.stderr,
            flush=True,
        )
        stop_process_group(process)
        return 124
    except ForwardedSignal as interruption:
        print(
            f"WATCHDOG_SIGNAL: received signal {interruption.signum}; "
            f"terminating process group {process.pid}.",
            file=sys.stderr,
            flush=True,
        )
        stop_process_group(process)
        return 128 + interruption.signum

    return return_code if return_code >= 0 else 128 - return_code


if __name__ == "__main__":
    raise SystemExit(main())
