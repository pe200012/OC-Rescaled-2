# robot.py - the robot's own component, like OpenComputers' Lua robot library.
#
# Actions take the side they act on, the front unless given, and return what the robot reports
# first: True, or None when it failed. Moving and turning pause the machine until done. The
# reasons for failures and the methods without a helper here are on `component`:
#   robot.component.move(robot.FRONT)  ->  [None, 'impossible move']

from devices import bus

component = bus.find("robot")
if component is None:
    raise Exception("robot device not found")

# Sides as the robot sees them.
DOWN, UP, BACK, FRONT, RIGHT, LEFT = range(6)


def _call(method, *args):
    # Several results come as a list; arguments left as None count as not given.
    result = getattr(component, method)(*args)
    return result[0] if isinstance(result, list) else result


def name():
    return _call("name")


def light_color(value=None):
    return _call("getLightColor") if value is None else _call("setLightColor", value)


# Movement

def forward():
    return _call("move", FRONT)


def back():
    return _call("move", BACK)


def up():
    return _call("move", UP)


def down():
    return _call("move", DOWN)


def turn_left():
    return _call("turn", False)


def turn_right():
    return _call("turn", True)


def turn_around():
    return turn_right() and turn_right()


# World

def detect(side=FRONT):
    return _call("detect", side)


def compare(side=FRONT, fuzzy=False):
    return _call("compare", side, fuzzy)


def swing(side=FRONT, face=None, sneaky=False):
    return _call("swing", side, face, sneaky)


def use(side=FRONT, face=None, sneaky=False, duration=None):
    return _call("use", side, face, sneaky, duration)


def place(side=FRONT, face=None, sneaky=False):
    return _call("place", side, face, sneaky)


def drop(side=FRONT, count=None):
    return _call("drop", side, count)


def suck(side=FRONT, count=None):
    return _call("suck", side, count)


def durability():
    return _call("durability")


# Inventory, slots counting from 1

def inventory_size():
    return _call("inventorySize")


def select(slot=None):
    return _call("select", slot)


def count(slot=None):
    return _call("count", slot)


def space(slot=None):
    return _call("space", slot)


def compare_to(slot):
    return _call("compareTo", slot)


def transfer_to(slot, count=None):
    return _call("transferTo", slot, count)
