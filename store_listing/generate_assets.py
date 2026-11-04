#!/usr/bin/env python3
"""
Generate professional Google Play Store assets for GeoChronicle:
1. ic_launcher-512.png (512x512 32-bit PNG)
2. feature_graphic.png (1024x500 24-bit RGB PNG)
"""

import math
import os
from PIL import Image, ImageDraw, ImageFont, ImageFilter

OUTPUT_DIR = os.path.join(os.path.dirname(__file__), "screenshots")
STORE_DIR = os.path.dirname(__file__)

FONT_SERIF_BOLD = "/System/Library/Fonts/Supplemental/Georgia Bold.ttf"
FONT_SERIF = "/System/Library/Fonts/Supplemental/Georgia.ttf"
FONT_SANS_BOLD = "/System/Library/Fonts/Supplemental/Arial Bold.ttf"
FONT_SANS = "/System/Library/Fonts/Supplemental/Arial.ttf"

def create_gradient_radial(width, height, center_x, center_y, r_inner, r_outer, color_inner, color_outer):
    base = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    # Render with math
    cx, cy = center_x, center_y
    for y in range(height):
        for x in range(width):
            dist = math.hypot(x - cx, y - cy)
            if dist <= r_inner:
                t = 0.0
            elif dist >= r_outer:
                t = 1.0
            else:
                t = (dist - r_inner) / (r_outer - r_inner)
            r = int(color_inner[0] * (1 - t) + color_outer[0] * t)
            g = int(color_inner[1] * (1 - t) + color_outer[1] * t)
            b = int(color_inner[2] * (1 - t) + color_outer[2] * t)
            a = int(color_inner[3] * (1 - t) + color_outer[3] * t)
            base.putpixel((x, y), (r, g, b, a))
    return base

def draw_temple(draw, cx, cy, scale=1.0, gold=(255, 213, 107, 255), gold_dark=(216, 181, 104, 255)):
    """Draws a classical Greco-Roman temple with pediment and columns."""
    # Scale reference: base width ~ 140 * scale
    w = 140 * scale
    
    # Tier 1 Base (bottom plinth)
    t1_w, t1_h = w, 8 * scale
    t1_y = cy + 40 * scale
    draw.rectangle([cx - t1_w / 2, t1_y, cx + t1_w / 2, t1_y + t1_h], fill=gold_dark)
    
    # Tier 2 Base
    t2_w, t2_h = w - 16 * scale, 6 * scale
    t2_y = t1_y - t2_h
    draw.rectangle([cx - t2_w / 2, t2_y, cx + t2_w / 2, t2_y + t2_h], fill=gold)
    
    # Tier 3 Stylobate
    t3_w, t3_h = w - 28 * scale, 6 * scale
    t3_y = t2_y - t3_h
    draw.rectangle([cx - t3_w / 2, t3_y, cx + t3_w / 2, t3_y + t3_h], fill=gold_dark)
    
    # 4 Columns
    col_h = 56 * scale
    col_w = 9 * scale
    col_y = t3_y - col_h
    spacing = (t3_w - 20 * scale) / 3
    col_starts = [
        cx - (t3_w - 20 * scale) / 2 + i * spacing
        for i in range(4)
    ]
    for col_x in col_starts:
        # Base
        draw.rectangle([col_x - col_w * 0.7, t3_y - 3 * scale, col_x + col_w * 0.7, t3_y], fill=gold)
        # Shaft with fluting highlight
        draw.rectangle([col_x - col_w / 2, col_y + 4 * scale, col_x + col_w / 2, t3_y - 3 * scale], fill=gold_dark)
        draw.line([col_x, col_y + 4 * scale, col_x, t3_y - 3 * scale], fill=(255, 240, 180, 255), width=int(max(1, 2 * scale)))
        # Capital
        draw.rectangle([col_x - col_w * 0.75, col_y, col_x + col_w * 0.75, col_y + 4 * scale], fill=gold)
        
    # Architrave (beam)
    arch_w, arch_h = w - 20 * scale, 8 * scale
    arch_y = col_y - arch_h
    draw.rectangle([cx - arch_w / 2, arch_y, cx + arch_w / 2, arch_y + arch_h], fill=gold)
    
    # Frieze / Cornice
    corn_w, corn_h = w - 12 * scale, 5 * scale
    corn_y = arch_y - corn_h
    draw.rectangle([cx - corn_w / 2, corn_y, cx + corn_w / 2, corn_y + corn_h], fill=gold_dark)
    
    # Triangular Pediment
    ped_top = corn_y - 34 * scale
    ped_poly = [
        (cx - corn_w / 2, corn_y),
        (cx, ped_top),
        (cx + corn_w / 2, corn_y)
    ]
    draw.polygon(ped_poly, fill=gold)
    
    # Inner Tympanum (relief shadow)
    inner_poly = [
        (cx - corn_w / 2 + 10 * scale, corn_y - 2 * scale),
        (cx, ped_top + 8 * scale),
        (cx + corn_w / 2 - 10 * scale, corn_y - 2 * scale)
    ]
    draw.polygon(inner_poly, fill=gold_dark)
    
    # Central Relief Medallion in tympanum
    med_r = 7 * scale
    draw.ellipse([cx - med_r, (corn_y + ped_top) / 2 - med_r * 0.3, cx + med_r, (corn_y + ped_top) / 2 + med_r * 1.7], fill=gold)


