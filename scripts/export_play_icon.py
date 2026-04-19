#!/usr/bin/env python3

from pathlib import Path

from PIL import Image, ImageDraw


SIZE = 512
VIEWBOX = 108.0
SCALE = SIZE / VIEWBOX

BACKGROUND = "#F5E7BF"
CROSS = "#7A2A1D"
GOLD = "#C6942C"

ROOT = Path(__file__).resolve().parent.parent
OUTPUT = ROOT / "release/play-assets/generated/play-icon-512.png"


def scale(value: float) -> int:
    return round(value * SCALE)


def rect(draw: ImageDraw.ImageDraw, left: float, top: float, right: float, bottom: float, fill: str) -> None:
    draw.rectangle((scale(left), scale(top), scale(right), scale(bottom)), fill=fill)


def main() -> None:
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    image = Image.new("RGBA", (SIZE, SIZE), BACKGROUND)
    draw = ImageDraw.Draw(image)

    rect(draw, 48, 16, 60, 72, CROSS)
    rect(draw, 30, 34, 78, 46, CROSS)
    rect(draw, 28, 78, 80, 86, CROSS)
    rect(draw, 32, 74, 40, 92, GOLD)
    rect(draw, 68, 74, 76, 92, GOLD)

    image.save(OUTPUT)
    print(f"Wrote {OUTPUT}")


if __name__ == "__main__":
    main()
