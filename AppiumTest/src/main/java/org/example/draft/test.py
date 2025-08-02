from appium import webdriver
from appium.webdriver.common.appiumby import AppiumBy
from appium.options.android import UiAutomator2Options
from selenium.webdriver.common.action_chains import ActionChains
from selenium.webdriver.common.actions.action_builder import ActionBuilder
from selenium.webdriver.common.actions.pointer_input import PointerInput
from selenium.webdriver.common.actions import interaction

options = UiAutomator2Options()
options.platform_name = "Android"
options.app_package = "com.google.android.youtube"
options.app_activity = "com.google.android.youtube.HomeActivity"
options.no_reset = True

driver = webdriver.Remote("http://127.0.0.1:4723", options=options)
# ==================================
#pip install Appium-Python-Client selenium
actions = ActionChains(driver)
actions.w3c_actions = ActionBuilder(driver, mouse=PointerInput(interaction.POINTER_TOUCH, "touch"))
actions.w3c_actions.pointer_action.move_to_location(519, 1899)
actions.w3c_actions.pointer_action.pointer_down()
actions.w3c_actions.pointer_action.move_to_location(470, 403)
actions.w3c_actions.pointer_action.release()
actions.perform()

el3 = driver.find_element(by=AppiumBy.ACCESSIBILITY_ID, value="Subscriptions")
el3.click()
