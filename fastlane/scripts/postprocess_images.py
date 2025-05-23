import os
import cv2
import numpy as np
from Font.funcs import putTTFText

# current cwd is the fastlane folder
screenshots_folder = "metadata/android/en-US/images/phoneScreenshots"

def read_image(name):
    path = os.path.join(screenshots_folder, name)
    screenshot = cv2.imread(path)
    screenshot = cv2.cvtColor(screenshot, cv2.COLOR_BGR2BGRA)
    os.remove(path)

    return screenshot

def combine(img1, img2):
    height, width, _ = img1.shape
    mask = np.fromfunction(lambda y, x: x > (200 + 500 * (height - y) / height), (height, width))

    img1[mask, 0:3] = img2[mask, 0:3]

    return img1

def get_text_width(text, font, font_scale):
    image = np.ones((100, 1000, 4), dtype=np.uint8) * 255
    
    image = putTTFText(image, text, (0, 0), font, 
                   font_scale, (0, 0, 0),
                   spacing=3, wordSpacing=20)
                   
    black_pixels = np.where(image == 0)
    x_coords = black_pixels[1]

    return np.max(x_coords) - np.min(x_coords)

def scale_image_to_width(image, desired_width):
    image_height, image_width, _ = image.shape

    scale = desired_width / image_width * 0.85
    scaled_height = int(image_height * scale)
    scaled_width = int(image_width * scale)

    image = cv2.resize(image, (scaled_width, scaled_height), interpolation=cv2.INTER_LINEAR)
    
    return image

def round_corners(image):
    height, width, _ = image.shape

    mask = np.ones((height, width), dtype=np.uint8) * 255

    mask = round_corner(mask, True, True)
    mask = round_corner(mask, True, False)
    mask = round_corner(mask, False, True)
    mask = round_corner(mask, False, False)

    image[:, :, 3] = mask

    return image

def round_corner(mask, h, v):
    size = 32
    height, width = mask.shape

    y = height - size if v else 0
    x = width - size if h else 0

    circle_y = 0 if v else size
    circle_x = 0 if h else size

    mask = cv2.rectangle(mask, (x, y), (x+size, y+size), 0, -1)
    mask = cv2.circle(mask, (x+circle_x, y+circle_y), size, 255, -1)

    return mask

def create_shadow(image):
    height, width, _ = image.shape

    shadow = scale_image_to_width(image, width*1.2)
    mask = shadow[:,:,3] == 255
    shadow[mask] = [0, 0, 0, 255]

    return shadow

def paste_image(background, image):
    background_height, background_width, _ = background.shape
    height, width, _ = image.shape

    mask = image[:,:,3] == 255

    x = background_width // 2 - width // 2
    y = int(background_height * 0.6  - height // 2)
    
    end_y = y+height
    if end_y >= background_height:
        end_y = background_height-1

    mask = mask[:end_y-y,:]
    image = image[:end_y-y,:]

    background_cut = background[y:end_y, x:x+width]

    background_cut[mask, 0:3] = image[mask, 0:3]
    background[y:end_y, x:x+width] = background_cut
    return background

def process_image(screenshot, output_name, text):
    output_path = os.path.join(screenshots_folder, output_name)

    width = 1080
    height = 1920

    image = np.ones((height, width, 4), dtype=np.uint8) * 255

    screenshot = scale_image_to_width(screenshot, width*0.9)
    screenshot = round_corners(screenshot)

    shadow = create_shadow(screenshot)

    image = paste_image(image, shadow)
    image = cv2.GaussianBlur(image, (161, 161), -1)
    image = paste_image(image, screenshot)
    
    font = "scripts/Roboto-VariableFont_wdth,wght.ttf"
    font_scale = 100
    
    text_width = get_text_width(text, font, font_scale)
    
    text_pos = (width//2-text_width//2, int(height * 0.05))
    image = putTTFText(image, text, text_pos, font, 
                   font_scale, (0, 0, 0),
                   spacing=3, wordSpacing=20)

    cv2.imwrite(output_path, image)

home = read_image("home_folder.png")
home_dark = read_image("home_folder_dark.png")

home_combined = combine(home, home_dark)

process_image(home_combined, "1.png", "simple")
process_image(read_image("media_folder.png"), "2.png", "yours")
process_image(read_image("search.png"), "3.png", "search")
process_image(read_image("search_dots.png"), "4.png", "Dots")
process_image(read_image("settings.png"), "5.png", "customizeable")
