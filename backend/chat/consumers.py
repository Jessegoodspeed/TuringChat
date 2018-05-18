from django.conf import settings
from django.contrib.auth.models import User
from .models import Room
import numpy as np
import pickle
import tensorflow as tf
from .utils import *

import asyncio
import os

os.environ['TF_CPP_MIN_LOG_LEVEL'] = '2'

#TODO remove global variable for numbots and make part of room model


from channels.generic.websocket import AsyncJsonWebsocketConsumer



# Consumer class is instantiated for every websocket connection
# Consumers connect to other consumers through the group_add over the
# Channel layer
class ChatConsumer(AsyncJsonWebsocketConsumer):


    # Called upon ws://chat/stream connection
    async def connect(self):
        """Connects client to instantiated consumer."""
        await self.accept()



    async def receive_json(self, content):
        """Takes json from client, dispatches routine."""

        print(content)
        # Get command to select routine
        command = content.get("command", None)

        if command == "join":
            await self.join_room(content["room"], content["username"], content["uid"])

        elif command == "send":
            await self.send_room(content["room"], content["message"], content["username"])

        elif command == "leave":
            await self.leave_room(content["room"])


    # called when the socket closes
    async def disconnect(self,close_code):
        self.leave_room(self.roomid)
        print('close')



    async def join_room(self, roomid, username, uid):
        """Connects the client and consumer to a room.

        takes roomid username, and uid(userid)
        disconnects client from consumer if uid is invalid
        """

        room = Room.objects.get(pk=roomid) # "room" is the room id
        self.roomid = roomid
        if not room:
            print("Room not found")
            await self.send_json({
                "status": "Failed"
            })
            await self.close()
            return

        user = User.objects.get(username=username)
        self.user = user
        if user.id is not uid:
            print("User id not found")
            await self.send_json({
                "status": "Failed"
            })
            await self.close()
            return

        # TODO what do with async_to_sync
        # Add them to the group so they get room messages
        await self.channel_layer.group_add(
            room.group_name,
            self.channel_name,
        )
        # Instruct their client to finish opening the room
        # Note: mobile apps may need different information
        await self.send_json({
            "join": room.id,
            "name": room.name,
        })

    async def send_room(self, roomid, message, username):
        """Sends a client message to a room aswell as dispatching bot responses.

        Takes roomid message and username
        Bot responses are dispatched at a
        delayed time relative to their length
        """
        room = Room.objects.get(pk=roomid)


        await self.self_send(roomid, username, message) # send message to self
        await self.channel_layer.group_send(            # send message to room
            room.group_name,
            {
            "type": "chat.message",
            "room_id": roomid,
            "username": username,
            "message": message,
            "origin" : username,
            }
        );

        response = []                               # bot responses go in list
        totallen = 0
        for i in range(numbots):
            msg = model[str(i)](str(message))
            response.append((len(msg), msg))        # tuple (legth, message)
            totallen += len(msg)

        response.sort(reverse=True)                 # sort accending
        i = 1
        while totallen > 0:
            msg = response.pop()
            await asyncio.sleep(msg[0]*.08)         # delay based on length
            print(msg)
            # combination of self_send and group send to get around the
            # lock channel layer creates when sending bot messages
            await self.self_send(roomid, "bot"+str(i), msg[1])
            await self.channel_layer.group_send(
                room.group_name,
                {
                "type": "chat.message",
                "room_id": roomid,
                "username": "bot" +
                str(i),
                "message": msg[1],
                "origin": username,
                }
            );
            i +=1
            totallen -= msg[0]


    async def leave_room(self, roomid):
        room = Room.objects.get(pk=roomid)

        await self.channel_layer.group_discard(
            room.group_name,
            self.channel_name,
        )

    async def self_send(self, roomid, username, message):
        """Helper function for group send channel layer locking"""
        await self.send_json(
            {
                "room": roomid,
                "username": username,
                "message": message,
            },
        )


    ##### Handlers for messages sent over the channel layer

    # methods named by the types we send - so chat.join becomes chat_join
    async def chat_join(self, event):

        # on chat join

        # Send a message to the client
        await self.send_json(
            {
                "room": event["room_id"],
            },
        )

    async def chat_leave(self, event):

        # on chat leave.

        # Send a message to the client
        await self.send_json(
            {
                "room": event["room_id"],
            },
        )

    async def chat_message(self, event):

        # on chat message.

        # Send a message to the client
        # dont send back to original sender
        if event["origin"] == self.user.username:
            return
        else:
            print(event["message"])
            await self.send_json(
                {
                    "room": event["room_id"],
                    "username": event["username"],
                    "message": event["message"],
                },
            )
