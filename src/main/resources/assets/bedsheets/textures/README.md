# BedSheets Textures

This directory contains textures for the BedSheets mod.

## Required Texture Files

### Base Bedsheet Textures
The mod needs bedsheet textures for each dye color and messiness level:

Pattern: `bedsheet_{color}_messiness_{level}.png`
- Colors: white, orange, magenta, light_blue, yellow, lime, pink, gray, light_gray, cyan, purple, blue, brown, green, red, black
- Messiness levels: 0 (made), 1 (slightly messy), 2 (messy), 3 (very messy)

Examples:
- `bedsheet_white_messiness_0.png` - Clean white bedsheet
- `bedsheet_red_messiness_3.png` - Very messy red bedsheet

### Texture Requirements
- Size: 16x16 pixels (standard Minecraft block texture)
- Format: PNG with transparency support
- Style: Should match Minecraft's vanilla aesthetic
- Messiness variations: Add wrinkles, creases, and disheveled appearance as level increases

### Dynamic Pattern Textures
The mod also supports dynamic texture generation for banner patterns applied to bedsheets. This is handled programmatically by combining:
- Base color texture
- Banner pattern overlays
- Messiness level modifications

## Current Status
Currently contains placeholder files. Actual textures need to be created by an artist.

## Asset Creation Guidelines
1. Maintain Minecraft's 16x16 pixel art style
2. Use appropriate colors that match vanilla dye colors
3. Create subtle but noticeable messiness progression
4. Ensure textures tile properly when used on multiple adjacent beds
5. Consider how patterns will overlay on the base textures