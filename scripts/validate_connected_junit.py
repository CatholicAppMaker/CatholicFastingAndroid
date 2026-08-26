#!/usr/bin/env python3
"""Validate fresh connected-test JUnit XML without trusting Gradle's exit alone."""

from __future__ import annotations

import argparse
from collections import Counter
from pathlib import Path
import sys
import xml.etree.ElementTree as ET


def local_name(tag: str) -> str:
    return tag.rsplit("}", 1)[-1]


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("results_root", type=Path)
    parser.add_argument("run_started_epoch", type=int)
    parser.add_argument("output_path", type=Path)
    parser.add_argument("--class-name")
    parser.add_argument("--expected-count", type=int)
    parser.add_argument("--expected-name", action="append", default=[])
    parser.add_argument("--require-one-report", action="store_true")
    args = parser.parse_args()

    cases: list[tuple[str, list[str], str]] = []
    report_files: list[str] = []
    parse_errors: list[str] = []
    suite_problem_count = 0
    negative_time_count = 0

    for xml_path in sorted(args.results_root.rglob("TEST-*.xml")):
        if xml_path.stat().st_mtime < args.run_started_epoch - 2:
            continue
        try:
            root = ET.parse(xml_path).getroot()
        except (ET.ParseError, OSError) as error:
            parse_errors.append(f"{xml_path}: {error}")
            continue

        matching_cases = []
        for testcase in root.iter():
            if local_name(testcase.tag) != "testcase":
                continue
            if args.class_name and testcase.attrib.get("classname") != args.class_name:
                continue
            matching_cases.append(testcase)

        if not matching_cases:
            continue
        report_files.append(str(xml_path))

        for suite in root.iter():
            if local_name(suite.tag) not in {"testsuite", "testsuites"}:
                continue
            for attribute in ("failures", "errors", "skipped"):
                try:
                    suite_problem_count += int(suite.attrib.get(attribute, "0"))
                except ValueError:
                    suite_problem_count += 1
            try:
                if float(suite.attrib.get("time", "0")) < 0:
                    negative_time_count += 1
            except ValueError:
                negative_time_count += 1

        for testcase in matching_cases:
            problems: list[str] = []
            status = testcase.attrib.get("status", "").lower()
            if status in {"failure", "failed", "error", "skipped", "ignored"}:
                problems.append(status)
            try:
                if float(testcase.attrib.get("time", "0")) < 0:
                    negative_time_count += 1
            except ValueError:
                negative_time_count += 1
            for child in testcase:
                child_name = local_name(child.tag)
                if child_name in {"failure", "error", "skipped", "ignored"}:
                    problems.append(child_name)
            cases.append((testcase.attrib.get("name", ""), problems, str(xml_path)))

    names = [name for name, _, _ in cases]
    counts = Counter(names)
    expected = set(args.expected_name)
    missing = sorted(expected - set(names))
    unexpected = sorted(set(names) - expected) if expected else []
    duplicates = sorted(name for name, count in counts.items() if count > 1)
    case_problem_count = sum(len(problems) for _, problems, _ in cases)

    valid = (
        len(cases) > 0
        and not parse_errors
        and suite_problem_count == 0
        and case_problem_count == 0
        and negative_time_count == 0
        and (args.expected_count is None or len(cases) == args.expected_count)
        and (not expected or (len(cases) == len(expected) and not missing and not unexpected and not duplicates))
        and (not args.require_one_report or len(report_files) == 1)
    )

    args.output_path.parent.mkdir(parents=True, exist_ok=True)
    args.output_path.write_text(
        "\n".join(
            [
                f"status={'valid' if valid else 'invalid'}",
                f"class={args.class_name or 'ALL'}",
                f"observed_count={len(cases)}",
                f"expected_count={args.expected_count if args.expected_count is not None else 'NONZERO'}",
                f"observed_names={','.join(sorted(names))}",
                f"missing_names={','.join(missing)}",
                f"unexpected_names={','.join(unexpected)}",
                f"duplicate_names={','.join(duplicates)}",
                f"suite_problem_count={suite_problem_count}",
                f"case_problem_count={case_problem_count}",
                f"negative_time_count={negative_time_count}",
                f"fresh_xml_count={len(report_files)}",
                f"fresh_xml_files={','.join(report_files)}",
                f"parse_errors={' | '.join(parse_errors)}",
            ]
        )
        + "\n",
        encoding="utf-8",
    )
    if not valid:
        print(f"Connected JUnit validation failed; see {args.output_path}.", file=sys.stderr)
        return 1
    print(f"Connected JUnit results valid: {len(cases)} fresh testcases, zero problems.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