def generate_app_icon():
    """Generates 512x512 App Icon matching Google Play requirements."""
    size = 512
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # Background: Rounded or full square deep indigo gradient
    # Google Play automatically applies the final squircle mask, so we provide full 512x512
    for y in range(size):
        t = y / (size - 1)
        r = int(9 + 12 * t)
        g = int(22 + 20 * t)
        b = int(34 + 32 * t)
        draw.line([(0, y), (size, y)], fill=(r, g, b, 255))
        
    # Concentric Radar / Astrolabe Glow Rings
    cx, cy = size // 2, size // 2
    
    # Background radar sweep sector (semitransparent glow)
    sweep_layer = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    sweep_draw = ImageDraw.Draw(sweep_layer)
    sweep_draw.pieslice([cx - 210, cy - 210, cx + 210, cy + 210], start=-60, end=15, fill=(255, 213, 107, 36))
    sweep_draw.pieslice([cx - 210, cy - 210, cx + 210, cy + 210], start=-30, end=15, fill=(255, 213, 107, 50))
    sweep_draw.line([(cx, cy), (cx + int(210 * math.cos(math.radians(15))), cy + int(210 * math.sin(math.radians(15))))], fill=(255, 240, 180, 180), width=3)
    img = Image.alpha_composite(img, sweep_layer)
    draw = ImageDraw.Draw(img)

    # Concentric Range Rings
    rings = [
        (210, (232, 199, 130, 110), 3),
        (160, (232, 199, 130, 70), 2),
        (110, (232, 199, 130, 50), 2),
        (60,  (232, 199, 130, 40), 1),
    ]
    for radius, color, width in rings:
        draw.ellipse([cx - radius, cy - radius, cx + radius, cy + radius], outline=color, width=width)
        
    # Astrolabe Graduation Notches (every 15 degrees on outer ring)
    for deg in range(0, 360, 15):
        rad = math.radians(deg)
        length = 10 if deg % 90 == 0 else (6 if deg % 45 == 0 else 4)
        r1 = 210
        r2 = 210 - length
        x1 = cx + int(r1 * math.cos(rad))
        y1 = cy + int(r1 * math.sin(rad))
        x2 = cx + int(r2 * math.cos(rad))
        y2 = cy + int(r2 * math.sin(rad))
        col = (255, 213, 107, 240) if deg % 90 == 0 else (232, 199, 130, 140)
        draw.line([(x1, y1), (x2, y2)], fill=col, width=3 if deg % 90 == 0 else 1)

    # Crosshair axes
    draw.line([(cx, cy - 220), (cx, cy + 220)], fill=(232, 199, 130, 45), width=1)
    draw.line([(cx - 220, cy), (cx + 220, cy)], fill=(232, 199, 130, 45), width=1)

    # Draw Classical Monument Temple in Center
    draw_temple(draw, cx, cy + 18, scale=1.45, gold=(255, 218, 118, 255), gold_dark=(216, 175, 85, 255))
    
    # Draw Navigation Compass Star Overlay at top of temple
    star_cy = cy - 86
    star_size = 28
    
    # 8-point compass star points
    # North (Red/Gold highlight)
    draw.polygon([(cx, star_cy - star_size), (cx + 5, star_cy - 4), (cx, star_cy), (cx - 5, star_cy - 4)], fill=(230, 57, 70, 255))
    draw.polygon([(cx, star_cy - star_size), (cx, star_cy), (cx - 5, star_cy - 4)], fill=(255, 90, 95, 255))
    # South
    draw.polygon([(cx, star_cy + star_size), (cx + 5, star_cy + 4), (cx, star_cy), (cx - 5, star_cy + 4)], fill=(216, 181, 104, 255))
    # East
    draw.polygon([(cx + star_size, star_cy), (cx + 4, star_cy + 5), (cx, star_cy), (cx + 4, star_cy - 5)], fill=(240, 205, 125, 255))
    # West
    draw.polygon([(cx - star_size, star_cy), (cx - 4, star_cy + 5), (cx, star_cy), (cx - 4, star_cy - 5)], fill=(216, 181, 104, 255))
    # Center jewel
    draw.ellipse([cx - 4, star_cy - 4, cx + 4, star_cy + 4], fill=(255, 255, 255, 255))

    # Landmark Radar Blips on the rings
    blips = [
        (cx + 95, cy - 80, (255, 183, 77, 240), 7),   # Northeast
        (cx - 120, cy - 70, (100, 210, 255, 240), 6),  # Northwest
        (cx + 140, cy + 90, (129, 199, 132, 240), 6),  # Southeast
        (cx - 85, cy + 130, (239, 83, 80, 240), 7),    # Southwest
    ]
    for bx, by, bcol, br in blips:
        # Halo ring
        draw.ellipse([bx - br * 2, by - br * 2, bx + br * 2, by + br * 2], outline=(bcol[0], bcol[1], bcol[2], 90), width=1)
        # Dot
        draw.ellipse([bx - br, by - br, bx + br, by + br], fill=bcol)
        # Inner white shine
        draw.ellipse([bx - 2, by - 2, bx + 2, by + 2], fill=(255, 255, 255, 255))

    # Subtle gold bevel border around icon edge
    draw.rectangle([0, 0, size - 1, size - 1], outline=(232, 199, 130, 60), width=2)

    out_path = os.path.join(STORE_DIR, "ic_launcher-512.png")
    img.save(out_path, "PNG")
    print(f"Generated app icon: {out_path} ({os.path.getsize(out_path)} bytes)")
    return out_path


