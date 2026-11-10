# Builds the README banner (logo, "dumGram" and a tagline) in a light and a dark variant.
# Text is real glyph outlines, so GitHub shows it the same everywhere. Needs fontTools:
#   python3 branding/gen_banner.py
# Fonts: Cal Sans (OFL 1.1, github.com/calcom/sans), license in branding/fonts/OFL.txt
from fontTools.pens.svgPathPen import SVGPathPen
from fontTools.pens.transformPen import TransformPen
from fontTools.ttLib import TTFont

LOGO = "branding/dumGram-logo.svg"
TITLE_FONT = "branding/fonts/CalSans-SemiBold.ttf"
TAGLINE_FONT = "branding/fonts/CalSansTextUI-Regular.ttf"
TITLE, TAGLINE = "dumGram", "Telegram, a little more yours"


def outline(path, text, tracking=0.0):
    """Glyph outlines of `text` in font units with y pointing down, plus its width and cap height."""
    font = TTFont(path)
    glyphs, cmap, upm = font.getGlyphSet(), font.getBestCmap(), font["head"].unitsPerEm
    pen = SVGPathPen(glyphs)
    x = 0
    for ch in text:
        name = cmap[ord(ch)]
        glyphs[name].draw(TransformPen(pen, (1, 0, 0, -1, x, 0)))
        x += font["hmtx"][name][0] + tracking * upm
    return pen.getCommands(), x - tracking * upm, font["OS/2"].sCapHeight


logo = open(LOGO).read()
logo_body = logo[logo.index(">") + 1:logo.rindex("</svg>")]  # circle, cat and the gradient defs

title, title_w, title_cap = outline(TITLE_FONT, TITLE, tracking=-0.015)  # display sizes read better a bit tight
tagline, tagline_w, tagline_cap = outline(TAGLINE_FONT, TAGLINE)

PAD, LOGO_SIZE, GAP = 16, 156, 34
TITLE_CAP_PX, TAGLINE_CAP_PX, LINE_GAP = 68, 17, 26
title_scale = TITLE_CAP_PX / title_cap
tagline_scale = TAGLINE_CAP_PX / tagline_cap

height = LOGO_SIZE + PAD * 2
center = height / 2
block = TITLE_CAP_PX + LINE_GAP + TAGLINE_CAP_PX  # caps to caps, so the block centers on the logo optically
title_baseline = center - block / 2 + TITLE_CAP_PX
tagline_baseline = title_baseline + LINE_GAP + TAGLINE_CAP_PX
text_x = PAD + LOGO_SIZE + GAP
width = text_x + max(title_w * title_scale, tagline_w * tagline_scale) + PAD

for variant, title_color, tagline_color in (("light", "#1F2328", "#59636E"), ("dark", "#F0F6FC", "#9198A1")):
    svg = (f'<svg xmlns="http://www.w3.org/2000/svg" width="{width:.0f}" height="{height:.0f}" '
           f'viewBox="0 0 {width:.0f} {height:.0f}" fill="none">\n'
           f'<g transform="translate({PAD} {PAD}) scale({LOGO_SIZE / 1000})">{logo_body}</g>\n'
           f'<path transform="translate({text_x} {title_baseline:.1f}) scale({title_scale:.5f})" '
           f'fill="{title_color}" d="{title}"/>\n'
           f'<path transform="translate({text_x + 2} {tagline_baseline:.1f}) scale({tagline_scale:.5f})" '
           f'fill="{tagline_color}" d="{tagline}"/>\n</svg>\n')
    open(f"branding/banner-{variant}.svg", "w").write(svg)
print("ok", round(width), round(height))
