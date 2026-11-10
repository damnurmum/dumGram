# Builds the README banner (logo + "dumGram" wordmark) in a light and a dark variant.
# The wordmark is real glyph outlines, so GitHub shows it the same everywhere. Needs fontTools:
#   python3 branding/gen_banner.py
import re

from fontTools.pens.svgPathPen import SVGPathPen
from fontTools.pens.transformPen import TransformPen
from fontTools.ttLib import TTFont

FONT = "TMessagesProj/src/main/assets/fonts/rextrabold.ttf"
LOGO = "branding/dumGram-logo.svg"

logo = open(LOGO).read()
logo_body = logo[logo.index(">") + 1:logo.rindex("</svg>")]  # circle, cat and the gradient defs

font = TTFont(FONT)
glyphs, cmap, upm = font.getGlyphSet(), font.getBestCmap(), font["head"].unitsPerEm
pen = SVGPathPen(glyphs)
x = 0
for ch in "dumGram":
    name = cmap[ord(ch)]
    glyphs[name].draw(TransformPen(pen, (1, 0, 0, -1, x, 0)))
    x += font["hmtx"][name][0]
text_width = x
cap = font["OS/2"].sCapHeight

# logo 1000 units scaled to 180px, text sized so its cap height is about half the logo
LOGO_SIZE, PAD, GAP = 180, 10, 44
scale = LOGO_SIZE * 0.5 / cap
width = PAD + LOGO_SIZE + GAP + text_width * scale + PAD
height = LOGO_SIZE + PAD * 2
baseline = PAD + LOGO_SIZE / 2 + cap * scale / 2

for variant, color in (("light", "#1F2328"), ("dark", "#F0F6FC")):
    svg = (f'<svg xmlns="http://www.w3.org/2000/svg" width="{width:.0f}" height="{height:.0f}" '
           f'viewBox="0 0 {width:.0f} {height:.0f}" fill="none">\n'
           f'<g transform="translate({PAD} {PAD}) scale({LOGO_SIZE / 1000})">{logo_body}</g>\n'
           f'<path transform="translate({PAD + LOGO_SIZE + GAP} {baseline:.1f}) scale({scale:.5f})" '
           f'fill="{color}" d="{pen.getCommands()}"/>\n</svg>\n')
    open(f"branding/banner-{variant}.svg", "w").write(svg)
print("ok", round(width), round(height))
