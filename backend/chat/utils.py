import time
from random import randint
import chat.model_0 as mod_0
import chat.model_1 as mod_1

numbots = 2

model = {
        "0": mod_0.pred,
        "1": mod_1.pred,
    }


def delay(length):
    time.sleep(length * .2)
