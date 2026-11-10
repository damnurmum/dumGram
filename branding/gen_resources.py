# Regenerates the launcher icon, notification icon, splash and the "dumGram" wordmark
# from branding/dumGram-logo.svg. Needs macOS (qlmanage renders the png), Pillow and fontTools:
#   qlmanage -t -s 1024 -o /tmp branding/dumGram-logo.svg
#   python3 branding/gen_resources.py branding/dumGram-logo.svg /tmp/dumGram-logo.svg.png \
#       TMessagesProj/src/main/res TMessagesProj/src/main/assets/fonts/rextrabold.ttf

import os
import re
import sys

from PIL import Image, ImageDraw
from fontTools.pens.svgPathPen import SVGPathPen
from fontTools.pens.transformPen import TransformPen
from fontTools.ttLib import TTFont

SVG, RENDER, RES, FONT = sys.argv[1:5]

svg = open(SVG).read()
cat = re.search(r'<path d="([^"]+)"', svg).group(1)
g = re.search(r'x1="([\d.]+)" y1="([\d.]+)" x2="([\d.]+)" y2="([\d.]+)"', svg).groups()
x1, y1, x2, y2 = map(float, g)
c1, c2 = re.findall(r'stop-color="(#[0-9A-Fa-f]{6})"', svg)
CIRCLE = "M0,500a500,500 0 1,0 1000,0a500,500 0 1,0 -1000,0z"


def gradient(scale, dx, dy):
    return (f'<aapt:attr name="android:fillColor"><gradient android:type="linear"'
            f' android:startX="{dx + x1 * scale:.2f}" android:startY="{dy + y1 * scale:.2f}"'
            f' android:endX="{dx + x2 * scale:.2f}" android:endY="{dy + y2 * scale:.2f}"'
            f' android:startColor="{c1}" android:endColor="{c2}"/></aapt:attr>')


def write(path, text):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    open(path, "w").write(text)


HEAD = '<?xml version="1.0" encoding="utf-8"?>\n'
NS = 'xmlns:android="http://schemas.android.com/apk/res/android"'
NS_AAPT = NS + ' xmlns:aapt="http://schemas.android.com/aapt"'

# adaptive icon: 108dp layers, the logo circle (1000 units) maps onto the 72dp visible area
S, D = 0.072, 18
write(f"{RES}/drawable/dum_icon_background.xml", HEAD +
      f'<vector {NS_AAPT} android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">\n'
      f'    <path android:pathData="M0,0h108v108h-108z">{gradient(S, D, D)}</path>\n</vector>\n')
write(f"{RES}/drawable/dum_icon_foreground.xml", HEAD +
      f'<vector {NS} android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">\n'
      f'    <group android:translateX="{D}" android:translateY="{D}" android:scaleX="{S}" android:scaleY="{S}">\n'
      f'        <path android:fillColor="#FFFFFFFF" android:fillType="evenOdd" android:pathData="{cat}"/>\n'
      f'    </group>\n</vector>\n')

# status bar icon: just the cat, cropped close (it spans roughly 240..765 x 214..787 of the 1000 box)
write(f"{RES}/drawable/notification.xml", HEAD +
      f'<vector {NS} android:width="24dp" android:height="24dp" android:viewportWidth="620" android:viewportHeight="620">\n'
      f'    <group android:translateX="-192" android:translateY="-190">\n'
      f'        <path android:fillColor="#FFFFFFFF" android:fillType="evenOdd" android:pathData="{cat}"/>\n'
      f'    </group>\n</vector>\n')

# android 12+ splash: icon without background, the logo has to fit a circle 2/3 of the canvas
write(f"{RES}/drawable/dum_splash.xml", HEAD +
      f'<vector {NS_AAPT} android:width="288dp" android:height="288dp" android:viewportWidth="1500" android:viewportHeight="1500">\n'
      f'    <group android:translateX="250" android:translateY="250">\n'
      f'        <path android:pathData="{CIRCLE}">{gradient(1, 0, 0)}</path>\n'
      f'        <path android:fillColor="#FFFFFFFF" android:fillType="evenOdd" android:pathData="{cat}"/>\n'
      f'    </group>\n</vector>\n')

# legacy launcher pngs (api 23-25): qlmanage renders on white, cut the circle out
src = Image.open(RENDER).convert("RGBA")
big = src.size[0]
mask = Image.new("L", (big * 4, big * 4), 0)
ImageDraw.Draw(mask).ellipse((0, 0, big * 4 - 1, big * 4 - 1), fill=255)
src.putalpha(mask.resize((big, big), Image.LANCZOS))
for density, px in (("mdpi", 48), ("hdpi", 72), ("xhdpi", 96), ("xxhdpi", 144), ("xxxhdpi", 192)):
    icon = src.resize((px, px), Image.LANCZOS)
    for name in ("ic_launcher.png", "ic_launcher_round.png"):
        icon.save(f"{RES}/mipmap-{density}/{name}", optimize=True)

# "dumGram" wordmark in place of telegram's, same box ratio so the code using it needs no changes
font = TTFont(FONT)
gs, cmap, upm = font.getGlyphSet(), font.getBestCmap(), font["head"].unitsPerEm
pen = SVGPathPen(gs)
x = 0
for ch in "dumGram":
    name = cmap[ord(ch)]
    gs[name].draw(TransformPen(pen, (1, 0, 0, -1, x, 0)))  # flip y, fonts grow upwards
    x += font["hmtx"][name][0]
top = max(font["glyf"][cmap[ord(c)]].yMax for c in "dumGram")
bottom = int(0.24 * upm)  # descender room, like telegram's "g", keeps the baseline where text sits
text_w, text_h = x, top + bottom


def wordmark(path, ratio, height_dp, fill_attr):
    vw, vh = text_w, text_h
    if vw / vh < ratio:
        vw = vh * ratio  # pad on the right, the left edge stays aligned with the title
    else:
        vh = vw / ratio
    write(path, HEAD +
          f'<vector {NS} android:width="{height_dp * vw / vh:.3f}dp" android:height="{height_dp}dp"'
          f' android:viewportWidth="{vw:.1f}" android:viewportHeight="{vh:.1f}">\n'
          f'    <group android:translateY="{top + (vh - text_h) / 2:.1f}">\n'
          f'        <path {fill_attr} android:pathData="{pen.getCommands()}"/>\n'
          f'    </group>\n</vector>\n')


wordmark(f"{RES}/drawable/telegram_logo.xml", 249 / 57, 19, 'android:fillColor="#FFFFFFFF"')
wordmark(f"{RES}/drawable/telegram_logo_2.xml", 269 / 61, 20.408922, 'android:fillColor="#FFFFFFFF"')
print("ok", text_w, text_h)