def generate_feature_graphic():
    """Generates 1024x500 Feature Graphic matching Google Play requirements (24-bit RGB PNG)."""
    width, height = 1024, 500
    # RGB mode (Google Play requires no alpha on feature graphics)
    img = Image.new("RGB", (width, height), (7, 18, 26))
    draw = ImageDraw.Draw(img)

    # 1. Rich dark background gradient
    for x in range(width):
        t = x / (width - 1)
        r = int(7 + 10 * t)
        g = int(18 + 14 * t)
        b = int(28 + 24 * t)
        draw.line([(x, 0), (x, height)], fill=(r, g, b))

    # 2. Subtle Cartographic Grid Lines
    grid_color = (20, 48, 68)
    for gx in range(0, width, 64):
        draw.line([(gx, 0), (gx, height)], fill=grid_color, width=1)
    for gy in range(0, height, 64):
        draw.line([(0, gy), (width, gy)], fill=grid_color, width=1)

    # 3. Ambient Star & Constellation Points
    import random
    random.seed(42)
    for _ in range(60):
        sx = random.randint(10, width - 10)
        sy = random.randint(10, height - 10)
        brightness = random.randint(120, 220)
        size = 1 if random.random() > 0.3 else 2
        draw.rectangle([sx, sy, sx + size, sy + size], fill=(brightness, int(brightness * 0.95), int(brightness * 0.8)))

    # 4. Right Side: Big Glowing Radar & Astrolabe
    rcx, rcy = 760, 250
    radar_radius = 210

    # Radar Sweep Sector
    overlay = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    odraw = ImageDraw.Draw(overlay)
    odraw.pieslice([rcx - radar_radius, rcy - radar_radius, rcx + radar_radius, rcy + radar_radius],
                   start=-70, end=25, fill=(255, 213, 107, 30))
    odraw.pieslice([rcx - radar_radius, rcy - radar_radius, rcx + radar_radius, rcy + radar_radius],
                   start=-20, end=25, fill=(255, 213, 107, 65))
    
    # Sweep line
    rad_end = math.radians(25)
    lx = rcx + int(radar_radius * math.cos(rad_end))
    ly = rcy + int(radar_radius * math.sin(rad_end))
    odraw.line([(rcx, rcy), (lx, ly)], fill=(255, 240, 190, 220), width=3)

    # Concentric rings
    for r, col, w in [
        (radar_radius, (232, 199, 130, 160), 2),
        (155, (232, 199, 130, 90), 1),
        (105, (232, 199, 130, 70), 1),
        (55, (232, 199, 130, 60), 1)
    ]:
        odraw.ellipse([rcx - r, rcy - r, rcx + r, rcy + r], outline=col, width=w)

    # Astrolabe ticks
    for deg in range(0, 360, 10):
        rad = math.radians(deg)
        length = 12 if deg % 90 == 0 else (7 if deg % 30 == 0 else 4)
        x1 = rcx + int(radar_radius * math.cos(rad))
        y1 = rcy + int(radar_radius * math.sin(rad))
        x2 = rcx + int((radar_radius - length) * math.cos(rad))
        y2 = rcy + int((radar_radius - length) * math.sin(rad))
        c = (255, 218, 120, 220) if deg % 90 == 0 else (210, 180, 115, 110)
        odraw.line([(x1, y1), (x2, y2)], fill=c, width=2 if deg % 90 == 0 else 1)

    # Crosshair axes
    odraw.line([(rcx, rcy - radar_radius - 10), (rcx, rcy + radar_radius + 10)], fill=(232, 199, 130, 50), width=1)
    odraw.line([(rcx - radar_radius - 10, rcy), (rcx + radar_radius + 10, rcy)], fill=(232, 199, 130, 50), width=1)

    # Center Temple Icon in Radar
    draw_temple(odraw, rcx, rcy + 10, scale=0.9, gold=(255, 220, 120, 240), gold_dark=(210, 175, 80, 240))

    # Multiple Landmark Blips with badges
    blips = [
        (rcx + 85, rcy - 110, "Colosseum", "11m", (255, 183, 77, 255)),
        (rcx - 115, rcy - 65, "Forum Romanum", "340m", (100, 210, 255, 255)),
        (rcx + 125, rcy + 75, "Arch of Titus", "420m", (129, 199, 132, 255)),
        (rcx - 70, rcy + 120, "Palatine Hill", "650m", (239, 83, 80, 255)),
    ]
    font_blip = ImageFont.truetype(FONT_SANS_BOLD, 12)
    font_dist = ImageFont.truetype(FONT_SANS, 10)
    for bx, by, name, dist, bcol in blips:
        # Glow ring
        odraw.ellipse([bx - 14, by - 14, bx + 14, by + 14], outline=(bcol[0], bcol[1], bcol[2], 90), width=1)
        odraw.ellipse([bx - 6, by - 6, bx + 6, by + 6], fill=bcol)
        odraw.ellipse([bx - 2, by - 2, bx + 2, by + 2], fill=(255, 255, 255, 255))
        # Tag
        tag_w = 90
        odraw.rectangle([bx + 10, by - 12, bx + 10 + tag_w, by + 16], fill=(12, 28, 42, 220), outline=(210, 180, 115, 120), width=1)
        odraw.text((bx + 16, by - 10), name, font=font_blip, fill=(255, 240, 200, 255))
        odraw.text((bx + 16, by + 3), dist, font=font_dist, fill=(180, 215, 240, 230))

    # Merge overlay into main image
    img.paste(overlay, (0, 0), overlay)
    draw = ImageDraw.Draw(img)

    # 5. Left Side: Typography & Brand Showcase
    # Super-Header / Category Tag
    font_cat = ImageFont.truetype(FONT_SANS_BOLD, 14)
    draw.text((64, 52), "HISTORICAL WALKING TOUR & EXPLORER RADAR", font=font_cat, fill=(255, 213, 107))
    
    # Title: GeoChronicle
    font_title = ImageFont.truetype(FONT_SERIF_BOLD, 54)
    draw.text((64, 76), "GeoChronicle", font=font_title, fill=(255, 255, 255))
    
    # Golden line accent
    draw.line([(64, 150), (420, 150)], fill=(255, 213, 107), width=3)
    
    # Subtitle / Pitch
    font_sub = ImageFont.truetype(FONT_SERIF, 20)
    draw.text((64, 168), "Uncover ancient landmarks & historical tales", font=font_sub, fill=(230, 238, 245))
    draw.text((64, 196), "around you in real time with audio narration.", font=font_sub, fill=(185, 205, 220))

    # 4 Feature Pills
    features = [
        ("🛰️ Real-Time Radar Scan", "360° bearing & proximity rings"),
        ("🎧 Hands-Free Audio Tour", "Multilingual TTS narration on walk"),
        ("🌍 100% Offline Exploration", "Saved landmarks & local caching"),
        ("🏛️ World Heritage Hubs", "Rome, Athens, Kyoto, Paris, Cairo"),
    ]
    pill_font_title = ImageFont.truetype(FONT_SANS_BOLD, 13)
    pill_font_desc = ImageFont.truetype(FONT_SANS, 11)
    
    start_y = 246
    for i, (f_title, f_desc) in enumerate(features):
        px = 64 + (i % 2) * 230
        py = start_y + (i // 2) * 70
        pw = 216
        ph = 56
        
        # Pill card
        draw.rounded_rectangle([px, py, px + pw, py + ph], radius=10, fill=(15, 34, 50), outline=(50, 90, 120), width=1)
        draw.text((px + 12, py + 10), f_title, font=pill_font_title, fill=(255, 225, 140))
        draw.text((px + 12, py + 30), f_desc, font=pill_font_desc, fill=(170, 195, 215))

    # Bottom Store Trust Bar
    font_trust = ImageFont.truetype(FONT_SANS_BOLD, 12)
    draw.text((64, 424), "★ FREE & AD-FREE HISTORICAL DISCOVERY", font=font_trust, fill=(255, 213, 107))
    draw.text((64, 444), "Powered by open heritage datasets • Privacy friendly GPS", font=ImageFont.truetype(FONT_SANS, 11), fill=(130, 160, 185))

    # Border around feature graphic
    draw.rectangle([0, 0, width - 1, height - 1], outline=(232, 199, 130), width=1)

    out_path = os.path.join(STORE_DIR, "feature_graphic.png")
    img.save(out_path, "PNG")
    print(f"Generated feature graphic: {out_path} ({os.path.getsize(out_path)} bytes)")
    return out_path

if __name__ == "__main__":
    generate_app_icon()
    generate_feature_graphic()

